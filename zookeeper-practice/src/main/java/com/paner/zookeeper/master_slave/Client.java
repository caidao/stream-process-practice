package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/6/11.
 */
public class Client implements Watcher {

    private ZooKeeper zk;

    private String hostPort;

    private Logger logger = Logger.getLogger(Client.class);
    private final CountDownLatch latch = new CountDownLatch(1);

    public Client(String hostPort){
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


    //提交任务
    private void submitTask(String task,TaskObject taskCtx){
        taskCtx.setTask(task);
        zk.create("/tasks/task-",
                task.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL,
                createTaskCallback,
                taskCtx);
    }

    private AsyncCallback.StringCallback createTaskCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            TaskObject taskObject = (TaskObject)ctx;
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    submitTask(taskObject.getTask(),taskObject);
                    break;
                case OK:
                    logger.info("create task name:"+name);
                    taskObject.setName(name);
                    watchStatus("/status/"+name.replace("/tasks/",""),ctx);
                    break;
                default:
                    logger.error("submit task fail.",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    ConcurrentHashMap<String,Object> ctxMap = new ConcurrentHashMap<String, Object>();
    //查看任务状态
    private void watchStatus(String path,Object ctx){
        ctxMap.put(path,ctx);
        zk.exists(path,statusWatcher,existsCallback,ctx);
    }

    private Watcher statusWatcher = new Watcher() {
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeCreated == event.getType()){
                try {
                    byte[] data = zk.getData(event.getPath(),false,null);
                    System.out.println("ctx = [" + ctxMap.get(event.getPath()) + "],data ="+new String(data));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private AsyncCallback.StatCallback existsCallback = new AsyncCallback.StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    watchStatus(path,ctx);
                    break;
                case OK:
                    try {
                        byte[] data = zk.getData(path, false, null);
                        System.out.println("data ="+new String(data));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case NONODE:
                    break;
                default:
                    logger.error("check  task  node exists  fail.",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    /**
     * 创建任务，并在其中添加一个执行命令，节点格式为持久序列化的
     * @param command
     * @return
     */
    public String queueCommand(String command){
        while (true){
            try {
                String name = zk.create("/tasks/task-",command.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
                return name;
            } catch (KeeperException e) {
                logger.error("",e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client("localhost:2181");
        client.startZk();
        //String  name = client.queueCommand("test2");
        client.submitTask("say hello",new TaskObject());
       // System.out.println("created = [" +  + "]");
        Thread.sleep(1000000);
    }
}
