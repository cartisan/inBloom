package plotmas.stories.little_red_hen;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.ERcycle.PersonalitySpaceSearchCycle;
import plotmas.storyworld.ScheduledHappeningDirector;

public class RedHenPersonalityCycle extends PersonalitySpaceSearchCycle {
	//TODO protected!!!!
	public RedHenPersonalityCycle() {
		// Create PlotCycle with needed agents.
		super(new String[] { "hen", "dog", "cow", "pig" }, "agent", 2);
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		logger.info("I am reflecting");
		onCycleResult(lastPersonalities, er.getTellability());
		
		// Save tellability, graph and hen personality if it was better than the best before
		if(er.getTellability().compute() > bestTellability) {
			bestTellability = er.getTellability().compute();
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
		
		List<LauncherAgent> agents = createAgs(new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		return new ReflectResult(lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), new ScheduledHappeningDirector()), agents);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		lastPersonalities = personalityIterator.next();
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		
		List<LauncherAgent> agents = createAgs(new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		ReflectResult rr = new ReflectResult(lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), new ScheduledHappeningDirector()), agents);
		
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
	
	public static void main(String[] args) {
		PersonalitySpaceSearchCycle.main(args);
		
		RedHenPersonalityCycle cycle = new RedHenPersonalityCycle();
		cycle.run();
	}
}
