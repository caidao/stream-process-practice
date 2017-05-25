package com.paner.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Map;

/**
 * Created by www-data on 16/9/4.
 */
public  class WordSplitterBolt extends BaseRichBolt {

    private static final long serialVersionUID = 1L;
   // private static final Log LOG = LogFactory.getLog(WordSplitterBolt.class);
    private OutputCollector collector;



    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }



    public void execute(Tuple input) {
        String record = input.getString(0);
        if(record != null && !record.trim().isEmpty()) {
            for(String word : record.split("\\s+")) {
                collector.emit(input, new Values(word, 1));
                //LOG.info("Emitted: word=" + word);
                System.out.println("Emitted: word=" + word);
                collector.ack(input);
            }
        }
    }


    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

}