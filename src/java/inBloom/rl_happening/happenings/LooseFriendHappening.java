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
 * A Happening in which one of the patient's friends is eaten by a monkey
 */
public class LooseFriendHappening extends Happening<IslandModel> {

	/**
	 * Constructor with trigger, patient and causal property.
	 * Creates a percept for the patient eaten(friend) and an emotion of distress.
	 * 
	 * @param trigger
	 * 			see Happening
	 * @param patient
	 * 			The agent whom the Happening is happening to
	 * @param causalProperty
	 * 			see Happening
	 */
	public LooseFriendHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "eaten(friend)"); // theoretically this percept should also only be added when the Happening has an effect TODO
		this.patient = patient;
		// TODO does it make sense to add an emotion by default
		// when an agent might not be affected
		this.annotation = PerceptAnnotation.fromEmotion("distress");
	}
	
	/**
	 * Constructor with trigger and patient.
	 * 
	 * @param trigger
	 * 			see Happening
	 * @param patient
	 * 			The agent whom the Happening is happening to
	 */
	public LooseFriendHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	/**
	 * Models the effects of the Happening onto the patient and the model.
	 * 
	 * The patient looses one friend to the hunger of the monkeys.
	 * 
	 * If the character has no friends or isn't on the island (where the monkeys
	 * are), they are not effected by the Happening.
	 * 
	 * @param model
	 * 			see Happening.execute
	 */
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(model.getNumberOfFriends(chara) > 0 && chara.location.equals(model.island)) {
			model.deleteFriend(chara);
			model.getLogger().info(chara + "'s friend was eaten by a monkey.");
		}
		
		// if the character didn't have any friends, he is not affected by this happening. Good for him!
		
	}
	
}
