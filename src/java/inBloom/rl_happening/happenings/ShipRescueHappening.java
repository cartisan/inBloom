/**
 * 
 */
package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * @author  Julia Wippermann
 * @version 13.11.19
 *
 */
public class ShipRescueHappening extends Happening<IslandModel> {

	public ShipRescueHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "stolen(food)");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("gratitude");
	}
	
	public ShipRescueHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(chara.location.equals(model.island)) {
			chara.goTo(model.civilizedWorld);
			model.getLogger().info(chara.name + " was rescued!");
		}
		
		// If character is not on island, he is unaffected by this

	}
}
