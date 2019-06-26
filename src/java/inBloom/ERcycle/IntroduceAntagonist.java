package inBloom.ERcycle;

import java.util.Arrays;
import java.util.LinkedList;

import inBloom.LauncherAgent;
import inBloom.ERcycle.EngageResult;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;
import jason.asSemantics.Personality;

public class IntroduceAntagonist implements ProblemFixCommand {

	private LauncherAgent antagonist;
	private RedHenHappeningCycle controller;
	
	public IntroduceAntagonist(RedHenHappeningCycle controller) {
		this.controller = controller;
		
		// An antagonist is a very, very bad person ;)
		// TODO: what about automatizing these initial beliefs and locations?
		this.antagonist = new LauncherAgent("antagonist" + this.controller.charCount,
										    Arrays.asList("hungry", "self(farm_animal)"),
											new LinkedList<String>(),
				 							new Personality(0, -1, 0, -1, 1));
	}
	
	@Override
	public void execute(EngageResult er) {
		// TODO: This just gets the protagonist's location, more reasoning here 
		this.antagonist.location = er.getLastAgents().get(0).location;
		
		er.getLastAgents().add(this.antagonist);
		this.controller.updateCharCount(1);		// notify that we added one char
	}

	@Override
	public void undo(EngageResult er) {
		er.getLastAgents().remove(this.antagonist);
		this.controller.updateCharCount(-1);		// notify that we removed one char
	}

	@Override
	public String message() {
		return "Introducing an antagonist";
	}

}
