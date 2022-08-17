package com.tccsafeo.agents;

import com.tccsafeo.entities.QueueConfig;
import com.tccsafeo.utils.Configuration;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.io.IOException;

public class LobbyOrganizerAgent extends Agent {

    QueueConfig queueConfig;

    protected void setup() {
        addBehaviour(new SetupLobbyBehaviour());
    }

    protected void setQueueConfig(QueueConfig queueConfig) {
        this.queueConfig = queueConfig;
        System.out.println(this.queueConfig);
    }

    private class SetupLobbyBehaviour extends OneShotBehaviour {
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
}
