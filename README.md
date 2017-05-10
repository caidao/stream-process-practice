### 流计算实战学习

#### kafka实战
+ [zookeeper安装配置](http://www.jianshu.com/p/0ba61bf7149f)
+ [kafka安装配置](http://nekomiao.me/2016/11/20/kafka/)
+ jar包的版本要跟kafka版本匹配

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
+ A:首先要了解[消息传输](http://matt33.com/2016/03/09/kafka-transmit/)，主要分散中情况：
    + commit后处理消息，消息会丢失
    + commit前处理消息，消息可能会重复
    + 为保证消息一定能处理且仅处理一次,需要consumer做额外的保证工作（两阶段提交或TCC[提交事务](http://www.roncoo.com/article/detail/124243)）
    
##### 常用命令汇总
+ 查看topic消费情况
`bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --group test  --topic topic_0508  --zookeeper localhost:2181`
+ 更改topic分区情况
`bin/kafka-topics.sh --zookeeper localhost:2181 --alter --partitions 20 --topic topic_0508`
    

