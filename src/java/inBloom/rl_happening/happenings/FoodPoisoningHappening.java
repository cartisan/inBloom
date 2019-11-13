/**
 * 
 */
package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * @author  Julia Wippermann
 * @version 13.11.19
 *
 * A Happening in which the patientäs food is poisoned.
 */
public class FoodPoisoningHappening extends Happening<IslandModel> {
	
	public FoodPoisoningHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "poisoned(food)");
		this.patient = patient;
		// TODO does it make sense to add an emotion by default
		// when an agent might not be affected
		this.annotation = PerceptAnnotation.fromEmotion("distress");
	}
	
	public FoodPoisoningHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		model.foodIsOkay = false;
		model.getLogger().info("The food was poisoned!");
		
		if(chara.has("food")) {
			// add a percept for the character?
			model.getLogger().info(chara + " owns poisoned food!");
		}
	}
	
}
