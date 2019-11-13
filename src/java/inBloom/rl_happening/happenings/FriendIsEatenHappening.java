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
 * @author juwi
 *
 */
public class FriendIsEatenHappening extends Happening<IslandModel> {

	public FriendIsEatenHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "eaten(friend)"); // theoretically this percept should also only be added when the Happening has an effect TODO
		this.patient = patient;
		// TODO does it make sense to add an emotion by default
		// when an agent might not be affected
		this.annotation = PerceptAnnotation.fromEmotion("distress");
	}
	
	public FriendIsEatenHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(model.getNumberOfFriends(chara) > 0) {
			model.friendIsEaten(chara);
			model.getLogger().info(chara + " has lost a friend");
		}
		
		// if the character didn't have any friends, he is not affected by this happening. Good for him!
		
	}
	
}
