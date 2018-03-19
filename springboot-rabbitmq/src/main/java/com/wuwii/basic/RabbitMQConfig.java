package com.wuwii.basic;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消费者的配置属性
 *
 * @author Zhang Kai
 * @version 1.0
 * @since <pre>2018/3/19 10:04</pre>
 */
@Configuration
public class RabbitMQConfig {
    public final static String QUEUE_NAME = "springboot.demo.test1";
    public final static String ROUTING_KEY = "route-key";
    public final static String EXCHANGES_NAME = "demo-exchanges";

    @Bean
    public Queue queue() {
        // 是否持久化
        boolean durable = true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = false;
        return new Queue(QUEUE_NAME, durable, exclusive, autoDelete);
    }

    /**
     * 设置交换器，这里我使用的是 topic exchange
     */
    @Bean
    public TopicExchange exchange() {
        // 是否持久化
        boolean durable = true;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = false;
        return new TopicExchange(EXCHANGES_NAME, durable, autoDelete);
    }

    /**
     * 绑定路由
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }


    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(receiver());
        //container.setMaxConcurrentConsumers(1);
        //container.setConcurrentConsumers(1); 默认为1
        //container.setExposeListenerChannel(true);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置为手动，默认为 AUTO，如果设置了手动应答 basicack，就要设置manual
        return container;
    }

    @Bean
    public MessageReceiver receiver() {
        return new MessageReceiver();
    }

}
