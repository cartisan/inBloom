package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PersonalitySpaceSearchCycle;
import inBloom.ERcycle.ReflectResult;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

public class RedHenPersonalityCycle extends PersonalitySpaceSearchCycle {
	
	private String[] agNames = new String[] { "hen", "dog", "cow", "pig" };
	
	protected RedHenPersonalityCycle() {
		// Create PlotCycle with needed agents.
		super("agent", 2);
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("I am reflecting");
		double currTellability = er.getTellability().compute();
		onCycleResult(lastPersonalities, er.getTellability());
		log(" Current Tellability: " + currTellability);
		// Save tellability, graph and hen personality if it was better than the best before
		if(currTellability > bestTellability) {
			bestTellability = currTellability;
			log("New best: " + bestTellability);
			bestPersonalities = lastPersonalities;
		}
		
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
		
		List<LauncherAgent> agents = createAgs(this.agNames, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		return new ReflectResult(lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector()), agents);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		lastPersonalities = personalityIterator.next();
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		
		List<LauncherAgent> agents = createAgs(this.agNames,new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		ReflectResult rr = new ReflectResult(lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector()), agents);
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
	
	/**
	 * Set up happening director with our predefined happening, as this cycle only searches through personality space
	 * and takes happenings as given.
	 * @return
	 */
	private ScheduledHappeningDirector getHappeningDirector() {
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 2 farm work actions
				(FarmModel model) -> {
	            		if(model.farm.farmingProgress > 2) {
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
		
		RedHenPersonalityCycle cycle = new RedHenPersonalityCycle();
		cycle.run();
	}

}
