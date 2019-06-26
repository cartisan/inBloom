package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.CounterfactualityCycle;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.helper.MoodMapper;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

public class RedHenCounterfactualityCycle extends CounterfactualityCycle {
	

	/**
	 * If false -> the cycles will run only for changing values of the protagonist's personality.
	 * If true -> the cycles will run for all values of all agents. Attention! That might take hours or days!
	 * (Hence, true is not recommended due to runtime purposes)
	 */
	private boolean allCharacters = false;

	public RedHenCounterfactualityCycle(PlotDirectedSparseGraph originalGraph, MoodMapper originalMood) {
		super("agent", new String[] {"hen", "cow", "dog", "pig"}, originalGraph, originalMood, "hen");
		// add Story-Specific Happening to superclass attribute avaibleHappenings
		this.availableHappenings.add(FindCornHappening.class);
		// outcomment setEndCycle for running through all possibilities
		setEndCycle(10);
		allCharacters = false;
	}
	
	/**
	 * Changes the values calculated for one aspect of the personality.
	 * Might be helpful if after one run it is clearer in which area of personality we should search.
	 * @param upper - the upper limit of one possible value is per default 1
	 * @param lower - the lower limit of one possible value is per default -1
	 * @param step - the difference/step between the possible  values is per default 1. 
	 */
	public void changePersonalityValueCalculation(int upper, int lower, int step) {
		this.upperPersonalityValue = upper;
		this.lowerPersonalityValue = lower;
		this.stepPersonalityValue = step;
		
	}
	
	public void setEndCycle(int lastCycle) {
		this.endCycle = lastCycle;
	}
	
	// TODO remove agents as parameter!!! only not tried here in order to keep changes minimal for later debugging
	
	/**
	 * Manages the Happenings that should occur in the Farm Model
	 * and the timepoint of the Happenings. This should be exactly like in
	 * the normal RedHenLauncher, since the counterfactual story should still
	 * occurr in the same Story Model and Environment!
	 * @param agents that have to deal with
	 * @return
	 */
	private ScheduledHappeningDirector getHappeningDirector(List<LauncherAgent> agents) {
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 2 farm work actions
				(FarmModel model) -> {
	            		if(model.farm.farmingProgress > 2) {
	            			return true;
	            		}
	            		return false; 
	    		},
				"hen",
				"farmingProgress");
		hapDir.scheduleHappening(findCorn);
		return hapDir;
	}
	
	
	@Override
	public List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters) {
		//if you want that all personalities of all protagonists are changed -> set allCharacters to true
		//we only want the personality of the protagonist to change, but no other personalities
		List<Personality[]> allPersonalityCombinations = new LinkedList<Personality[]>();
		if(!allCharacters) {
			log("right choice");
			Personality dog = new Personality(0, -1, 0, -0.7, -0.8);
			Personality cow = new Personality(0, -1, 0, -0.7, -0.8);
			Personality pig = new Personality(0, -1, 0, -0.7, -0.8);
			for(Personality pers : personalitySpace) {
				Personality[] persAdd = {pers, dog, cow, pig};
				allPersonalityCombinations.add(persAdd);
			}
			
		} else {	
			log("I start creating the plot space");
			List<int[]> values = allCombinations(characters, personalitySpace.length, allCharacters);
			log("I have calculated all Combinations");
			log("Now I start the for loop");
			for(int[] charPersonalities : values) {
				Personality[] personalityArray = new Personality[characters];
				for(int i = 0; i < characters; i++) {
					personalityArray[i] = personalitySpace[charPersonalities[i]];
				}
				allPersonalityCombinations.add(personalityArray);
			}
		}
		return allPersonalityCombinations;
	}
	
	@Override
	public List<LauncherAgent> createPlotAgs(String[] agentNames, Personality[] personalities) {
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i],
					Arrays.asList("hungry", "self(farm_animal)"),
			    	new LinkedList<String>(), personalities[i]));
		}
		return agents;
	}

	@Override
	public PlotLauncher<?, ?> getPlotLauncher() {
		return new RedHenLauncher();
	}

	@Override
	public PlotModel<?> getPlotModel(List<LauncherAgent> agents) {
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), getHappeningDirector(agents));
		for (LauncherAgent ag : agents) {
			ag.location = model.farm.name;
		}
		return model;
	}



/*	public static void main(String[] args) {
		RedHenCounterfactualityCycle cycle = new RedHenCounterfactualityCycle();
		cycle.run();
		
	}*/

}
