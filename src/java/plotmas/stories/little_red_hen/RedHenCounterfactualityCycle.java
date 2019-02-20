package plotmas.stories.little_red_hen;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.ERcycle.PersonalitySpaceSearchCycle;
import plotmas.storyworld.ScheduledHappeningDirector;

public class RedHenCounterfactualityCycle extends PersonalitySpaceSearchCycle {
	
	static double[] personalityValues;
	RedHenPersonalityCycle redhen = new RedHenPersonalityCycle();
	
	public RedHenCounterfactualityCycle(double[] pValues) {
		super(new String[]  { "hen", "dog", "cow", "pig" }, "agent", 2);
//		super(new String[] { "hen", "dog", "cow", "pig" }, "agent", 2, pValues, true, true);
		personalityValues = pValues;	
		if(cycleNum > -1) {
			endCycle = currentCycle + cycleNum;
		}
		this.run();
		
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
