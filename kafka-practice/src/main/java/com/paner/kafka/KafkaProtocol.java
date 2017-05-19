package com.paner.kafka;

import kafka.api.*;
import kafka.api.FetchRequest;
import kafka.api.FetchResponse;
import kafka.api.GroupCoordinatorResponse;
import kafka.api.OffsetCommitResponse;
import kafka.api.OffsetFetchRequest;
import kafka.api.TopicMetadataResponse;
import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.OffsetCommitRequest;
import kafka.javaapi.OffsetFetchResponse;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.TopicMetadataRequest;
import kafka.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.network.BlockingChannel;
import org.apache.kafka.common.requests.ListGroupsRequest;
import org.apache.kafka.common.requests.ListOffsetRequest;
import org.junit.Before;
import org.junit.Test;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.JavaConverters$;


import java.util.*;

/**
 * Created by paner on 17/5/15.
 */
public class KafkaProtocol {

    private BlockingChannel channel;
    final String MY_CLIENTID = "demoClientId";
    int correlationId = 0;

    @Before
    public void before(){

    }

    //topic元数据接口
    @Test
    public void topicMetadata(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            TopicMetadataRequest request = new TopicMetadataRequest(Arrays.asList("streams-file-input"));
            channel.send(request);
            TopicMetadataResponse response = TopicMetadataResponse.readFrom(channel.receive().payload());
            System.out.println("TopicMetadataResponse:"+convertSeq(convertSeq(response.topicsMetadata()).get(0).partitionsMetadata()));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }

    //发送produce请求
    @Test
    public void produceAPi(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
           // TopicMetadataRequest request = new TopicMetadataRequest(Arrays.asList("topic_0511_2"));
            final TopicAndPartition testPartition0 = new TopicAndPartition("topic_0511_2", 1);
            Message msgBuf =new Message("values".getBytes());
            final ByteBufferMessageSet message = new ByteBufferMessageSet(convertList(Arrays.asList(msgBuf)));
            Map<TopicAndPartition,ByteBufferMessageSet> map = new HashMap<TopicAndPartition, ByteBufferMessageSet>(){{
                put(testPartition0,message);
            }};
            ProducerRequest request = new ProducerRequest((short)1,correlationId,MY_CLIENTID,(short)1,300,convertMap(map));
            channel.send(request);
            ProducerResponse response = ProducerResponse.readFrom(channel.receive().payload());
            System.out.println("TopicMetadataResponse:"+response.describe(true));
        }catch (Exception ex){
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            System.out.println("TopicMetadataResponse:"+convertSeq(response.topicsMetadata()));
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }

    //提交offset的方式
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

    //获取offset的方式
    @Test
    public void fetchOffsetApi(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            String groupId = "streams-consumer";
            final TopicAndPartition testPartition0 = new TopicAndPartition("streams-file-input", 1);
            List<TopicAndPartition> partitions = new ArrayList<TopicAndPartition>();
            partitions.add(testPartition0);
            partitions.add(new TopicAndPartition("streams-file-input", 0));
            partitions.add(new TopicAndPartition("streams-file-input", 2));
            OffsetFetchRequest request = new OffsetFetchRequest(groupId,convertList(partitions),(short)1,correlationId,MY_CLIENTID);
            channel.send(request);
            OffsetFetchResponse response = kafka.javaapi.OffsetFetchResponse.readFrom(channel.receive().payload());
            System.out.println("OffsetCommitRequest:"+response.offsets());
        }catch (Exception ex){

        }finally {
            channel.disconnect();
        }
    }


    @Test
    public void groupCoordinator(){
        BlockingChannel channel =null;
        try {
            channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
            channel.connect();
            GroupCoordinatorRequest request = new GroupCoordinatorRequest("group_paner",(short)1,correlationId,MY_CLIENTID);
            channel.send(request);
            GroupCoordinatorResponse response = GroupCoordinatorResponse.readFrom(channel.receive().payload());
            System.out.println("TopicMetadataResponse:"+response.describe(true));
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

    public <K, V> scala.collection.mutable.Map<K, V> convertMap(Map<K, V> m) {
        return JavaConverters$.MODULE$.mapAsScalaMapConverter(m).asScala().seq();
    }

    public <K> scala.collection.Seq<K> convertList(List<K> m) {
        return JavaConverters$.MODULE$.asScalaBufferConverter(m).asScala().toSeq().seq();
    }

    public <K> List<K> convertSeq(scala.collection.Seq<K> m){
        return JavaConverters$.MODULE$.seqAsJavaListConverter(m).asJava();
    }
}
