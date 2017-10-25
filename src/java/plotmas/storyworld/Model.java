package plotmas.storyworld;

import java.util.HashMap;
import java.util.List;

import plotmas.PlotEnvironment;


public class Model {
	public HashMap<String, StoryworldAgent> agents;
	protected PlotEnvironment environment;
	
	public Model(List<StoryworldAgent> agentList, PlotEnvironment env) {
		this.environment = env;
		
        agents = new HashMap<String, StoryworldAgent>();
        // set up connections between agents, model and environment
        for (StoryworldAgent agent : agentList) {
        	agents.put(agent.name, agent);
        	agent.setEnvironment(env);
        }
	}
	
	public StoryworldAgent getAgent(String name) {
		return this.agents.get(name);
	}
}
