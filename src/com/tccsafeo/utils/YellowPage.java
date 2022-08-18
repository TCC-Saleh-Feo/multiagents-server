package com.tccsafeo.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;

public class YellowPage {
    public static ArrayList<AID> getAgents(Agent agent, String type) {
        ArrayList<AID> yellowPageAgents = new ArrayList<>();
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(agent, template);
            for (DFAgentDescription yellowPageAgentDescription : result) {
                yellowPageAgents.add(yellowPageAgentDescription.getName());
            }
            return yellowPageAgents;
        } catch (FIPAException fipaException) {
            throw new RuntimeException(fipaException);
        }
    }

    public static void addAgent(Agent agent, String type) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(agent.getName());
        dfd.addServices(sd);
        try {
            DFService.register(agent, dfd);
        } catch (FIPAException fipaException) {
            fipaException.printStackTrace();
        }
    }
}
