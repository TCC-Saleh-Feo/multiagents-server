package com.tccsafeo.agents;

import com.tccsafeo.entities.Player;
import com.tccsafeo.utils.FileUtil;
import com.tccsafeo.utils.JsonParser;
import com.tccsafeo.utils.Messenger;
import com.tccsafeo.utils.YellowPage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.ArrayList;

public class AdderAgent extends Agent {

    ArrayList<Player> playerList = new ArrayList<>();
    ArrayList<Player> addedPlayers = new ArrayList<>();
    Player currentPlayer;

    private ArrayList<AID> lobbyOrganizerAgents = new ArrayList<>();

    protected void setup() {
        addBehaviour(new SetupPlayersBehaviour());
        // Behaviour to send players to lobby agents from time to time
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (addedPlayers.size() < playerList.size()) {
                    Integer currentPlayerPosition = addedPlayers.size();
                    currentPlayer = playerList.get(currentPlayerPosition);

                    addedPlayers.add(currentPlayer);
                    System.out.println("Current Player: " + currentPlayer + "\nAdded players: " + addedPlayers.size());
                    addBehaviour(new OfferPlayerBehaviour());
                } else {
                    System.out.println("All Players Added!");
                    this.stop();
                    doDelete();
                }
            }
        });
        // Behaviour to update available LobbyOrganizerAgents
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                lobbyOrganizerAgents = YellowPage.getAgents(myAgent, "lobby-organizer");
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
        private MessageTemplate mt;
        private Integer repliesCount = 0;
        private AID bestLobby;
        private Integer bestScore;
        private Player player = currentPlayer;

        @Override
        public void action() {
            switch (actionStep) {
                case 0:
                    try {
                        if (lobbyOrganizerAgents.size() > 0) {
                            mt = Messenger.sendPlayerOffer(myAgent, lobbyOrganizerAgents, player);
                            System.out.println("Message sent");
                        }
                    } catch (IOException exception) {
                        System.out.println("Error converting player to json");
                    }
                    actionStep++;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            Integer score = Integer.parseInt(reply.getContent());
                            if (bestLobby == null || score > bestScore) {
                                bestScore = score;
                                bestLobby = reply.getSender();
                            }
                            repliesCount++;
                        }
                        if (repliesCount >= lobbyOrganizerAgents.size()) {
                            actionStep++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    if (bestLobby != null) {
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(bestLobby);
                        order.setConversationId("offering-player-" + player.playerId);
                        order.setReplyWith("order" + System.currentTimeMillis());
                        myAgent.send(order);

                        mt = MessageTemplate.and(
                                MessageTemplate.MatchConversationId("offering-player-" + player.playerId),
                                MessageTemplate.MatchInReplyTo(order.getReplyWith())
                            );
                        actionStep++;
                    } else {
                        actionStep = 4;
                    }
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // TODO: what to do when player is inserted into lobby
                        }
                    }
            }
        }

        @Override
        public boolean done() {
            if (actionStep == 4)
                return true;
            return false;
        }
    }
}
