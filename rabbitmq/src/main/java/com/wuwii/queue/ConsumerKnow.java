package com.wuwii.queue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 验证消息应答的消费者
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 14:09</pre>
 */
public class ConsumerKnow {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        final String worker = " Consumer test";
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                try {
                    work(message);
                    System.out.println(worker + " Received '" + message + "'");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        // 关闭自动应答
        boolean ack = false;
        channel.basicConsume(QUEUE_NAME, ack, consumer);
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
