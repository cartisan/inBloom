package inBloom.juwistest;

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

		ImmutableList<LauncherAgent> agents = ImmutableList.of(
						new LauncherAgent("harry",
								new Personality(0, -0.5, 0, 1, 0)
						),

						new LauncherAgent("ron",
								new Personality(-0.5, -1, 1, 0, 1)
						),

						new LauncherAgent("hermione",
								new Personality(1, 1, 0.5, -1, -1)
						)

				);

		// Initialise MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();

		HogwartsModel model = new HogwartsModel(agents, hapDir);

		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "agentJulia");
		runner.run();
	}

}
