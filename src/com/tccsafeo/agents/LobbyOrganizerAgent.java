package com.tccsafeo.agents;

import com.tccsafeo.entities.QueueConfig;
import com.tccsafeo.utils.Configuration;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.io.IOException;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;

    protected void setup() {
        addBehaviour(new SetupConfigsBehaviour());
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

    // TODO: Behaviour to add LobbyOrganizerAgent into yellow page
    private class TurnAvailableBehaviour extends OneShotBehaviour {
        @Override
        public void action() {}
    }

    // TODO: Behaviour to listen for player offers from AdderAgent
    private class ListerAdderAgentBehaviour extends CyclicBehaviour {
        @Override
        public void action() {}
    }
}
