package inBloom.juwistest;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;

/**
* @author juwi
*
*/
public class HogwartsLauncher extends PlotLauncher<HogwartsEnvironment, HogwartsModel> {

	public HogwartsLauncher() {
		ENV_CLASS = HogwartsEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}

	public static void main(String[] args) throws JasonException {
		logger.info("Starting up from Launcher");

		PlotControlsLauncher.runner = new HogwartsLauncher();

		
		LauncherAgent harry = new LauncherAgent("harry",
				Arrays.asList("hungry", "self(farm_animal)"),
		    	new LinkedList<String>(),
				new Personality(0, -0.5, 0, 1, 0)
		);
		
		LauncherAgent ron = new LauncherAgent("ron",
				Arrays.asList("hungry", "self(farm_animal)"),
		    	new LinkedList<String>(),
				new Personality(-0.5, -1, 1, 0, 1)
		);
		
		LauncherAgent hermione = new LauncherAgent("hermione",
				Arrays.asList("hungry", "self(farm_animal)"),
		    	new LinkedList<String>(),
				new Personality(1, 1, 0.5, -1, -1)
		);
		
		ImmutableList<LauncherAgent> agents = ImmutableList.of(harry, ron, hermione);

		// Initialise MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();

		HogwartsModel model = new HogwartsModel(agents, hapDir);
		
		/**harry.location = model.hogwarts.name;
		ron.location = model.hogwarts.name;
		hermione.location = model.hogwarts.name;*/
		
		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "hogwartsAgent");
		runner.run();
	}

}
