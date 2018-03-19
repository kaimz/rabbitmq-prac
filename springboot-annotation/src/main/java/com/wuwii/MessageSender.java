package com.wuwii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/19 15:35</pre>
 */
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
