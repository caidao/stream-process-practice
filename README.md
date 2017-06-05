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
+ [storm源码之理解Storm中Worker、Executor、Task关系](http://weyo.me/pages/techs/storm-translations-understanding-the-parallelism-of-a-storm-topology/)
+ [storm基础](http://danzhuibing.github.io/Storm_basic.html)

### Q&A
+ Q:并发度问题
+ A:storm集群里的1台物理机器会启动1个或多个worker（JVM）进程，所有的topology将在这些worker进程里被运行，在一个单独的worker进程里会运行1个或多个executor线程。每个executor只会运行1个topology的1个component（spout或bolt）的task实例，1个task最终完成数据处理的实体单元
`parallelism_hint`,它代表着一个组件的初executor（线程）始数量，所有component的executor数量就是整个topology的整体并行度
+ Q:Nimbus单点问题
+ A:出现单点故障概率低，因为nimbus进程不参与任务运行，本身是无状态的。nimbus通过Zookeeper记录所有supervisor节点的状态和分配给它们的task，如果nimbus发现某个supervisor没有上报心跳或已经不可达，它将会把分配给故障supervisor的task重新分配给其他节点。
+ Q:[消息保证机制](http://www.jianshu.com/p/d521c7c91298)
+ A:利用Spout,Bolt以及Acker的组合可以实现At Most Once以及At Least Once语义，Storm在At Least Once的基础上进行了一次封装（Trident），从而实现Exactly Once语义
+ Q:[分组策略](http://www.cnblogs.com/xymqx/p/4365190.html)
+ A:随机分组、按字段分组、广播..

### storm布署
+ [快速安装文档](http://www.jianshu.com/p/6e9496d7b51c)
+ [提交任务](http://www.jianshu.com/p/6783f1ec2da0)


## zookeeper实战
+ [集群搭建](http://www.jianshu.com/p/abbc1411ed9d)
+ [Zookeeper原理与应用](http://www.jianshu.com/p/84ad63127cd1)
+ [使用说明](https://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/)

### 应用场景
+ 数据发布与订阅（配置中心）:主要通过Watcher机制实现
+ 命名服务（Naming Service）:能够生成全局唯一的ID
+ 分布式协调/通知 ： 通过Watcher与异步通知机制，实现分布式环境中不同系统之间的通知与协调
+ 心跳检测：根据临时节点的特性，进行分布式机器间的心跳检测，大大减少了系统耦合
+ 工作进度汇报：通过创建临时子节点，可以判断机器是否存活，同时各个系统可以将自己的任务进度写到临时节点，以便中心系统获取执行进度
+ Master选举：利用ZooKeepr的强一致性，能够很好地保证在分布式高并发情况下节点的创建一定能够保证全局唯一性，即ZooKeeper将会保证客户端无法创建一个已经存在的ZNode。成功创建该节点的客户端所在的机器就成为了Master。同时，其他没有成功创建该节点的客户端，都会在该节点上注册一个子节点变更的Watcher，用于监控当前Master机器是否存活，一旦发现当前的Master挂了，那么其他客户端将会重新进行Master选举
+ 分布式锁：控制分布式系统之间同步访问共享资源的一种方式

### zookeeper API
+ ZooKeeper构造函数
   + [Wathcer机制](https://www.ibm.com/developerworks/cn/opensource/os-cn-apache-zookeeper-watcher/)
   ![Watcher通知状态和事件类型表](https://www.ibm.com/developerworks/cn/opensource/os-cn-apache-zookeeper-watcher/img003.png)
   + [监听事件总结](https://my.oschina.net/u/1540325/blog/610347)
+ ACL控制
    + 权限
        + CREATE: 能创建子节点
        + READ：能获取节点数据和列出其子节点
        + WRITE: 能设置节点数据
        + DELETE: 能删除子节点
        + ADMIN: 能设置权限
    + scheme
        + world:它下面只有一个id, 叫anyone,zookeeper中对所有人有权限的结点就是属于world
        + auth:它不需要id, 只要是通过authentication的user都有权限
        + digest: 它对应的id为username:BASE64(SHA1(password))，它需要先通过username:password形式的authentication
        + ip: 它对应的id为客户机的IP地址，设置的时候可以设置一个ip段
        + super: 在这种scheme情况下，对应的id拥有超级权限，可以做任何事情
        + sasl: sasl的对应的id，是一个通过sasl authentication用户的id
    +  [实例参考](http://www.cnblogs.com/yjmyzz/p/zookeeper-acl-demo.html)
+ [节点分类](http://www.bug315.com/article/166.htm)

### 实例
+ [选举](http://zookeeper.apache.org/doc/r3.3.1/recipes.html#sc_leaderElection)
+ [案例分析](http://www.besttest.cn/blog/43.html)
  
   