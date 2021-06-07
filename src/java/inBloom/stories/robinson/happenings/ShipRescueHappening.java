/**
 *
 */
package inBloom.stories.robinson.happenings;

import java.util.function.Predicate;

import jason.asSyntax.Literal;

import inBloom.stories.robinson.IslandModel;
import inBloom.storyworld.Character;

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
		model.getLogger().info(chara.name + " was rescued!");
		model.environment.addPercept(chara.name, Literal.parseLiteral("rescueEnd"));
		//model.happyRescueEnd(chara);
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
