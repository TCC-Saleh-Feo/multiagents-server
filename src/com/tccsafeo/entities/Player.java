package com.tccsafeo.entities;

import java.util.ArrayList;
import java.util.stream.IntStream;

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

    public Integer findPlayerDataIndex(String playerDataName) {
        return IntStream.range(0, playerData.size())
                .filter(i -> playerData.get(i).key.equals(playerDataName))
                .findFirst()
                .orElse(-1);
    }

    @Override
    public String toString() {
        return "Player {" +
                "playerId = " + playerId +
                ", playerData = " + playerData +
                "}";
    }
}
