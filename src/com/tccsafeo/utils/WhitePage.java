package com.tccsafeo.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhitePage {

    private static final String PLATFORM = "@the-platform";

    public static List<AMSAgentDescription> listAgents(Agent agent, String[] agentsName) throws FIPAException {
        AMSAgentDescription agentDescription = new AMSAgentDescription();
        List<AMSAgentDescription> foundAgents = new ArrayList<>();
        for (String agentName : agentsName) {
            agentDescription.setName(_buildAID(agentName + PLATFORM));
            AMSAgentDescription[] result = AMSService.search(agent, agentDescription);
            foundAgents.addAll(Arrays.asList(result));
        }
        return foundAgents;
    }

    private static AID _buildAID(String name) {
        return new AID(name, true);
    }
}
