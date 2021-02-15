package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jason.asSemantics.Personality;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.evo.NIEnvironment;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;

public class FarmNIEnvironment extends NIEnvironment<FarmEnvironment, FarmModel> {

	public FarmNIEnvironment() {
		//    env class		agent.asl	chars	static-haps		dynamic haps
		super(FarmEnvironment.class, "agent_folktale_animal", 4, 0, 1);
	}

	@Override
	public List<LauncherAgent> init_agents(double[][] personalityArry) {
		List<LauncherAgent> charList = new ArrayList<>();

		int index = 0;
		List<String> nameList = Arrays.asList("hen", "dog", "pig", "cow");
		for(String name : nameList) {
			Personality personality = new Personality(personalityArry[index][0], personalityArry[index][1], personalityArry[index][2], personalityArry[index][3], personalityArry[index][4]);
			charList.add(new LauncherAgent(name, Arrays.asList("hungry", "self(farm_animal)"), new LinkedList<String>(), personality));
			index += 1;
		}

		return charList;
	}

	@Override
	public List<Happening> init_staticHappenings(List<LauncherAgent> agents) {
		// no static happenings
		List<Happening> happeningList = new ArrayList<>();
		return happeningList;
	}

	@Override
	public Happening init_dynamicHappening(LauncherAgent agent, int happeningIndex, int step) {
		switch(happeningIndex) {
			case 0:
				FindCornHappening findCorn = new FindCornHappening(
						// patient finds wheat after at last two farm work actions
						(FarmModel model) -> {
			            		if(model.farm.farmingProgress > 1) {
			            			return true;
			            		}
			            		return false;
			    		},
						agent.name,
						"farmingProgress");
				return findCorn;
		}

		return null;
	}

	@Override
	public PlotModel<FarmEnvironment> init_model(List<LauncherAgent> agents, ScheduledHappeningDirector hapDir) {
		FarmModel model = new FarmModel(agents, hapDir);
		return model;
	}

	@Override
	public void init_location(List<LauncherAgent> agents, PlotModel model) {
		String locName = ((FarmModel) model).farm.name;

		for (LauncherAgent ag : agents) {
			ag.location = locName;
		}
	}

}
