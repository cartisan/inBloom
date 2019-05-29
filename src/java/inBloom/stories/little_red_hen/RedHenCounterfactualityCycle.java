package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.ERcycle.PersonalitySpaceSearchCycle;
import inBloom.ERcycle.PlotCycle;
import inBloom.ERcycle.PlotCycle.EngageResult;
import inBloom.ERcycle.PlotCycle.ReflectResult;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

/**The class provides the possibility to find a counterfactual graph to
 * a current given graph. Like the PersonalitySpaceSearchCycle it tries
 * to find all possible values, but it does that only for a list of given characters
 * (in the ideal case only the main character)
 * @author Julia Kaltenborn
 *
 */
public class RedHenCounterfactualityCycle extends PlotCycle {
	
	/** set of domain-specific happenings allowed to be scheduled by the ER Cycle */
	public HashSet<Class<?>> availableHappenings = new HashSet<>();
	private double lower = -1;
	private double upper = 1;
	private double step = 1;
	/**
	 * The personalities corresponding to the
	 * best counterfactuality score.
	 */
	protected Personality[] bestPersonalities = null;
	
	/**
	 * The list of personalities for all cycles to run.
	 */
	protected List<Personality[]> personalityList;
	/**
	 * An iterator used to iterate over the personalities.
	 */
	protected Iterator<Personality[]> personalityIterator;
	
	/**
	 * The personalities of the last cycle.
	 */
	protected Personality[] lastPersonalities;
	/**
	 * The launcher of the last cycle.
	 */
	protected PlotLauncher<?,?> lastRunner;
	/**
	 * The best tellability score.
	 */
	protected double bestTellability = -1f;
	
	public RedHenCounterfactualityCycle() {
		//red hen specific
		super(new String[] {"hen", "cow", "dog", "pig"}, "agent");
		this.availableHappenings.add(FindCornHappening.class);
		//calculate all possible personalities
		double[] personalityValues = this.calcAllPersonalityValues();
		//log(Arrays.toString(personalityValues));
		Personality[] personalitySpace = createPersonalitySpace(personalityValues);
		//TODO second parameter will change after changing the method!
		this.personalityList = createPlotSpace(personalitySpace, agentNames.length);	
		this.personalityIterator = personalityList.iterator();	
	}
	/**
	 * one can choose in which range the personalities should be choosen
	 * and in which steps, otherwise default values are used,
	 * -1.0 for lower, +1.0 for upper and 1 as step
	 * @param lower
	 * @param upper
	 * @param step
	 * TODO if lower or upper is not [-1.0 , +1.0] or step > 2 -> throw exception
	 */
	public void setPersonalityRange(double lower, double upper, double step) {
		this.lower = lower;
		this.upper = upper;
		this.step = step;
	}
	
