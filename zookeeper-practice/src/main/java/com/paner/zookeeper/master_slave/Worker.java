package com.paner.zookeeper.master_slave;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Random;

/**
 * Created by paner on 17/6/10.
 */
public class Worker implements Watcher{

    private static Random random = new Random(100);
    private  String serverId = Integer.toString(random.nextInt());
    private ZooKeeper zk;
    private String hostPort;
    private Logger logger = Logger.getLogger(Watcher.class);

    public Worker(String hostPort){
        this.hostPort = hostPort;
    }

    public void startZk() throws IOException {
      zk = new ZooKeeper(hostPort,15000,this) ;
    }

    public void process(WatchedEvent event) {
        logger.info(event.toString()+","+hostPort);
    }

    private AsyncCallback.StringCallback createWorkCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    register();
                    break;
                case OK:
                    logger.info("register successfully:" + serverId);
                    break;
                case NODEEXISTS:
                    logger.warn("already register:"+serverId);
                    break;
                default:
                    logger.error("something went wrong:",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

    private void register(){
        zk.create("/workers/worker_"+serverId,"idle".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,createWorkCallback,null);
    }
}
