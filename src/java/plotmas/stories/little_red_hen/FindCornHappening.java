package plotmas.stories.little_red_hen;

import java.util.function.BiPredicate;

import plotmas.storyworld.Happening;
import plotmas.storyworld.StoryworldAgent;

public class FindCornHappening extends Happening<FarmModel> {

	public FindCornHappening(BiPredicate<FarmModel, Integer> trigger) {
		super(trigger);
	}

	@Override
	public void execute(FarmModel model) {
		model.wheat = model.new Wheat();
		StoryworldAgent agent = model.getAgent("hen");
		
		agent.addToInventory(model.wheat);
		model.getEnvironment().addEventPerception(agent.name, "found(wheat)[emotion(joy)]");  
		model.wheatFound = true;
		
		model.getLogger().info(agent.name + " found a grain of wheat"); 
	}
}
