package inBloom.stories.robinson.happenings;

import java.util.function.Predicate;

import inBloom.stories.robinson.IslandModel;
import inBloom.storyworld.Character;

/**
 * A Happening in which a storm occurs that either leads to a ship wreck or the destruction of the hut
 *
 * @author  Julia Wippermann
 */
public class StormHappening extends ConditionalHappening<IslandModel>{


	/**
	 * Constructor with trigger, patient and causalProperty
	 *
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public StormHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		// The Happening has (different) effects when the agent is on the ship
		// or there exists a hut on the island.
		return chara.location.equals(model.ship) || model.island.hasHut();
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {

		// Shipwrecked
		if(chara.location.equals(model.ship)) {
			model.shipWrecked(chara);
		}

		// Destroy Hut
		if(model.island.hasHut()) {
			// TODO hasHut als Unterlocation -> gibt es ein Location exists (!=null)
			model.destroyHut();
			// feature of having a hut is automatically deactivated in destroyHut()
			model.getLogger().info("The hut was destroyed.");
		}
	}

	@Override
	protected String getConditionalPercept() {
		return "storm";
	}

	@Override
	protected String getConditionalEmotion() {
		return "fear";
	}

}