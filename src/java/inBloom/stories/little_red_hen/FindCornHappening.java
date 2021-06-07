package inBloom.stories.little_red_hen;

import inBloom.LauncherAgent;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

public class FindCornHappening extends Happening<FarmModel> {

	private FarmModel.Wheat wheat = new FarmModel.Wheat();
	private int step;

	/**
	 * Initializes a version of the find corn happening that automatically detects its patient
	 * as the one who performed the farmWork action that changed the counter in {@linkplain FarmModel#farm.farmingProgress}
	 * over the threshold defined in {@linkplain FindCornHappening#triggered(FarmModel)} and its step as the first step
	 * after it was triggered.
	 */
	public FindCornHappening(){
		this.effect = null;			// we implement a custom execute method, no need for Consumer stored in effect
		this.trigger = null;		// we implement a custom triggered method, no need for Predicate stored in trigger

		this.percept = "found(" + this.wheat.literal() +")";
		this.causalProperty = "farmingProgress";
		this.patient = null;  		// patient can be inferred from agent responsible for a change in farmingProgress
		this.step = -1;
	}

	/**
	 * Initializes a version of the find corn happening that targets the patient provided as parameter and can not
	 * be triggered before the environment step defined by step.
	 */
	public FindCornHappening(LauncherAgent patient, int step){
		this.effect = null;			// we implement a custom execute method, no need for Consumer stored in effect
		this.trigger = null;		// we implement a custom triggered method, no need for Predicate stored in trigger
		
		this.percept = "found(" + this.wheat.literal() +")";
		this.causalProperty = "farmingProgress";
		this.patient = patient.name;
		this.step = step;
	}
	
	@Override
	public void execute(FarmModel model) {
		Character chara = model.getCharacter(this.getPatient());
		chara.addToInventory(this.wheat);
		model.getLogger().info(this.getPatient() + " found a grain of wheat");
	}
	
	@Override
	public boolean triggered(FarmModel model) {
		if(model.farm.farmingProgress > 1 && model.getStep() > this.step) {
			return true;
		}
		return false;
	}
}
