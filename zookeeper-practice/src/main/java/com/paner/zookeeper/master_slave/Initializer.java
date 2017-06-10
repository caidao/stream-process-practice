package com.paner.zookeeper.master_slave;


import org.apache.log4j.Logger;
import org.apache.zookeeper.*;


/**
 * Created by paner on 17/6/10.
 */
public class Initializer {

    private ZooKeeper zk;
    private  Logger logger = Logger.getLogger(Initializer.class);

    public void bootstrap(){

        createParent("/workers",new byte[0]);
        createParent("/assign",new byte[0]);
        createParent("/tasks",new byte[0]);
        createParent("/status",new byte[0]);

    }

    private void createParent(String path,byte[] data){
        zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,createParentCallback,data);
    }

    private AsyncCallback.StringCallback createParentCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    createParent(path, (byte[]) ctx);
                    break;
                case OK:
                    logger.info("parent created.");
                    break;
                case NODEEXISTS:
                    logger.warn("parent already register :"+path);
                    break;
                default:
                    logger.error("something went wrong:",KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };

}
