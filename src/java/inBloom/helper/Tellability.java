package inBloom.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import inBloom.framing.ConnectivityGraph;
import inBloom.graph.CountingVisitor;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.Vertex.Type;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnit.Instance;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;
import inBloom.graph.visitor.EdgeGenerationPPVisitor;
import inBloom.graph.visitor.VertexMergingPPVisitor;

public class Tellability {
	public static int GRAPH_MATCHING_TOLERANCE = 1;

	protected static Logger logger = Logger.getLogger(Tellability.class.getName());

	// plot preconditions
	public CountingVisitor counter;
	public int productiveConflicts = 0;

	// Functional Polyvalence
	public List<FunctionalUnit> plotUnitTypes;
	public int numFunctionalUnits;
	public int numPolyvalentVertices;
	public int numAllVertices;
	public Map<FunctionalUnit, Integer> functionalUnitCount = new HashMap<>();
	public ConnectivityGraph connectivityGraph;

	// Semantic Symmetry
	public double symmetry;
	public int charNum;
	public Collection<Instance> fUinstances = new ArrayList<>();

	// Semantic Opposition

	// Suspense
	public int suspense;
	public int plotLength;



	// Dynamic Points



	/**
	 * Takes an analyzed graph and computes all necessary statistics of the plot to compute tellability.
	 * @param graph whose tellability needs to be determined and that has been been processed by
	 * {@link VertexMergingPPVisitor} and  {@link EdgeGenerationPPVisitor}.
	 */
	public Tellability(PlotDirectedSparseGraph graph) {
		this.counter = new CountingVisitor();
		this.plotUnitTypes = new LinkedList<>();

		// Find Functional Units and polyvalent Vertices
		this.detectPolyvalence(graph);

		// calculate symmetry: intentionally switched off
		 this.detectSymmetry(graph);

		// Perform quantitative analysis of plot
		this.computeSimpleStatistics(graph);
	}

	/**
	 * Computes all statistics that can be determined by counting in a single pass
	 * @param graph a graph that has been processed by both VertexMergingPPVisitor and VisualizationFilterPPVisitor
	 */
	private void computeSimpleStatistics(PlotDirectedSparseGraph graph) {
		this.counter.apply(graph);
		this.productiveConflicts = this.counter.getProductiveConflictNumber();
		this.suspense = this.counter.getSuspense();
		this.plotLength = this.counter.getPlotLength();
		this.numAllVertices = this.counter.getVertexNum();

		this.charNum = graph.getRoots().size();
	}

