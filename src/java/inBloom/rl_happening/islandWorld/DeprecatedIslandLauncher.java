/**
 * 
 */
package inBloom.rl_happening.islandWorld;

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
 * @version 20.11.19
 *
 */
public class DeprecatedIslandLauncher extends PlotLauncher<IslandEnvironment, IslandModel> {

	public DeprecatedIslandLauncher() {
		ENV_CLASS = IslandEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {		
		logger.info("Starting up from Launcher");
		
		PlotControlsLauncher.runner = new DeprecatedIslandLauncher();
		
		LauncherAgent robinson = new LauncherAgent("robinson",
				new Personality(1, 0, 0.5, -0.5, 0));
		
		ImmutableList<LauncherAgent> agents = ImmutableList.of(robinson);
		
		// TODO all triggers that are actual preconditions -> f.e. homesickness can only happen after
		// x time steps, need to be implemented in hasEffect() of the relevant Happening
		
		// Initialise MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		
		StormHappening shipWrecked = new StormHappening(
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
		
		FoodStolenHappening foodStolen = new FoodStolenHappening(
				(IslandModel model) -> {
					// triggered when robinson has food
					// TODO not hardcoded to robinson
					if(model.getCharacter("robinson").has("food") && model.getStep() > 9) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
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
		
		LooseFriendHappening friendIsEaten = new LooseFriendHappening(
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
		
		StormHappening hutDestroyed = new StormHappening(
				(IslandModel model) -> {
					if(model.island.hasHut()) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		HomesickHappening homesick = new HomesickHappening(
				(IslandModel model) -> {
					if(model.getStep() > 18) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		FireHappening fire = new FireHappening(
				(IslandModel model) -> {
					if(model.getStep() > 25) {
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
					if(!model.island.getCharacters().isEmpty() && model.getStep() > 29) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
				
		hapDir.scheduleHappening(shipWrecked);
		hapDir.scheduleHappening(foodStolen);
		hapDir.scheduleHappening(foodPoisoning);
		hapDir.scheduleHappening(friendIsEaten);
		hapDir.scheduleHappening(hutDestroyed);
		hapDir.scheduleHappening(homesick);
		hapDir.scheduleHappening(fire);
		hapDir.scheduleHappening(shipRescue);
		
		IslandModel model = new IslandModel(agents, hapDir);
		
		robinson.location = model.civilizedWorld.name;
		
		// Execute MAS
		// HERE IS THE LINK TO THE AGENT.ASL FILE!!!
		runner.initialize(args, model, agents, "islandAgent");
		runner.run();
	}
}
