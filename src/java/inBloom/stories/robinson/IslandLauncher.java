/**
 * THE NEW ISLAND LAUNCHER USING THE AUTOMATED HAPPENING DIRECTOR
 */
package inBloom.stories.robinson;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;

/**
 * @author Julia Wippermann
 * @version 20.11.19
 *
 */
public class IslandLauncher extends PlotLauncher<IslandEnvironment, IslandModel> {

	public IslandLauncher() {
		this.ENV_CLASS = IslandEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}


	public static void main(String[] args) throws JasonException {
		logger.info("Starting up from Launcher");

		PlotControlsLauncher.runner = new IslandLauncher();

		LauncherAgent robinson = new LauncherAgent("robinson",
				new Personality(1, 0, 0.5, -0.5, 0));

		ImmutableList<LauncherAgent> agents = ImmutableList.of(robinson);


		IslandModel model = new IslandModel(agents, happeningDirector);

		robinson.location = model.civilizedWorld.name;

		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "islandAgent");
		runner.run();
	}
}
