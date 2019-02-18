package plotmas.ERcycle;

import java.util.stream.Collectors;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class IntroduceAntagonist implements ProblemFixCommand {

	private LauncherAgent antagonist;
	private RedHenHappeningCycle controller;
	
	public IntroduceAntagonist(RedHenHappeningCycle controller) {
		this.controller = controller;
		this.antagonist = new LauncherAgent("antagonist" + this.controller.charCount,
				 new Personality(0, -1, 0, -1, 0));		
	}
	
	@Override
	public void execute(EngageResult er) {
		// Create Agent with low agreeableness
		er.getLastAgents().add(this.antagonist);
		this.controller.updateCharCount(1);
		
		// change protagonist extraversion (or make this a new problem-fix pair?)
		LauncherAgent protagonist = er.getLastAgents().stream().filter(ag -> ag.name.compareTo("protagonist") == 0).collect(Collectors.toList()).get(0);
		protagonist.personality.E = 1;
	}

	@Override
	public void undo(EngageResult er) {
		er.getLastAgents().remove(this.antagonist);
		this.controller.updateCharCount(-1);
	}

	@Override
	public String message() {
		return "Introducing an antagonist";
	}

}
