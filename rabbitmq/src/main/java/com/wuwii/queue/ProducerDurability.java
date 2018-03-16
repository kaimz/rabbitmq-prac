package com.wuwii.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 验证持久消息的生产者
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 16:03</pre>
 */
public class ProducerDurability {
    // 要持久的队列必须是目前不存在的，不然会抛出异常
    //The AMQP operation was interrupted: AMQP close-reason, initiated by Peer, code=406, text="PRECONDITION_FAILED
    // - inequivalent arg 'durable' for queue 'queuename' in vhost '/': received 'false' but current is 'true'",
    private static final String QUEUE_NAME = "hello_d";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // durable 参数设置为 true，消息持久化
        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        // 设置公平转发，平均分配任务
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        String message = "KronChan say:";
        for (int i = 0; i < 10; i++) {
            // 标识信息为持久化， PERSISTENT_TEXT_PLAIN。
            channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, (message + i).getBytes());
        }
        channel.close();
        connection.close();
    }
}
