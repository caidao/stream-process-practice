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
+ 2、master管理权变化
    + 连接丢失的情况下，客户端会检查/master节点是否存在；
    + 返回ok,则形使领导权；
    + 如果其他节点已经创建了这个节点，则客户端需要监视该节点；
    + 如果发生了某些未知，记录错误日志；
    + 通过exists调用在/master节点上设置监视点；
    + 如果/master节点删除，那么再次竞选主节点。
    
+   主节点等待从节点列表的变化
    +  观察/workers的子节点变化
    + 当检测到子节点变化，需要重新分配崩溃从节点的任务，并重新设置新的从节点列表

+ 主节点等待新任务进行分配
    + 在任务列表变化时，监视点获得任务列表
    + 分配列表中的任务，获得任务信息
    + 随机选择一个从节点，分配任务给这个从节点
    + 创建分配节点，路径形式为/assign/worker-id/task-num
    + 删除/tasks下对应的任务节点
+ 从节点等待分配新任务
    + 在assign创建/assign/worker-id节点，先注册该节点是为了防止主节点给从节点分配任务
    + 然后创建一个znode节点来注册从节点/workers/worker-id
    + 监控/assign/worker-id的子节点，获得变化通知后，获取子节点的列表，
    + 通过线程池，循环子节点，获得任务信息并执行任务，将正在执行的任务添加到执行列表中，防止多次执行

+ 客户端等待任务的执行结果
    + 创建提交任务节点/tasks/task-id
    + 针对任务id，监控其/status/task-id的状态
    + 若监控节点存在，获取执行结果


+ 执行过程
    + 启动Master程序，执行runForMaster()创建/master节点，若创建成功，则该客户端为主节点，则执行主节点任务，第一，监控从节点，通过getWorkers()监视/workers下的子节点。第二，分配任务，通过getTasks()监控/tasks下的子节点，若发现有任务提交，则随机将其分配可用的从节点，；若创建失败则作为master的备份客户端，同时通过masterExists()监控/master的状态,一旦/master节点消失，客户端收到通知就运行runForMaster();master创建成功后，初始化创建/workers、/assign、/tasks、/status节点；
    + 启动第一个Worker程序，首先在可分配任务/assign目录下创建节点/assign/worker-id，再在/workers目录下注册/workers/worker-id节点，成功后需要监控/assign/worker-id子节点，该子节点表示master分配给worker的任务，一旦监控到就需要执行该任务