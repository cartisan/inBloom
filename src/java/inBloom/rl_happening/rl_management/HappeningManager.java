/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.LinkedList;

import inBloom.rl_happening.happenings.*;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Happening;

/**
 * @author Julia Wippermann
 * @version 9.1.20
 *
 */
public class HappeningManager {
	
	// TODO evtl. zu ConditionalHappening ändern
	private static LinkedList<Happening<?>> allHappenings;

	
	/* WHY STATIC?
	 * create and add all Happenings when this class is created -> not only when scheduleHappenings is called
	 * because that only happens when RobinsinCycle.run() and we already need to know the Happenings for the
	 * initialisation of Sarsa(lambda) (and therefore the initialisation of RTobinsonCycle)
	 */
	
	/* WHY THESE ARGUMENTS FOR THE CONSTRUCTORS?
	 * trigger = null, because Happenings are not triggered anymore, but introdcued by SarsaLambda
	 * patient = "robinson", hardcoded
	 * causalProperty = null, because there is no reason / nothing that triggered it. We just. felt. like it.
	 */
	
	static {
		
		// create all Happenings
		
		StormHappening shipWrecked = new StormHappening(null, "robinson", null);		
		FoodStolenHappening foodStolen = new FoodStolenHappening(null,"robinson", null);
		FoodPoisoningHappening foodPoisoning = new FoodPoisoningHappening(null,"robinson", null);
		LooseFriendHappening friendIsEaten = new LooseFriendHappening(null,"robinson", null);
		StormHappening hutDestroyed = new StormHappening(null,"robinson", null);
		HomesickHappening homesick = new HomesickHappening(null,"robinson", null);
		FireHappening fire = new FireHappening(null,"robinson", null);
		ShipRescueHappening shipRescue = new ShipRescueHappening(null,"robinson", null);
		EmptyHappening empty = new EmptyHappening(null,"robinson", null);
		
		// save all Happenings
		
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
	
	public static AutomatedHappeningDirector<IslandModel> createHappeningDirector(SarsaLambda sarsa) {
		return new AutomatedHappeningDirector<IslandModel>(sarsa);
	}
	

	
	// TODO Beware that allHappenings are only declared after scheduleHappenings has been called.
	// This should be okay since scheduleHappenings should be called at the very beginning of each
	// run, but I am not 100% sure.
	public static LinkedList<Happening<?>> getAllHappenings() {
		return allHappenings;
	}

}
