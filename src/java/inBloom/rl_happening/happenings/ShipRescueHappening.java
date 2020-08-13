/**
 * 
 */
package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * A Happening in which the patient is rescued by a ship
 * 
 * @author  Julia Wippermann
 */
public class ShipRescueHappening extends ConditionalHappening<IslandModel> {

	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public ShipRescueHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.location.equals(model.island);
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		chara.goTo(model.civilizedWorld);
		model.deactivateFeature(IslandModel.onIsland);
		model.getLogger().info(chara.name + " was rescued!");
		model.removeAgent(chara);
	}

	@Override
	protected String getConditionalPercept() {
		return "rescued";
	}

	@Override
	protected String getConditionalEmotion() {
		return "gratitude";
	}
}
