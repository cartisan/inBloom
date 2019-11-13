package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class FoodStolenHappening extends Happening<IslandModel> {

	public FoodStolenHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "stolen(food)");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("resentment");
	}
	
	public FoodStolenHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(chara.location.equals(model.island) && chara.has("food")) {
			chara.removeFromInventory("food");
			model.getLogger().info("Monkey stole " + chara.name + "'s food. Holy crap.");
		}
		
		// else there is no effect of this happening -> should also not have a percept annotation then! TODO

	}
	
}
