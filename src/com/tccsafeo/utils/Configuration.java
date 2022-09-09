package com.tccsafeo.utils;

import java.io.IOException;

import com.tccsafeo.persistence.entities.QueueConfig;

public class Configuration {
    private static Configuration instance;
    private static QueueConfig queueConfig;

    private Configuration() throws IOException {
        String jsonContent = FileUtil.readFileAsString("src/com/tccsafeo/data/config.json");
        this.queueConfig = JsonParser.entity(jsonContent, QueueConfig.class);
    }

    public static Configuration getInstance() throws IOException {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public QueueConfig getQueueConfig() {
        return queueConfig;
    }
}
