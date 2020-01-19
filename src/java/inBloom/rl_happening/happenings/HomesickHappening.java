package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * A Happening in which the agent gets homesick
 * 
 * @author  Julia Wippermann
 */
public class HomesickHappening extends ConditionalHappening<IslandModel> {

	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public HomesickHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}
	
	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		// TODO this is not ... really ...? It's a prerequisite for the Happening taking place at all
		// so it NEEDS to be implemented in the Launcher already
		return chara.location.equals(model.island) && model.getStep() > 10;
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		model.getLogger().info(chara.name + " is homesick.");
	}

	@Override
	protected String getConditionalPercept() {
		return "homesick";
	}

	@Override
	protected String getConditionalEmotion() {
		return "remorse";
	}
	
}
