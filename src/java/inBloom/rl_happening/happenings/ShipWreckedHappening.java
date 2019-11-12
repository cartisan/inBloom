package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Happening;
import inBloom.storyworld.Character;

public class ShipWreckedHappening extends Happening<IslandModel>{

	public ShipWreckedHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "shipWrecked");
		this.patient = patient;
		this.annotation = PerceptAnnotation.fromEmotion("fear");
	}
	
	public ShipWreckedHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		Character chara = model.getCharacter(this.getPatient());
		
		// the character is only affected by this if he was on the ship
		if(chara.location.equals(model.ship)) {
			chara.goTo(model.island);
			model.getLogger().info(this.getPatient() + " stranded on island " + model.island.name);
		}
	}
	
}

// TODO dinge, die ich als causalProperty haben will, muss ich als @ModelState annotieren
// für Locations und Characters kann ich das machen