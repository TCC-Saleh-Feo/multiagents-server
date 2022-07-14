package com.tccsafeo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class HelloWorldAgent extends Agent {
    public Map<UUID, List<DataSource.Player>> lobby = new HashMap<>();
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("lobby-builder");
        sd.setName("CASIMIRO");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        DataSource aa = new DataSource();
        Queue<DataSource.Player> jogadors =  aa.setUpPlayers();

        addBehaviour(new SetUpPlayersOnLobby(jogadors));
        addBehaviour(new ListenOrganizer());
        addBehaviour(new AgreeBehavoior());

    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class SetUpPlayersOnLobby extends OneShotBehaviour {
        private Queue<DataSource.Player> jogadores;
        public SetUpPlayersOnLobby(Queue<DataSource.Player> jogadors) {
            this.jogadores = jogadors;
        }

        @Override
        public void action() {
            if (lobby.isEmpty()) {
                while (!jogadores.isEmpty()) {
                    UUID lobbyId = UUID.randomUUID();
                    List<DataSource.Player> playersOnLobby = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        playersOnLobby.add(jogadores.remove());
                    }
                    lobby.put(lobbyId, playersOnLobby);
                }
            }
//            _printLobby();
        }

        private void _printLobby() {
            for(UUID uuid : lobby.keySet()) {
                System.out.print(uuid + " ");
                for (DataSource.Player jog : lobby.get(uuid)) {
                    System.out.println(jog.name);
                }
            }
        }
    }

    private class ListenOrganizer extends CyclicBehaviour
    {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(generateLobbyString());
                myAgent.send(reply);
            } else {
                block();
            }
        }

        private String generateLobbyString()
        {
            ObjectMapper _mapper = new ObjectMapper();
            try {
                return _mapper.writeValueAsString(lobby);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class AgreeBehavoior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                System.out.println(message.getContent());
//                takeDown();
            } else {
                block();
            }
        }
    }
}
