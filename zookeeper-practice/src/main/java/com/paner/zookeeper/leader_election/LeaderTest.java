package com.paner.zookeeper.leader_election;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/6/4.
 */
public class LeaderTest {

    public static void main(String args[]) throws KeeperException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final LeaderLatch leaderLatch = new LeaderLatch("127.0.0.1:2181","");
        Random random = new Random(100);
        String threadName = "thread-"+ random.nextInt();
        Thread.currentThread().setName(threadName);
        leaderLatch.register();
        //
        Thread.sleep(10000);
        System.out.println("leader:"+leaderLatch.getLeaderPath());
        //监控election节点
        leaderLatch.monitorElectionNode(new Watcher() {
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeDataChanged){
                    System.out.println("election data = [" + leaderLatch.getElectionData(this) + "]");
                }
            }
        });
       latch.await();
    }


    public static void monitorElectionNode(String path){

    }
}
