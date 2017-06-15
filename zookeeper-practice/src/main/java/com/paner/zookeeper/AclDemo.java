package com.paner.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by paner on 17/6/1.
 */
public class AclDemo {

    private static final String zkAddress = "localhost:2181";
    private static final String testNode = "/testp";
    private static final String readAuth = "read-user:123456";
    private static final String writeAuth = "write-user:123456";
    private static final String deleteAuth = "delete-user:123456";
    private static final String allAuth = "super-user:123456";
    private static final String adminAuth = "admin-user:123456";
    private static final String digest = "digest";

    public static void main(String[] args) throws NoSuchAlgorithmException {

        initNode();

        System.out.println("---------------------");

        readTest();

        System.out.println("---------------------");

        writeTest();

        System.out.println("---------------------");

        changeACLTest();

        System.out.println("---------------------");

        deleteTest();
    }

    private static  void initNode() throws NoSuchAlgorithmException {
        ZkClient zkClient = new ZkClient(zkAddress);
        zkClient.addAuthInfo(digest,allAuth.getBytes());

        if (zkClient.exists(testNode)){
            zkClient.delete(testNode);
            System.out.println("节点删除成功");
        }

        List<ACL> acls = new ArrayList<ACL>();
        acls.add(new ACL(ZooDefs.Perms.ALL, new Id(digest, DigestAuthenticationProvider.generateDigest(allAuth))));
        acls.add(new ACL(ZooDefs.Perms.READ, new Id(digest, DigestAuthenticationProvider.generateDigest(readAuth))));
        acls.add(new ACL(ZooDefs.Perms.WRITE, new Id(digest, DigestAuthenticationProvider.generateDigest(writeAuth))));
        acls.add(new ACL(ZooDefs.Perms.DELETE, new Id(digest, DigestAuthenticationProvider.generateDigest(deleteAuth))));
        acls.add(new ACL(ZooDefs.Perms.ADMIN, new Id(digest, DigestAuthenticationProvider.generateDigest(adminAuth))));

        zkClient.createPersistent(testNode, "test-data", acls);
        System.out.println(zkClient.readData(testNode));
        System.out.println("节点创建成功");
        zkClient.close();
    }

    private static void readTest(){

        ZkClient zkClient =  new ZkClient(zkAddress);

        try {
            System.out.println(zkClient.readData(testNode));//没有认证信息，读取会出错
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        try {
            zkClient.addAuthInfo(digest, adminAuth.getBytes());
            System.out.println(zkClient.readData(testNode));//admin权限与read权限不匹配，读取也会出错
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            zkClient.addAuthInfo(digest, readAuth.getBytes());
            System.out.println(zkClient.readData(testNode));//只有read权限的认证信息，才能正常读取
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        zkClient.close();
    }

    private static void writeTest() {
        ZkClient zkClient = new ZkClient(zkAddress);

        try {
            zkClient.writeData(testNode, "new-data");//没有认证信息，写入会失败
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            zkClient.addAuthInfo(digest, writeAuth.getBytes());
            zkClient.writeData(testNode, "new-data");//加入认证信息后,写入正常
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            zkClient.addAuthInfo(digest, readAuth.getBytes());
            System.out.println(zkClient.readData(testNode));//读取新值验证
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        zkClient.close();
    }

    private static void deleteTest() {
        ZkClient zkClient = new ZkClient(zkAddress);
        //zkClient.addAuthInfo(digest, deleteAuth.getBytes());
        try {
            //System.out.println(zkClient.readData(testNode));
            zkClient.delete(testNode);
            System.out.println("节点删除成功！");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        zkClient.close();
    }

    private static void changeACLTest() {
        ZkClient zkClient = new ZkClient(zkAddress);
        //注：zkClient.setAcl方法查看源码可以发现，调用了readData、setAcl二个方法
        //所以要修改节点的ACL属性，必须同时具备read、admin二种权限
        zkClient.addAuthInfo(digest, adminAuth.getBytes());
        zkClient.addAuthInfo(digest,readAuth.getBytes());
        try {
            List<ACL> acls = new ArrayList<ACL>();
            acls.add(new ACL(ZooDefs.Perms.ALL, new Id(digest, DigestAuthenticationProvider.generateDigest(adminAuth))));
            zkClient.setAcl(testNode, acls);
            Map.Entry<List<ACL>, Stat> aclResult = zkClient.getAcl(testNode);
            System.out.println(aclResult.getKey());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        zkClient.close();
    }

    @Test
    public void test(){
        Random random = new Random();
        System.out.println(random.nextInt(100));
    }
}
