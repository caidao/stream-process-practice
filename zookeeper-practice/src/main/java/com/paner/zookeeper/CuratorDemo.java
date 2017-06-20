package com.paner.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

/**
 * Created by paner on 17/6/20.
 */
public class CuratorDemo {

    private CuratorFramework createFramework(String hostports){
        return CuratorFrameworkFactory.builder()
                .connectString(hostports)
                .sessionTimeoutMs(500000)
                .connectionTimeoutMs(500000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .namespace("base")
                .build();
    }


    @Test
    public void demo1() throws Exception {
        CuratorFramework czk = createFramework("127.0.0.1:2181");
        czk.start();
        czk.create().withMode(CreateMode.PERSISTENT).inBackground(createStringCallback).forPath("/test1", "test".getBytes());
        Thread.sleep(1000);
        byte[] bytes = czk.getData().forPath("/test1");
        System.out.println(new String(bytes));
       // czk.delete().forPath("/test1");

        czk.close();
    }

    private BackgroundCallback createStringCallback = new BackgroundCallback() {
        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
            switch (event.getResultCode()){
                case -110:
                    System.out.println("client = [" + client + "], event = [" + event + "]");
                    client.delete().forPath("/test1");
            }
        }
    };


    @Test
    public void  transaction() throws Exception {
        CuratorFramework czk = createFramework("127.0.0.1:2181");
        czk.start();
        czk.inTransaction().check().withVersion(-1).forPath("/test2")
                .and().create().withMode(CreateMode.EPHEMERAL).forPath("/test2","data".getBytes())
                .and().setData().withVersion(-1).forPath("/test2","data2".getBytes())
                .and().commit();
        Thread.sleep(10000);
        czk.close();
    }

}
