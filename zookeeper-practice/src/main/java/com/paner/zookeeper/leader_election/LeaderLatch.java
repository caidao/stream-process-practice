package com.paner.zookeeper.leader_election;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import javax.sound.midi.Soundbank;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by paner on 17/6/2.
 */
public class LeaderLatch {

    private static final String ELECTION_PATH  ="/election";
    private static final String CLIENT_PATH  = "/client_";
    private  String currentClientId = null;
    private ZooKeeper zk =null;
    private String path =null;
    private String leaderPath = null;
    private CountDownLatch latch = new CountDownLatch(1);


    public LeaderLatch(String hostPorts,String path){
        try {
            this.zk = new ZooKeeper(hostPorts, Integer.MAX_VALUE, new LeaderWatcher(this));
            this.path = path;
        }catch (Exception ex){
            System.out.println("register failure, error:"+ex.getMessage());
        }
    }

    public void register() throws KeeperException, InterruptedException {
        //1.创建election节点
        createElectionNode();
        //2.在election节点上创建子节点B，该节点的信息含有当前机器的信息
        createElectionChildNode();
        //3.监控前一个子节点A
        monitorPriorNode();
        //4.获取并缓存leader节点
        latch.await();
        cacheLeaderPath();
        //5.leader处理
        leaderProcess();

    }



    private String getElectionPath(){
        return path+ELECTION_PATH;
    }

    private String formatPath(String srcPath){
        return getElectionPath()+"/"+srcPath;
    }


    //创建选举节点
    private void createElectionNode() throws KeeperException, InterruptedException {
        if (zk.exists(getElectionPath(),false)==null){
            String path =zk.create(getElectionPath(),null ,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            System.out.println("create election parent path:"+path);
        }else {
            System.out.println("election node already created.");
        }
    }

    //将当前节点注册上去
    private void createElectionChildNode() throws KeeperException, InterruptedException {
        byte[] localhost = Thread.currentThread().getName().getBytes();
        currentClientId = zk.create(getElectionPath()+CLIENT_PATH,
                localhost,ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("create current election node, path:"+currentClientId);
        latch.countDown();
    }

    //找到leader节点
    public    String findLeader() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(getElectionPath(), false);
        if (children==null || children.isEmpty()){
            System.out.println("node don't exist.");
            return null;
        }
        String leader = getElectionPath()+ "/"+Collections.min(children);
        System.out.println("leader node is " + leader);
        return leader;
    }

    /**
     * 监控当前节点序号左边第一个节点
     */
    public void monitorPriorNode(){
        try {
            String temp = findWatchNode();
            if (temp !=null){
                System.out.println("monitor node:"+temp);
                zk.exists(temp, true);
            }
        }catch (Exception ex){
            System.out.println("monitor failure, error message:"+ex.getMessage());
        }
    }

    private String  findWatchNode() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(getElectionPath(),false);
        if (children!=null && !children.isEmpty()){
            //找到最近的最小的节点
            String watchNode = null;
            for (String child:children){
                String temp = formatPath(child);
                if (temp.compareTo(currentClientId)<0){
                    watchNode = temp;
                    break;
                }
            }

            if (watchNode==null){
                return null;
            }

            //找到最接近当前节点的client节点
            for (String child:children) {
                String temp = formatPath(child);
                if (temp.compareTo(watchNode) > 0 && temp.compareTo(currentClientId) < 0) {
                    watchNode = temp;
                }
            }
            System.out.println("findWatchNode  node  = " + watchNode);
            return watchNode;
        }
        System.out.println("findWatchNode no  exist! ");
        return null;
    }

    public void leaderProcess() throws KeeperException, InterruptedException {
        //如果当前节点为leader节点，则将该信息写到election上
        if (currentClientId.equalsIgnoreCase(findLeader())) {
            Stat stat = new Stat();
            zk.getData(getElectionPath(), false, stat);
            String temp = "id:" + Thread.currentThread().getName() + ",path:" + currentClientId;
            zk.setData(getElectionPath(), temp.getBytes(), stat.getVersion());
        }
    }

    public String getLeaderPath() {
        return leaderPath;
    }

    //缓存leader path
    public void cacheLeaderPath() {
        try {
            this.leaderPath = findLeader();
        } catch (Exception e) {
            System.out.println("cache Leader Path ! ");
        }
    }

    public void monitorElectionNode(Watcher watcher) throws KeeperException, InterruptedException {
        zk.exists(getElectionPath(),watcher);
    }

    public String getElectionData(Watcher watcher){
        try {
            byte[] bytes=   zk.getData(getElectionPath(),watcher,new Stat());
            if (bytes!=null){
                return new String(bytes);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
