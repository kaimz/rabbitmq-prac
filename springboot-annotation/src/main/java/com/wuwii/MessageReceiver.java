package com.wuwii;

import lombok.extern.java.Log;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/19 15:35</pre>
 */
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
