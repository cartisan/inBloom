package plotmas.stories.little_red_hen;

import java.util.function.BiPredicate;

import plotmas.storyworld.Happening;
import plotmas.storyworld.Character;

public class FindCornHappening extends Happening<FarmModel> {

	public FindCornHappening(BiPredicate<FarmModel, Integer> trigger) {
		super(trigger);
	}

	@Override
	public void execute(FarmModel model) {
		model.wheat = model.new Wheat();
		Character chara = model.getCharacter("hen");
		
		chara.addToInventory(model.wheat);
		model.getEnvironment().addEventPerception(chara.name, "found(wheat)[emotion(joy)]");  
		model.wheatFound = true;
		
		model.getLogger().info(chara.name + " found a grain of wheat"); 
	}
}
