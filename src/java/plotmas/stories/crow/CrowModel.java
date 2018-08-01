package plotmas.stories.crow;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Item;
import plotmas.storyworld.Character;

public class CrowModel extends PlotModel<CrowEnvironment> {
	
	// public int cheesePosition; 
	public Cheese cheese;
	public boolean cheeseSeen;
	public int actionCount;
	
	public CrowModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		this.cheese = new Cheese();
		this.actionCount = 0;
		this.cheeseSeen = false; 
		// this.cheesePosition = 0; //in the beak of the crow
		
		for(Character agent: this.characters.values()) {
			if( agent.name == "crow") {
				agent.addToInventory(this.cheese);
			}
		}
	}
	
	public boolean hasSeenCheese() {
		return cheeseSeen;
	}
	
	public boolean walkAround(Character agent){
		this.actionCount +=1;
		
		if((!cheeseSeen) && (this.actionCount >= 3) && (agent.name == "fox")) {
			this.cheeseSeen = true;
			this.environment.addEventPerception(agent.name, "seen(cheese)[emotion(joy)]");
			logger.info(agent.name + " saw cheese");
		}
		
		return true; 
	}
	
	public boolean sitAround(Character agent) {
		//agent.relax();
		return true;
	}
	
	public boolean askForCheese(Character asking, Character asked) {
		logger.info(asking.name + " asked "  + asked.name + " for Cheese");
		this.environment.addEventPerception(asked.name, "wasAsked");
		return true;
	}
	
	public boolean answerNegatively(Character asked, Character asking) {
		logger.info(asked.name + " answered"  + asking.name + " negatively");
		this.environment.addEventPerception(asking.name, "wasAnsweredNegatively[emotion(anger)]");
		return true;
	}
	
	
	public boolean flatter(Character flatterer, Character flattered) {
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
	
	public boolean pickUpCheese(Character picker) {
		boolean freeCheese = true; 
		
		for(Character agent: this.characters.values()) {
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
	
	
	public boolean sing(Character singer) {
		this.environment.addEventPerception(singer.name, "sang[emotion(joy)]");
		logger.info(singer.name + " sang");
		
		if(singer.has("cheese")) {
			singer.removeFromInventory(this.cheese);
			logger.info(singer.name + " lost cheese");

			for(Character agent: this.characters.values()) {
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
