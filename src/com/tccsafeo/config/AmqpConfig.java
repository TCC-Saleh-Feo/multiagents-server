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

    public AmqpConfig() {
        try {
            channel = connection();
            createExchange(channel);
            createQueue(channel);
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

    private void createQueue(Channel channel) throws IOException {
        channel.queueDeclare("INCOMING_QUEUE", true, false, false, null);
        channel.queueBind("INCOMING_QUEUE", "DX_PLAYER", "INCOMING_QUEUE");
    }

    public Channel getChannel() {
        return channel;
    }
}
