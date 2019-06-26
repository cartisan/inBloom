package inBloom.ERcycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.data.xy.XYSeriesCollection;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.helper.Counterfactuality;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;
import jason.asSemantics.Personality;

public abstract class CounterfactualityCycle extends PlotCycle {
	
	/**
	 * The last cycle to run. If Infinity -> all possible cycles will be run.
	 * Should be overridden by subclass if helpful.
	 */
	protected double endCycle = Double.POSITIVE_INFINITY;
	/**
	 * Defines the lower end of the possible personality values.
	 * Can be overridden by subclass.
	 */
	protected double lowerPersonalityValue = -1;
	/**
	 * Defines the upper end of the possible personality values.
	 * Can be overridden by subclass.
	 */
	protected double upperPersonalityValue = 1;
	/**
	 * Defines the difference/step between the possible personality values.
	 * Can be overridden by subclass.
	 */
	protected double stepPersonalityValue = 1;
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
	/**
	 * The best counterfactuality score.
	 */
	protected double bestCounterfactuality = -1f;
	/**
	 * The best Engage Result.
	 */
	protected EngageResult bestResult;
	/** 
	 * set of domain-specific happenings allowed to be scheduled by the ER Cycle 
	 */
	public HashSet<Class<?>> availableHappenings = new HashSet<>();
	/**
	 * Original Graph.
	 */
	private PlotDirectedSparseGraph originalGraph;
	/**
	 * Original Mood Data.
	 */
	private MoodMapper originalMood;
	/**
	 * Name of the Agent for whom a counterfactual story should be found.
	 */
	private String counterAgent;

	/**
	 * Constructor of CounterfactualityCycle, must be called by
	 * a subclass in order to get all necessary information.
	 */
	public CounterfactualityCycle(String agentSource, String[] agentNames, PlotDirectedSparseGraph originalGraph, MoodMapper originalMood, String counterAgent) {
		super(agentNames, agentSource, true, counterAgent, originalMood);
		this.originalGraph = originalGraph;
		this.originalMood = originalMood;
		this.counterAgent = counterAgent;
		//calculate all possible personalities
		double[] personalityValues = this.calcAllPersonalityValues();
		Personality[] personalitySpace = createPersonalitySpace(personalityValues);
		//createPlotSpace must be implemented by subclass
		this.personalityList = createPlotSpace(personalitySpace, agentNames.length);
		this.personalityIterator = personalityList.iterator();	
	}
	
