package com.paner.zookeeper.leader_election;

import org.apache.zookeeper.KeeperException;
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

    /**
     * 处理最接近当前节点的delete事件
     * @param event
     */
    public void process(WatchedEvent event) {
        System.out.println("event = [" + event + "]");
        if (event.getState() == Event.KeeperState.Disconnected
                || event.getType()== Event.EventType.NodeDeleted){
            String delNode = event.getPath();
            try {
                //如果失效节点为leader，则重新选举
                if (delNode.equalsIgnoreCase(leaderLatch.getLeaderPath())){
                    leaderLatch.leaderProcess();
                }
                //更新leader缓存节点
                leaderLatch.cacheLeaderPath();
                //监控当前节点的邻近最小节点
                leaderLatch.monitorPriorNode();

            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
