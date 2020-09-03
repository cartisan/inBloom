package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Character;

public class TorrentialRainHappening extends ConditionalHappening<IslandModel> {
	
	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public TorrentialRainHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.location.equals(model.island);
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		// TODO burning as a boolean in Island
		model.island.startRain();
		//model.activateFeature(IslandModel.rain);
		model.getLogger().info("The island is under torrential rain!");
	}

	@Override
	protected String getConditionalPercept() {
		return "rain";
	}

	@Override
	protected String getConditionalEmotion() {
		return "distress";
		// return "fear";
	}

}
