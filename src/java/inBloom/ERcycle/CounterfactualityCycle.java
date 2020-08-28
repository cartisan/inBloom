package inBloom.ERcycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jason.asSemantics.Personality;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.helper.Counterfactuality;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;

@SuppressWarnings("all")
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

	private String[] agentNames;

	/**
	 * Constructor of CounterfactualityCycle, must be called by
	 * a subclass in order to get all necessary information.
	 */
	public CounterfactualityCycle(String agentSource, String[] agentNames, PlotDirectedSparseGraph originalGraph, MoodMapper originalMood, String counterAgent) {
		super(agentSource, true);
		this.agentNames = agentNames;
		this.originalGraph = originalGraph;
		this.originalMood = originalMood;
		this.counterAgent = counterAgent;
		//calculate all possible personalities
		double[] personalityValues = this.calcAllPersonalityValues();
		Personality[] personalitySpace = this.createPersonalitySpace(personalityValues);
		//createPlotSpace must be implemented by subclass
		this.personalityList = this.createPlotSpace(personalitySpace, agentNames.length);
		this.personalityIterator = this.personalityList.iterator();
	}

	/**
	 * @return all possible values one aspect of a personality can have
	 */
	public double[] calcAllPersonalityValues() {
		ArrayList<Double> allValues = new ArrayList<>();
		for(double i = this.lowerPersonalityValue; i <= this.upperPersonalityValue; i += this.stepPersonalityValue) {
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
		List<Personality> personalities = new LinkedList<>();
		List<int[]> values = this.allCombinations(5, posVal.length, true);
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
		List<int[]> res = new ArrayList<>((int)Math.pow(k, n));
		this.allCombinations(new int[n], k, res, 0, 0, repeat);
		return res;
	}

	private void allCombinations(int[] v, int k, List<int[]> result, int index, int min, boolean repeat) {
		if(index == v.length) {
			result.add(v);
			return;
		}
		for(int i = min; i < k; i++) {
			v[index] = i;
			this.allCombinations(v.clone(), k, result, index + 1, repeat ? min : i, repeat);
		}
	}

	@Override
	protected ReflectResult reflect(EngageResult erOriginal) {
		CounterfactualityEngageResult er = (CounterfactualityEngageResult) erOriginal;
		this.log("I am reflecting");
		// TELLABILITY
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		this.log(" Current Tellability: " + currTellability);
		// Save tellability, graph and agent's personality if it was better than the best before
		if(currTellability > this.bestTellability) {
			this.bestTellability = currTellability;
			this.log("New best Tellability: " + this.bestTellability);
			this.bestPersonalities = this.lastPersonalities;
			this.bestResult = er;

		}
		this.log("Best Tellability So Far: " + this.bestTellability);


		// COUNTERFACTUALITY
		Counterfactuality counterfactuality = er.getCounterfactuality();
		double currCounterfactuality = counterfactuality.compute();
		this.log(" Current Counterfactuality: " + currCounterfactuality);
		if(currCounterfactuality > this.bestCounterfactuality) {
			this.bestCounterfactuality = currCounterfactuality;
			this.log("New best counterfactuality: " + this.bestCounterfactuality);
			this.bestPersonalities = this.lastPersonalities;
			this.bestResult = er;
		}
		this.log("Best Counterfactuality So Far: " + this.bestCounterfactuality);

		// Stop cycle if there are no other personality combinations
		if(!this.personalityIterator.hasNext() || currentCycle >= this.endCycle) {
			return new ReflectResult(null, null, null, false);
		}

		// Start the next cycle
		this.lastPersonalities = this.personalityIterator.next();
		this.log("Next Personalities: ");
		for (Personality pers : this.lastPersonalities) {
			this.log("\t" + pers.toString());
		}

		this.lastRunner = this.getPlotLauncher();
		this.lastRunner.setShowGui(false);

		List<LauncherAgent> agents = this.createAgs(this.agentNames, new Personality[] {this.lastPersonalities[0], this.lastPersonalities[1], this.lastPersonalities[1], this.lastPersonalities[1]});

		PlotModel<?> model = this.getPlotModel(agents);
		return new ReflectResult(this.lastRunner, model, agents);
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		this.log("Creating initial Reflect Results");
		this.lastPersonalities = this.personalityIterator.next();
		//this kills the programm
		PlotLauncher<?, ?> runner = this.getPlotLauncher();
		runner.setShowGui(false);
		List<LauncherAgent> agents = this.createAgs(this.agentNames,new Personality[] {new Personality(0, 0, 0, 0, 0), this.lastPersonalities[1], this.lastPersonalities[1], this.lastPersonalities[1]});
		PlotModel<?> model = this.getPlotModel(agents);
		ReflectResult rr = new ReflectResult(runner, model, agents);
		this.log("Cycle " + currentCycle);
		return rr;
	}

	@Override
	protected void finish(EngageResult erOriginal) {
		CounterfactualityEngageResult er = (CounterfactualityEngageResult) erOriginal;
		// Print results
		this.log("Best tellability: " + this.bestTellability);
		this.log("Personalities:");
		for(Personality p : this.bestPersonalities) {
			this.log("\t" + p.toString());
		}
		this.showGraph(this.bestResult);
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
		this.log("Displaying resulting story...");

		PlotGraphController graphViewer = new PlotGraphController();
		for (PlotDirectedSparseGraph graph : this.stories) {
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
		graphViewer.addInformation("Suspense: " + er.getTellability().absoluteSuspense);
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

		List<LauncherAgent> agents = this.createPlotAgs(agentNames, personalities);

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
