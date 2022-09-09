package com.tccsafeo.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class AmqpConfig {
    private Channel channel;
    private String queueName;

    public AmqpConfig(String queueName) {
        try {
            this.queueName = queueName;
            channel = connection();
            createExchange(channel);
            createQueue(channel, queueName);
        } catch (IOException |
                URISyntaxException |
                NoSuchAlgorithmException |
                KeyManagementException |
                TimeoutException exception) {
            System.out.println("Could not connect to RabbitMQ!");
            System.out.println(exception.getCause());
            System.out.println(exception.getStackTrace());
        }
    }

    private Channel connection() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://user:password@localhost:5672");
        Connection conn = factory.newConnection();
        return conn.createChannel();
    }

    private void createExchange(Channel channel) throws IOException {
        channel.exchangeDeclare("DX_PLAYER", "direct", true);
    }

    private void createQueue(Channel channel, String queueName) throws IOException {
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, "DX_PLAYER", queueName);
    }

    public Channel getChannel() {
        return channel;
    }
}
