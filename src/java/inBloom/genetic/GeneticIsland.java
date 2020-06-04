package inBloom.genetic;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.rl_happening.happenings.FireHappening;
import inBloom.rl_happening.happenings.FoodPoisoningHappening;
import inBloom.rl_happening.happenings.FoodStolenHappening;
import inBloom.rl_happening.happenings.HomesickHappening;
import inBloom.rl_happening.happenings.LooseFriendHappening;
import inBloom.rl_happening.happenings.ShipRescueHappening;
import inBloom.rl_happening.happenings.StormHappening;
import inBloom.rl_happening.islandWorld.IslandEnvironment;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

@SuppressWarnings("rawtypes")
public class GeneticIsland extends GeneticEnvironment<IslandEnvironment, IslandModel>{

	public GeneticIsland() {
		super(IslandEnvironment.class, "islandAgent", 1, 3, 5, 30);
	}
	public GeneticIsland(int pop_size, int number_selections, double crossover_prob, double mutation_prob) {
		super(IslandEnvironment.class, "islandAgent", 1, 3, 5, 30, pop_size, number_selections, crossover_prob, mutation_prob);
	}

	@Override
	public ImmutableList<LauncherAgent> init_agents(double[][] personality) {
		
		Personality robinson_personality = new Personality(personality[0][0], personality[0][1], personality[0][2], personality[0][3], personality[0][4]);
		LauncherAgent robinson = new LauncherAgent("robinson", robinson_personality);
		
		ImmutableList<LauncherAgent> agents = ImmutableList.of(robinson);
		
		return agents;
	}

	/*
	 * All static happenings
	 * Define Happenings that definitely need to happen (e.g. Robinson stranding on an island)
	 * Define triggered Happenings?
	 */
	
	@Override
	public ImmutableList<Happening> init_staticHappenings(ImmutableList<LauncherAgent> agents) {
		
		List<Happening> happeningList = new ArrayList<Happening>();
		
		
		StormHappening shipWrecked = new StormHappening(
				(IslandModel model) -> {
					if(!model.ship.getCharacters().isEmpty()) {
						return true;
					}
					return false;
				},
				"robinson",
				null
		);
		
		FoodPoisoningHappening foodPoisoning = new FoodPoisoningHappening(
				(IslandModel model) -> {
					if(model.getCharacter("robinson").has("food")) {
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
		
		happeningList.add(shipWrecked);
		happeningList.add(foodPoisoning);
		happeningList.add(hutDestroyed);
		
		ImmutableList<Happening> immutable = ImmutableList.copyOf(happeningList);
		
		return immutable;
		
	}
	
	/*
	 * Defines dynamic happenings
	 * The point in time and patient of a happening will be handed over from the GA
	 */
	
	public Happening init_dynamicHappening(LauncherAgent agent, int happeningIndex, int step) {		
		
		switch(happeningIndex) {
		
		case(0):
			
			FoodStolenHappening foodStolen = new FoodStolenHappening(
					(IslandModel model) -> {
						if(model.getCharacter(agent.name).has("food") && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);
			return foodStolen;
		
		case(1):
			
			LooseFriendHappening friendIsEaten = new LooseFriendHappening(
					(IslandModel model) -> {
						if(model.getNumberOfFriends(agent.name) > 0 && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);
			return friendIsEaten;
		
		case(2):
			
			HomesickHappening homesick = new HomesickHappening(
					(IslandModel model) -> {
						if(model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);		
			return homesick;
		
		case(3):
			
			FireHappening fire = new FireHappening(
					(IslandModel model) -> {
						if(model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);		
			return fire;
		
		case(4):
			
			ShipRescueHappening shipRescue = new ShipRescueHappening(
					(IslandModel model) -> {
						if(!model.island.getCharacters().isEmpty() && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);
			return shipRescue;			
		}
		
		return null;
	}

	@Override
	public PlotModel<IslandEnvironment> init_model(ImmutableList<LauncherAgent> agents, ScheduledHappeningDirector hapDir) {
		
		IslandModel model = new IslandModel(agents, hapDir);
		
		return model;
	}
	
	
	@Override
	public void init_location(ImmutableList<LauncherAgent> agents, PlotModel model) {
		
		IslandModel island = (IslandModel) model;
		agents.get(0).location = island.civilizedWorld.name;
		
	}
}
