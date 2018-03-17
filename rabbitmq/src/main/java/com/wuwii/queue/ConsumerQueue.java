package com.wuwii.queue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基本消息的队列学习，多个消费者 轮询调度 消息
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 10:39</pre>
 */
public class ConsumerQueue {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        for (int i = 1; i <= 4; i++) {
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            final String worker = " Consumer " + i;
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    work(message);
                    System.out.println(worker + " Received '" + message + "'");
                }
            };
            channel.basicConsume(QUEUE_NAME, true, consumer);
        }
    }

    private static void work(String message) {
        String body = message.split(":")[1];
        try {
            TimeUnit.SECONDS.sleep(Long.valueOf(body));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
