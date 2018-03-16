package com.wuwii.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 基本工作队列的学习， 发送多个消息，并且设置了公平转发，
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 10:37</pre>
 */
public class ProducerQueue {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 设置公平转发，平均分配任务
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        String message = "KronChan say:";
        for (int i = 0; i < 10; i++) {
            channel.basicPublish("", QUEUE_NAME, null, (message + i).getBytes());
        }
        channel.close();
        connection.close();
    }
}
