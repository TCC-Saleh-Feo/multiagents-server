package com.tccsafeo.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.tccsafeo.persistence.entities.QueueMessage;

import java.io.IOException;

public class AmqpListener {

    private final Channel channel;
    private String queueName;

    public AmqpListener(Channel channel, String queueName) {
        this.channel = channel;
        this.queueName = queueName;
    }

    public QueueMessage getMessage() {
        try {
            GetResponse response = channel.basicGet(queueName, false);
            if (response != null) {
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                String message = new String(response.getBody());
                return new QueueMessage(message, deliveryTag);
            }
        } catch (IOException exception) {
            System.out.println("Could not get message from queue!");
        }
        return null;
    }

    public void ackMessage(long deliveryTag) {
        try {
        channel.basicAck(deliveryTag, false);
        } catch (IOException exception) {
            System.out.println("Could not ack message!");
        }
    }

    public GetResponse getResponse() {
        try {
            return channel.basicGet(queueName, false);
        } catch (IOException exception) {
            System.out.println("Could not get response from queue!");
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
