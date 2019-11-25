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
 *
 * A Happening in which the patient's food is poisoned.
 */
public class FoodPoisoningHappening extends ConditionalHappening<IslandModel> {
	
	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public FoodPoisoningHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.has("food");
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
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
