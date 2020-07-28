/**
 * 
 */
package inBloom.rl_happening.archive;

import java.util.LinkedList;

import inBloom.rl_happening.happenings.*;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.rl_happening.rl_management.AutomatedHappeningDirector;
import inBloom.rl_happening.rl_management.SarsaLambda;
import inBloom.storyworld.Happening;

/**
 * @author Julia Wippermann
 * @version 9.1.20
 *
 */
public class HappeningManagerDEPRECATED {
	
	// TODO evtl. zu ConditionalHappening ändern
	private static LinkedList<Happening<?>> allHappenings;

	
	// create and add all Happenings when this class is created -> not only when scheduleHappenings is called
	// because that only happens when RobinsinCycle.run() and we already need to know the Happenings for the
	// initialisation of Sarsa(lambda) (and therefore the initialisation of RTobinsonCycle)
	static {
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
		
		EmptyHappening empty = new EmptyHappening(
				(IslandModel model) -> {
					// never triggered
					return false;
				},
				"robinson",
				null
		);
		
				
//		hapDir.scheduleHappening(shipWrecked);
//		hapDir.scheduleHappening(foodStolen);
//		hapDir.scheduleHappening(foodPoisoning);
//		hapDir.scheduleHappening(friendIsEaten);
//		hapDir.scheduleHappening(hutDestroyed);
//		hapDir.scheduleHappening(homesick);
//		hapDir.scheduleHappening(fire);
//		hapDir.scheduleHappening(shipRescue);
		
		allHappenings = new LinkedList<Happening<?>>();
		allHappenings.add(empty);
		allHappenings.add(shipWrecked);
		allHappenings.add(foodStolen);
		allHappenings.add(foodPoisoning);
		allHappenings.add(friendIsEaten);
		allHappenings.add(hutDestroyed);
		allHappenings.add(homesick);
		allHappenings.add(fire);
		allHappenings.add(shipRescue);
	}
	
	public static AutomatedHappeningDirector createHappeningDirector(SarsaLambda sarsa) {
		return new AutomatedHappeningDirector(sarsa);
	}
	
//	public static void scheduleHappenings(AutomatedHappeningDirector hapDir) {
//		
//		StormHappening shipWrecked = new StormHappening(
//				// wenn du das Model model bekommst, mache dies damit
//				(IslandModel model) -> {
//					// if anyone is on the ship
//					// -> then true, aka the happening is triggered
//					// TODO does it make more sense to trigger it anyways, bc effect is defined in happening
//					// should be able to deal with noone being on there. BUT: Will be triggered differently
//					// anyways. Don't overthink this too much.
//					if(!model.ship.getCharacters().isEmpty()) {
//					//if(model.isOnCruise) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				// TODO how do I get the model in here???
//				//model.ship.getCharacters().get(0).name,
//				null
//		);	// causal property
//		
//		FoodStolenHappening foodStolen = new FoodStolenHappening(
//				(IslandModel model) -> {
//					// triggered when robinson has food
//					// TODO not hardcoded to robinson
//					if(model.getCharacter("robinson").has("food") && model.getStep() > 9) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		FoodPoisoningHappening foodPoisoning = new FoodPoisoningHappening(
//				(IslandModel model) -> {
//					// triggered when robinson has food
//					// TODO not hardcoded to robinson
//					if(model.getCharacter("robinson").has("food")) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		LooseFriendHappening friendIsEaten = new LooseFriendHappening(
//				(IslandModel model) -> {
//					// triggered when robinson has food
//					// TODO not hardcoded to robinson
//					if(model.getNumberOfFriends("robinson") > 0 && model.getStep() > 8) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		StormHappening hutDestroyed = new StormHappening(
//				(IslandModel model) -> {
//					if(model.island.hasHut()) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		HomesickHappening homesick = new HomesickHappening(
//				(IslandModel model) -> {
//					if(model.getStep() > 18) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		FireHappening fire = new FireHappening(
//				(IslandModel model) -> {
//					if(model.getStep() > 25) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//		ShipRescueHappening shipRescue = new ShipRescueHappening(
//				(IslandModel model) -> {
//					// triggered when robinson / someone is on island and story has progressed enough
//					// TODO not hardcoded to robinson
//					//if(model.island.contains("robinson")) {
//					if(!model.island.getCharacters().isEmpty() && model.getStep() > 29) {
//						return true;
//					}
//					return false;
//				},
//				"robinson",
//				null
//		);
//		
//				
////		hapDir.scheduleHappening(shipWrecked);
////		hapDir.scheduleHappening(foodStolen);
////		hapDir.scheduleHappening(foodPoisoning);
////		hapDir.scheduleHappening(friendIsEaten);
////		hapDir.scheduleHappening(hutDestroyed);
////		hapDir.scheduleHappening(homesick);
////		hapDir.scheduleHappening(fire);
////		hapDir.scheduleHappening(shipRescue);
//		
//		allHappenings.add(shipWrecked);
//		allHappenings.add(foodStolen);
//		allHappenings.add(foodPoisoning);
//		allHappenings.add(friendIsEaten);
//		allHappenings.add(hutDestroyed);
//		allHappenings.add(homesick);
//		allHappenings.add(fire);
//		allHappenings.add(shipRescue);
//		
//	}
	
	// TODO Beware that allHappenings are only declared after scheduleHappenings has been called.
	// This should be okay since scheduleHappenings should be called at the very beginning of each
	// run, but I am not 100% sure.
	public static LinkedList<Happening<?>> getAllHappenings() {
		return allHappenings;
	}

}
