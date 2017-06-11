package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by paner on 17/6/11.
 */
public class Client implements Watcher {

    private ZooKeeper zk;

    private String hostPort;

    private Logger logger = Logger.getLogger(Client.class);

    public Client(String hostPort){
        this.hostPort = hostPort;
    }

    public void startZk() throws IOException {
        zk = new ZooKeeper(hostPort,15000,this) ;
    }

    public void process(WatchedEvent event) {
        logger.info(event.toString()+","+hostPort);
    }

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


    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost:2181");
        client.startZk();
        String  name = client.queueCommand("test");
        System.out.println("created = [" + name + "]");
    }
}
