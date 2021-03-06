package com.paner.kafka;

import org.apache.kafka.clients.producer.*;
import org.junit.Test;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by paner on 17/5/8.
 */
public class ProducerDemo {

    @Test
    public  void demo() throws InterruptedException, ExecutionException {
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

        boolean isSync =true;
        String topic = "topic_0511_2";
        Producer<String,String> producer = new KafkaProducer<String, String>(props);
        for (int i=0;i<events;i++){
            RecordMetadata metadata =null;
            if (isSync){
               metadata= producer.send(new ProducerRecord<String, String>(topic, String.valueOf(i), String.valueOf(rnd.nextInt()))).get();
                System.out.println("RecordMetadata async:"+metadata);
            }else {
                //回调处理错误信息
                producer.send(new ProducerRecord<String, String>(topic, String.valueOf(i), String.valueOf(rnd.nextInt()))
                        , new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        System.out.println("RecordMetadata sync:"+metadata);
                    }
                });
            }

        }
        producer.close();

    }


}
