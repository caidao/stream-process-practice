package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/6/10.
 */
public class Master {
    private static Random random = new Random(100);
    private  String serverId = Integer.toString(random.nextInt());
    private Logger logger = Logger.getLogger(Master.class);
    private final CountDownLatch latch = new CountDownLatch(1);

    public boolean isLeader() {
        return isLeader;
    }

    public void setIsLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    private  boolean isLeader;
    private  ZooKeeper zk;
    private  String master_path = "/master";

    public void startZk(String hostPort) throws IOException, InterruptedException {
        zk = new ZooKeeper(hostPort, Integer.MAX_VALUE, new Watcher() {
            public void process(WatchedEvent event) {
                if (Event.KeeperState.SyncConnected == event.getState()){
                    latch.countDown();
                }
            }
        }) ;
        latch.await();
    }

    public void stopZk() throws InterruptedException {
        zk.close();
    }

    private  AsyncCallback.StringCallback masterCreateCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    checkMaster();
                    return;
                case OK:
                    isLeader=true;
                    //take is leader
                    break;
                case NODEEXISTS:
                    masterExists();
                    break;
                default:
                    isLeader = false;
                    logger.error(KeeperException.create(KeeperException.Code.get(rc),path));
            }
            System.out.println("I'm"+(isLeader?"":"not")+" the leader");
        }
    };

    //检验master节点
    private  boolean checkMaster(){
        while (true){
            Stat stat = new Stat();
            try {
                byte data[] = zk.getData(master_path,false,stat);
                isLeader = new String(data).equals(serverId);
                return true;
            } catch (KeeperException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    //监视master节点
    private void masterExists(){
        zk.exists("/master", masterExistsWatcher, masterExistsCallback, null);
    }

    private Watcher  masterExistsWatcher  = new Watcher() {
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeDeleted == event.getType()){
                runForMaster();
            }
        }
    };

    private AsyncCallback.StatCallback masterExistsCallback = new AsyncCallback.StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    masterExists();
                    break;
                case OK:
                    if (stat == null){
                        runForMaster();
                    }
                    break;
                default:
                    checkMaster();
            }
        }
    };

    /**
     * 启动master
     */
    public void runForMaster(){
        System.out.println("master server ID:" + serverId);
        zk.create(master_path, serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL, masterCreateCallback, null);
    }


    /**
     * 主从模型其他节点 ：/tasks 、 /assign 、 /workers
     */
    public void bootstrap(){

        createParent("/workers",new byte[0]);
        createParent("/assign",new byte[0]);
        createParent("/tasks",new byte[0]);
        createParent("/status", new byte[0]);

    }

    /**
     * 通过回调上下文参数对create操作进行跟踪数据
     * @param path
     * @param data
     */
    private void createParent(String path,byte[] data){
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, createParentCallback, data);
    }

    private AsyncCallback.StringCallback createParentCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    //重试机制
                    createParent(path, (byte[]) ctx);
                    break;
                case OK:
                    logger.info("parent created.");
                    break;
                case NODEEXISTS:
                    logger.warn("parent already register :"+path);
                    break;
                default:
                    logger.error("something went wrong:",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    //监控workers的状态，并对其分配任务
    private void getWorkers(){
        zk.getChildren("/workers",workersChangeWathcer,workersGetChildrenCallback,null);
    }

    private Watcher workersChangeWathcer = new Watcher() {
        public void process(WatchedEvent event) {
            if (Event.EventType.NodeChildrenChanged == event.getType()){
                getWorkers();
            }
        }
    };

    private AsyncCallback.ChildrenCallback workersGetChildrenCallback = new AsyncCallback.ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    //获取子节点
                    getWorkers();
                    break;
                case OK:
                    logger.info("successfully got a list of workers:"+children.size()+" workers");
                    //给每个work分配任务
                    reassignAndSet(children);
                    break;
                default:
                    logger.error("get children failed",KeeperException.create(KeeperException.Code.get(rc)));
            }
        }
    };

    private List<String> workersCache = null;
    //重新分配任务
    private void reassignAndSet(List<String> children){
        List<String> toPorcess ;
        if (workersCache==null){
            workersCache =children;
            toPorcess = null;
        }else {
            logger.info("use cache and clear cache.");
            toPorcess = getRemoveChildren(children);
        }

        if (toPorcess!=null && toPorcess.size()>0){
            //重新分配已失效worker节点的任务
            for (String worker:toPorcess){
                //重新分配absent work task
            }
        }
    }

    private List<String> getRemoveChildren(List<String> children){
        List<String> removeChildren = new ArrayList<String>();
        for (String cc:workersCache){
            if (!children.contains(cc)){
                removeChildren.add(cc);
            }
        }
        workersCache=children;
        return removeChildren;
    }


    //监控task节点，并分配task任务
    private void getTasks(){
         zk.getChildren("/tasks", tasksChangeWatcher, tasksGetChildrenCallback, null);
    }

    private Watcher tasksChangeWatcher = new Watcher() {
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged){
                getTasks();
            }
        }
    };

    private AsyncCallback.ChildrenCallback tasksGetChildrenCallback = new AsyncCallback.ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    getTasks();
                    break;
                case OK:
                    if (children == null){
                        //分配任务
                    }
                    break;
                default:
                    logger.error("getChildren failed.",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    //将tasks节点下的任务分配给assign
    private void assignTasks(List<String> tasks){

        for (String task:tasks){
            getTaskData(task);
        }
    }

    private void getTaskData(String task){
        zk.getData("/tasks/" + task, false, taskDataCallback, task);
    }

    private AsyncCallback.DataCallback taskDataCallback = new AsyncCallback.DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    getTasks();
                    break;
                case OK:
                    int worker = random.nextInt(workersCache.size());
                    String designatedWorker = workersCache.get(worker);
                    String assignmentPath = "/assign/"+designatedWorker+"/"+(String)ctx;
                    //分配任务
                    createAssignment(assignmentPath,data);
                    break;
                default:
                    logger.error("Error when trying to get task data.",KeeperException.create(KeeperException.Code.get(rc),path));

            }
        }
    };

    //在assign节点创建已分配的任务
    private void createAssignment(String path,byte[] data){
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, assignTaskCallback, data);
    }

    private AsyncCallback.StringCallback assignTaskCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    createAssignment(path, (byte[]) ctx);
                    break;
                case OK:
                    //删除task任务，防止重复分配
                    delteTask(name.substring(name.indexOf("/")+1));
                    break;
                case NODEEXISTS:
                    logger.warn("Task already assigned.");
                    break;
                default:
                    logger.error("error when trying to assign task.",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    private void delteTask(String path){
        Stat stat = new Stat();
        try {
            zk.getData(path,null,stat);
            zk.delete(path,stat.getVersion());
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Master master = new Master();
        master.startZk("127.0.0.1:2181");
        master.runForMaster();
        master.bootstrap();
        Thread.sleep(1000);
        if (master.isLeader){
            System.out.println("I'm the leader.");
            Thread.sleep(6000000);
        }else {
            System.out.println("Someone else is leader.");
        }
        master.stopZk();
    }
}
