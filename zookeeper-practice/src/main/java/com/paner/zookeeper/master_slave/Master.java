package com.paner.zookeeper.master_slave;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Random;

/**
 * Created by paner on 17/6/10.
 */
public class Master {
    private static Random random = new Random(100);
    private  String serverId = Integer.toString(random.nextInt());
    private  boolean isLeader;
    private  ZooKeeper zk;
    private  String master_path = "/master";

    private  AsyncCallback.StringCallback masterCreateCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    checkMaster();
                    return;
                case OK:
                    isLeader=true;
                    break;
                default:
                    isLeader = false;
            }
            System.out.println("I'm"+(isLeader?"":"not")+" the leader");
        }
    };

    public  boolean checkMaster(){
        while (true){
            Stat stat = new Stat();
            try {
                byte data[] = zk.getData(master_path,false,stat);
                isLeader = new String(data).equals(serverId);
                return true;
            } catch (KeeperException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void runForMaster(){
        zk.create(master_path,serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,masterCreateCallback,null);
    }
}
