package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

// DOCME
public class HomesickHappening extends ConditionalHappening<IslandModel> {

	public HomesickHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}
	
	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		// TODO this is not ... really ...? It's a prerequisite for the Happening taking place at all
		// so it NEEDS to be implemented in the Launcher already
		return chara.location.equals(model.island);
	}

	@Override
	protected void executeEffects(IslandModel model, Character chara) {
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
