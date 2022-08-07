package com.company;

public class Player {
    public String name;
    public Integer kills;
    public Integer victory;

    public Player() {
        super();
    }

    public Player(String name, Integer kills, Integer victory) {
        this.name = name;
        this.kills = kills;
        this.victory = victory;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", kills=" + kills +
                ", victory=" + victory +
                '}';
    }
}
