# 流计算实战学习

## kafka实战
+ [zookeeper安装配置](http://www.jianshu.com/p/0ba61bf7149f)
+ [kafka安装配置](http://nekomiao.me/2016/11/20/kafka/)
+ jar包的版本要跟kafka版本匹配
[官方文档](http://kafka.apache.org/0100/documentation/#quickstart)
+ [kafka中文教程](http://www.orchome.com/kafka/index)
+ [apache kafka技术分享系列(目录索引)](http://blog.csdn.net/lizhitao/article/details/39499283)
+ [源码解析](http://zqhxuyuan.github.io/2016/01/06/2016-01-06-Kafka_Producer/)

##### Q&A
+ Q:producer消息丢失处理？
+ A:通过配置清单来规避producer端的消息丢失：
    + block.on.buffer.full = true  使得producer将一直等待缓冲区直至其变为可用
    + acks=all  所有follower都响应了才认为消息提交成功
    + retries = MAX 无限重试
    + 使用KafkaProducer.send(record, callback)而不是send(record)方法   自定义回调逻辑处理消息发送失败
    + replication.factor >= 3   这个完全是个人建议了，参考了Hadoop及业界通用的三备份原则
    + min.insync.replicas > 1 消息至少要被写入到这么多副本才算成功，也是提升数据持久性的一个参数。与acks配合使用  
+ Q:producer消息乱序处理？
+ A:max.in.flight.requests.per.connection=1 限制客户端在单个连接上能够发送的未响应请求的个数。设置此值是1表示kafka broker在响应请求之前client不能再向同一个broker发送请求 [顺序消息](http://www.lpnote.com/2017/01/17/sequence-message-in-kafka/)
+ Q:producer分区策略？
+ A:producer分区数，它实际上是用多个线程并发地向不同分区所在的broker发起Socket连接同时给这些分区发送消息。分区数越多，内存和线程所消耗的资源就越多，所需要打开状态的文件句柄就越多，降低可用性

+ Q:consumer消息丢失处理？
+ A:首先要了解[消息传输](http://matt33.com/2016/03/09/kafka-transmit/)，主要分这几种情况：
    + commit后处理消息，消息会丢失
    + commit前处理消息，消息可能会重复
    + 为保证消息一定能处理且仅处理一次,需要consumer做额外的保证工作（两阶段提交或TCC[提交事务](http://www.roncoo.com/article/detail/124243)）
    

    
##### 常用命令汇总
+ 创建topic
`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 3 --topic topic_0511`
+ 查看topic列表
`bin/kafka-topics.sh --list  --zookeeper localhost:2181`
+ [删除topic](http://blog.csdn.net/fengzheku/article/details/50585972)
+ 查看某个topic的信息
`bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic topic_0511`
+ 验证消息生产成功
`bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic topic_0511_2 --time -1`
+ 查看topic消费情况
0.9之前：`bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --group test  --topic topic_0508  --zookeeper localhost:2181`
0.9之后版本：`bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 -describe  --new-consumer -group test` 消费组在运行状态中
+ 更改topic分区情况
`bin/kafka-topics.sh --zookeeper localhost:2181 --alter --partitions 20 --topic topic_0508`
+ [查看__consumer_offsets中的信息](http://www.cnblogs.com/huxi2b/p/6061110.html)
+ [消费组相关解析](http://www.cnblogs.com/huxi2b/p/6223228.html)
+ 查询`__consumer_offsets` topic所有内容
`bin/kafka-console-consumer.sh --topic __consumer_offsets --zookeeper localhost:2181 --formatter "kafka.coordinator.GroupMetadataManager\$OffsetsMessageFormatter" --consumer.config config/consumer.properties --from-beginning`
+ 获取指定consumer group的位移信息
`bin/kafka-simple-consumer-shell.sh --topic __consumer_offsets --partition 11 --broker-list localhost:9092 --formatter "kafka.coordinator.GroupMetadataManager\$OffsetsMessageFormatter"`

##### 通讯协议总结
 [协议详情](http://colobu.com/2017/01/26/A-Guide-To-The-Kafka-Protocol/)
 
+ 元数据接口(Metadata api)
    + TopicMetadataRequest
    + TopicMetadataResponse
    
+ 生产接口(Produce API)
    + ProducerRequest
    + ProducerResponse

+ 获取消息接口（Fetch API）
用于获取一些Topic分区的一个或多个的日志块。
    + FetchRequest
    + FetchResponse

+ 提交offset的方式
    + OffsetCommitRequest
    + OffsetCommitResponse

+ 获取offset的方式
    + OffsetFetchRequest
    + OffsetFetchResponse


#### kafka stream
[介绍文档](http://blog.csdn.net/opensure/article/details/51507698)

#### kafka connector
[文档](https://www.confluent.io/product/connectors/)


## storm实战
+ [storm源码之理解Storm中Worker、Executor、Task关系](http://www.cnblogs.com/yufengof/p/storm-worker-executor-task.html)