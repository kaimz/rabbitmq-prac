package com.wuwii.basic;

import com.rabbitmq.client.Channel;
import lombok.extern.java.Log;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

/**
 * 消费者
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/19 10:37</pre>
 */
@Log
public class MessageReceiver implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            byte[] body = message.getBody();
            log.info(">>>>>>> receive： " + new String(body));
        } finally {
            // 确认成功消费，否则消息会转发给其他的消费者，或者进行重试
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}
