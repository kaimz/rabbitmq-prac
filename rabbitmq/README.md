### 前言

> RabbitMQ是实现AMQP（高级消息队列协议）的消息中间件的一种，最初起源于金融系统，用于在分布式系统中存储转发消息，在易用性、扩展性、高可用性等方面表现不俗。RabbitMQ主要是为了实现系统之间的双向解耦而实现的。当生产者大量产生数据时，消费者无法快速消费，那么需要一个中间层。保存这个数据。
>
> AMQP [^1]，是应用层协议的一个开放标准，为面向消息的中间件设计。消息中间件主要用于组件之间的解耦，消息的发送者无需知道消息使用者的存在，反之亦然。AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。

关于学习 Rabiit 的相关笔记，学习的内容来自[RabbitMQ 实战教程 文集](http://blog.720ui.com/columns/rabbitmq_action_all/)，按照自己的思路记了个笔记。

[个人学习代码](https://github.com/kaimz/rabbitmq-prac/tree/master/rabbitmq)
<!--more-->

### 正文

#### 加入依赖
在 `pom.xml` 中加入 `amqp` 的依赖 ，它封装了对 RabbitMQ 的支持相关依赖信息：

[^1]: Advanced Message Queuing Protocol，高级消息队列协议

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.2.0</version>
</dependency>
```
#### 生产者
发送消息的程序。
```java
public class Producer {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名
        factory.setHost("k.wuwii.com");
        factory.setPort(5672);
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个队列
        // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        // 参数1 queue ：队列名
        // 参数2 durable ：是否持久化
        // 参数3 exclusive ：仅创建者可以使用的私有队列，断开后自动删除
        // 参数4 autoDelete : 当所有消费客户端连接断开后，是否自动删除队列
        // 参数5 arguments
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发送消息
        String message = "Hello World!";
        // basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body)
        // 参数1 exchange ：交换器
        // 参数2 routingKey ： 路由键，我们将要把消息发送到哪个队列
        // 参数3 props ： 消息的其他参数
        // 参数4 body ： 消息体
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        // 关闭频道和连接
        channel.close();
        connection.close();
    }
}
```
#### 消费者
等待接收消息的程序。
```java
public class Consumer {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名，默认localhost
        factory.setHost("k.wuwii.com");
        // 设置端口， 默认 端口5672
        factory.setPort(5672);
        // 设置 Username，默认 guest
        factory.setUsername("kronchan");
        // 设置 Password，默认 guest
        factory.setPassword("123456");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个队列
        // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        // 参数1 queue ：队列名
        // 参数2 durable ：是否持久化
        // 参数3 exclusive ：仅创建者可以使用的私有队列，断开后自动删除
        // 参数4 autoDelete : 当所有消费客户端连接断开后，是否自动删除队列
        // 参数5 arguments
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        // 创建队列消费者
        com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        // basicConsume(String queue, boolean autoAck, Consumer callback)
        // 参数1 queue ：队列名
        // 参数2 autoAck ： 是否自动ACK，消息应答，为true关闭它
        // 参数3 callback ： 消费者对象的一个接口，用来配置回调
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
```
执行完生产者和消费者，后消费者可以接收到到对应的消息
#### 工作队列
队列类似于邮箱。虽然消息通过 RabbitMQ 在你的应用中传递，但是它们只能存储在队列中。队列只受主机的内存和磁盘限制的限制，它本质上是一个大的消息缓冲区。不同的生产者可以通过同一个队列发送消息，此外，不同的消费者也可以从同一个队列上接收消息。
![image](http://7xivgs.com1.z0.glb.clouddn.com/rabbitmq_python-two.png)
##### 轮询调度（Round-robin dispatching）
使用任务队列的有点之一就是能够轻松并行的执行任务，实际上就是多建立几个通道 `Channel` 来工作。
##### 消息应答（Message acknowledgment）
执行一个任务可能需要几秒钟。你可能会想，如果一个消费者开始一个长期的任务，并且只有部分地完成它，会发生什么事情？使用我们当前的代码，一旦 RabbitMQ 向客户发送消息，它立即将其从内存中删除。在这种情况下，如果你杀死正在执行任务的某个工作进程，我们会丢失它正在处理的信息。我们还会丢失所有发送给该特定工作进程但尚未处理的消息。
但是，我们不想失去任何消息。如果某个工作进程被杀死时，我们希望把这个任务交给另一个工作进程。

但是，我们不想失去任何消息。如果某个工作进程被杀死时，我们希望把这个任务交给另一个工作进程。

为了确保消息永远不会丢失，RabbitMQ 支持消息应答。从消费者发送一个确认信息告诉 RabbitMQ 已经收到消息并已经被接收和处理，然后RabbitMQ 可以自由删除它。

如果消费者被杀死而没有发送应答，RabbitMQ 会认为该信息没有被完全的处理，然后将会重新转发给别的消费者。如果同时有其他消费者在线，则会迅速将其重新提供给另一个消费者。这样就可以确保没有消息丢失，即使工作进程偶尔也会死亡。

简单验证下，开启两个消费者，接收消息的时候，关闭一个，例外的一个消费者还是只是接收到原来它自己的那一部分消息。

完善，在其中一个消费者上面关闭自动应答机制：
```java
boolean ack = false ;  
channel.basicConsume(QUEUE_NAME, ack, consumer);  
```
然后，每次处理完成一个消息后，手动发送一次应答。

```java
channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
```
接收消息的时候然后异常让它中断，会发现其他的消费者会接收到其他的所有的消息。

##### 消息持久化（Message durability）
当 RabbitMQ 退出或崩溃时，将会丢失所有的队列和信息，除非你告诉它不要丢失。需要两件事来确保消息不丢失：我们需要分别将队列和消息标记为持久化。

首先，我们需要确保 RabbitMQ 永远不会失去我们的队列。为了这样做，我们需要将其声明为持久化的。

在生产者上面设置：
```java
boolean durable = true;
channel.queueDeclare("hello_dirable", durable, false, false, null);
```
其次，我们需要标识我们的信息为持久化的。通过设置 `MessageProperties` 值为 `PERSISTENT_TEXT_PLAIN`。
##### 公平转发（Fair dispatch）
设置 RabbitMQ 往**空闲的工作线程**中发送任务，避免某些工作线程的任务过高，而部分工作线程空闲的问题。

在生产者的管道设置参数：
```java
int prefetchCount = 1;
channel.basicQos(prefetchCount) ;
```
##### 临时队列（Temporary queues）
之前，我们都是使用的队列指定了一个特定的名称。不过，对于我们的日志系统而言，我们并不关心队列的名称。我们想要接收到所有的消息，而且我们也只对当前正在传递的消息感兴趣。要解决我们需求，需要做两件事。

首先，每当我们连接到 RabbitMQ，我们需要一个新的空的队列。为此，我们可以创建一个具有随机名称的队列，或者甚至更好 - 让服务器或者，让服务器为我们选择一个随机队列名称。

其次，一旦消费者与 RabbitMQ 断开，消费者所接收的那个队列应该被自动删除。

在 Java 客户端中，我们可以使用 queueDeclare() 方法来创建一个非持久的、唯一的、自动删除的队列，且队列名称由服务器随机产生。

```java
String queueName = channel.queueDeclare().getQueue();
```
此时，queueName 包含一个随机队列名称。

#### 交换器（Exchanges）
RabbitMQ 消息模型的核心思想是，生产者不直接发送任何消息给队列。实际上，一般的情况下，生产者甚至不知道消息应该发送到哪些队列。
相反的，生产者只能将信息发送到交换器。交换器是非常简单的。它一边收到来自生产者的消息，另一边将它们推送到队列。交换器必须准确知道接收到的消息如何处理。是否被添加到一个特定的队列吗？是否应该追加到多个队列？或者是否应该被丢弃？这些规则通过交换器类型进行定义。
![image](http://7xivgs.com1.z0.glb.clouddn.com/rabbitmq_exchanges.png)
交换器一共四种类型：`direct`、`topic`、`headers`、`fanout`。
##### fanout
将所有收到的消息广播到所有它所知道的队列。
![image](http://7xivgs.com1.z0.glb.clouddn.com/rabbitmq_bindings.png)
* 生产者，指定一个交换器：
```java
public class FanoutProducer {
    private final static String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //开始写生产者 BIGIN
        String message = "error log";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        System.out.println(" >>>>>>> sent logs");
        // 生产者 END
    }
}
```
* 消费者，我们创建两个消费者，绑定我们指定的交换器和队列：
```java
public class FanoutConsumer {
    private final static String EXCHANGE_NAME = "logs";

    public void execute() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //开始创建消费者 BEGIN
        // 创建一个非持久的，唯一性，自动删除的队列
        String queueName = channel.queueDeclare().getQueue();
        // 绑定交换器和队列
        // queueBind(String queue, String exchange, String routingKey)
        // 参数1 queue ：队列名
        // 参数2 exchange ：交换器名
        // 参数3 routingKey ：路由键名
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
        // 创建消费者完成 END
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        FanoutConsumer fanoutConsumer = new FanoutConsumer();
        fanoutConsumer.execute();
        FanoutConsumer fanoutConsumer1 = new FanoutConsumer();
        fanoutConsumer.execute();
    }
}
```
先启动两个消费者注册交换器，再去启动生产者。结果是两个交换器都接收到消息。
##### direct 直接交换
![image](http://7xivgs.com1.z0.glb.clouddn.com/rabbitmq_direct-exchange.png)
涉及到 `路由键值`，也就是前面用的主题。
```java
// queueBind(String queue, String exchange, String routingKey)
        // 参数1 queue ：队列名
        // 参数2 exchange ：交换器名
        // 参数3 routingKey ：路由键名
