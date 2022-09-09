package com.tccsafeo.persistence.entities;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity("lobbyEntity")
public class LobbyEntity {
    @Id
    private String id;

    private List<List<Player>> lobby = new ArrayList<>();

    private Instant startLobbyTime;

    private Instant endLobbyTime;

    public LobbyEntity(Instant startLobbyTime) {
        this.startLobbyTime = startLobbyTime;
    }

    public LobbyEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<Player>> getLobby() {
        return lobby;
    }

    public void setLobby(List<List<Player>> lobby) {
        this.lobby = lobby;
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
