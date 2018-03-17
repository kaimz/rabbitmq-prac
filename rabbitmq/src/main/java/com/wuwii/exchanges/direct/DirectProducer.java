package com.wuwii.exchanges.direct;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 18:53</pre>
 */
public class DirectProducer {
    private final static String EXCHANGE_NAME = "direct_logs";
    private final static String[] KEYS = {"orange", "black"};

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        //开始写生产者 BIGIN
        channel.basicPublish(EXCHANGE_NAME, KEYS[0], null, "first orange".getBytes());
        channel.basicPublish(EXCHANGE_NAME, KEYS[0], null, "second orange".getBytes());
        channel.basicPublish(EXCHANGE_NAME, KEYS[1], null, "first black".getBytes());
        channel.basicPublish(EXCHANGE_NAME, KEYS[1], null, "second black".getBytes());
        System.out.println(" >>>>>>> sent logs");
        // 生产者 END
    }
}
