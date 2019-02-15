package plotmas.stories.little_red_hen;

import java.util.function.Predicate;

import plotmas.helper.PerceptAnnotation;
import plotmas.storyworld.Character;
import plotmas.storyworld.Happening;

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

		model.wheat = model.new Wheat();
		chara.addToInventory(model.wheat);
		model.wheatFound = true;
		
		model.getLogger().info(this.getPatient() + " found a grain of wheat"); 
	}
}
