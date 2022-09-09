package com.tccsafeo.agents;

import com.tccsafeo.config.AmqpConfig;
import com.tccsafeo.persistence.entities.Player;
import com.tccsafeo.persistence.entities.PlayerEntity;
import com.tccsafeo.persistence.entities.WaitingQueue;
import com.tccsafeo.persistence.repositories.PlayerRepository;
import com.tccsafeo.utils.AmqpListener;
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
import java.time.Instant;
import java.util.ArrayList;


public class AdderAgent extends Agent {
    Double MIN_SCORE = 0.1;

    private ArrayList<AID> lobbyOrganizerAgents = new ArrayList<>();

    WaitingQueue waitingQueue = WaitingQueue.getInstance();
    AmqpListener amqpListener;
    private PlayerRepository _playerRepository = new PlayerRepository();

    protected void setup() {
        addBehaviour(new SetupPlayersBehaviour());
        // Behaviour to send players to lobby agents from time to time
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                String playerToAddString = amqpListener.getMessage("INCOMING_QUEUE");
                if (playerToAddString != null) {
                    Player playerToAdd = JsonParser.entity(playerToAddString, Player.class);
                    if (playerToAdd != null) {
                        addBehaviour(new OfferPlayerBehaviour(playerToAdd));
                    } else {
                        System.out.println("All Players Added!");
                        this.stop();
                        doDelete();
                    }
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
        // Behaviour to offer players in waiting queue
        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                if (waitingQueue.getQueueSize() > 0) {
                    System.out.println("Offering player on waiting queue!");
                    addBehaviour(new OfferPlayerBehaviour(waitingQueue.getNextPlayer()));
                }
            }
        });
    }

    private class SetupPlayersBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            AmqpConfig amqpConfig = new AmqpConfig();
            amqpListener = new AmqpListener(amqpConfig.getChannel());
        }
    }

    // Behaviour to offer Player to all available LobbyOrganizerAgents
    private class OfferPlayerBehaviour extends Behaviour {
        private Integer actionStep = 0;
        private MessageTemplate mt;
        private Integer repliesCount = 0;
        private AID bestLobby;
        private Double bestScore;
        private Player player;
        private PlayerEntity playerWithTime;

        public OfferPlayerBehaviour(Player currentPlayer) {
            this.player = currentPlayer;
        }

        @Override
        public void action() {
            switch (actionStep) {
                case 0:
                    try {
                        if (lobbyOrganizerAgents.size() > 0) {
                            _setPlayerInitialTime(player);    // sets the player's entry time in the queue
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
                            Double score = Double.parseDouble(reply.getContent());
                            if (bestLobby == null || score < bestScore) {
                                bestScore = score;
                                bestLobby = reply.getSender();
                            }
                            repliesCount++;
                        }
                        // TODO: add timout in case a lobbyOrganizer does not respond
                        if (repliesCount >= lobbyOrganizerAgents.size()) {
                            actionStep++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    if (bestLobby != null) {
                        if (bestScore > MIN_SCORE) {
                            System.out.println("Adding player to waiting queue!");
                            waitingQueue.addPlayer(player);
                            actionStep = 4;
                        } else {
                            try {
                                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                order.addReceiver(bestLobby);
                                order.setConversationId("offering-player-" + player.playerId);
                                order.setReplyWith("order" + System.currentTimeMillis());
                                order.setContent(JsonParser.toJson(player));
                                myAgent.send(order);
                                mt = MessageTemplate.and(
                                        MessageTemplate.MatchConversationId("offering-player-" + player.playerId),
                                        MessageTemplate.MatchInReplyTo(order.getReplyWith())
                                );
                            } catch (IOException e) {
                                System.out.println("Could not parse player to json!");
                            }
                        }
                        actionStep++;
                    } else {
                        actionStep = 4;
                    }
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // sets the player's final waiting time in the queue
                            _setPlayerFinalTime(player);
                            _savePlayerWithTime();
                        }
                        actionStep++;
                    } else {
                        block();
                    }
            }
        }

        @Override
        public boolean done() {
            if (actionStep == 4)
                return true;
            return false;
        }

        private void _setPlayerInitialTime(Player player) {
            playerWithTime = new PlayerEntity(player, Instant.now());
        }

        private void _setPlayerFinalTime(Player player) {
            playerWithTime.setEndLobbyTime(Instant.now());
        }

        private void _savePlayerWithTime() {
            _playerRepository.save(playerWithTime);
        }
    }
}
