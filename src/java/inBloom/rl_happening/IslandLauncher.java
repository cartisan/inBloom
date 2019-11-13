/**
 * 
 */
package inBloom.rl_happening;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.rl_happening.happenings.*;
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
					// if anyone is on the ship
					// -> then true, aka the happening is triggered
					// TODO does it make more sense to trigger it anyways, bc effect is defined in happening
					// should be able to deal with noone being on there. BUT: Will be triggered differently
					// anyways. Don't overthink this too much.
					if(!model.ship.getCharacters().isEmpty()) {
					//if(model.isOnCruise) {
						return true;
					}
					return false;
				},
				"robinson",
				// TODO how do I get the model in here???
				//model.ship.getCharacters().get(0).name,
				null
		);	// causal property
				
		
		FoodPoisoningHappening foodPoisoning = new FoodPoisoningHappening(
				(IslandModel model) -> {
					// triggered when robinson has food
					// TODO not hardcoded to robinson
					if(model.getCharacter("robinson").has("food")) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		FoodStolenHappening foodStolen = new FoodStolenHappening(
				(IslandModel model) -> {
					// triggered when robinson has food
					// TODO not hardcoded to robinson
					if(model.getCharacter("robinson").has("food")) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		ShipRescueHappening shipRescue = new ShipRescueHappening(
				(IslandModel model) -> {
					// triggered when robinson / someone is on island and story has progressed enough
					// TODO not hardcoded to robinson
					//if(model.island.contains("robinson")) {
					if(!model.island.getCharacters().isEmpty() && model.getStep() > 10) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		FriendIsEatenHappening friendIsEaten = new FriendIsEatenHappening(
				(IslandModel model) -> {
					// triggered when robinson has food
					// TODO not hardcoded to robinson
					if(model.getNumberOfFriends("robinson") > 0 && model.getStep() > 8) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
				
		hapDir.scheduleHappening(shipWrecked);
		hapDir.scheduleHappening(foodPoisoning);
		//hapDir.scheduleHappening(foodStolen);
		hapDir.scheduleHappening(friendIsEaten);
		hapDir.scheduleHappening(shipRescue);
		
		IslandModel model = new IslandModel(agents, hapDir);
		
		robinson.location = model.civilizedWorld.name;
		
		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "islandAgent");
		runner.run();
	}
}
