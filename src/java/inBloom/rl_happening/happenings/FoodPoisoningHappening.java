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
public class FoodPoisoningHappening extends ConditionalHappening<IslandModel> {
	
	public FoodPoisoningHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.has("food");
	}

	@Override
	protected void executeEffects(IslandModel model, Character chara) {
		model.foodIsOkay = false;
		model.getLogger().info(chara + "'s food was poisoned!");
		
	}

	@Override
	protected String getConditionalPercept() {
		return "poisoned(food)";
	}

	@Override
	protected String getConditionalEmotion() {
		return "distress";
	}
	
}
