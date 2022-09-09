package com.tccsafeo.utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;

public class AmqpListener {

    private final Channel channel;
    private String queueName;

    public AmqpListener(Channel channel, String queueName) {
        this.channel = channel;
        this.queueName = queueName;
    }

    public String getMessage() {
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

    public void publishMessage(String message) {
        try {
            byte[] messageBytes = message.getBytes();
            channel.basicPublish("DX_PLAYER", queueName, null, messageBytes);
        } catch (IOException exception) {
            System.out.println("Message could not be published!");
        }
    }
}
