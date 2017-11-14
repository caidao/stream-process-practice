package com.paner.rocksdb;

import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.List;

public class RocksDbClient {

    private static RocksDbClient rocksDbClient;

    private  RocksDB rocksDB;
    private static Options options;
    private static String dbPath = "/Users/pan/code/stream-process-practice/rocksdbpractice/src/main/resources/data/";

    public static RocksDbClient getInstance(){
        if (rocksDbClient==null){
            options = new Options();
            options.setCreateIfMissing(true);
            rocksDbClient = new RocksDbClient();
        }
        return rocksDbClient;
    }


    private void openRocksDb() throws RocksDBException {
        rocksDB = RocksDB.open(options, dbPath);
    }

    private void closeRocksDb(){
        if (rocksDB!=null){
            rocksDB.close();
        }
    }

    private void put(String key,String value) throws RocksDBException {
        rocksDB.put(key.getBytes(),value.getBytes());

        List<byte[]> cfs = RocksDB.listColumnFamilies(options,dbPath);
        for (byte[] cf: cfs){
            System.out.println(new String(cf));
        }

        byte[] getValue = rocksDB.get(key.getBytes());
        System.out.println(new String(getValue));
    }

    @Test
    public void writeDb() throws RocksDBException {

        RocksDbClient.getInstance().openRocksDb();
        RocksDbClient.getInstance().put("hello","world");
        RocksDbClient.getInstance().closeRocksDb();

    }


}
