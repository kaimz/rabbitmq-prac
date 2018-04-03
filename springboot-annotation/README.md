在上一个项目学习了 Spring Boot 中使用 RabbitMQ 的基本使用方法，
这次在 Spring Boot 中使用注解的方式使用 RabbitMQ ，以及有返回消息的消费者的使用。

##### 消费者
```java
@Component
@Log
public class MessageReceiver {

    /**
     * 无返回消息的
     *
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = Constant.QUEUE_NAME, durable = "true", exclusive = "false", autoDelete = "false"),
            exchange = @Exchange(value = Constant.EXCHANGES_NAME, ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC, autoDelete = "false"),
            key = Constant.ROUTING_KEY))
    public void receive(byte[] message) {
        log.info(">>>>>>>>>>> receive：" + new String(message));
    }

    /**
     * 设置有返回消息的
     * 需要注意的是，
     * 1. 在消息的在生产者（发送消息端）一定要使用 SendAndReceive(……) 这种带有 receive 的方法，否则会抛异常，不捕获会死循环。
     * 2. 该方法调用时会锁定当前线程，并且有可能会造成MQ的性能下降或者服务端/客户端出现死循环现象，请谨慎使用。
     *
     * @param message
     * @return
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = Constant.QUEUE_NAME, durable = "true", exclusive = "false", autoDelete = "false"),
            exchange = @Exchange(value = Constant.EXCHANGES_NAME, ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC, autoDelete = "false"),
            key = Constant.ROUTING_REPLY_KEY))
    public String receiveAndReply(byte[] message) {
        log.info(">>>>>>>>>>> receive：" + new String(message));
        return ">>>>>>>> I got the message";
    }

}
```
主要是使用到 `@RabbitListener`，虽然看起来参数很多，仔细的你会发现这个和写配置类里面的基本属性是一摸一样的，没有任何区别。

需要注意的是我在这里多做了个有返回值的消息，这个使用异常的话，会不断重试消息，从而阻塞了线程。而且使用它的时候只能使用带有 `receive` 的方法给它发送消息。

##### 生产者
生产者没什么变化。
```java
@Component
public class MessageSender implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    private RabbitTemplate rabbitTemplate;

    /**
     * 注入 RabbitTemplate
     */
    @Autowired
    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 测试无返回消息的
     */
    public void send() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(Constant.EXCHANGES_NAME, Constant.ROUTING_KEY, ">>>>>> Hello World".getBytes(), correlationData);
        log.info(">>>>>>>>>> Already sent message");
    }

    /**
     * 测试有返回消息的，需要注意一些问题
     */
    public void sendAndReceive() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        Object o = rabbitTemplate.convertSendAndReceive(Constant.EXCHANGES_NAME, Constant.ROUTING_REPLY_KEY, ">>>>>>>> Hello World Second".getBytes(), correlationData);
        log.info(">>>>>>>>>>> {}", Objects.toString(o));
    }

    /**
     * Confirmation callback.
     *
     * @param correlationData correlation data for the callback.
     * @param ack             true for ack, false for nack
     * @param cause           An optional cause, for nack, when available, otherwise null.
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info(">>>>>>> 消息id:{} 发送成功", correlationData.getId());
        } else {
            log.info(">>>>>>> 消息id:{} 发送失败", correlationData.getId());
        }
    }

    /**
     * Returned message callback.
     *
     * @param message    the returned message.
     * @param replyCode  the reply code.
     * @param replyText  the reply text.
     * @param exchange   the exchange.
     * @param routingKey the routing key.
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息id：{} 发送失败", message.getMessageProperties().getCorrelationId());
    }
}
```

##### 测试
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootAnnotationApplicationTests {

    @Autowired
    private MessageSender sender;

    @Test
    public void send() {
        sender.send();
    }

    @Test
    public void sendAndReceive() {
        sender.sendAndReceive();
    }
}
```
