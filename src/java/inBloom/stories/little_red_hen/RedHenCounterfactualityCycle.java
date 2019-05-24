package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.ERcycle.PersonalitySpaceSearchCycle;
import inBloom.ERcycle.PlotCycle.EngageResult;
import inBloom.ERcycle.PlotCycle.ReflectResult;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

public class RedHenCounterfactualityCycle extends PersonalitySpaceSearchCycle {
	
//	static double[] personalityValues;
//	RedHenPersonalityCycle redhen = new RedHenPersonalityCycle();
	/** set of domain-specific happenings allowed to be scheduled by the ER Cycle */
	public HashSet<Class<?>> availableHappenings = new HashSet<>();
	
	public RedHenCounterfactualityCycle(/**double[] pValues**/) {
		super(new String[]  { "dog", "cow", "pig", "hen" }, "agent", 2);
		this.availableHappenings.add(FindCornHappening.class);
		//personalityValues = pValues;	
//		if(cycleNum > -1) {
//			endCycle = currentCycle + cycleNum;
//		}
		//this.run();
		
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("I am reflecting");
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		onCycleResult(lastPersonalities, er.getTellability());
		log(" Current Tellability: " + currTellability);
		// Save tellability, graph and hen personality if it was better than the best before
		if(currTellability > bestTellability) {
			bestTellability = currTellability;
			log("New best: " + bestTellability);
			bestPersonalities = lastPersonalities;
		}
		log("Best Tellability So Far: " + bestTellability);
		// Stop cycle if there are no other personality combinations
		if(!personalityIterator.hasNext() || currentCycle >= endCycle) {
			return new ReflectResult(null, null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			setupFileLogger();
		}
		
		List<LauncherAgent> agents = createAgs(this.agentNames, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), er.getLastModel().happeningDirector.clone());
		return new ReflectResult(lastRunner, model, agents);
	}


	@Override
	protected ReflectResult createInitialReflectResult() {
		lastPersonalities = personalityIterator.next();
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), getHappeningDirector());
		List<LauncherAgent> agents = createAgs(this.agentNames,new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		ReflectResult rr = new ReflectResult(runner, model, agents);
		log("Cycle " + currentCycle);
		
		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			setupFileLogger();
		}
		
		return rr;
	}
	
	@Override
	protected void finish(EngageResult er) {
		// Print results
		log("Best tellability: " + bestTellability);
		log("Personalities:");
		for(Personality p : bestPersonalities) {
			log("\t" + p.toString());
		}
		
		// flush and close handled by super implementation
		super.finish(er);
	}
	
	// added by Julia
	// currently there are no happenings
	// this method should solve the problem:
	private ScheduledHappeningDirector getHappeningDirector() {
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 2 farm work actions
				(FarmModel model) -> {
	            		if(model.FARM.farmingProgress > 2) {
	            			return true;
	            		}
	            		return false; 
	    		},
				"hen",
				"actionCount");
		hapDir.scheduleHappening(findCorn);
		return hapDir;
	}
	
	public static void main(String[] args) {
		PersonalitySpaceSearchCycle.main(args);
		RedHenCounterfactualityCycle cycle = new RedHenCounterfactualityCycle();
		cycle.run();
	}
}
