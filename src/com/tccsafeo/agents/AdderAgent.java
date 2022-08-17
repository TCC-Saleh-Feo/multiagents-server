package com.tccsafeo.agents;

import com.tccsafeo.entities.Player;
import com.tccsafeo.utils.FileUtil;
import com.tccsafeo.utils.JsonParser;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.IOException;
import java.util.ArrayList;

public class AdderAgent extends Agent {

    ArrayList<Player> playerList = new ArrayList<>();
    ArrayList<Player> addedPlayers = new ArrayList<>();

    private ArrayList<AID> lobbyOrganizerAgents;

    protected void setup() {
        addBehaviour(new SetupPlayersBehaviour());
        // Behaviour to send players to lobby agents from time to time
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
        // TODO: Behaviour to update available LobbyOrganizerAgents
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {

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

    // TODO: Behaviour to offer Player to all available LobbyOrganizerAgents
    private class OfferPlayerBehaviour extends Behaviour {
        private Integer actionStep = 0;

        @Override
        public void action() {
            switch (actionStep) {
                default:
                    actionStep++;
            }
        }

        @Override
        public boolean done() {
            if (actionStep == 1)
                return true;
            return false;
        }
    }
}
