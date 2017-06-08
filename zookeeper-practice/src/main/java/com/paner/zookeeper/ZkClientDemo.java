package com.paner.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/5/31.
 */

public class ZkClientDemo {

    private  static  Logger log = LoggerFactory.getLogger(ZkClientDemo.class);

    public ZooKeeper  createZk() throws IOException, InterruptedException {
        final  CountDownLatch latch= new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 500000, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("监听器，监听到的事件是：" + watchedEvent);
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()){
                    if (watchedEvent.getType()== Event.EventType.None){
                        latch.countDown();
                    }
                    if (watchedEvent.getType()== Event.EventType.NodeDataChanged){
                        System.out.println("节点值被修改");
                    }else if (watchedEvent.getType()== Event.EventType.NodeDeleted){
                        System.out.println("节点被删除");
                    }
                }
            }
        });
        latch.await();
        return zk;
    }

    @Test
    //异步创建path
    public void asyncCreate() throws IOException, InterruptedException {
        ZooKeeper zk = createZk();
        final  String path = "/temp";
        zk.create(path, "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
            public void processResult(int rc, String path, Object ctx, String name) {
                switch (KeeperException.Code.get(rc)){
                    case CONNECTIONLOSS:
                        break;
                    case OK:
                        System.out.println(path);
                        break;
                    default:
                        System.out.println(name);
                }
            }
        },path);
        System.out.println("1");
        Thread.sleep(100000);
        zk.close();
    }


    @Test
    public void demo1() throws InterruptedException, KeeperException, IOException {

        ZooKeeper zk = createZk();
        //等待连接建立
        String path = "/test2";
        if(zk.exists(path, true)==null){
            zk.create(path,"init".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        }


        Thread.sleep(1000);
        Stat stat = new Stat();
        System.out.println(zk.getData(path, false, stat).toString());
        zk.exists(path, true);//触发创建时的watcher
        zk.setData(path, "change".getBytes(), stat.getVersion());
        Thread.sleep(1000);
        System.out.println(zk.getData(path, false, stat).toString());
        //注册新的监听器
        zk.exists(path, new Watcher() {
            public void process(WatchedEvent event) {
                System.out.println("我是新的监听器：节点被删除");
            }
        });
        zk.delete(path,stat.getVersion());
        zk.close();
        Thread.sleep(1000);
    }

    @Test
    public void childrenDemo() throws KeeperException, InterruptedException, IOException {
        ZooKeeper zk = createZk();
        String parentPath = "/parent";

       if(zk.exists(parentPath,true)==null) {
           zk.create(parentPath, "init".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
       }

        for (int i=0;i<10;i++) {
            zk.create(parentPath + "/child", String.valueOf(i).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        }

        List<String> children = zk.getChildren(parentPath,true);
        System.out.println(children);
        Thread.sleep(10000);
        zk.close();
    }
}
