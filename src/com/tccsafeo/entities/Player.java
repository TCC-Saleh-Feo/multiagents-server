package com.tccsafeo.entities;

import java.util.ArrayList;

public class Player {
    public String playerId;
    public ArrayList<PlayerData> playerData;

    public Player() {
        super();
    }

    public Player(String playerId, ArrayList<PlayerData> playerData) {
        this.playerId = playerId;
        this.playerData = playerData;
    }

    @Override
    public String toString() {
        return "Player {" +
                "playerId = " + playerId +
                ", playerData = " + playerData +
                "}";
    }
}
