package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.math.Stats;

import jason.asSemantics.Personality;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PlotCycle;
import inBloom.ERcycle.ReflectResult;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;


public class RepeatingRedHenLauncher extends PlotCycle {

	private static final int FINAL_RUN_NUMBER = 2;

	/**
	 * Safes results of individual cycles
	 */
	private List<Tellability> cycleResults;

	/**
	 * The personalities of the last cycle.
	 */
	protected Personality[] lastPersonalities;
	/**
	 * The launcher of the last cycle.
	 */
	protected PlotLauncher<?,?> lastRunner;


    public static double calculateSD(List<Double> values, double mean) {
        double standardDeviation = 0.0;
        int length = values.size();

        for(double num: values) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

	protected RepeatingRedHenLauncher() {
		// Create PlotCycle with needed agents.
		super("agent_folktale_animal", true);
		this.cycleResults = new ArrayList<>();
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		this.log("I am reflecting");
		Tellability result = er.getTellability();
		result.compute();

		this.log(String.format("    FP : %f", result.balancedFunctionalPolyvalence));
		this.log(String.format("    SYM: %f", result.balancedSymmetry));
		this.log(String.format("    OPO: %f", result.balancedOpposition));
		this.log(String.format("    SUS: %f", result.balancedSuspense));
		this.log(String.format("  TEL: %f", result.value));

		this.cycleResults.add(result);

		// save mood chart for last run
//		MoodGraph.getMoodListener().setSelectedAgent("hen");
//		MoodGraph.getMoodListener().saveGraph(er.getLastModel().moodMapper, String.valueOf(currentCycle));

		// Stop cycle if we ran all iterations (add 1, so we can ignore the first run that is always an outlier
		if(currentCycle >= FINAL_RUN_NUMBER + 1) {
			return new ReflectResult(null, null, null, false);
		}

		// Start the next cycle
		this.lastRunner = new RedHenLauncher();
		this.lastRunner.setShowGui(false);
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), this.createHappeningDirector());
		List<LauncherAgent> agentList = this.createAgs(model);

		return new ReflectResult(this.lastRunner, model, agentList);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		this.lastRunner = new RedHenLauncher();
		this.lastRunner.setShowGui(false);
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), this.createHappeningDirector());
		List<LauncherAgent> agentList = this.createAgs(model);

		ReflectResult rr = new ReflectResult(this.lastRunner, model, agentList);
		this.log("Cycle " + currentCycle);

		return rr;
	}

	@Override
	protected void finish(EngageResult er) {
		List<Double> fps = new ArrayList<>();
		List<Double> syms = new ArrayList<>();
		List<Double> opos = new ArrayList<>();
		List<Double> susps = new ArrayList<>();
		List<Double> ress = new ArrayList<>();

		// Print results
		this.log("\nRESULTS:\n");
		this.log(String.format("      %s\t%s\t%s\t%s\t%s", "FP", "SYM", "OPO", "SUS", "BAL TELL"));
		int i = 0;
		for (Tellability t : this.cycleResults) {
			i += 1;
			if (i==1) {
				// on first run, the suspense is always an outlier, ignore it!
				this.log(String.format(Locale.US, "   ---%f\t%f\t%f\t%f\t%f---", t.balancedFunctionalPolyvalence, t.balancedSymmetry, t.balancedOpposition, t.balancedSuspense, t.value));
			} else {
				fps.add(t.balancedFunctionalPolyvalence);
				syms.add(t.balancedSymmetry);
				opos.add(t.balancedOpposition);
				susps.add(t.balancedSuspense);
				ress.add(t.value);

				this.log(String.format(Locale.US, "      %f\t%f\t%f\t%f\t|%f|\t\t%f\t%f\t%f\t%f\t", t.balancedFunctionalPolyvalence, t.balancedSymmetry, t.balancedOpposition, t.balancedSuspense, t.value, t.absoluteFunctionalPolyvalence, t.absoluteSymmetry, t.absoluteOpposition, t.absoluteSuspense));
//				this.log(String.format(Locale.US, "      %f\t%f\t%f\t%f\t%f", t.absoluteFunctionalPolyvalence, t.absoluteSymmetry, t.absoluteOpposition, t.absoluteSuspense, t.value));
			}
		}

		double meanFps = Stats.meanOf(fps);
		double meanSyms = Stats.meanOf(syms);
		double meanOpos = Stats.meanOf(opos);
		double meanSusps = Stats.meanOf(susps);
		double meanRess = Stats.meanOf(ress);
		this.log(String.format(Locale.US, "      %.2f\t%.2f\t%.2f\t%.2f\t%.2f\t| AVE", meanFps, Stats.meanOf(syms), Stats.meanOf(opos), Stats.meanOf(susps), Stats.meanOf(ress)));
		this.log(String.format(Locale.US, "      %.2f\t%.2f\t%.2f\t%.2f\t%.2f\t| STD", calculateSD(fps, meanFps), calculateSD(syms, meanSyms), calculateSD(opos, meanOpos), calculateSD(susps, meanSusps), calculateSD(ress, meanRess)));
	}

	/**
	 * Set up happening director with our predefined happening, as this cycle only searches through personality space
	 * and takes happenings as given.
	 * @return
	 */
	private ScheduledHappeningDirector createHappeningDirector() {
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening();
		hapDir.scheduleHappening(findCorn);
		return hapDir;
	}

	protected List<LauncherAgent> createAgs(FarmModel model) {
        LauncherAgent hen = new LauncherAgent("hen",
					Arrays.asList("hungry", "self(farm_animal)"),
					    new LinkedList<String>(),
					    new Personality(0,  1, 0.7, -0.3, -0.2)    //punishment
//						new Personality(0, -1, 0.7, -0.3, -0.2)    //low consc --> no plot
//						new Personality(0,  1, 0.7, -0.3,   -1)    //low neurot --> eat alone, no punishment
//						new Personality(0,  1, 0.7,  1,   -0.2)    //high aggrea --> sharing despite refusals
//						new Personality(0,  1, 0,   -0.3, -0.2)    //lower extra --> no help requests, no punishment, sharing
//						new Personality(0,  1, 0,   -0.3,  -1)     //lower extra, low neurot --> no help requests, no punishment, no sharing
////					    new Personality(0,  1, 0.7, 1, -1)   //low neurot, neg aggrea --> no punishment, no share
        );
        LauncherAgent dog = new LauncherAgent("dog",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
//						new Personality(0, -1, -0.3, -0.7, -0.7)
						new Personality(0, -1, -0.3, 0.7, -0.7)	// doggie helps hen v1
		);
        LauncherAgent cow = new LauncherAgent("cow",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
//						new Personality(0, -1, -0.3, -0.7, -0.7)
						new Personality(0, -1, -0.3, 0.7, -0.7)	// cow helps hen v1
		);
        LauncherAgent pig = new LauncherAgent("pig",
					Arrays.asList("hungry", "self(farm_animal)"),
						new LinkedList<String>(),
						new Personality(0, -1, -0.3, -0.7, -0.7)
		);
		hen.location = model.farm.name;
		dog.location = model.farm.name;
		cow.location = model.farm.name;
		pig.location = model.farm.name;

		return Stream.of(hen, dog, cow, pig).collect(Collectors.toList());
	}

	public static void main(String[] args) {
		RepeatingRedHenLauncher cycle = new RepeatingRedHenLauncher();
		cycle.run();
	}
}
