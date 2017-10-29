package plotmas.storyworld;

import java.util.HashMap;
import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;


/**
 * Responsible for modeling the storyworld. Subclasses should implement methods to handle each action 
 * that is available to ASL agents. Their action requests are relayed to your model by your 
 * {@link plotmas.PlotEnvironment environment subclass}. <br>
 * Your subclass should maintain the current state of all the objects and agents in the story world. This class
 * provides you with domain-independent model functionality. For now this is just a collection of
 * {@link StoryworldAgent agent models}.
 * 
 * 
 * @see plotmas.little_red_hen.FarmModel
 * @author Leonid Berov
 */
public abstract class Model {
	public HashMap<String, StoryworldAgent> agents;
	protected PlotEnvironment environment;
	
	public Model(List<LauncherAgent> agentList, PlotEnvironment env) {
		this.environment = env;
		
        agents = new HashMap<String, StoryworldAgent>();
        // set up connections between agents, model and environment
        for (LauncherAgent agentSetup : agentList) {
        	StoryworldAgent ag = new StoryworldAgent(agentSetup.name) ;
        	agents.put(agentSetup.name, ag);
        	ag.setEnvironment(env);
        }
	}
	
	public StoryworldAgent getAgent(String name) {
		return this.agents.get(name);
	}
}
