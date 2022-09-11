package com.tccsafeo.agents;

import com.tccsafeo.persistence.entities.*;
import com.tccsafeo.persistence.repositories.LobbyRepository;
import com.tccsafeo.utils.Calculator;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.JsonParser;
import com.tccsafeo.utils.YellowPage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;
    LobbyEntity lobby = new LobbyEntity(getLocalName(), Instant.now());
    Integer completedTeams = 0;
    private final LobbyRepository lobbyRepository = new LobbyRepository();

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
        addBehaviour(new TurnAvailableBehaviour());
        addBehaviour(new ListenAdderAgentBehaviour());
        addBehaviour(new ListenAcceptedProposalsBehaviour());
    }

    void resetLobby() {
        lobby = new LobbyEntity(getLocalName(), Instant.now());
        completedTeams = 0;

        for (int i = 0; i < queueConfig.teamAmount; i++) {
            lobby.getLobby().add(new ArrayList<>());
        }
    }

    Double getLobbyScoreForCriteria(Criteria criteria, Player newPlayer) {
        ArrayList<Player> mergedLobby = getMergedLobby();
        ArrayList<Double> normalizedCriteriaValues = getCriteriaValuesNormalized(criteria, mergedLobby);

        Double standardDeviationWithoutNewPlayer = Calculator.getStandardDeviation(normalizedCriteriaValues);

        Double newPlayerNormalizedCriteria = getPlayerNormalizedCriteria(newPlayer, criteria);
        normalizedCriteriaValues.add(newPlayerNormalizedCriteria);
        Double standardDeviationWithNewPlayer = Calculator.getStandardDeviation(normalizedCriteriaValues);

        return standardDeviationWithNewPlayer - standardDeviationWithoutNewPlayer;
    }

    Double getPlayerNormalizedCriteria(Player player, Criteria criteria) {
        Integer playerDataIndex = player.findPlayerDataIndex(criteria.name);
        if (playerDataIndex >= 0) {
            PlayerData playerData = player.playerData.get(playerDataIndex);
            return Calculator.normalize(playerData.value, criteria.min, criteria.max);
        } else {
            throw new RuntimeException("Criteria does not exist on player!");
        }
    }

    ArrayList<Double> getCriteriaValuesNormalized(Criteria criteria, ArrayList<Player> playerList) {
        ArrayList<Double> criteriaValueList = new ArrayList<>();
        for (Player player : playerList) {
            criteriaValueList.add(getPlayerNormalizedCriteria(player, criteria));
        }
        return criteriaValueList;
    }

    ArrayList<Player> getMergedLobby() {
        ArrayList<Player> mergedLobby = new ArrayList<>();
        for (List<Player> team : lobby.getLobby())
            mergedLobby.addAll(team);
        return mergedLobby;
    }

    // Behaviour to get lobby configurations from json
    private class SetupConfigsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(">>> Setup Lobby Organizer Agent, Name: " + getLocalName());

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
    private static class TurnAvailableBehaviour extends OneShotBehaviour {
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
                Player offeredPlayer = JsonParser.entity(message.getContent(), Player.class);

                ArrayList<Double> criteriaScores = new ArrayList<>();
                for (Criteria criteria : queueConfig.criteria) {
                    Double criteriaValue = getLobbyScoreForCriteria(criteria, offeredPlayer);
                    criteriaScores.add(criteriaValue);
                }

                System.out.println("Calculated scores: " + criteriaScores);
                Double finalScore = Calculator.getAverage(criteriaScores);
                finalScore = Double.isNaN(finalScore) ? 0D : finalScore;

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);

                String replyContent = String.valueOf(finalScore);

                reply.setContent(replyContent);
                myAgent.send(reply);
                System.out.println("Sending reply: " + replyContent);
            } else {
                block();
            }
        }
    }

    private void _setCriteriaOnLobbyEntity(String name, Double criteriaValue) {
        lobby.getDeviation().put(name, criteriaValue.toString());
    }

    // Behaviour to listen for accepted player proposals
    private class ListenAcceptedProposalsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                Player playerToAdd = JsonParser.entity(message.getContent(), Player.class);

                System.out.println("Adding " + playerToAdd);

                if (completedTeams < queueConfig.teamAmount) {
                    lobby.getLobby().get(completedTeams).add(playerToAdd);
                    if (lobby.getLobby().get(completedTeams).size() >= queueConfig.teamSize) {
                        completedTeams++;
                    }

                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("OK");

                    myAgent.send(reply);

                    for (Criteria criteria : queueConfig.criteria) {
                        ArrayList<Player> mergedLobby = getMergedLobby();
                        ArrayList<Double> criteriaValuesNormalized = getCriteriaValuesNormalized(criteria, mergedLobby);
                        Double standardDeviation = Calculator.getStandardDeviation(criteriaValuesNormalized);
                        _setCriteriaOnLobbyEntity(criteria.name, standardDeviation);
                    }

                    if (completedTeams >= queueConfig.teamAmount) {
                        for (List<Player> team : lobby.getLobby()) {
                            System.out.println(myAgent.getName() + " completed Team in Lobby: " + team);
                        }
                        try {
                            _setLobbyFinalTime();   // Set final time on Lobby and save history
                        } catch (Exception e) {
                            System.out.println(e.getCause());
                            System.out.println(e.getLocalizedMessage());
                            for (StackTraceElement element : e.getStackTrace()) {
                                System.out.println(element);
                            }
                        }
                        resetLobby();
                    }
                }
            } else {
                block();
            }
        }

        private void _setLobbyFinalTime() {
            lobby.setEndLobbyTime(Instant.now());
            lobbyRepository.save(lobby);
        }
    }
}



