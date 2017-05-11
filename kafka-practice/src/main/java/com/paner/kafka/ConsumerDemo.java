package com.paner.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by paner on 17/5/8.
 */
public class ConsumerDemo {


    @Test
    public void demo(){
        Properties props = new Properties();
        props.put("bootstrap.servers","localhost:9092");
        props.put("enable.auto.commit", "true");
        props.put("group.id", "test");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        new ConsumerThread(props).start();
        new ConsumerThread(props).start();
        while (true);

    }


    public class ConsumerThread extends Thread{

        private  KafkaConsumer<String, Object> consumer;

        public ConsumerThread(Properties props){
            consumer = new KafkaConsumer<String,Object>(props);
            consumer.subscribe(Arrays.asList("__consumer_offsets"));
        }

        public void run(){
            while (true){
                ConsumerRecords<String, Object> records = consumer.poll(100);
                for(TopicPartition partition : records.partitions()){
                    List<ConsumerRecord<String,Object>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<String,Object> record:partitionRecords){
                        System.out.printf(Thread.currentThread().getName()+",partition =%d,offset = %d, key = %s, value = %s \n",record.partition(), record.offset(), record.key(), record.value());
                    }
                }

            }
        }
    }

}
