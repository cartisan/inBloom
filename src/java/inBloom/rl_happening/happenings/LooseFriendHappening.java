package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * A Happening in which one of the patient's friends is eaten by a monkey
 * 
 * @author  Julia Wippermann
 */
public class LooseFriendHappening extends ConditionalHappening<IslandModel> {

	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public LooseFriendHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}
	
	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return model.getNumberOfFriends(chara) > 0 && chara.location.equals(model.island);
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		model.deleteFriend(chara);
		// Feature of having a friend is automatically deactivated in deleteFriend
		model.getLogger().info(chara + "'s friend was eaten by a monkey.");
	}

	@Override
	protected String getConditionalPercept() {
		return "eaten(friend)";
	}

	@Override
	protected String getConditionalEmotion() {
		return "pity";
	}
	
}
