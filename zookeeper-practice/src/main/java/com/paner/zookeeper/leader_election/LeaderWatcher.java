package com.paner.zookeeper.leader_election;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by paner on 17/6/2.
 */
public class LeaderWatcher implements Watcher{

    private  LeaderLatch leaderLatch = null;

    public  LeaderWatcher(LeaderLatch leaderLatch){
        this.leaderLatch = leaderLatch;
    }

    public void process(WatchedEvent event) {
        System.out.println("event = [" + event + "]");
        if (event.getState() == Event.KeeperState.Disconnected
                || event.getType()== Event.EventType.NodeDeleted){
            //父节点断开连接或删除，则子节点重新选举
        }
    }
}
