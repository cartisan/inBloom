package plotmas.test.story;

import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Item;
import plotmas.storyworld.Model;
import plotmas.storyworld.StoryworldAgent;

public class TestModel extends Model {

	private int step = 0;

	private Wallet wallet = new Wallet();
	
	public TestModel(List<LauncherAgent> agentList, PlotEnvironment<?> env) {
		super(agentList, env);
		for(StoryworldAgent ag : this.agents.values()) {
			ag.addToInventory(wallet);
		}
	}
	
	public boolean doStuff(StoryworldAgent agent) {
		
		logger.info("Doing stuff.");
		if(step == 1) {
			agent.removeFromInventory(wallet);
			logger.info(agent.name + " lost their wallet...");
		}

		this.environment.addEventPerception(agent.name, "do_stuff");
		
		if(step == 9) {
			this.environment.addEventPerception(agent.name, "is_holiday(friday)" + addEmotion("joy"));
		}
		step++;
		return true;
	}
	
	public boolean search(StoryworldAgent agent, String item) {
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
