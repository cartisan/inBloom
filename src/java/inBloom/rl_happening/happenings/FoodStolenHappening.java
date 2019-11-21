package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class FoodStolenHappening extends ConditionalHappening<IslandModel> {

	/**public FoodStolenHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		// TODO should only be PERCEIVED if has effect as well. I mean, could be like this too. But confusing :(
		// IDEA: hasEffect method to use here AND in execute -> no duplicate code
		// Problem 1: here no model known, but we need the model
		// Problem 2: super constructor müssen wir trotzdem aufrufen -> trotzdem percept gesendet
		// 			  aber immerhin die Emotion könnten wir bedingen, was auch wichtiger ist (percept an sich okay, können wir
		// 			  einfach ignorieren im Agent, wenn auch doof, weil Percept sollte er nicht kriegen, wenn nicht an dieser
		//			  Location -> so wie bei anderen Percepts halt
		// für hasEffect könnte man ein Interface (sinnvoller) zwischen schalten
		super(trigger, causalProperty, "stolen(food)");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("resentment");
	}
	
	public FoodStolenHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}*/
	
	public FoodStolenHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		super(trigger, patient, causalProperty);
	}

	/**public void execute(IslandModel model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(hasEffect(model)) {
			chara.removeFromInventory("food");
			model.getLogger().info("Monkey stole " + chara.name + "'s food. Holy crap.");
		}
		
		// else there is no effect of this happening -> should also not have a percept annotation then! TODO

	}*/
	
	@Override
	protected boolean hasEffect(IslandModel model, Character chara) {
		return chara.location.equals(model.island) && chara.has("food");
	}
	
	@Override
	protected void executeEffects(IslandModel model, Character chara) {
		chara.removeFromInventory("food");
		model.getLogger().info("Monkey stole " + chara.name + "'s food. Holy crap.");
		
	}

	@Override
	protected String getConditionalPercept() {
		return "stolen(food)";
	}
	
	@Override
	protected String getConditionalEmotion() {
		return "resentment";
	}
	
}
