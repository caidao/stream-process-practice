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
    + Q:主节点崩溃
    + A:重新选举master节点，注意脑裂问题
    + Q:从节点崩溃
    + A:主节点要能检测到从节点的状态，崩溃节点未完成的任务需要重新分配
    + Q:通信故障
    + A:
+ [案例分析](http://www.besttest.cn/blog/43.html)

### 常见问题
+ Q:znode节点大小？
+ A:默认1M大小


### 主-从模式
+ 1、实现主要框架
    + Master节点
    + workers节点
    + tasks节点
    + assign节点
    + status节点
