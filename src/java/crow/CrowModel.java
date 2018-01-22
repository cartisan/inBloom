package crow;

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
		
		this.actionCount = 0;
		this.cheeseSeen = false; 
		// this.cheesePosition = 0; //in the beak of the crow
	}
	
	public boolean hasSeenCheese() {
		return cheeseSeen;
	}
	
	public boolean walkAround(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was walking around");
		
		if((!cheeseSeen) && (this.actionCount >= 3) && (agent.name == "fox")) {
			this.cheeseSeen = true;
			this.environment.addEventPerception(agent.name, "seen(cheese)");
			logger.info(agent.name + " saw cheese");
		}
		
		return true; 
	}
	
	public boolean sitAround(StoryworldAgent agent) {
		
		logger.info(agent.name + "sat around");
		return agent.relax();
	}
	
	
	public boolean flatter(StoryworldAgent flatterer, StoryworldAgent flattered) {
		
		this.environment.addEventPerception(flattered.name, "wasFlattered[emotion(joy)]");
		
		logger.info(flatterer.name + "flattered " + flattered.name);
		
		if(flatterer.has ("Cheese")) {
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
			if( agent.has("Cheese")) {
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
			return false;
		}
	}
	
	
	public boolean sing(StoryworldAgent singer) {
		this.environment.addEventPerception(singer.name, "sang[emotion(joy)]");
		logger.info(singer.name + "sang");
		
		if( singer.has("Cheese")) {
			singer.removeFromInventory(this.cheese);
			this.environment.addEventPerception(singer.name, "lostCheese[emotion(shame)]");
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
	


