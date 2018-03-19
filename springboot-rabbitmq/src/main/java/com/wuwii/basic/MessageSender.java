package com.wuwii.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MessageSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    public void send() {
        // public void convertAndSend(String exchange, String routingKey, final Object object, CorrelationData correlationData)
        // exchange:    交换机名称
        // routingKey:  路由关键字
        // object:      发送的消息内容
        // correlationData:消息ID
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        // ConfirmListener是当消息无法发送到Exchange被触发，此时Ack为False，这时cause包含发送失败的原因，例如exchange不存在时
        // 需要在系统配置文件中设置 publisher-confirms: true
        if (!rabbitTemplate.isConfirmListener()) {
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (ack) {
                    log.info(">>>>>>> 消息id:{} 发送成功", correlationData.getId());
                } else {
                    log.info(">>>>>>> 消息id:{} 发送失败", correlationData.getId());
                }
            });
        }
        // ReturnCallback 是在交换器无法将路由键路由到任何一个队列中，会触发这个方法。
        // 需要在系统配置文件中设置 publisher-returns: true
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("消息id：{} 发送失败", message.getMessageProperties().getCorrelationId());
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGES_NAME, RabbitMQConfig.ROUTING_KEY, ">>>>> Hello World", correlationId);
        log.info("Already sent message.");
    }

}
