package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

// TODO this happening can actually only happen when the agent is on the island / not home.
// Otherwise it does not only have no effect, but can literally NOT HAPPEN
// This I implement in the trigger conditions in the Launcher, but considering that I was
// thinking about having prerequisites for the effects, would it make sense to have prerequisites
// for the happening itself as well in the happening? Probably not. Would be hard to realise and
// except for being neat, it wouldn't have that much of a great advantage.
public class HomesickHappening extends Happening<IslandModel> {

	public HomesickHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "homesick");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("remorse");
	}
	
	public HomesickHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(hasEffect(model)) {
			// not many more effects, because percept and emotion already handeled in constructor
			// and that is all, since it only has an internal effect on the agent, no external consequences
			// on the model
			model.getLogger().info(chara.name + " is homesick.");
		}
		
	}
	
	private boolean hasEffect(IslandModel model) {
		Character chara = model.getCharacter(this.getPatient());
		// TODO this is not ... really ...? It's a prerequisite for the Happening taking place at all
		// so it NEEDS to be implemented in the Launcher already
		//return chara.location.equals(model.island);
		return true;
	}
	
}
