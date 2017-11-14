package com.paner.elasticsearch;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
        private static final Log LOG = LogFactory.getLog(RedisClient.class);
        private JedisPool redisPool;

        private RedisClient() {
            init();
        }

        public static RedisClient getInstance() {
            return SingletonHandler.redisClient;
        }

        /**
         * get a redis connection from pool
         */
        public Jedis getResource() {
            return redisPool.getResource();
        }

        private void init() {
            if (redisPool == null) {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMinIdle(20);
                config.setMaxIdle(100);
                config.setMaxTotal(500);
                config.setBlockWhenExhausted(false);
                config.setMaxWaitMillis(1000 * 3);
                config.setTestOnBorrow(false);
                config.setTestOnReturn(false);
                config.setTestWhileIdle(false);

                String hostPortParam = "10.101.95.234:6379";// "10.101.95.234:6379";
                LOG.info("--> hostPortParam = " + hostPortParam);
                String[] hostPort = hostPortParam.split(":");
                String host = hostPort[0];
                int port = 8793;
                try {
                    port = Integer.parseInt(hostPort[1]);
                } catch (Exception e) {
                    LOG.error( e.getMessage(), e);
                }
                redisPool = new JedisPool(config, host, port);
            }
            LOG.info("--> Init RedisPool OK");
            return ;
        }

        /**
         * 单例
         * */
        private static class SingletonHandler {
            private static RedisClient redisClient = new RedisClient();
            private SingletonHandler() { }
        }

}