	/**
	 * @return all possible values one aspect of a personality can have
	 */
	public double[] calcAllPersonalityValues() {
		ArrayList<Double> allValues = new ArrayList<Double>();
		//TODO think about using BigDecimal for preventing rounding errors
		for(double i = this.lower; i <= this.upper; i += step) {
			allValues.add(i);
		}
		return allValues.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	/**
	 * creates all possible personality values of the complete personality
	 * @param posVal -> all possible values of one personality aspect
	 * @return -> all possible values of the complete personality
	 */
	protected Personality[] createPersonalitySpace(double[] posVal){
		List<Personality> personalities = new LinkedList<Personality>();
		List<int[]> values = allCombinations(5, posVal.length, true);
		for(int[] ocean : values) {
			personalities.add(new Personality(
					posVal[ocean[0]],
					posVal[ocean[1]], 
					posVal[ocean[2]],
					posVal[ocean[3]],
					posVal[ocean[4]]));
		}
		Personality[] result = new Personality[personalities.size()];
		return personalities.toArray(result);
	}
	
	private List<int[]> allCombinations(int n, int k, boolean repeat) {
		List<int[]> res = new ArrayList<int[]>((int)Math.pow(k, n));
		allCombinations(new int[n], k, res, 0, 0, repeat);
		return res;
	}
	
	private void allCombinations(int[] v, int k, List<int[]> result, int index, int min, boolean repeat) {
		if(index == v.length) {
			result.add(v);
			return;
		}
		for(int i = min; i < k; i++) {
			v[index] = i;
			allCombinations(v.clone(), k, result, index + 1, repeat ? min : i, repeat);
		}
	}
	
	protected List<LauncherAgent> createAgents(Personality[] personalities){
		if(personalities.length != this.agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i], personalities[i]));
		}
		return agents;
	}
	
	public List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters) {
		//we only want the personality of the protagonist to change, but no other personalities
		boolean repeat = false;
		//TODO red hen specific
		// find a good possibility to construct this method -> agent name of protagonist given, etc
		List<Personality[]> allPersonalityCombinations = new LinkedList<Personality[]>();
		
		if(!repeat) {
			Personality dog = new Personality(0, -1, 0, -0.7, -0.8);
			Personality cow = new Personality(0, -1, 0, -0.7, -0.8);
			Personality pig = new Personality(0, -1, 0, -0.7, -0.8);
			for(Personality pers : personalitySpace) {
				Personality[] persAdd = {pers, dog, cow, pig};
				allPersonalityCombinations.add(persAdd);
			}
			log("The Protagonist's Personalities were created for PlotSpace");
			
		} else {	
			log("I start creating the plot space");
			List<int[]> values = allCombinations(characters, personalitySpace.length, repeat);
			log("I have calculated all Combinations");
			log("Now I start the for loop");
			for(int[] charPersonalities : values) {
				Personality[] personalityArray = new Personality[characters];
				for(int i = 0; i < characters; i++) {
					personalityArray[i] = personalitySpace[charPersonalities[i]];
				}
				allPersonalityCombinations.add(personalityArray);
			}
			log("All personalities calculated for plot space");
		}
		return allPersonalityCombinations;
	}
	
	//red hen specific, overriding same method in plotcycle
	protected List<LauncherAgent> createAgs(String[] agentNames, Personality[] personalities) {
		if(personalities.length != agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i],
					//these two lines are red-hen specific
					Arrays.asList("hungry", "self(farm_animal)"),
			    	new LinkedList<String>(), personalities[i]));
		}
		return agents;
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("I am reflecting");
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		log(" Current Tellability: " + currTellability);
		// Save tellability, graph and hen personality if it was better than the best before
		if(currTellability > bestTellability) {
			bestTellability = currTellability;
			log("New best: " + bestTellability);
			bestPersonalities = lastPersonalities;
			showGraph(er);
		}
		log("Best Tellability So Far: " + bestTellability);
		// Stop cycle if there are no other personality combinations
		if(!personalityIterator.hasNext() || currentCycle >= 72/**|| currentCycle >= endCycle**/) {
			return new ReflectResult(null, null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		log("Next Personalities: ");
		for (Personality pers : lastPersonalities) {
			log("\t" + pers.toString());
		}
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		
		List<LauncherAgent> agents = createAgs(this.agentNames, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		
		//FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), er.getLastModel().happeningDirector.clone());
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), getHappeningDirector(agents));
		return new ReflectResult(lastRunner, model, agents);
	}


	@Override
	protected ReflectResult createInitialReflectResult() {
		lastPersonalities = personalityIterator.next();
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);	
		List<LauncherAgent> agents = createAgs(this.agentNames,new Personality[] {new Personality(-1.0, 1.0, 1.0, -1.0,  -1.0), lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});			
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(agents), getHappeningDirector(agents));
		ReflectResult rr = new ReflectResult(runner, model, agents);
		log("Cycle " + currentCycle);
		return rr;
	}
	
	@Override
	protected void finish(EngageResult er) {
		// Print results
		log("Best tellability: " + bestTellability);
		log("Personalities:");
		for(Personality p : bestPersonalities) {
			log("\t" + p.toString());
		}
		
		// flush and close handled by super implementation
		super.finish(er);
	}
	
	protected void showGraph(EngageResult er) {
		log("Displaying resulting story...");
		
		PlotGraphController graphViewer = new PlotGraphController();
		for (PlotDirectedSparseGraph graph : stories) {
			graphViewer.addGraph(graph);
			graphViewer.setSelectedGraph(graph);
		}
		
		// for last graph
		for (FunctionalUnit fu : er.getTellability().plotUnitTypes) {
			graphViewer.addDetectedPlotUnitType(fu);
		}
		graphViewer.addInformation("#Functional Units: " + er.getTellability().numFunctionalUnits);
		graphViewer.addInformation("Highlight Units:", graphViewer.getUnitComboBox());
		graphViewer.addInformation("#Polyvalent Vertices: " + er.getTellability().numPolyvalentVertices);
		graphViewer.addInformation("Suspense: " + er.getTellability().suspense);
		graphViewer.addInformation("Tellability: " + er.getTellability().compute());
		
		graphViewer.visualizeGraph();
	}
	
	// added by Julia
	// currently there are no happenings
	// this method should solve the problem:
	private ScheduledHappeningDirector getHappeningDirector(List<LauncherAgent> agents) {
		// TODO that in createAgs
		for (LauncherAgent agent : agents) {
			agent.location = FarmModel.FARM.name;
		}
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 2 farm work actions
				(FarmModel model) -> {
	            		if(model.FARM.farmingProgress > 2) {
	            			return true;
	            		}
	            		return false; 
	    		},
				"hen",
				"farmingProgress");
		hapDir.scheduleHappening(findCorn);
		return hapDir;
	}
	
	public static void main(String[] args) {
		PersonalitySpaceSearchCycle.main(args);
		RedHenCounterfactualityCycle cycle = new RedHenCounterfactualityCycle();
		cycle.run();
	}
	
}
