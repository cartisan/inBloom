package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.Personality;

import inBloom.LauncherAgent;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PersonalitySpaceSearchCycle;
import inBloom.ERcycle.ReflectResult;
import inBloom.storyworld.ScheduledHappeningDirector;

public class RedHenPersonalityCycle extends PersonalitySpaceSearchCycle {

	private String[] agNames = new String[] { "hen", "dog", "cow", "pig" };

	protected RedHenPersonalityCycle() {
		// Create PlotCycle with needed agents.
		super("agent_folktale_animal", 2);
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		this.log("I am reflecting");
		double currTellability = er.getTellability().compute();
		this.onCycleResult(this.lastPersonalities, er.getTellability());
		this.log(" Current Tellability: " + currTellability);
		// Save tellability, graph and hen personality if it was better than the best before
		if(currTellability > this.bestTellability) {
			this.bestTellability = currTellability;
			this.log("New best: " + this.bestTellability);
			this.bestPersonalities = this.lastPersonalities;
		}

		// Stop cycle if there are no other personality combinations
		if(!this.personalityIterator.hasNext() || currentCycle >= endCycle) {
			return new ReflectResult(null, null, null, false);
		}

		// Start the next cycle
		this.lastPersonalities = this.personalityIterator.next();
		this.lastRunner = new RedHenLauncher();
		this.lastRunner.setShowGui(false);
		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			this.setupFileLogger();
		}

		List<LauncherAgent> agents = this.createAgs(this.agNames, new Personality[] {this.lastPersonalities[0], this.lastPersonalities[1], this.lastPersonalities[1], this.lastPersonalities[1]});
		return new ReflectResult(this.lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector()), agents);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		this.lastPersonalities = this.personalityIterator.next();
		this.lastRunner = new RedHenLauncher();
		this.lastRunner.setShowGui(false);

		List<LauncherAgent> agents = this.createAgs(this.agNames,new Personality[] {this.lastPersonalities[0], this.lastPersonalities[1], this.lastPersonalities[1], this.lastPersonalities[1]});
		ReflectResult rr = new ReflectResult(this.lastRunner, new FarmModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector()), agents);
		this.log("Cycle " + currentCycle);

		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			this.setupFileLogger();
		}

		return rr;
	}

	@Override
	protected void finish(EngageResult er) {
		// Print results
		this.log("Best tellability: " + this.bestTellability);
		this.log("Personalities:");
		for(Personality p : this.bestPersonalities) {
			this.log("\t" + p.toString());
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
		FindCornHappening findCorn = new FindCornHappening();
		hapDir.scheduleHappening(findCorn);
		return hapDir;
	}

	public static void main(String[] args) {
		PersonalitySpaceSearchCycle.main(args);

		RedHenPersonalityCycle cycle = new RedHenPersonalityCycle();
		cycle.run();
	}

}
