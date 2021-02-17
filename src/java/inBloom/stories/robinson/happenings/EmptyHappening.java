/**
 * 
 */
package inBloom.stories.robinson.happenings;

import java.util.function.Predicate;

import inBloom.stories.robinson.IslandModel;
import inBloom.storyworld.Character;

/**
 * @author juwi
 *
 */
public class EmptyHappening extends ConditionalHappening<IslandModel> {
	
	/**
	 * Constructor with trigger, patient and causalProperty
	 * 
	 * @see @ConditionalHappening.ConditionalHappening
	 */
	public EmptyHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return false;
	}

	@Override
	protected void executeModelEffects(IslandModel model, Character chara) {
		return;
	}

	@Override
	protected String getConditionalPercept() {
		return null;
	}

	@Override
	protected String getConditionalEmotion() {
		return null;
	}
	
	protected boolean isEmpty() {
		return true;
	}
}
