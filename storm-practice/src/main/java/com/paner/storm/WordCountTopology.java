package com.paner.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

/**
 * Created by www-data on 16/9/4.
 */
public class WordCountTopology {
    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, InterruptedException, AuthorizationException {
        // configure & build topology
        TopologyBuilder builder = new TopologyBuilder();
        String[] records = new String[] {
                "A Storm cluster is superficially similar to a Hadoop cluster",
                "All coordination between Nimbus and the Supervisors is done through a Zookeeper cluster",
                "The core abstraction in Storm is the stream"
        };
        builder
                .setSpout("spout-producer", new ProduceRecordSpout(records), 1)
                .setNumTasks(3);
        builder
                .setBolt("bolt-splitter", new WordSplitterBolt(), 2)
                .shuffleGrouping("spout-producer")
                .setNumTasks(2);
        builder.setBolt("bolt-counter", new WordCounterBolt(), 1)
                .fieldsGrouping("bolt-splitter", new Fields("word"))
                .setNumTasks(2);

        // submit topology
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_ACKER_EXECUTORS,0);
        String name = WordCountTopology.class.getSimpleName();
        if (args != null && args.length > 0) {
            String nimbus = args[0];
            conf.put(Config.NIMBUS_HOST, nimbus);
            conf.setNumWorkers(2);
            StormSubmitter.submitTopologyWithProgressBar(name, conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(name, conf, builder.createTopology());
            Thread.sleep(60000);
            cluster.shutdown();
        }
    }
}
