package inBloom.ERcycle;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import inBloom.graph.CounterfactualityLauncher;
import jason.asSemantics.Personality;

public class CounterfactualitySpaceSearchCycle extends PersonalitySpaceSearchCycle {
	
	protected static Logger logger = Logger.getLogger(CounterfactualityLauncher.class.getName());
	/**
	 * Constructor calling the superclass
	 * 
	 * TODO add model, launcher, etc in order to implement methods
	 */
	
	public CounterfactualitySpaceSearchCycle(String[] agentNames, String agentSrc, int personalityNum, double[] personalityValues) {
		super(agentNames, agentSrc, personalityNum);
	}
	@Override
	protected ReflectResult reflect(EngageResult er) {
		// TODO take a look at RedHenPersonalityCycle
		//do it only with tellability in the beginning
		return null;
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		// TODO take a look at RedHenPersonalityCycle
		//do it only with tellability in the beginning
		return null;
	}
	//TODO understand interaction of engage and reflect!!!

}
