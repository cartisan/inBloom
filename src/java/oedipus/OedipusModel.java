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
	
	public boolean reigning(StoryworldAgent agent) {
		this.actionCount +=1;
		logger.info(agent.name + " reigned");
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
	
	public boolean askForCheese(StoryworldAgent asking, StoryworldAgent asked) {
		
		this.environment.addEventPerception(asked.name, " wasAsked");
		
		logger.info(asking.name + " asked"  + asked.name + " for Cheese");
		
		return true;
	}
	
	public boolean answerNegatively(StoryworldAgent asked, StoryworldAgent asking) {
		
		this.environment.addEventPerception(asking.name, "wasAnsweredNegatively[emotion(anger)]");
		logger.info(asked.name + " answered"  + asking.name + " negatively");
		return true;
	}
	
	
	public boolean flatter(StoryworldAgent flatterer, StoryworldAgent flattered) {
		
		this.environment.addEventPerception(flattered.name, "wasFlattered[emotion(joy)]");
		
		logger.info(flatterer.name + "flattered " + flattered.name);
		
		if(flatterer.has("cheese")) {
		Cheese cheeseItem = (Cheese) flatterer.get(Cheese.itemName);
		flatterer.removeFromInventory(cheeseItem);
		this.environment.addEventPerception(flatterer.name, "wasFlattered[emotion(shame)]");
		logger.info(flatterer.name + "lost cheese");
		}
		return true;
	}
	
	public boolean pickUpCheese(StoryworldAgent picker) {
		boolean freeCheese = true; 
		
		for(StoryworldAgent agent: this.agents.values()) {
			if( agent.has("cheese")) {
				freeCheese = false;
			}
		}
		if(freeCheese) {
			picker.addToInventory(this.cheese);
		
			this.environment.addEventPerception(picker.name, "gotCheese[emotion(joy)]");
			logger.info(picker.name + "picked up cheese");	
			return true;
		}
		else {
			
			this.environment.addEventPerception(picker.name, "missedCheese[emotion(anger)]");
			logger.info(picker.name + "failed to pick up cheese");
			return true;
		}
	}
	
	
	public boolean sing(StoryworldAgent singer) {
		this.environment.addEventPerception(singer.name, "sang[emotion(joy)]");
		logger.info(singer.name + "sang");
		
		if( singer.has("cheese")) {
			//this.environment.addEventPerception(singer.name, "lostCheese[emotion(shame)]");
			for(StoryworldAgent agent: this.agents.values()) {
				if(agent != singer){
					this.environment.addEventPerception(agent.name, "freeCheese");
				}
			}
			logger.info(singer.name + "lost cheese");
		}
		return true;
	
	}
	
	

}
