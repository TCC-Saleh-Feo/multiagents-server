package com.tccsafeo.entities;

import java.util.ArrayList;

public class QueueConfig {
    public ArrayList<String> playerData;
    public Integer teamSize;
    public Integer teamAmount;

    public QueueConfig() {
        super();
    }

    public QueueConfig(ArrayList<String> playerData, Integer teamSize, Integer teamAmount) {
        this.playerData = playerData;
        this.teamSize = teamSize;
        this.teamAmount = teamAmount;
    }

    @Override
    public String toString() {
        return "QueueConfig {" +
                "playerData=" + playerData +
                ", teamSize=" + teamSize +
                ", teamAmount=" + teamAmount +
                '}';
    }
}
