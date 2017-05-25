package com.paner.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.apache.storm.shade.com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by www-data on 16/9/4.
 */
public class WordCounterBolt extends BaseRichBolt {

    private static final long serialVersionUID = 1L;
   // private static final Log LOG = LogFactory.getLog(WordCounterBolt.class);
    private OutputCollector collector;
    private final Map<String, AtomicInteger> counterMap = Maps.newHashMap();


    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }


    public void execute(Tuple input) {
        String word = input.getString(0);
        int count = input.getIntegerByField("count"); // 通过Field名称取出对应字段的数据
        AtomicInteger ai = counterMap.get(word);
        if(ai == null) {
            ai = new AtomicInteger(0);
            counterMap.put(word, ai);
        }
        ai.addAndGet(count);
        //LOG.info("DEBUG: word=" + word + ", count=" + ai.get());
        System.out.println("DEBUG: word=" + word + ", count=" + ai.get());
        collector.ack(input);
    }


    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public void cleanup() {
        // print count results
        //LOG.info("Word count results:");
        for(Map.Entry<String, AtomicInteger> entry : counterMap.entrySet()) {
           // LOG.info("\tword=" + entry.getKey() + ", count=" + entry.getValue().get());
            System.out.println("\tword=" + entry.getKey() + ", count=" + entry.getValue().get());
        }
    }



}