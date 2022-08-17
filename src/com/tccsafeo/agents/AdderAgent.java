package com.tccsafeo.agents;

import com.tccsafeo.entities.Player;
import com.tccsafeo.utils.FileUtil;
import com.tccsafeo.utils.JsonParser;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.IOException;
import java.util.ArrayList;

public class AdderAgent extends Agent {

    ArrayList<Player> playerList = new ArrayList<>();
    ArrayList<Player> addedPlayers = new ArrayList<>();

    protected void setup() {
        addBehaviour(new SetupPlayersBehaviour());
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (addedPlayers.size() < playerList.size()) {
                    Integer currentPlayerPosition = addedPlayers.size();
                    Player currentPlayer = playerList.get(currentPlayerPosition);

                    addedPlayers.add(currentPlayer);
                    System.out.println("Current Player: " + currentPlayer + "\nAdded players: " + addedPlayers.size());
                } else {
                    System.out.println("All Players Added!");
                    this.stop();
                    doDelete();
                }
            }
        });
    }

    protected void setPlayerList(ArrayList<Player> playerList) {
        this.playerList = playerList;
    }

    private class SetupPlayersBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            try {
                String playerContent = FileUtil.readFileAsString("src/com/tccsafeo/data/players.json");
                setPlayerList(JsonParser.arrayList(playerContent, Player.class));
            } catch (IOException exception) {
                System.out.println("Could not read players file.");
            }
        }
    }
}
