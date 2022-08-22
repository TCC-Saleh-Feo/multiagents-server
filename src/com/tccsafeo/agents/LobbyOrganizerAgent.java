package com.tccsafeo.agents;

import com.tccsafeo.entities.Player;
import com.tccsafeo.entities.QueueConfig;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.JsonParser;
import com.tccsafeo.utils.YellowPage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Random;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
        addBehaviour(new TurnAvailableBehaviour());
        addBehaviour(new ListenAdderAgentBehaviour());
    }

    protected void setQueueConfig(QueueConfig queueConfig) {
        this.queueConfig = queueConfig;
        System.out.println(this.queueConfig);
    }

    // Behaviour to get lobby configurations from json
    private class SetupConfigsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            try {
                Configuration config = Configuration.getInstance();
                setQueueConfig(config.getQueueConfig());
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
            MessageTemplate mt = MessageTemplate.MatchPerformative((ACLMessage.CFP));
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                try {
                    Player offeredPlayer = JsonParser.entity(message.getContent(), Player.class);
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

    // TODO: Behaviour to listen for accepted player proposals
    private class ListenAcceptedProposalsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {

        }
    }
}
