package little_red_hen;

import java.util.HashMap;
import java.util.List;


public class PlotModel {
	public HashMap<String, AgentModel> agents;
	protected PlotEnvironment environment;
	
	public PlotModel(List<AgentModel> agentList, PlotEnvironment env) {
		this.environment = env;
		
        agents = new HashMap<String, AgentModel>();
        // set up connections between agents, model and environment
        for (AgentModel agent : agentList) {
        	agents.put(agent.name, agent);
        	agent.setEnvironment(env);
        }
	}
	
	public AgentModel getAgent(String name) {
		return this.agents.get(name);
	}
}
