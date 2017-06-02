package com.paner.zookeeper.leader_election;

import com.sun.tools.corba.se.idl.StringGen;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by paner on 17/6/2.
 */
public class LeaderLatch implements Watcher{

    private static final String ELECTION_PATH  ="/election";
    private static final String CLIENT_PATH  = "client_";
    private  String currentClientId = null;
    private ZooKeeper zk =null;
    private String path =null;

    public static void  main(String [] args) throws IOException, KeeperException, InterruptedException {

        ZooKeeper zk = new ZooKeeper("127.0.0.1:8121", 5000, new Watcher() {
            public void process(WatchedEvent event) {
                System.out.println("event change:"+event);
            }
        });
    }

    private String getElectionPath(){
        return path+ELECTION_PATH;
    }

    private void createElectionNode() throws KeeperException, InterruptedException {
        if (zk.exists(getElectionPath(),false)==null){
            String path =zk.create(getElectionPath(),null ,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            System.out.println("create election parent path:"+path);
        }else {
            System.out.println("election node already created.");
        }
    }

    private void createElectionChildNode() throws KeeperException, InterruptedException {
        byte[] localhost = Thread.currentThread().getName().getBytes();
        currentClientId = zk.create(getElectionPath()+CLIENT_PATH,localhost,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("create current election node, path:"+currentClientId);
    }

    private   String findLeader() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(getElectionPath(),false);
        if (children==null || children.isEmpty()){
            System.out.println("node don't exist.");
            return null;
        }
        String leader = getElectionPath()+ Collections.min(children);
        System.out.println("leader node is "+leader);
        return leader;
    }

    private String  findWatchNode() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(getElectionPath(),false);
        if (children!=null && !children.isEmpty()){
            //找到最近的最小的节点
            String watchNode = null;
            for (String child:children){
                if (child.compareTo(currentClientId)<0){
                    watchNode = child;
                    break;
                }
            }

            //找到最接近当前节点的client节点
            for (String child:children) {
                if (child.compareTo(watchNode) > 0 && child.compareTo(currentClientId) < 0) {
                    watchNode = child;
                }
            }
            System.out.println("findWatchNode  node  = " + watchNode);
            return watchNode;
        }
        System.out.println("findWatchNode no  exist! ");
        return null;
    }


    public void process(WatchedEvent event) {
        if (event.getState()== Event.KeeperState.Disconnected){

        }
    }
}
