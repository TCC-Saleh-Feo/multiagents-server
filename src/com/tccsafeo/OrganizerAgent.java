package com.tccsafeo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class OrganizerAgent extends Agent {
    private List<AID> lobbyAgents;

    protected void setup() {
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("lobby-builder");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    lobbyAgents = new ArrayList<>();
                    for (DFAgentDescription agent : result) {
                        lobbyAgents.add(agent.getName());
                    }
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                myAgent.addBehaviour(new RequestPerformer());
            }
        });
    }

    private class RequestPerformer extends Behaviour
    {
        private MessageTemplate mt; // The template to receive replies
        private Integer step = 0;

        @Override
        public void action() {
            switch(step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(lobbyAgents.get(0));
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    cfp.setConversationId("lobby-trade");
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("lobby-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step++;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                Map<UUID, List<DataSource.Player>> response = mapper.readValue(reply.getContent(), Map.class);
                                Map<UUID, List<DataSource.Player>> playersOrganized = _organizerPlayers(response);
                                ACLMessage aclMessage = new ACLMessage(ACLMessage.AGREE);
                                aclMessage.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                                aclMessage.setConversationId("lobby-trade");
                                String playersString = mapper.writeValueAsString(playersOrganized);
                                aclMessage.setContent(playersString);
                                myAgent.send(aclMessage);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        step++;
                    } else {
                        block();
                    }
                    break;
                case 2:
                default:
                    step++;
            }
        }

        private Map<UUID, List<DataSource.Player>> _organizerPlayers(Map<UUID, List<DataSource.Player>> lobby)
        {
            List<DataSource.Player> playersListed = new ArrayList<>();
            for (UUID ids: lobby.keySet()) {
                playersListed.addAll(lobby.get(ids));
            }
            Collections.sort(playersListed, new Comparator<DataSource.Player>() {
                @Override
                public int compare(DataSource.Player player, DataSource.Player t1) {
                    return t1.kills.compareTo(player.kills);
                }
            });
            for (UUID ids: lobby.keySet()) {
                List<DataSource.Player> firstPlayers = new ArrayList<>();
                for (DataSource.Player play : playersListed) {
                    firstPlayers.add(play);
                }
                playersListed.removeAll(firstPlayers);
                lobby.put(ids, firstPlayers);
            }
            return lobby;
        }

        @Override
        public boolean done() {
            if (step == 3) {
                return true;
            }
            return false;
        }
    }
}
