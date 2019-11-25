package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * A Happening in which the agent's food is stolen by a monkey
 * 
 * @author  Julia Wippermann
 */
public class FoodStolenHappening extends ConditionalHappening<IslandModel> {
	
	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public FoodStolenHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}
	
	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.location.equals(model.island) && chara.has("food");
	}
	
	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		chara.removeFromInventory("food");
		model.getLogger().info("Monkey stole " + chara.name + "'s food. Holy crap.");
		
	}

	@Override
	protected String getConditionalPercept() {
		return "stolen(food)";
		//return "happening";
	}
	
	@Override
	protected String getConditionalEmotion() {
		return "resentment";
	}
	
}
