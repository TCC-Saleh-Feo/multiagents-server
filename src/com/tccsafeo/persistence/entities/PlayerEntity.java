package com.tccsafeo.persistence.entities;

import java.time.Instant;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("playerEntity")
public class PlayerEntity {
    @Id
    private String id;

    private Player player;

    private Instant startLobbyTime;

    private Instant endLobbyTime;

    public PlayerEntity() {
    }

    public PlayerEntity(Player player, Instant startLobbyTime) {
        this.player = player;
        this.startLobbyTime = startLobbyTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Instant getStartLobbyTime() {
        return startLobbyTime;
    }

    public void setStartLobbyTime(Instant startLobbyTime) {
        this.startLobbyTime = startLobbyTime;
    }

    public Instant getEndLobbyTime() {
        return endLobbyTime;
    }

    public void setEndLobbyTime(Instant endLobbyTime) {
        this.endLobbyTime = endLobbyTime;
    }
}
