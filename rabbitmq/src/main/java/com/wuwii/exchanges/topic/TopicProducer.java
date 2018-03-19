package com.wuwii.exchanges.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 学习主题交换器
 * @author KronChan
 * @version 1.0
 * @since <pre>2018/3/18 23:03</pre>
 */
public class TopicProducer {
    private final static String EXCHANGES_NAME = "topic_logs";
    private final static String[] TOPICS = {"animal.female.pig", "man.female.asia"};

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGES_NAME, "topic");

        //开始写生产者 BIGIN
        String message = "error log";
        channel.basicPublish(EXCHANGES_NAME, TOPICS[0], null, "猪猪".getBytes());
        channel.basicPublish(EXCHANGES_NAME, TOPICS[1], null, "亚洲女性".getBytes());
        channel.basicPublish(EXCHANGES_NAME, "", null, "测试为空的路由键，*通配符能否接收到".getBytes()); // 测试为空的路由键，*通配符能否接收到
        System.out.println(" >>>>>>> sent logs");
        // 生产者 END
        channel.close();
        connection.close();
    }
}
