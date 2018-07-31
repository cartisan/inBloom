package plotmas.stories.crow;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.Item;
import plotmas.storyworld.StoryworldAgent;

public class CrowModel extends PlotModel<CrowEnvironment> {
	
	// public int cheesePosition; 
	public Cheese cheese;
	public boolean cheeseSeen;
	public int actionCount;
	
	public CrowModel(List<LauncherAgent> agentList, CrowEnvironment env) {
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
		
		if((!cheeseSeen) && (this.actionCount >= 3) && (agent.name == "fox")) {
			this.cheeseSeen = true;
			this.environment.addEventPerception(agent.name, "seen(cheese)[emotion(joy)]");
			logger.info(agent.name + " saw cheese");
		}
		
		return true; 
	}
	
	public boolean sitAround(StoryworldAgent agent) {
		//agent.relax();
		return true;
	}
	
	public boolean askForCheese(StoryworldAgent asking, StoryworldAgent asked) {
		logger.info(asking.name + " asked "  + asked.name + " for Cheese");
		this.environment.addEventPerception(asked.name, "wasAsked");
		return true;
	}
	
	public boolean answerNegatively(StoryworldAgent asked, StoryworldAgent asking) {
		logger.info(asked.name + " answered"  + asking.name + " negatively");
		this.environment.addEventPerception(asking.name, "wasAnsweredNegatively[emotion(anger)]");
		return true;
	}
	
	
	public boolean flatter(StoryworldAgent flatterer, StoryworldAgent flattered) {
		logger.info(flatterer.name + " flattered " + flattered.name);
		this.environment.addEventPerception(flattered.name, "wasFlattered[emotion(joy)]");
		
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
		logger.info(singer.name + " sang");
		
		if(singer.has("cheese")) {
			singer.removeFromInventory(this.cheese);
			logger.info(singer.name + " lost cheese");

			for(StoryworldAgent agent: this.agents.values()) {
				if(agent != singer){
					logger.info(agent + " saw cheese fall");
					this.environment.addEventPerception(agent.name, "freeCheese");
				}
			}
		}
		return true;
	
	}
	
	
	
	
	
	
	public class Cheese extends Item {
		static final String itemName = "cheese";
		
		@Override
		public boolean isEdible() {
			return true;
		}

		@Override
		public String getItemName() {
			return itemName;
		}

		@Override
		public String literal() {
			return "cheese";
			
		}
	}
}
