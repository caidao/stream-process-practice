package com.paner.zookeeper.master_slave;


import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by paner on 17/6/10.
 */
public class Worker implements Watcher{

    private  Random random = new Random();
    private  String serverId = Integer.toString(random.nextInt(1000));
    private ZooKeeper zk;
    private String hostPort;
    private Logger logger = Logger.getLogger(Watcher.class);
    private String status ;
    private final CountDownLatch latch = new CountDownLatch(1);

    private String getWorkerPath(){
        System.out.println("client id :"+ serverId);
        return "/workers/worker_"+serverId;
    }

    private String getAssignPath(){
        return "/assign/worker_"+serverId;
    }

    public Worker(String hostPort){
        this.hostPort = hostPort;
    }


    public void startZk() throws IOException, InterruptedException {
      zk = new ZooKeeper(hostPort,15000,this) ;
        latch.await();
    }

    public void process(WatchedEvent event) {
        logger.info(event.toString() + "," + hostPort);
        if (Event.KeeperState.SyncConnected == event.getState()){
            latch.countDown();
        }
    }

    private AsyncCallback.StringCallback createWorkCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    register();
                    break;
                case OK:
                    logger.info("register successfully:" + serverId);
                    getTasks();
                    break;
                case NODEEXISTS:
                    logger.warn("already register:"+serverId);
                    getTasks();
                    break;
                default:
                    logger.error("something went wrong:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    private AsyncCallback.StringCallback assignWorkerCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    createAssignPath();
                    break;
                case OK:
                    logger.info("assign worker successfully:" + serverId);
                    register();
                    break;
                case NODEEXISTS:
                    logger.warn("assign worker  register:"+serverId);
                    register();
                    break;
                default:
                    logger.error("something went wrong:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    //注册可分配任务节点/assign/worker-id
    private void createAssignPath(){
        zk.create(getAssignPath(),new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT,assignWorkerCallback,null);
    }


    //注册从节点
    private void register(){
        //先注册/assign/worker-id节点
        zk.create(getWorkerPath(), "idle".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, createWorkCallback, null);
    }

    private AsyncCallback.StatCallback statCallback = new AsyncCallback.StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    updateStatus((String) ctx);
                    return;
            }
        }
    };

    //获取任务，并提交执行
    private void getTasks(){
        zk.getChildren(getAssignPath(),newTaskWatcher,taskGetChildrenCallback,null);
    }

    private Watcher newTaskWatcher = new Watcher() {
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeChildrenChanged == event.getType()){
                getTasks();
            }
        }
    };

    private ExecutorService executor = Executors.newFixedThreadPool(20);
    private List<String> onGoingTask = new ArrayList<String>();
    private ChildrenCallback  taskGetChildrenCallback= new ChildrenCallback(){
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    getTasks();
                    break;
                case OK:
                    if (children!=null){
                        executor.submit(new Runnable() {
                            private List<String> children;
                            private DataCallback cb;

                            public Runnable init(List<String>children,DataCallback cb){
                                this.children = children;
                                this.cb  =cb;
                                return this;
                            }

                            public void run() {
                                logger.info("looping into tasks");
                                synchronized (children){
                                    for(String task:children){
                                        if (!onGoingTask.contains(task)){
                                            zk.getData(getAssignPath()+"/"+task,false,cb,task);
                                            onGoingTask.add(task);
                                        }
                                    }
                                }
                            }
                        }.init(children,dataCallback));
                    }
                    break;
                default:
                    logger.error("task children changed:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    private AsyncCallback.DataCallback dataCallback = new AsyncCallback.DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    //删除任务，便于下次重新执行
                    onGoingTask.remove(new String(data));
                    break;
                case OK:
                    try {
                        String task = (String) ctx;
                        logger.info("execute task " + task + ",data:" + new String(data));
                        Thread.sleep(1000);//模仿任务执行
                        //更新任务状态
                        createTaskStatus(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    logger.error("task executor failed:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    public void createTaskStatus(String task){
        zk.create("/status/"+task,(serverId+" execute task successfully.").getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT,createTaskStatusCallback,task);
    }

    private AsyncCallback.StringCallback createTaskStatusCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    createTaskStatus((String) ctx);
                    break;
                case OK:
                    logger.info("updated task status successfully.");
                    break;
                case NODEEXISTS:
                    break;
                default:
                    logger.error("create task status fail.", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    synchronized private void updateStatus(String status)  {
        if (status == this.status){
            Stat stat = new Stat();
            try {
                zk.getData(getWorkerPath(),true,stat);
            } catch (KeeperException e) {
                logger.info("worker trt again.");
                updateStatus(status);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //stat.version  为-1表示无条件更新
            zk.setData(getWorkerPath(),status.getBytes(),stat.getVersion(),statCallback,null);
        }
    }

    public void setStatus(String status){
        this.status = status;
        updateStatus(status);
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        Worker worker = new Worker("localhost:2181");
        worker.startZk();
        worker.createAssignPath();

        Thread.sleep(30000000);
    }
}
