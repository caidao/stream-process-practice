package com.paner.kafka;

import kafka.api.*;
import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetCommitRequest;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.TopicMetadataRequest;
import kafka.network.BlockingChannel;
import org.apache.kafka.common.requests.ListOffsetRequest;
import org.junit.Before;
import org.junit.Test;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.JavaConverters$;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paner on 17/5/15.
 */
public class KafkaProtocol {

    private BlockingChannel channel;
    final String MY_CLIENTID = "demoClientId";

    @Before
    public void before(){

    }

    //元数据接口
    @Test
    public void topicMetadata(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            TopicMetadataRequest request = new TopicMetadataRequest(Arrays.asList("__consumer_offsets"));
            channel.send(request);
            TopicMetadataResponse response = TopicMetadataResponse.readFrom(channel.receive().payload());
            System.out.println("TopicMetadataResponse:"+response.describe(true));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }

    //获取消息接口
    @Test
    public void fetchRequest(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            final String MY_GROUP = "demoGroup";

            int correlationId = 0;
            final TopicAndPartition testPartition0 = new TopicAndPartition("topic_0511_2", 1);
            final PartitionFetchInfo partitionFetchInfo0 = new PartitionFetchInfo(0,100);
            Map<TopicAndPartition, PartitionFetchInfo> requestInfo =new HashMap<TopicAndPartition,PartitionFetchInfo>();
            requestInfo.put(testPartition0, partitionFetchInfo0);
            //这里的map是kafak封装的，而非java.util中提供的
            FetchRequest request = new FetchRequest(++correlationId,MY_CLIENTID,10,10,
                    convert(requestInfo));
            channel.send(request);
            FetchResponse response = FetchResponse.readFrom(channel.receive().payload(), 100);
            System.out.println(response.describe(true));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }


    @Test
    public void listOffset(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            String groupId = "test";
            long now = System.currentTimeMillis();
            final TopicAndPartition testPartition0 = new TopicAndPartition("topic_0511_2", 1);
            final OffsetAndMetadata partitionFetchInfo0 = new OffsetAndMetadata(new OffsetMetadata(0,null),now,now);
            Map<TopicAndPartition, OffsetAndMetadata> requestInfo =new HashMap<TopicAndPartition,OffsetAndMetadata>();
            requestInfo.put(testPartition0, partitionFetchInfo0);
            //ListOffsetRequest request = new ListOffsetRequest(null);
            //channel.send(request.);
            TopicMetadataResponse response = TopicMetadataResponse.readFrom(channel.receive().payload());
            System.out.println("TopicMetadataResponse:"+response.describe(true));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }

    @Test
    public void offsetCommitApi(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            String groupId = "test";
            long now = System.currentTimeMillis();
            final TopicAndPartition testPartition0 = new TopicAndPartition("topic_0511_2", 1);
            final OffsetAndMetadata partitionFetchInfo0 = new OffsetAndMetadata(new OffsetMetadata(100,"more metadata"),now,now);
            Map<TopicAndPartition, OffsetAndMetadata> requestInfo =new HashMap<TopicAndPartition,OffsetAndMetadata>();
            requestInfo.put(testPartition0, partitionFetchInfo0);
            OffsetCommitRequest request = new OffsetCommitRequest(groupId,requestInfo,1,MY_CLIENTID);
            channel.send(request.underlying());
            OffsetCommitResponse response = OffsetCommitResponse.readFrom(channel.receive().payload());
            System.out.println("OffsetCommitRequest:"+response.describe(true));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }



    public <K, V> scala.collection.immutable.Map<K, V> convert(Map<K, V> m) {
        return JavaConverters$.MODULE$.mapAsScalaMapConverter(m).asScala().toMap(
                scala.Predef$.MODULE$.<scala.Tuple2<K, V>>conforms()
        );
    }
}
