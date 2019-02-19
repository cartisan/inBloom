package plotmas.ERcycle;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import plotmas.LauncherAgent;
import plotmas.ERcycle.PlotCycle.EngageResult;

public class MakeExtraverted implements ProblemFixCommand {

	private LauncherAgent protagonist;
	private double protagonistOldE;
	
	private LauncherAgent otherChar;
	private double otherOldE;
	
	public MakeExtraverted(EngageResult er) {
		this.protagonist = er.getLastAgents().stream().filter(ag -> ag.name.compareTo("protagonist") == 0).collect(Collectors.toList()).get(0);
		this.protagonistOldE = protagonist.personality.E;

		// select an other character at random to also make extraverted 
		Random rand = new Random();
		List<LauncherAgent> charList =  er.getLastAgents().stream().filter(ag -> ag.name.compareTo("protagonist") != 0)
			    												   .collect(Collectors.toList());
		this.otherChar = charList.get(rand.nextInt(charList.size()));
		this.otherOldE = this.otherChar.personality.E;

	}
	
	@Override
	public void execute(EngageResult er) {
		this.protagonist.personality.E = 1;
		this.otherChar.personality.E = 1;
	}

	@Override
	public void undo(EngageResult er) {
		this.protagonist.personality.E = this.protagonistOldE;
		this.otherChar.personality.E = this.otherOldE;
	}

	@Override
	public String message() {
		return "Increasing to maximum the extraversion of protagonist and " + otherChar.name;
	}

}