```
就是一个队列中有多个路由键值。

其中，第一个队列与绑定键 orange 绑定，第二个队列有两个绑定，一个绑定键为 black，另一个绑定为 green。在这样的设置中，具有 orange 的交换器的消息将被路由到队列 Q1。具有 black 或 green 的交换器的消息将转到 Q2。所有其他消息将被丢弃。

支持多重绑定
![image](http://7xivgs.com1.z0.glb.clouddn.com/rabbitmq_direct-exchange-multiple.png)
此外，使用相同的绑定键绑定多个队列是完全合法的。在我们的示例中，我们可以在 X 和 Q1 之间添加绑定键 black。在这种情况下，direct 类型的交换器将消息广播到所有匹配的队列 Q1 和 Q2。
##### topic
>使用 topic 类型的交换器，不能有任意的绑定键，它必须是由点隔开的一系列的标识符组成。标识符可以是任何东西，但通常它们指定与消息相关联的一些功能。其中，有几个有效的绑定键，例如 `stock.usd.nyse`， `nyse.vmw`， `quick.orange.rabbit`。可以有任何数量的标识符，最多可达 **255** 个字节。

topic 类型的交换器和 direct 类型的交换器很类似，一个特定路由的消息将被传递到与匹配的绑定键绑定的匹配的所有队列。关于绑定键有两种有两个重要的特殊情况：
* `*` 可以匹配一个标识符。
* `#` 可以匹配零个或多个标识符。

