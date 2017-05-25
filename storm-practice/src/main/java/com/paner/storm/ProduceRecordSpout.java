package com.paner.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by www-data on 16/9/4.
 */
public class ProduceRecordSpout extends BaseRichSpout {

    private static final long serialVersionUID = 1L;
   // private static final Log LOG = LogFactory.getLog(ProduceRecordSpout.class);
    private SpoutOutputCollector collector;
    private Random random;
    private String[] records;

    public ProduceRecordSpout(String[] records) {
        this.records = records;
    }


    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        random = new Random();
    }


    public void nextTuple() {
        Utils.sleep(500);
        String record = records[random.nextInt(records.length)];
        List<Object> values = new Values(record);
        collector.emit(values, values);
        //LOG.info("Record emitted: record=" + record);
        System.out.println("Record emitted: record=" + record);
    }


    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("record"));
    }
}