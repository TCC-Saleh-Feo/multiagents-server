package com.tccsafeo.agents;

import com.tccsafeo.entities.QueueConfig;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.YellowPage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.io.IOException;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
        addBehaviour(new TurnAvailableBehaviour());
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

    // TODO: Behaviour to listen for player offers from AdderAgent
    private class ListenAdderAgentBehaviour extends CyclicBehaviour {
        @Override
        public void action() {}
    }
}
