package com.tccsafeo;

import java.util.*;

public class DataSource {
    public Queue<Player> playerList = new ArrayDeque<>();

    public class Player {
        public String name;
        public Integer kills;
        public Integer victory;

        public Player(String name, Integer kills, Integer victory) {
            this.name = name;
            this.kills = kills;
            this.victory = victory;
        }
    }

    public Queue<Player> setUpPlayers()
    {
        for (int i = 0; i < 10; i++) {
            Random rand = new Random();
            Player jogador = new Player("Elma Maria " + i, rand.nextInt(100), rand.nextInt(100));
            playerList.add(jogador);
        }
        return playerList;
    }
}
