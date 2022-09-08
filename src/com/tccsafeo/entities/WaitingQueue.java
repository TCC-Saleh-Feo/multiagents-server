package com.tccsafeo.entities;

import java.util.LinkedList;
import java.util.Queue;

public class WaitingQueue {
    private static WaitingQueue instance;
    private Queue<Player> playerQueue;

    private WaitingQueue() {
        playerQueue = new LinkedList<>();
    }

    public static WaitingQueue getInstance() {
        if (instance == null) {
            instance = new WaitingQueue();
        }
        return instance;
    }

    public Player getNextPlayer() {
        return playerQueue.poll();
    }

    public void addPlayer(Player player) {
        playerQueue.add(player);
    }

    public Integer getQueueSize() {
        return playerQueue.size();
    }
}
