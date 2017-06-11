package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
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
                    break;
                default:
                    isLeader = false;
            }
            System.out.println("I'm"+(isLeader?"":"not")+" the leader");
        }
    };

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

    /**
     * 启动master
     */
    public void runForMaster(){
        System.out.println("master server ID:"+serverId);
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
        createParent("/status",new byte[0]);

    }

    /**
     * 通过回调上下文参数对create操作进行跟踪数据
     * @param path
     * @param data
     */
    private void createParent(String path,byte[] data){
        zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,createParentCallback,data);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        Master master = new Master();
        master.startZk("127.0.0.1:2181");
        master.runForMaster();
        master.bootstrap();
        Thread.sleep(1000);
        if (master.isLeader){
            System.out.println("I'm the leader.");
            Thread.sleep(60000);
        }else {
            System.out.println("Someone else is leader.");
        }
        master.stopZk();
    }
}
