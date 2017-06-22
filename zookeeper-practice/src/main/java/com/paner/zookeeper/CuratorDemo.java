package com.paner.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        czk.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground(createStringCallback).forPath("/test_", "test".getBytes());
        Thread.sleep(100000);
       // byte[] bytes = czk.getData().forPath("/test1");
      //  System.out.println(new String(bytes));
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
        czk.getConnectionStateListenable().addListener(connectionStateListener);
        czk.start();
        czk.inTransaction().check().withVersion(-1).forPath("/test2")
                .and().create().withMode(CreateMode.EPHEMERAL).forPath("/test2","data".getBytes())
                .and().setData().withVersion(-1).forPath("/test2","data2".getBytes())
                .and().commit();
        Thread.sleep(10000);
        czk.close();
    }


    @Test
    /**
     * Path Cache用来监控一个ZNode的子节点
     */
    public void pathCache() throws Exception {
        String path = "/example/pathCache";
        CuratorFramework czk = createFramework("127.0.0.1:2181");
        czk.start();
        PathChildrenCache cache = new PathChildrenCache(czk,path,true);
        cache.start();
        //事件监听器
        PathChildrenCacheListener cacheListener = new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                System.out.println("事件类型 = [" + event + "]");
                if (event.getData() !=null){
                    System.out.println("节点数据：" + event.getData().getPath() + " = " + new String(event.getData().getData()));
                }
            }
        };

        cache.getListenable().addListener(cacheListener);
        czk.create().creatingParentContainersIfNeeded().forPath(path + "/test01", "01".getBytes());
        czk.create().creatingParentContainersIfNeeded().forPath(path+"/test02","02".getBytes());
        czk.create().creatingParentContainersIfNeeded().forPath(path+"/test03","03".getBytes());
        Thread.sleep(1000);
        for (ChildData data:cache.getCurrentData()){
            System.out.println("getCurrentData:"+data.getPath()+"="+ new String(data.getData()));
        }
        Thread.sleep(10000);
        cache.close();
        czk.close();
    }

    private ConnectionStateListener connectionStateListener = new ConnectionStateListener() {
        public void stateChanged(CuratorFramework client, ConnectionState newState) {

            if (newState == ConnectionState.LOST){
                //处理session过期

            }

            if (!newState.isConnected()){
                System.out.println("client = [" + client + "], newState = [" + newState + "]");
            }
        }
    };



    @Test
    //leader latch选举
    public void leaderLatch() throws Exception {
        int client_ids = 10;
        String leaderPath = "/recepies/master";
        List<CuratorFramework> clients = new ArrayList<CuratorFramework>();
        List<LeaderLatch> examples = new ArrayList<LeaderLatch>();

        for (int i=0;i<client_ids;i++){
            CuratorFramework client = createFramework("127.0.0.1:2181");
            clients.add(client);
            LeaderLatch leaderLatch = new LeaderLatch(client,leaderPath,"client#"+i);
            leaderLatch.addListener(new LeaderLatchListener() {
                public void isLeader() {
                    System.out.println("I am leader.");
                }

                public void notLeader() {
                    System.out.println("I am not Leader");
                }
            });
            examples.add(leaderLatch);
            client.start();
            leaderLatch.start();
        }
        Thread.sleep(10000);
        LeaderLatch currentLeader = null;
        for (LeaderLatch leaderLatch:examples){
            if(leaderLatch.hasLeadership()){
              currentLeader = leaderLatch;
            }
        }
        System.out.println("current leader is " + currentLeader.getId());
        System.out.println("release the leader " + currentLeader.getId());
        currentLeader.close();

        //关闭后重新选举
        Thread.sleep(5000);
        for (LeaderLatch latch : examples) {
            if (latch.hasLeadership()) {
                currentLeader = latch;
            }
        }
        System.out.println("current leader is " + currentLeader.getId());
        System.out.println("release the leader " + currentLeader.getId());

        //关闭所有client和leader
        Thread.sleep(5000);
        for (LeaderLatch latch:examples){
            latch.close();
        }
        for (CuratorFramework client:clients){
            client.close();
        }
    }

}
