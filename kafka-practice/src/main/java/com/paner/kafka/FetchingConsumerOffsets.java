package com.paner.kafka;

import kafka.api.FetchRequest;
import kafka.common.OffsetAndMetadata;
import kafka.common.TopicAndPartition;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.OffsetCommitRequest;
import kafka.network.BlockingChannel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.*;

/**
 * Created by paner on 17/5/12.
 */
public class FetchingConsumerOffsets {

    private BlockingChannel channel;

    private void init(){
        try {
            BlockingChannel channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            final String MY_GROUP = "demoGroup";
            final String MY_CLIENTID = "demoClientId";
            int correlationId = 0;
            final TopicAndPartition testPartition0 = new TopicAndPartition("demoTopic", 0);
            final TopicAndPartition testPartition1 = new TopicAndPartition("demoTopic", 1);


        }catch (Exception ex){

        }
    }

    @Test
    public void offset_partition(){
        System.out.println("partition ID:"+Math.abs("group_paner".hashCode()) % 50);
    }

    @Test
    public void offset(){
        Properties props = new Properties();
        props.put("bootstrap.servers","localhost:9092");
        props.put("enable.auto.commit", "true");
        props.put("group.id", "group_offset");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String,Object> consumer = new KafkaConsumer<String,Object>(props);
        consumer.subscribe(Arrays.asList("__consumer_offsets"));
//        TopicPartition partitions = new TopicPartition("__consumer_offsets", 10);
//        consumer.assign(Arrays.asList(partitions));
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
