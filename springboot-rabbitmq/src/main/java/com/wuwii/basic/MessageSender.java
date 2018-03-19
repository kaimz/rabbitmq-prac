package com.wuwii.basic;

import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 生产者
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/19 10:32</pre>
 */
@Component
@Log
public class MessageSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send() {
        // public void convertAndSend(String exchange, String routingKey, final Object object, CorrelationData correlationData)
        // exchange:    交换机名称
        // routingKey:  路由关键字
        // object:      发送的消息内容
        // correlationData:消息ID
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGES_NAME, RabbitMQConfig.ROUTING_KEY, ">>>>> Hello World", correlationId);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info(">>>>>>> 消息发送成功");
            } else {
                log.info(">>>>>>> 消息发送失败");
            }
        });
        log.info("Already sent message.");
    }

}
