package inBloom.stories.little_red_hen;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class FindCornHappening extends Happening<FarmModel> {

	public FindCornHappening(Predicate<FarmModel> trigger, String patient, String cause){
		// setup how this event will be perceived
		super(trigger, cause, "found(wheat)");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("joy");
	}
	
	public FindCornHappening(Predicate<FarmModel> trigger, String patient){
		this(trigger, patient, "");
	}

	@Override
	public void execute(FarmModel model) {
		Character chara = model.getCharacter(this.getPatient());
		chara.addToInventory(new FarmModel.Wheat());
		model.getLogger().info(this.getPatient() + " found a grain of wheat"); 
	}
}
