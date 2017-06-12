package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;
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
        logger.info(event.toString()+","+hostPort);
        if (Event.KeeperState.SyncConnected == event.getState()){
            latch.countDown();
        }
    }

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
        String  name = client.queueCommand("test2");
        System.out.println("created = [" + name + "]");
        Thread.sleep(1000000);
    }
}
