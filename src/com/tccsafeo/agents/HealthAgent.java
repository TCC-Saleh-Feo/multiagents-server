package com.tccsafeo.agents;

import com.tccsafeo.persistence.entities.AgentType;
import com.tccsafeo.utils.ClassNameAgents;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.WhitePage;
import com.tccsafeo.utils.YellowPage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HealthAgent extends Agent {

    private static String[] agentsName = {"Adder", "Lobby1", "Lobby2", "Lobby3"};

    protected void setup() {
        addBehaviour(new SetupHealthAgentBehaviour());
        addBehaviour(new CheckAgentsStateBehaviour(this, 2000));
//        addBehaviour(new SleepUnemployedAgentBehaviour());
//        addBehaviour(new WakeUpAgentBehaviour());
    }

    private class SetupHealthAgentBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println(">>> Setup Health Agent, Name: " + getLocalName());
            YellowPage.addAgent(myAgent, "health");
        }
    }

    private class CheckAgentsStateBehaviour extends TickerBehaviour {
        public CheckAgentsStateBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            try {
                List<AMSAgentDescription> foundAgents = WhitePage.listAgents(myAgent, agentsName);
                List<String> foundAgentsName = foundAgents.stream().map(AMSAgentDescription::getName).map(AID::getLocalName)
                                .collect(Collectors.toList());
                checkLiveAgents(foundAgentsName);
            } catch (FIPAException e) {
                block();
            }
        }
    }

    private void checkLiveAgents(List<String> foundAgentsName) {
        List<String> agentsNameList = new LinkedList<>(Arrays.asList(agentsName));
        if (!foundAgentsName.containsAll(agentsNameList)) {
            agentsNameList.removeAll(foundAgentsName);  // Remove live agents
            for (String createAgentName : agentsNameList) {
                addBehaviour(new CreateNewAgentBehaviour(createAgentName));
            }
        }
    }

    private static class CreateNewAgentBehaviour extends OneShotBehaviour {
        private final String deathAgentName;

        public CreateNewAgentBehaviour(String deathAgentName) {
            this.deathAgentName = deathAgentName;
        }

        @Override
        public void action() {
            String agentRealName = Configuration.getAgentRealName(deathAgentName);
            AgentContainer container = myAgent.getContainerController();
            String className = ClassNameAgents.fromName(agentRealName).getClassName();
            try {
                AgentController liveAgent = container.createNewAgent(deathAgentName, className, new Object[]{});
                liveAgent.start();
            } catch (StaleProxyException e) {
                System.out.println(e.getLocalizedMessage());
                block();
            }
        }
    }
}
