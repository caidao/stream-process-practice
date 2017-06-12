package com.paner.zookeeper.master_slave;

import com.sun.xml.internal.bind.v2.util.StackRecorder;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/6/11.
 */
public class AdminClient implements Watcher{

    private ZooKeeper zk;

    private String hostPort;

    private Logger logger = Logger.getLogger(Client.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public AdminClient(String hostPort){
        this.hostPort = hostPort;
    }

    public void startZk() throws IOException, InterruptedException {
        zk = new ZooKeeper(hostPort,15000,this) ;
        latch.await();
    }

    public void listState() throws KeeperException, InterruptedException {

        Stat stat = new Stat();
        byte[] masterData = zk.getData("/master",false,stat);
        Date date = new Date(stat.getCtime());
        System.out.println("master;" + new String(masterData) + "since " + date);

        System.out.println("workers:");
        for (String w:zk.getChildren("/workers",false)){
            byte[] data = zk.getData("/workers/"+w,false,null);
            System.out.println("\t" + w + ":" + new String(data));
        }

        System.out.println("tasks:");
        for (String t:zk.getChildren("/assign",false)){
            System.out.println("\t"+t);
        }


    }

    public void process(WatchedEvent event) {
        logger.info(event.toString()+","+hostPort);
        if (Event.KeeperState.SyncConnected == event.getState()){
            latch.countDown();
        }
    }


    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        AdminClient adminClient = new AdminClient("localhost:2181");
        adminClient.startZk();
        adminClient.listState();
    }
}
