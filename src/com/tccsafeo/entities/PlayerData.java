package com.tccsafeo.entities;

public class PlayerData {
    public String key;
    public Integer value;

    public PlayerData() {
        super();
    }

    public PlayerData(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "PlayerData {" +
                "key = " + key +
                ", value = " + value +
                "}";
    }
}
