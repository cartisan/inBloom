package plotmas.test.story;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.helper.PerceptAnnotation;
import plotmas.storyworld.Character;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Item;

public class TestModel extends PlotModel<TestEnvironment> {

	public int step = 0;

	public Wallet wallet = new Wallet();

	public TestModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		
		for(Character ag : this.characters.values()) {
			ag.addToInventory(wallet);
		}
	}
	
	@Override
	public void initialize(List<LauncherAgent> agentList) {
		super.initialize(agentList);
		for(Character ag : this.characters.values()) {
			ag.addToInventory(wallet);
		}	
	}
	
	public boolean doStuff(Character agent) {
		
		logger.info("Doing stuff.");
		if(step == 1) {
			agent.removeFromInventory(wallet);
			logger.info(agent.name + " lost their wallet...");
		}

		this.environment.addEventPerception(agent.name, "do_stuff");
		
		if(step == 9) {
			this.environment.addEventPerception(agent.name,
												"is_holiday(friday)",
												PerceptAnnotation.fromEmotion("joy"));
		}
		step++;
		return true;
	}
	
	public boolean search(Character agent, String item) {
		logger.info(agent.name + " is looking for their " + item + "!");
		if(step == 5) {
			agent.addToInventory(wallet);
			logger.info(agent.name + " found their wallet!");
		}
		step++;
		return true;
	}

	class Wallet extends Item {

		@Override
		public String getItemName() {
			return "Wallet";
		}

		@Override
		public String literal() {
			return "wallet";
		}
		
	}
}
