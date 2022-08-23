package com.tccsafeo.entities;

import java.util.ArrayList;

public class QueueConfig {
    public ArrayList<Criteria> criteria;
    public Integer teamSize;
    public Integer teamAmount;

    public QueueConfig() {
        super();
    }

    public QueueConfig(ArrayList<Criteria> criteria, Integer teamSize, Integer teamAmount) {
        this.criteria = criteria;
        this.teamSize = teamSize;
        this.teamAmount = teamAmount;
    }

    @Override
    public String toString() {
        return "QueueConfig {" +
                "criteria=" + criteria +
                ", teamSize=" + teamSize +
                ", teamAmount=" + teamAmount +
                '}';
    }
}