例如：
![image](https://zqnight.gitee.io/kaimz.github.io/image/hexo/rabbitmq/1.png)

上面我用两个消费者分别订阅了主题为 `*.female.*` 和 `#.asia` ，服务端发送两个消息，对他们进行匹配，最后获得到结果。

1. 生产者

   ```java
   public class TopicProducer {
       private final static String EXCHANGES_NAME = "topic_logs";
       private final static String[] TOPICS = {"animal.female.pig", "man.female.asia"};

       public static void main(String[] args) throws IOException, TimeoutException {
           ConnectionFactory factory = new ConnectionFactory();
           factory.setHost("k.wuwii.com");
           factory.setUsername("kronchan");
           factory.setPassword("123456");
           Connection connection = factory.newConnection();
           Channel channel = connection.createChannel();
           // 指定一个交换器
           channel.exchangeDeclare(EXCHANGES_NAME, "topic");

           //开始写生产者 BIGIN
           String message = "error log";
           channel.basicPublish(EXCHANGES_NAME, TOPICS[0], null, "猪猪".getBytes());
           channel.basicPublish(EXCHANGES_NAME, TOPICS[1], null, "亚洲女性".getBytes());
           System.out.println(" >>>>>>> sent logs");
           // 生产者 END
           channel.close();
           connection.close();
       }
   }
   ```

   ​

2. 消费者

   ```java
   public class TopicConsumer1 {
       private final static String EXCHANGE_NAME = "topic_logs";
       private final static String TOPIC = "*.female.*"; // 接收所有雌性的

       public static void main(String[] args) throws IOException, TimeoutException {
           ConnectionFactory factory = new ConnectionFactory();
           factory.setHost("k.wuwii.com");
           factory.setUsername("kronchan");
           factory.setPassword("123456");
           Connection connection = factory.newConnection();
           Channel channel = connection.createChannel();
           // 指定一个交换器
           channel.exchangeDeclare(EXCHANGE_NAME, "topic");

           //开始创建消费者 BEGIN
           // 创建一个非持久的，唯一性，自动删除的队列
           String queueName = channel.queueDeclare().getQueue();
           // 绑定交换器和队列
           // queueBind(String queue, String exchange, String routingKey)
           // 参数1 queue ：队列名
           // 参数2 exchange ：交换器名
           // 参数3 routingKey ：路由键名
           channel.queueBind(queueName, EXCHANGE_NAME, TOPIC);

           final Consumer consumer = new DefaultConsumer(channel) {
               @Override
               public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                          byte[] body) throws IOException {
                   String message = new String(body, "UTF-8");
                   System.out.println(" Received '" + message + "'");
               }
           };
           channel.basicConsume(queueName, true, consumer);
           // 创建消费者完成 END
       }
   }
   ```

   ​

##### Headers Exchange

headers 也是根据规则匹配, 相较于 direct 和 topic 固定地使用 routing_key , headers 则是一个自定义匹配规则的类型. 在队列与交换器绑定时, 会设定一组键值对规则, 消息中也包括一组键值对( headers 属性), 当这些键值对有一对, 或全部匹配时, 消息被投送到对应队列。



#### 消息确认机制

虽然之前我们使用消息持久化解决了服务器退出或者崩溃后造成的消息丢失的情况，但是当我们生产者发送消息的时候，中间出现了问题，消息并没有成功到达消息代理服务器，提供了两种解决方案：

1. 事务机制，
2. confirm模式。

##### 事务机制

有关事务机制的方法有三个，一般情况下需要一起配合起来使用：

1. `txSelect`用于将当前channel设置成transaction模式，
2. `txCommit`用于提交事务，
3. `txRollback`用于回滚事务。

```java
try {
    channel.txSelect();
    channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
    channel.txCommit();
} catch (Exception e) {
    e.printStackTrace();
    channel.txRollback();
}
```

使用事务机制的话会降低RabbitMQ的性能，可以使用下面的 Confirm 模式。

##### Confirm模式

原理

生产者将信道设置成confirm模式，一旦信道进入confirm模式，所有在该信道上面发布的消息都会被指派一个唯一的ID(从1开始)，一旦消息被投递到所有匹配的队列之后，消息代理服务器就会发送一个确认给生产者（包含消息的唯一ID）,这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会将消息写入磁盘之后发出，消息代理服务器回传给生产者的确认消息中 `deliver-tag`域包含了确认消息的序列号，此外消息服务器也可以设置basic.ack的multiple域，表示到这个序列号之前的所有消息都已经得到了处理。



confirm模式最大的好处在于他是异步的，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果RabbitMQ因为自身内部错误导致消息丢失，就会发送一条nack消息，生产者应用程序同样可以在回调方法中处理该nack消息。

在channel 被设置成 confirm 模式之后，所有被 publish 的后续消息都将被 confirm（即 ack） 或者被nack一次。但是没有对消息被 confirm 的快慢做任何保证，并且同一条消息不会既被 confirm又被nack 。

1. 普通confirm模式：每发送一条消息后，调用waitForConfirms()方法，等待服务器端confirm。实际上是一种串行confirm了。
2. 批量confirm模式：每发送一批消息后，调用waitForConfirms()方法，等待服务器端confirm。
3. 异步confirm模式：提供一个回调方法，服务端confirm了一条或者多条消息后Client端会回调这个方法。

测试以及使用参考 [RabbitMQ之消息确认机制（事务+Confirm）](https://blog.csdn.net/u013256816/article/details/55515234)

###  中途出现的问题

1. 使用自己新增的账户进去的时候，给的是管理员权限，但是使用应用程序进行登陆的时候，不能进行登陆。
```java
Caused by: com.rabbitmq.client.ShutdownSignalException: connection error; protocol method: #method<connection.close>(reply-code=530, reply-text=NOT_ALLOWED - access to vhost '/' refused for user 'kronchan', class-id=10, method-id=40)
```
原因是该账户没有访问权限 `/`，进入 `rabbitmq` 机器中使用命令 
```bash
rabbitmqctl  set_permissions -p / username '.*' '.*' '.*'
```
该命令使用户 `username` 具有 `/`这个 `virtual host` 中所有资源的配置、写、读权限以便管理其中的资源。[参考](http://blog.csdn.net/godspeedlaile9/article/details/50594488)
2. 如果需要设置持久的队列，要持久的队列必须是目前不存在的，不然会抛出异常
```
//The AMQP operation was interrupted: AMQP close-reason, initiated by Peer, code=406, text="PRECONDITION_FAILED
    // - inequivalent arg 'durable' for queue 'queuename' in vhost '/': received 'false' but current is 'true'",
```

