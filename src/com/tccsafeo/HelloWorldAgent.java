package com.tccsafeo;

import com.tccsafeo.entities.Lobby;
import com.tccsafeo.entities.Player;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.*;

public class HelloWorldAgent extends Agent {

    private ManageAgentsService _manageService = new ManageAgentsService(this);

    private Queue<Player> _playersToPlay;

    protected void setup() {
        _playersToPlay = new LinkedList<>();

        registerAgentInYellowPages();
        addBehaviour(new CheckPlayersInTheQueueBehaviour(this, 60000));
        addBehaviour(new BuildLobbyWithPlayers());
    }

    private void registerAgentInYellowPages() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("organize-agent");
        sd.setName("JADE-organizer");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class CheckPlayersInTheQueueBehaviour extends TickerBehaviour {

        public CheckPlayersInTheQueueBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            List<Player> playersToPlay = _manageService.getPlayersReadyToPlay();
            for (Player playerToPlay : playersToPlay) {
                _playersToPlay.add(playerToPlay);
                System.out.println("Player: " + playerToPlay.getName() + "inserted to queue.");
            }
        }
    }

    private class BuildLobbyWithPlayers extends CyclicBehaviour {

        @Override
        public void action() {
            if (!_playersToPlay.isEmpty()) {
                Lobby lobby = _buildLobby();
                if (lobby != null) {
                    System.out.println("Builded Lobby " + lobby.getPlayer1().getName() + " VS " + lobby.getPlayer2().getName());
                }
            }
        }

        private Lobby _buildLobby() {
            Lobby lobby = new Lobby();
            Player player1 = _playersToPlay.remove();
            lobby.setPlayer1(player1);
            Player player2 = _calculateSecondPlayer(player1);
            if (player2 == null) {
                _playersToPlay.add(player1);
                return null;
            }
            lobby.setPlayer2(player2);
            return lobby;
        }

        private Player _calculateSecondPlayer(Player player1) {
            Double referenceParam = 1.0;
            Player chosedPlayer = null;
            for (Player possibleOponent : _playersToPlay) {
                Double E1 = _calculateProbabilityToVictory(player1, possibleOponent);
                Double E2 = _calculateProbabilityToVictory(possibleOponent, player1);
                Double equivalence = Math.abs(E1 - E2);
                if (equivalence < referenceParam) {
                    referenceParam = equivalence;
                    chosedPlayer = possibleOponent;
                }
            }
            _playersToPlay.remove(chosedPlayer);
            return chosedPlayer;
        }

        private Double _calculateProbabilityToVictory(Player player1, Player player2) {
            return 1.0 / (1.0 + Math.pow(10, (player2.getRating() - player1.getRating())/400.0));
        }
    }
}
