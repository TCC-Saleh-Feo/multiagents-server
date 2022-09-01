package com.tccsafeo.agents;

import com.tccsafeo.entities.Criteria;
import com.tccsafeo.entities.Player;
import com.tccsafeo.entities.QueueConfig;
import com.tccsafeo.utils.Calculator;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.JsonParser;
import com.tccsafeo.utils.YellowPage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;
    ArrayList<ArrayList<Player>> lobby = new ArrayList<>();
    Integer completedTeams = 0;

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
        addBehaviour(new TurnAvailableBehaviour());
        addBehaviour(new ListenAdderAgentBehaviour());
        addBehaviour(new ListenAcceptedProposalsBehaviour());
        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                System.out.println(getName() + ":" + lobby);
            }
        });
    }

    void resetLobby() {
        lobby = new ArrayList<>();
        completedTeams = 0;

        for (Integer i = 0; i < queueConfig.teamAmount; i++) {
            lobby.add(new ArrayList<>());
        }
    }

    // Behaviour to get lobby configurations from json
    private class SetupConfigsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            try {
                Configuration config = Configuration.getInstance();
                queueConfig = config.getQueueConfig();

                resetLobby();
            } catch (IOException exception) {
                System.out.println("Could not load configurations!");
            }
        }
    }

    // Behaviour to add LobbyOrganizerAgent into yellow page
    private class TurnAvailableBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            YellowPage.addAgent(myAgent, "lobby-organizer");
        }
    }

    // Behaviour to listen for player offers from AdderAgent
    private class ListenAdderAgentBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                try {
                    Player offeredPlayer = JsonParser.entity(message.getContent(), Player.class);

                    for (Criteria criteria : queueConfig.criteria) {
                        Integer criteriaIndexOnPlayer = offeredPlayer.findPlayerDataIndex(criteria.name);
                        if (criteriaIndexOnPlayer >= 0) {
                            Double normalizedValue = Calculator.normalize(offeredPlayer.playerData.get(criteriaIndexOnPlayer).value, criteria.min, criteria.max);
                            System.out.println("Normalized " + criteria.name + ": " + normalizedValue);
                        } else {
                            throw new RuntimeException("Criteria not found on player data!");
                        }
                    }

                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);

                    Random r = new Random();
                    String replyContent = String.valueOf(r.nextInt(100));

                    reply.setContent(replyContent);
                    myAgent.send(reply);
                    System.out.println("Sending reply: " + replyContent);
                } catch (IOException exception) {
                    System.out.println("Could not transform json into Player!");
                }
            } else {
                block();
            }
        }
    }

    // Behaviour to listen for accepted player proposals
    private class ListenAcceptedProposalsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                try {
                    Player playerToAdd = JsonParser.entity(message.getContent(), Player.class);

                    if (completedTeams < queueConfig.teamAmount) {
                        lobby.get(completedTeams).add(playerToAdd);
                        if (lobby.get(completedTeams).size() >= queueConfig.teamSize) {
                            completedTeams++;
                        }

                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("OK");

                        myAgent.send(reply);

                        if (completedTeams >= queueConfig.teamAmount) {
                            System.out.println("Completed Lobby: " + lobby);
                            resetLobby();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Could not parse json to Player!");
                }
            } else {
                block();
            }
        }
    }
}

// TODO: Conectar a aplicação a um banco para fins de teste

// TODO: Define how the Criteria should be used to determine player score -> normalizar os valores e calcular a diferença do desvio padrão

// TODO: Decide if there should be a waiting room for players with low score in all lobbies -> vai ter waiting room

// TODO: Define priority based on player queue time -> a prioridade é a ordem da fila

// TODO: Verify if team is considered balanced before closing lobby -> comparar com valores de outros algoritmos (elo por exemplo)

// TODO: Define if a communication between LobbyOrganizers should be added -> nice to have



