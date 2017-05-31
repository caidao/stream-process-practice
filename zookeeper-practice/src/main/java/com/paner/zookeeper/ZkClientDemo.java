package com.paner.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/5/31.
 */

public class ZkClientDemo {

//    private   CountDownLatch latch;
//    @Before
//    public  void before(){
//        latch = new CountDownLatch(1);
//    }


    @Test
    public void demo1() throws IOException, KeeperException, InterruptedException {
       final CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 5000, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("监听器，监听到的事件时：" + watchedEvent);
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()){
                    //如果客户端已经建立连接闭锁减一
                    System.out.println("建立连接");
                }
            }
        });
        //等待连接建立
        Thread.sleep(1000);
        String path = "/test";
        zk.create(path,"init".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

        Thread.sleep(1000);
        System.out.println(zk.getData(path, false, new Stat()));
        zk.close();
    }
}