	/**
	 * Identifies all fUinstances of functional units in the plot graph and detects polyvalent vertices
	 * at their overlap.
	 * @param graph a graph that has been processed by both VertexMergingPPVisitor and VisualizationFilterPPVisitor
	 */
	private void detectPolyvalence(PlotDirectedSparseGraph graph) {
		Map<Vertex, Integer> vertexUnitCount = new HashMap<>();

		UnitFinder finder = new UnitFinder();
		int polyvalentVertices = 0;
		int unitInstances = 0;
		Set<Vertex> polyvalentVertexSet = new HashSet<>();

		this.connectivityGraph = new ConnectivityGraph(graph);

		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			logger.info("Finding units of type: '" + unit.getName() + "'...");
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(unit.getGraph(), graph, GRAPH_MATCHING_TOLERANCE);
			unitInstances += mappings.size();
			this.functionalUnitCount.put(unit, mappings.size());

			if (mappings.size() > 0 ) {
				PlotGraphController.getPlotListener().addDetectedPlotUnitType(unit);
				this.plotUnitTypes.add(unit);
			}

			// maps from FU vertex to plot graph vertex
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = unit.new Instance(graph, map.values(), unit.getName());
				instance.identifySubject(map);
				this.connectivityGraph.addVertex(instance);
				this.fUinstances.add(instance);
				graph.addFUInstance(unit, instance);

				// check for polyvalence
				for(Vertex v : map.values()) {
					if(!vertexUnitCount.containsKey(v)) {
						vertexUnitCount.put(v, 1);
					} else {
						int count = vertexUnitCount.get(v);
						count++;
						if(count == 2) {
							polyvalentVertices++;
							polyvalentVertexSet.add(v);
						}
						vertexUnitCount.put(v, count);
					}
				}
			}
		}

		this.numFunctionalUnits = unitInstances;
		this.numPolyvalentVertices = polyvalentVertices;

		String foundUnits = this.functionalUnitCount.entrySet().stream().filter(entry -> entry.getValue() > 0)
													.map(entry -> entry.getKey().getName() + ": " + entry.getValue())
													.sorted()
													.reduce( (a,b) -> a = a + ", " + b)
													.orElse("<none>");
		logger.info("-> Found units: " + foundUnits);

		// Mark polyvalent vertices with asterisk
		for(Vertex v : polyvalentVertexSet) {
			v.setPolyvalent();
		}

		// identify primitive Units for connectivity graph
		for(FunctionalUnit primitiveUnit : FunctionalUnits.PRIMITIVES) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(primitiveUnit.getGraph(), graph);
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = primitiveUnit.new Instance(graph, map.values(), primitiveUnit.getName());
				this.connectivityGraph.addVertex(instance);
			}
		}
	}


	/**
	 * Calculates the story's overall symmetry based on the characters' beliefs, intentions, actions and emotions
	 */
	private void detectSymmetry(PlotDirectedSparseGraph graph) {
		HashMap<String, Collection<Instance>> fuSequences = new HashMap<>();
		for(Instance i : this.fUinstances) {
			Collection<Instance> agList = fuSequences.getOrDefault(i.getFirstAgent(), new ArrayList<>());
			agList.add(i);
			fuSequences.put(i.getFirstAgent(), agList);

			if (i.getSecondAgent() != null) {
				agList = fuSequences.getOrDefault(i.getSecondAgent(), new ArrayList<>());
				agList.add(i);
				fuSequences.put(i.getSecondAgent(), agList);
			}
		}

		HashMap<String, List<Instance>> agentFuOrderMap = new HashMap<>();
		for (String agent : fuSequences.keySet()) {
			Collection<Instance> instances = fuSequences.get(agent);
			agentFuOrderMap.put(agent, instances.stream().sorted(new FunctionalUnit.InstanceSubgraphOrderComparator(agent))
														  .collect(Collectors.toList()));
		}

		logger.info("FU order (hen): " + agentFuOrderMap.get("hen"));
		logger.info("FU order (dog): " + agentFuOrderMap.get("dog"));
		logger.info("FU order (pig): " + agentFuOrderMap.get("pig"));
		logger.info("FU order (cow): " + agentFuOrderMap.get("cow"));
		logger.info("\n");

		for (String agent : fuSequences.keySet()) {
			double fuSym = this.sequenceAnalyser(agentFuOrderMap.get(agent).stream().map(i -> i.toString()).collect(Collectors.toList()));
			double normFuEmotion = (fuSym - 0) / (this.sequenceAnalyser(Lists.newArrayList(Strings.repeat("A", agentFuOrderMap.get(agent).size()).split(""))) - 0);

			logger.info("normalized FU similarity (" + agent + "): " + normFuEmotion);
		}

		// get the actions, emotions, intentions and beliefs of a character
		double[] characterSymmetries = new double[graph.getRoots().size()];
		double minNormal = 0;

		HashMap<String, Collection<Vertex>> agentEventMap = new HashMap<>();
		// for each character in the story
		for (Vertex root : graph.getRoots()) {
			agentEventMap.put(root.toString(), new ArrayList<>());

			List<String> emotionSequences = new ArrayList<>();
			List<String> intentionSequences = new ArrayList<>();
			List<String> beliefSequences = new ArrayList<>();
			List<String> actionSequences = new ArrayList<>();

			int charCounter = 0;

			// get the character's story graph
			for(Vertex v : graph.getCharSubgraph(root)) {
				agentEventMap.get(root.toString()).add(v);

				// get the emotions of the character
				if (!v.getEmotions().isEmpty())
				{
					for (String emotion : v.getEmotions())
					{
						emotionSequences.add(emotion);
					}
				}

				// get the intentions of character
				if (!v.getIntention().isEmpty())
				{
					intentionSequences.add(v.getIntention());
				}

				// get the intentions of character
				if (v.getType() == Type.PERCEPT)
				{
					beliefSequences.add(v.toString());
				}

				// get the actions of the character
				if(v.getType() == Type.ACTION || v.getType() == Type.SPEECHACT)
				{
					actionSequences.add(v.getFunctor());
				}
			}

			logger.info("\n\nCharacter: " + root.toString() + ": ");
			logger.info("All Event Sequence: ");
			logger.info(agentEventMap.get(root.toString()).toString());

			// run the analysis on the different states
			double emotionSym = this.sequenceAnalyser(emotionSequences);
			double intentionSym = this.sequenceAnalyser(intentionSequences);
			double beliefSym = this.sequenceAnalyser(beliefSequences);
			double actionSym = this.sequenceAnalyser(actionSequences);


			// NORMALISATION
			// for normalisaton: create maximal possible sequence and get its symmetry value
			List<String> maxSeqEmotion = new ArrayList<>();
			List<String> maxSeqIntention = new ArrayList<>();
			List<String> maxSeqBelief = new ArrayList<>();
			List<String> maxSeqAction = new ArrayList<>();

			for (int i = 0 ; i < emotionSequences.size(); i++)
			{
				maxSeqEmotion.add("a");
			}

			for (int i = 0 ; i < intentionSequences.size(); i++)
			{
				maxSeqIntention.add("a");
			}

			for (int i = 0 ; i < beliefSequences.size(); i++)
			{
				maxSeqBelief.add("a");
			}

			for (int i = 0 ; i < actionSequences.size(); i++)
			{
				maxSeqAction.add("a");
			}


			// z-normalisation: (x - min) / (max - min)
			double normSymEmotion = (emotionSym - minNormal) / (this.sequenceAnalyser(maxSeqEmotion) - minNormal);
			double normSymIntention = (intentionSym - minNormal) / (this.sequenceAnalyser(maxSeqIntention) - minNormal);
			double normSymBelief = (beliefSym - minNormal) / (this.sequenceAnalyser(maxSeqBelief) - minNormal);
			double normSymAction = (actionSym - minNormal) / (this.sequenceAnalyser(maxSeqAction) - minNormal);


			// take overall mean over the different states of the character
			characterSymmetries[charCounter] = (normSymEmotion + normSymIntention + normSymBelief + normSymAction) / 4;

			logger.info("\nSequences:" +
					"\n Emotions: " + emotionSequences +
					"\n Intentions: " + intentionSequences +
					"\n Beliefs: " + beliefSequences +
					"\n Actions: " + actionSequences);
			logger.info("\nAverage Vertex Symmetry: "+ characterSymmetries[charCounter] +
					"\nWith:\n " + normSymEmotion + "(Emotions),\n " +
					normSymIntention + "(Intentions),\n " +
					normSymBelief + "(Beliefs),\n " +
					normSymAction + "(Actions)");
			charCounter++;
		}

		// overall symmetry (normalisation to number of characters happens in the compute method)
//		this.symmetry = Arrays.stream(characterSymmetries).sum();
	}

	/**
	 * Calculates the story's overall symmetry based on the characters' beliefs, intentions, actions and emotions
	 * It uses the normal distance measurement. For testing all three: run TestSymmetry.java
	 * @param a graph containing the respective character's events (beliefs, intentions, actions, emotions) as a list of strings
	 * @return symmetry for the input graph
	 */
	public double sequenceAnalyser(List<String> graphSequence)
	{
		// saves a sequences as key with corresponding values [counter, List of Start Indices]
		Map<List<String>, List<Integer>> sequenceMap = new HashMap<>();

		// loop over the graph and create the (sub)sequences
		for (int start = 0; start < graphSequence.size(); start++)
		{
			for (int end = graphSequence.size(); end > start + 1; end--)
			{
				List<String> currentSeq = new ArrayList<>(graphSequence.subList(start, end));

				// if the sequence already in the list, only increase the counter
				if (sequenceMap.containsKey(currentSeq))
				{
					List<Integer> newSeq = new ArrayList<>(sequenceMap.get(currentSeq));
					newSeq.add(start);

					sequenceMap.put(currentSeq, newSeq);
				}
				else
				{
					List<Integer> newSeq = new ArrayList<>();
					newSeq.add(start);

					sequenceMap.put(currentSeq, newSeq);
				}
			}
		}

		double sumWeightedNormals = 0;
		int sequenceValueNormal = 0;

		for (Map.Entry<List<String>, List<Integer>> entry : sequenceMap.entrySet())
		{
			// if a sequence appears more than once, weight them by their
			// number of appearances and save the values in a new list
			if (entry.getValue().size() > 1)
			{
				// reset sequence value
				sequenceValueNormal = 0;
				// sum the distances between the start indices within a sequence
				for (int prevIdx = 0; prevIdx < entry.getValue().size(); prevIdx ++)
				{
					for (int currentIdx = prevIdx+1; currentIdx < entry.getValue().size(); currentIdx ++)
					{
						// sum distances between start indices
						sequenceValueNormal += entry.getValue().get(currentIdx) - entry.getValue().get(prevIdx);
					}
				}
				// weight the distances by their length
				sumWeightedNormals += sequenceValueNormal * entry.getKey().size();
			}
		}
		return sumWeightedNormals;
	}

	/**
	 * Computes the overall tellability score, by normalizing all features into a range of (0,1) and adding them,
	 * which amounts to assigning each feature equal weight.
	 * @return
	 */
	public double compute() {
		if (this.productiveConflicts < 1) {
			// need at least one conflict and one attempt at resolution, for this to be a plot
			return 0;
		}

		logger.info("normalized polyvalence: " + (double) this.numPolyvalentVertices / this.numAllVertices);
		logger.info("normalized suspense: " + (double) this.suspense / this.plotLength);
//		logger.info("normalized symmetry: " + this.symmetry / this.charNum);

		double tellability = (double) this.numPolyvalentVertices / this.numAllVertices +
							 (double) this.suspense / this.plotLength;
//							 this.symmetry / this.charNum;					// normalize symmetry by the number of agents in the story
		tellability /= 2;

		logger.info("Overall tellability: " + tellability);
		return tellability;
	}
}
