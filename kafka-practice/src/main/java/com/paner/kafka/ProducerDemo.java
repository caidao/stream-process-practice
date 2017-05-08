package com.paner.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Properties;
import java.util.Random;

/**
 * Created by paner on 17/5/8.
 */
public class ProducerDemo {

    @Test
    public  void demo() throws InterruptedException {
        Random rnd = new Random();
        int events = 100;

        // 设置配置属性
        Properties props = new Properties();
        props.put("bootstrap.servers","localhost:9092");
        //值为0,1,-1,可以参考
        props.put("acks","all");
        props.put("batch.size", 16384);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String,String> producer = new KafkaProducer<String, String>(props);
        for (int i=0;i<events;i++){
            producer.send(new ProducerRecord<String, String>("topic_0508",String.valueOf(i),String.valueOf(rnd.nextInt())));
            Thread.sleep(500);
        }
        producer.close();

    }


}
