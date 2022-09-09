package com.tccsafeo.agents;

import com.tccsafeo.config.AmqpConfig;
import com.tccsafeo.persistence.entities.Player;
import com.tccsafeo.persistence.entities.PlayerEntity;
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

import java.time.Instant;
import java.util.ArrayList;


public class AdderAgent extends Agent {
    Double MIN_SCORE = 0.1;

    private ArrayList<AID> lobbyOrganizerAgents = new ArrayList<>();

    AmqpListener incomingQueueListener;
    AmqpListener waitingQueueListener;
    private final PlayerRepository _playerRepository = new PlayerRepository();

    boolean isIncomingOfferExecuting = false;
    boolean isWaitingOfferExecuting = false;

    protected void setup() {
        addBehaviour(new SetupPlayersBehaviour());
        // Behaviour to send players to lobby agents from time to time
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (!isIncomingOfferExecuting) {
                    String playerToAddString = incomingQueueListener.getMessage();
                    if (playerToAddString != null) {
                        Player playerToAdd = JsonParser.entity(playerToAddString, Player.class);
                        if (playerToAdd != null) {
                            addBehaviour(new OfferPlayerBehaviour(playerToAdd, "INCOMING_QUEUE"));
                        }
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
                if (!isWaitingOfferExecuting) {
                    String playerToAddString = waitingQueueListener.getMessage();
                    if (playerToAddString != null) {
                        Player playerToAdd = JsonParser.entity(playerToAddString, Player.class);
                        if (playerToAdd != null) {
                            addBehaviour(new OfferPlayerBehaviour(playerToAdd, "WAITING_QUEUE"));
                        }
                    }
                }
            }
        });
    }

    private class SetupPlayersBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            AmqpConfig inComingQueueConfig = new AmqpConfig("INCOMING_QUEUE");
            incomingQueueListener = new AmqpListener(inComingQueueConfig.getChannel(), "INCOMING_QUEUE");

            AmqpConfig waitingQueueConfig = new AmqpConfig("WAITING_QUEUE");
            waitingQueueListener = new AmqpListener(waitingQueueConfig.getChannel(), "WAITING_QUEUE");

            YellowPage.addAgent(myAgent, "adder");
        }
    }

    // Behaviour to offer Player to all available LobbyOrganizerAgents
    private class OfferPlayerBehaviour extends Behaviour {
        private Integer actionStep = 0;
        private MessageTemplate mt;
        private Integer repliesCount = 0;
        private AID bestLobby;
        private Double bestScore;
        private final Player player;
        private PlayerEntity playerWithTime;
        private long executionStart;
        private String queueType;

        public OfferPlayerBehaviour(Player currentPlayer, String queueType) {
            this.player = currentPlayer;
            this.queueType = queueType;
        }

        @Override
        public void action() {
            switch (actionStep) {
                case 0:
                    if (queueType == "INCOMING_QUEUE")
                        isIncomingOfferExecuting = true;
                    if (queueType == "WAITING_QUEUE")
                        isWaitingOfferExecuting = true;
                    executionStart = System.currentTimeMillis();
                    if (lobbyOrganizerAgents.size() > 0) {
                        _setPlayerInitialTime(player);    // sets the player's entry time in the queue
                        mt = Messenger.sendPlayerOffer(myAgent, lobbyOrganizerAgents, player);
                        System.out.println("Message sent");
                        actionStep++;
                    } else {
                        block();
                    }
                    break;
                case 1:
                    ACLMessage reply;
                    System.out.println("Time: " + (System.currentTimeMillis() - executionStart));
                    if (System.currentTimeMillis() - executionStart > 30000) {
                        actionStep++;
                    } else {
                        reply = myAgent.receive(mt);
                        if (reply != null) {
                            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                                double score = Double.parseDouble(reply.getContent());
                                if (bestLobby == null || score < bestScore) {
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
                    }
                    break;
                case 2:
                    if (bestLobby != null) {
                        if (bestScore > MIN_SCORE) {
                            System.out.println("Adding player to waiting queue!");
                            waitingQueueListener.publishMessage(JsonParser.toJson(player));
                            actionStep = 4;
                        } else {
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
                            actionStep++;
                        }
                    } else {
                        actionStep = 4;
                    }
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // sets the player's final waiting time in the queue
                            _setPlayerFinalTime();
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
            if (actionStep == 4) {
                if (queueType == "INCOMING_QUEUE")
                    isIncomingOfferExecuting = false;
                if (queueType == "WAITING_QUEUE")
                    isWaitingOfferExecuting = false;
                return true;
            }
            return false;
        }

        private void _setPlayerInitialTime(Player player) {
            playerWithTime = new PlayerEntity(player, Instant.now());
        }

        private void _setPlayerFinalTime() {
            playerWithTime.setEndLobbyTime(Instant.now());
        }

        private void _savePlayerWithTime() {
            _playerRepository.save(playerWithTime);
        }
    }
}