	/**
	 * @return all possible values one aspect of a personality can have
	 */
	public double[] calcAllPersonalityValues() {
		ArrayList<Double> allValues = new ArrayList<Double>();
		for(double i = this.lowerPersonalityValue; i <= this.upperPersonalityValue; i += stepPersonalityValue) {
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
	
	public List<int[]> allCombinations(int n, int k, boolean repeat) {
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

	@Override
	protected ReflectResult reflect(EngageResult erOriginal) {
		CounterfactualityEngageResult er = (CounterfactualityEngageResult) erOriginal;
		log("I am reflecting");
		// TELLABILITY
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		log(" Current Tellability: " + currTellability);
		// Save tellability, graph and agent's personality if it was better than the best before
		if(currTellability > bestTellability) {
			bestTellability = currTellability;
			log("New best Tellability: " + bestTellability);
			bestPersonalities = lastPersonalities;
			bestResult = er;
			
		}
		log("Best Tellability So Far: " + bestTellability);
		
		
		// COUNTERFACTUALITY
		Counterfactuality counterfactuality = er.getCounterfactuality();
		double currCounterfactuality = counterfactuality.compute();
		log(" Current Counterfactuality: " + currCounterfactuality);
		if(currCounterfactuality > bestCounterfactuality) {
			bestCounterfactuality = currCounterfactuality;
			log("New best counterfactuality: " + bestCounterfactuality);
			bestPersonalities = lastPersonalities;
			bestResult = er;
		}
		log("Best Counterfactuality So Far: " + bestCounterfactuality);
		
		// Stop cycle if there are no other personality combinations
		if(!personalityIterator.hasNext() || currentCycle >= endCycle) {
			return new ReflectResult(null, null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		log("Next Personalities: ");
		for (Personality pers : lastPersonalities) {
			log("\t" + pers.toString());
		}
		
		lastRunner = getPlotLauncher();
		lastRunner.setShowGui(false);
		
		List<LauncherAgent> agents = createAgs(this.agentNames, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});

		PlotModel<?> model = getPlotModel(agents);
		return new ReflectResult(lastRunner, model, agents);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		log("Creating initial Reflect Results");
		lastPersonalities = personalityIterator.next();
		//this kills the programm
		PlotLauncher<?, ?> runner = getPlotLauncher();
		runner.setShowGui(false);	
		List<LauncherAgent> agents = createAgs(this.agentNames,new Personality[] {new Personality(0, 0, 0, 0, 0), lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});			
		PlotModel<?> model = getPlotModel(agents);
		ReflectResult rr = new ReflectResult(runner, model, agents);
		log("Cycle " + currentCycle);
		return rr;
	}
	
	@Override
	protected void finish(EngageResult erOriginal) {
		CounterfactualityEngageResult er = (CounterfactualityEngageResult) erOriginal;
		// Print results
		log("Best tellability: " + bestTellability);
		log("Personalities:");
		for(Personality p : bestPersonalities) {
			log("\t" + p.toString());
		}
		showGraph(bestResult);
		// flush and close handled by super implementation
		super.finish(er);
	}
	
	@Override
	protected EngageResult createEngageResult(ReflectResult rr, PlotLauncher<?, ?> runner,
			PlotDirectedSparseGraph analyzedGraph, Tellability tel, MoodMapper moodData) {
		Counterfactuality counterfactuality = new Counterfactuality(this.originalMood, moodData, tel.compute(), this.counterAgent);
		EngageResult er;
		er = new CounterfactualityEngageResult(counterfactuality,		   
									   analyzedGraph,
									   tel,
									   rr.getAgents(),
									   runner.getUserModel(),
									   moodData);
		return er;
	}
	
	// TODO modify background colours etc.
	// User can choose between different counterfactual stories to display
	// changing colour for each graph
	/**
	 * Shows the Graph of a given EngageResult. All Graphs are added,
	 * but only the best Graph is selected.
	 * @param er - the bestResult from the Cycle Results
	 */
	protected void showGraph(EngageResult er) {
		log("Displaying resulting story...");
		
		PlotGraphController graphViewer = new PlotGraphController();
		for (PlotDirectedSparseGraph graph : stories) {
			graphViewer.addGraph(graph);
		}

		PlotDirectedSparseGraph bestGraph = er.getPlotGraph();
		graphViewer.addGraph(bestGraph);
		graphViewer.setSelectedGraph(bestGraph);
		
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
	
	/**
	 * Is overridding the same method in a subclass, since we need a story specific
	 * individualisation when creating the Agents. Standard code is performed here,
	 * Specification is done in the subclass (via abstract method).
	 */
	@Override
	protected List<LauncherAgent> createAgs(String[] agentNames, Personality[] personalities) {
		if(personalities.length != agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		
		List<LauncherAgent> agents = createPlotAgs(agentNames, personalities);
		
		return agents;
	}
	
	/**
	 * A subclass must override this method.
	 * Returns a List of LauncherAgent s that get a story specific initialisation.
	 * The initialisation of the launcher agents should be done like in the respective
	 * PlotLauncher of the Story.
	 * @param agentNames - of the agent that should be created
	 * @param personalities - the respective agents should get
	 * @return List of LauncherAgent s that can be used for launching the plot.
	 */
	public abstract List<LauncherAgent> createPlotAgs(String[] agentNames, Personality[] personalities);
	
	/**
	 * A subclass must override this method.
	 * Creates a list of all the possible personalities of all agents occurring in the story.
	 * @param personalitySpace - The values a complete personality can have
	 * @param characters - Number of existing characters in the story
	 * @return list of all possible agents-personality combinations
	 */
	public abstract List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters);
	
	/**
	 * A subclass must override this method.
	 * Returns a new Launcher of the specific story.
	 * @param agents
	 * @return a brand new Launcher of the specific story.
	 */
	public abstract PlotLauncher<?, ?> getPlotLauncher();
	
	/**
	 * A subclass must override this method.
	 * The subclass must take care that the right happenings are set in the model.
	 * The subclass must take care that the agents are set to the right location.
	 * @param agents occurring in the story.
	 * @return the PlotModel of a specific story.
	 */
	public abstract PlotModel<?> getPlotModel(List<LauncherAgent> agents);
	

}
