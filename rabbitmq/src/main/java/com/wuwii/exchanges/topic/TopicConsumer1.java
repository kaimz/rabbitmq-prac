package com.wuwii.exchanges.topic;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author KronChan
 * @version 1.0
 * @since <pre>2018/3/18 23:04</pre>
 */
public class TopicConsumer1 {
    private final static String EXCHANGE_NAME = "topic_logs";
    private final static String TOPIC = "*.female.*"; // 接收所有雌性的

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        //开始创建消费者 BEGIN
        // 创建一个非持久的，唯一性，自动删除的队列
        String queueName = channel.queueDeclare().getQueue();
        // 绑定交换器和队列
        // queueBind(String queue, String exchange, String routingKey)
        // 参数1 queue ：队列名
        // 参数2 exchange ：交换器名
        // 参数3 routingKey ：路由键名
        channel.queueBind(queueName, EXCHANGE_NAME, TOPIC);

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
        // 创建消费者完成 END
    }
}
