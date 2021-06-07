/**
 *
 */
package inBloom.stories.robinson.happenings;

import java.util.function.Predicate;

import inBloom.stories.robinson.IslandModel;
import inBloom.stories.robinson.IslandModel.Food;
import inBloom.storyworld.Character;

/**
 * A Happening in which the patient's food is poisoned.
 *
 * @author  Julia Wippermann
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

		// not as a precondition, but to make sure we don't run into a NullPointerException
		// though this should theoretically be implicetely true through the precondition in hasEffect
		if(chara.has("food")) {
			((Food)model.getCharacter(chara.name).get("food")).poison();
			model.getLogger().info(chara + "'s food was poisoned!");
		} else {
			// Should never occur due to the precondition of this Happening
			model.getLogger().info("ERROR: There was no food to be poisoned.");
		}
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
