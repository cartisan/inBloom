/**
 * 
 */
package inBloom.rl_happening;

import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;
import inBloom.storyworld.Character;
import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

/**
 * @author Julia Wippermann
 * @version 29.10.19
 *
 */
public class IslandLauncher extends PlotLauncher<IslandEnvironment, IslandModel> {

	public IslandLauncher() {
		ENV_CLASS = IslandEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {		
		logger.info("Starting up from Launcher");
		
		PlotControlsLauncher.runner = new IslandLauncher();
		
		LauncherAgent robinson = new LauncherAgent("robinson",
				new Personality(1, 0, 0.5, -0.5, 0));
		
		ImmutableList<LauncherAgent> agents = ImmutableList.of(robinson);
		
		// Initialise MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		ShipWreckedHappening shipWrecked = new ShipWreckedHappening(
				// wenn du das Model model bekommst, mache dies damit
				(IslandModel model) -> {
					//List<Character> characters = model.ship.getCharacters();
					//if(!model.ship.getCharacters().isEmpty()) {
					if(model.isOnCruise) {
						return true;
					}
					return false;
				},
				"robinson"
				);	// causal property
				
				
		//hapDir.scheduleHappening(shipWrecked);
		
		IslandModel model = new IslandModel(agents, hapDir);
		
		robinson.location = model.civilizedWorld.name;
		
		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "islandAgent");
		runner.run();
	}
}
