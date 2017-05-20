package com.paner.kafka;

import kafka.consumer.KafkaStream;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by paner on 17/5/18.
 */
public class StreamsProcessDemo {


    /**
     * step1:创建topic： bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-input
     *
     * step2:生成消息：bin/kafka-console-producer.sh --broker-list lcalhost:9092 --topic streams-input
     *
     * step3:运行程序，处理消息: 统计结束后消息发往另一个topic=streams-output ,注意每次运行APPLICATION_ID_CONFIG需要更改
     *
     * step4:查看消费情况 bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic streams-output
     * --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.key=true
     * --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
     * --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
     * @throws InterruptedException
     */
    @Test
    public void wordCountDemo() throws InterruptedException {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String,String> source = builder.stream("streams-input");

        KTable<String,Long> counts = source.flatMapValues(new ValueMapper<String, Iterable<String>>() {
            public Iterable<String> apply(String value) {
                return Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" "));
            }
        }).map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
            public KeyValue<String, String> apply(String key, String value) {
                return new KeyValue<String, String>(value,value);
            }
        }).countByKey("Counts");

        counts.to(Serdes.String(), Serdes.Long(), "streams-output-1");
        KafkaStreams streams = new KafkaStreams(builder,props);
        streams.start();

        Thread.sleep(5000L);
        streams.close();

    }
}
