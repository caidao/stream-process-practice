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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by paner on 17/5/18.
 */
public class StreamsProcessDemo {

    @Test
    public void wordCountDemo() throws InterruptedException {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String,String> source = builder.stream("streams-file-input");

        KTable<String,Long> counts = source.flatMapValues(new ValueMapper<String, Iterable<String>>() {
            public Iterable<String> apply(String value) {
                return Arrays.asList(value.toLowerCase(Locale.getDefault()).split("W+"));
            }
        }).map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
            public KeyValue<String, String> apply(String key, String value) {
                return new KeyValue<String, String>(value,value);
            }
        }).countByKey("Counts");

        counts.to(Serdes.String(), Serdes.Long(), "streams-wordcount-output");
        KafkaStreams streams = new KafkaStreams(builder,props);
        streams.start();

        Thread.sleep(5000L);
        streams.close();

    }
}
