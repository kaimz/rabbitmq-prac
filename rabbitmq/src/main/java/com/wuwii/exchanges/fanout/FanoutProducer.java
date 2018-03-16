package com.wuwii.exchanges.fanout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * fanout 交换器
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/16 17:07</pre>
 */
public class FanoutProducer {
    private final static String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("k.wuwii.com");
        factory.setUsername("kronchan");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //开始写生产者 BIGIN
        String message = "error log";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        System.out.println(" >>>>>>> sent logs");
        // 生产者 END
    }
}
