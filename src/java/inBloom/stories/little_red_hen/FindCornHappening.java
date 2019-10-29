package inBloom.stories.little_red_hen;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class FindCornHappening extends Happening<FarmModel> {

	FarmModel.Wheat wheat = new FarmModel.Wheat();

	public FindCornHappening(Predicate<FarmModel> trigger, String patient, String causalProperty){
		this.trigger = trigger;
		this.patient = patient;
		this.causalProperty = causalProperty;

		this.effect = null;			// we implement a custom execute method, no need for Consumer stored in effect
		this.percept = "found(" + this.wheat.literal() +")";
		this.annotation = PerceptAnnotation.fromEmotion("hope");
	}

	public FindCornHappening(Predicate<FarmModel> trigger, String patient){
		this(trigger, patient, "");
	}

	@Override
	public void execute(FarmModel model) {
		Character chara = model.getCharacter(this.getPatient());
		chara.addToInventory(this.wheat);
		model.getLogger().info(this.getPatient() + " found a grain of wheat");
	}
}
