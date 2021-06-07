/**
 *
 */
package inBloom.stories.robinson.happenings;

import java.util.function.Predicate;

import inBloom.stories.robinson.IslandModel;
import inBloom.storyworld.Character;

/**
 * A Happening in which there is a fire on the island
 *
 * @author Julia Wippermann
 */
public class FireHappening extends ConditionalHappening<IslandModel> {

	/**
	 * Constructor with trigger, patient and causalProperty
	 *
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public FireHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.location.equals(model.island);
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		// TODO burning as a boolean in Island
		model.island.startFire();
		model.getLogger().info("The island is burning!");
	}

	@Override
	protected String getConditionalPercept() {
		return "fire";
	}

	@Override
	protected String getConditionalEmotion() {
		return "distress";
		// return "fear";
	}

}
