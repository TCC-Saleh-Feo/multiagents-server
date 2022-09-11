package com.tccsafeo.agents;

import com.tccsafeo.utils.ClassNameAgents;
import com.tccsafeo.utils.Configuration;
import com.tccsafeo.utils.WhitePage;
import com.tccsafeo.utils.YellowPage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.*;
import java.util.stream.Collectors;

public class HealthAgent extends Agent {

    private static String[] agentsName = {"Adder1", "Adder2", "Lobby1", "Lobby2", "Lobby3"};
//    private Map<String, String> agentWithState = new HashMap<>();  // TODO: Save Agent State to economize resources

    protected void setup() {
        addBehaviour(new SetupHealthAgentBehaviour());
        addBehaviour(new CheckAgentsStateBehaviour(this, 2000));
//        addBehaviour(new SleepUnemployedAgentBehaviour(this, 2000));  // TODO: Sleep unemployed agents
//        addBehaviour(new WakeUpAgentBehaviour());  // TODO: Wakeup Agents
    }

    private class SetupHealthAgentBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println(">>> Setup Health Agent, Name: " + getLocalName());
            YellowPage.addAgent(myAgent, "health");
        }
    }

    private class CheckAgentsStateBehaviour extends TickerBehaviour {
        public CheckAgentsStateBehaviour(HealthAgent healthAgent, int period) {
            super(healthAgent, period);
        }

        @Override
        protected void onTick() {
            try {
                List<AMSAgentDescription> foundAgents = WhitePage.listAgents(myAgent, agentsName);
                List<String> foundAgentsName = foundAgents.stream().map(AMSAgentDescription::getName).map(AID::getLocalName)
                                .collect(Collectors.toList());
//                updateAgentsState(foundAgents);
                checkLiveAgents(foundAgentsName);
            } catch (FIPAException e) {
                block();
            }
        }
    }

    // TODO: Use this method to update agent state
//    private void updateAgentsState(List<AMSAgentDescription> agents) {
//        for (AMSAgentDescription agent : agents) {
//            agentWithState.put(agent.getName().getLocalName(), agent.getState());
//        }
//    }

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
