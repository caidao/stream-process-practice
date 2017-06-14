package com.paner.zookeeper.master_slave;


import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by paner on 17/6/10.
 */
public class Worker implements Watcher{

    private static Random random = new Random(100);
    private  String serverId = Integer.toString(random.nextInt());
    private ZooKeeper zk;
    private String hostPort;
    private Logger logger = Logger.getLogger(Watcher.class);
    private String status ;
    private final CountDownLatch latch = new CountDownLatch(1);

    private String getWorkerPath(){
        System.out.println("client id :"+ serverId);
        return "/workers/worker_"+serverId;
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
                    break;
                case NODEEXISTS:
                    logger.warn("already register:"+serverId);
                    break;
                default:
                    logger.error("something went wrong:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    private void register(){
        zk.create(getWorkerPath(),"idle".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,createWorkCallback,null);
    }

    private AsyncCallback.StatCallback statCallback = new AsyncCallback.StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    updateSatus((String) ctx);
                    return;
            }
        }
    };

    private void getTasks(){
        zk.getChildren("/assign/worker-"+serverId,newTaskWatcher,taskGetChildrenCallback,null);
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
                                            zk.getData("/assign/worker-"+serverId+"/"+task,false,cb,task);
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
                        logger.info("data:"+new String(data));
                        Thread.sleep(1000);//模仿任务执行
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    logger.error("task executor failed:", KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };

    synchronized private void updateSatus(String status)  {
        if (status == this.status){
            Stat stat = new Stat();
            try {
                zk.getData(getWorkerPath(),true,stat);
            } catch (KeeperException e) {
                logger.info("worker trt again.");
                updateSatus(status);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //stat.version  为-1表示无条件更新
            zk.setData(getWorkerPath(),status.getBytes(),stat.getVersion(),statCallback,null);
        }
    }

    public void setStatus(String status){
        this.status = status;
        updateSatus(status);
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        Worker worker = new Worker("localhost:2181");
        worker.startZk();
        worker.register();

        Thread.sleep(30000000);
    }
}
