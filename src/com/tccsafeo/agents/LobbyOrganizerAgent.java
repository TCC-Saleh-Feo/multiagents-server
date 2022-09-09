package com.tccsafeo.agents;

import com.tccsafeo.persistence.entities.Criteria;
import com.tccsafeo.persistence.entities.Player;
import com.tccsafeo.persistence.entities.PlayerData;
import com.tccsafeo.persistence.entities.QueueConfig;
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
import java.util.ArrayList;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;
    ArrayList<ArrayList<Player>> lobby = new ArrayList<>();
    Integer completedTeams = 0;

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
        addBehaviour(new TurnAvailableBehaviour());
        addBehaviour(new ListenAdderAgentBehaviour());
        addBehaviour(new ListenAcceptedProposalsBehaviour());
    }

    void resetLobby() {
        lobby = new ArrayList<>();
        completedTeams = 0;

        for (Integer i = 0; i < queueConfig.teamAmount; i++) {
            lobby.add(new ArrayList<>());
        }
    }

    Double getLobbyScoreForCriteria(Criteria criteria, Player newPlayer) {
        ArrayList<Player> mergedLobby = getMergedLobby();
        ArrayList<Double> normalizedCriteriaValues = getCriteriaValuesNormalized(criteria, mergedLobby);

        Double standardDeviationWithoutNewPlayer = Calculator.getStandardDeviation(normalizedCriteriaValues);

        Double newPlayerNormalizedCriteria = getPlayerNormalizedCriteria(newPlayer, criteria);
        normalizedCriteriaValues.add(newPlayerNormalizedCriteria);
        Double standardDeviationWithNewPlayer = Calculator.getStandardDeviation(normalizedCriteriaValues);

        return Math.abs(standardDeviationWithNewPlayer - standardDeviationWithoutNewPlayer);
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
        for (ArrayList<Player> team : lobby)
            mergedLobby.addAll(team);
        return mergedLobby;
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
                Player offeredPlayer = JsonParser.entity(message.getContent(), Player.class);

                ArrayList<Double> criteriaScores = new ArrayList<>();
                for (Criteria criteria : queueConfig.criteria) {
                    criteriaScores.add(getLobbyScoreForCriteria(criteria, offeredPlayer));
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
            } else {
                block();
            }
        }
    }
}

// TODO: Conectar a aplicação a um banco para fins de teste

// TODO: Verify if team is considered balanced before closing lobby -> comparar com valores de outros algoritmos (elo por exemplo)

// TODO: Define if a communication between LobbyOrganizers should be added -> nice to have



