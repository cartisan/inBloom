package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.IslandModel;
import inBloom.storyworld.Happening;
import inBloom.storyworld.Character;

public class StormHappening extends Happening<IslandModel>{

	public StormHappening(Predicate<IslandModel> trigger, String patient, String causalProperty) {
		// setup for how this event will be perceived
		super(trigger, causalProperty, "storm");
		this.patient = patient;
		// TODO diese emotion evtl bedingt machen?	
		this.annotation = PerceptAnnotation.fromEmotion("fear");
	}
	
	public StormHappening(Predicate<IslandModel> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(IslandModel model) {
		Character chara = model.getCharacter(this.getPatient());
		
		// the character is only affected by this if he was on the ship
		if(chara.location.equals(model.ship)) {
			chara.goTo(model.island);
			model.getLogger().info(this.getPatient() + " stranded on island " + model.island.name);
		}
		
		// TODO hasHut evtl. auch über Location lösen -> dann aber unter-Location
		// if the character is on the island and there exists a hut, the hut is destroyed
		// this, theoretically is independent of the character's location
		if(model.hasHut) {
			model.hasHut = false;
			// TODO absolut patient unabhängig momentan ... kann man allgemeine Happenings machen und dann in den
			// Happenings schauen, welche Patients es betreffen würde (anhand von Location)
			model.getLogger().info("The hut was destroyed.");
		}
	}
	
}

// TODO dinge, die ich als causalProperty haben will, muss ich als @ModelState annotieren
// für Locations und Characters kann ich das machen