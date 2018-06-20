package oedipus;

import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Item;
import plotmas.storyworld.Model;
import plotmas.storyworld.StoryworldAgent;

public class OedipusModel extends Model {
	
	
	public int actionCount;
	public String location; 
	
	public OedipusModel(List<LauncherAgent> agentList, PlotEnvironment<OedipusModel> env) {
		super(agentList, env);
		this.actionCount = 0;
	}
	
	
	public boolean chilling(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was chilling");
		
		return agent.relax(); 
	}
	
	public boolean working(StoryworldAgent agent) {
		this.actionCount +=1;
		logger.info(agent.name + " worked");
		return true;
	}
	
	public boolean goToPlace(StoryworldAgent agent, String new_location ) {
		this.actionCount +=1;
		location = new_location;
		logger.info(agent.name + " went to" + location);
		//this.environment.updateStatePercepts(agent.name, "wentTo [position(location)]");
		return true;
	}
	
	public boolean suicide(StoryworldAgent agent) {
		this.actionCount +=1;
		logger.info(agent.name + " killed self");
		this.environment.addEventPerception(agent.name, "killed self");
		// kill self ?? wie?
		return true;
	}
	
	public boolean blinding(StoryworldAgent agent) {
		this.actionCount +=1;
		logger.info(agent.name + " blinded self");
		this.environment.addEventPerception(agent.name, "blinded self");
		return true;
	}
	
	

}
