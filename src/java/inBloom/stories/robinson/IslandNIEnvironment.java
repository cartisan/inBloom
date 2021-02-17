package inBloom.stories.robinson;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.nia.NIEnvironment;
import inBloom.stories.robinson.happenings.FireHappening;
import inBloom.stories.robinson.happenings.FoodPoisoningHappening;
import inBloom.stories.robinson.happenings.FoodStolenHappening;
import inBloom.stories.robinson.happenings.HomesickHappening;
import inBloom.stories.robinson.happenings.LooseFriendHappening;
import inBloom.stories.robinson.happenings.PlantDiseaseHappening;
import inBloom.stories.robinson.happenings.ShipRescueHappening;
import inBloom.stories.robinson.happenings.StormHappening;
import inBloom.stories.robinson.happenings.TorrentialRainHappening;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

public class IslandNIEnvironment extends NIEnvironment<IslandEnvironment, IslandModel>{
	
	public IslandNIEnvironment() {
		super(IslandEnvironment.class, "islandAgent", 1, 1, 9);
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
	public ImmutableList<Happening<?>> init_staticHappenings(List<LauncherAgent> agents) {
		
		List<Happening<?>> happeningList = new ArrayList<Happening<?>>();
		
		
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
		
		happeningList.add(shipWrecked);
		
		ImmutableList<Happening<?>> immutable = ImmutableList.copyOf(happeningList);
		
		return immutable;
		
	}
	
	/*
	 * Defines dynamic happenings
	 * The point in time and patient of a happening will be handed over from the GA
	 */
	
	public Happening<?> init_dynamicHappening(LauncherAgent agent, int happeningIndex, int step) {		
		
		switch(happeningIndex) {
		
		case(0):
			
			FoodPoisoningHappening foodPoisoning = new FoodPoisoningHappening(
				
				(IslandModel model) -> {
					if(model.getCharacter(agent.name).has("food") && model.getStep() > step) {
						return true;
					}
					return false;
				},
				agent.name,
				null
				);
			return foodPoisoning;
		
		case(1):
			
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
			
		case(4):
		
		StormHappening hutDestroyed = new StormHappening(
				
				(IslandModel model) -> {
					if(model.island.hasHut() && model.getStep() > step) {
						return true;
					}
					return false;
				},
				agent.name,
				null
				);
			return hutDestroyed;

		case(5):
			
			TorrentialRainHappening rain = new TorrentialRainHappening(
					(IslandModel model) -> {
						if(!model.island.isRaining() && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);		
			return rain;
			
		case(6):
			
			FireHappening fire = new FireHappening(
					(IslandModel model) -> {
						if(!model.island.isBurning() && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);		
			return fire;
		
		case(7):
			
			PlantDiseaseHappening plantDisease = new PlantDiseaseHappening(
					(IslandModel model) -> {
						if(model.island.hasHealingPlants() && model.getStep() > step) {
							return true;
						}
						return false;
					},
					agent.name,
					null
			);		
			return plantDisease;
			
		case(8):
			
			ShipRescueHappening shipRescue = new ShipRescueHappening(
					(IslandModel model) -> {
						if(!model.island.getCharacters().isEmpty() && model.getStep()>5 && model.getStep() > step) {
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
	public PlotModel<IslandEnvironment> init_model(List<LauncherAgent> agents, ScheduledHappeningDirector hapDir) {
		
		IslandModel model = new IslandModel(agents, hapDir);
		
		return model;
	}
	
	
	@Override
	public void init_location(List<LauncherAgent> agents, PlotModel<?> model) {
		
		IslandModel island = (IslandModel) model;
		agents.get(0).location = island.civilizedWorld.name;
		
	}
}
