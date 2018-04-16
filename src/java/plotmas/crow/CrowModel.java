package plotmas.crow;

import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Item;
import plotmas.storyworld.Model;
import plotmas.storyworld.StoryworldAgent;

public class CrowModel extends Model {
	
	// public int cheesePosition; 
	public Cheese cheese;
	public boolean cheeseSeen;
	public int actionCount;
	
	public CrowModel(List<LauncherAgent> agentList, PlotEnvironment<CrowModel> env) {
		super(agentList, env);
		this.cheese = new Cheese();
		this.actionCount = 0;
		this.cheeseSeen = false; 
		// this.cheesePosition = 0; //in the beak of the crow
		
		for(StoryworldAgent agent: this.agents.values()) {
			if( agent.name == "crow") {
				agent.addToInventory(this.cheese);
			}
		}
	}
	
	public boolean hasSeenCheese() {
		return cheeseSeen;
	}
	
	public boolean walkAround(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was walking around");
		
		if((!cheeseSeen) && (this.actionCount >= 3) && (agent.name == "fox")) {
			this.cheeseSeen = true;
			this.environment.addEventPerception(agent.name, "seen(cheese)[emotion(joy)]");
			logger.info(agent.name + " saw cheese");
		}
		
		return true; 
	}
	
	public boolean sitAround(StoryworldAgent agent) {
		
		logger.info(agent.name + " sat around");
		return agent.relax();
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
			singer.removeFromInventory(this.cheese);
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
	
	
	
	
	
	
	public class Cheese extends Item {
		static final String itemName = "cheese";

		@Override
		public String getItemName() {
			// TODO Auto-generated method stub
			return itemName;
		}

		@Override
		public String literal() {
			// TODO Auto-generated method stub
			return null;
			
		}
	}
}
