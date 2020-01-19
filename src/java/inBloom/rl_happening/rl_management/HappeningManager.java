/**
 * 
 */
package inBloom.rl_happening.rl_management;

import inBloom.rl_happening.happenings.FireHappening;
import inBloom.rl_happening.happenings.FoodPoisoningHappening;
import inBloom.rl_happening.happenings.FoodStolenHappening;
import inBloom.rl_happening.happenings.HomesickHappening;
import inBloom.rl_happening.happenings.LooseFriendHappening;
import inBloom.rl_happening.happenings.ShipRescueHappening;
import inBloom.rl_happening.happenings.StormHappening;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.ScheduledHappeningDirector;

/**
 * @author Julia Wippermann
 * @version 9.1.20
 *
 */
public class HappeningManager {
	
	public static ScheduledHappeningDirector createHappeningDirector() {
		return new ScheduledHappeningDirector();
	}
	
	public static void scheduleHappenings(ScheduledHappeningDirector hapDir) {
		
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
	}

}
