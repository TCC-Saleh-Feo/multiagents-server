package com.tccsafeo.utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;

public class AmqpListener {

    private final Channel channel;

    public AmqpListener(Channel channel) {
        this.channel = channel;
    }

    public String getMessage(String queueName) {
        try {
            GetResponse response = channel.basicGet(queueName, false);
            if (response != null) {
                byte[] body = response.getBody();
                return new String(body);
            }
        } catch (IOException exception) {
            System.out.println("Could not get message from queue!");
        }
        return null;
    }
}
