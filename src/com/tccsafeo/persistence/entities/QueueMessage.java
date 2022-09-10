package com.tccsafeo.persistence.entities;

public class QueueMessage {
    private String message;
    private long deliveryTag;

    public QueueMessage(String message, long deliveryTag) {
        this.message = message;
        this.deliveryTag = deliveryTag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }
}
