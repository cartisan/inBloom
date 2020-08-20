package inBloom.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.common.math.Stats;

import jason.asSemantics.Mood;
import jason.util.Pair;

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
import inBloom.graph.visitor.VisualizationFilterPPVisitor;

public class Tellability {
	public static final Long FORTUNE_CHANGE_INTERVAL_LENGTH = 10l;
	public static final double FORTUNE_CHANGE_DELTA_MOOD_THRESHOLD = 0.5;
	public static int GRAPH_MATCHING_TOLERANCE = 1;
	public static final int SIMILARITY_FU_THRESHOLD = 5;

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
	public double opposition;

	// Suspense
	public int suspense;
	public Triple<String, Vertex, Vertex> mostSuspensefulIntention;			 // (agent, intention, action)
	public int plotLength;



	// Dynamic Points



	/**
	 * Takes an analyzed graph and computes all necessary statistics of the plot to compute tellability.
	 * @param graph whose tellability needs to be determined and that has been been processed by
	 * {@link VertexMergingPPVisitor} and  {@link EdgeGenerationPPVisitor}.
	 */
	public Tellability(PlotDirectedSparseGraph graph, MoodMapper moodData) {
		// Perform quantitative analysis of plot
		this.counter = new CountingVisitor();
		this.computeSimpleStatistics(graph);

		// find functional units and polyvalent vertices
		this.plotUnitTypes = new LinkedList<>();
		this.detectPolyvalence(graph);

		// calculate semantic symmetry and parallelism
		this.detectSymmetryAndParallelism(graph);

		// calculate semantic opposition
		this.detectOpposition(graph, moodData);

		// calculate suspense
		this.detectSuspense(graph);

	}

	/**
	 * Computes all statistics that can be determined by counting in a single pass
	 * @param graph a graph that has been processed by both VertexMergingPPVisitor and VisualizationFilterPPVisitor
	 */
	private void computeSimpleStatistics(PlotDirectedSparseGraph graph) {
		this.counter.apply(graph);
		this.productiveConflicts = this.counter.getProductiveConflictNumber();
		this.plotLength = this.counter.getPlotLength();
		this.numAllVertices = this.counter.getVertexNum();
		this.charNum = this.counter.agents.size();
	}

	/**
	 * Identifies all fUinstances of functional units in the plot graph and detects polyvalent vertices
	 * at their overlap.
	 * @param graph an analyzed plot graph, i.e. one that has been processed by {@linkplain VertexMergingPPVisitor},
	 *              {@linkplain EdgeGenerationPPVisitor}, and {@linkplain VisualizationFilterPPVisitor} visitors.
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
		String foundUnits = this.functionalUnitCount.entrySet().stream().filter(entry -> entry.getValue() > 0)
													.map(entry -> entry.getKey().getName() + ": " + entry.getValue())
													.sorted()
													.reduce( (a,b) -> a = a + ", " + b)
													.orElse("<none>");
		logger.info("-> Found units: " + foundUnits);

		this.numPolyvalentVertices = polyvalentVertices;
		logger.info("Number of polyvalent vertices: " + this.numPolyvalentVertices);

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
	 * Calculates the plot's  overall symmetry and parallelism based on FUs or, if none, raw events (emotions, intentions, beliefs, actions).
	 * @param graph an analyzed plot graph, i.e. one that has been processed by {@linkplain VertexMergingPPVisitor},
	 *              {@linkplain EdgeGenerationPPVisitor}, and {@linkplain VisualizationFilterPPVisitor} visitors.
	 */
	private void detectSymmetryAndParallelism(PlotDirectedSparseGraph graph) {
		HashMap<String, List<String>> agentFuSeqMap = this.extractOrderedFUSequences(graph.getRoots().stream().map(v -> v.toString()).collect(Collectors.toList()));
		boolean sufficientFUPresent = agentFuSeqMap.entrySet().stream().map( entry -> entry.getValue().size() )
																	   .mapToInt(size -> size)
																	   .max().orElse(0) >= SIMILARITY_FU_THRESHOLD;

		HashMap<String, List<String>> agentSeqMap;
		List<Float> similarityScores =  new ArrayList<>();
		if (sufficientFUPresent) {
			logger.info("Perform FU based symmetry and parallelism analysis");
			agentSeqMap = agentFuSeqMap;
		} else {
			logger.info("Not sufficient number of FU present in any of the sub graphs, fall back on event based symmetry and parallelism analysis");

			// extract event sequence
			HashMap<String, List<String>> agentEventSeqMap = new HashMap<>();
			for (Vertex root : graph.getRoots()) {
				agentEventSeqMap.put(root.toString(), new ArrayList<>());
				for(Vertex v : graph.getCharSubgraph(root)) {
					// ignore vertices created from reading the ASL, only include plot from actual simulation
					if(v.getStep() > 0) {
						agentEventSeqMap.get(root.toString()).add(TermParser.removeAnnots(v.getLabel()));
					}
				}
			}
			agentSeqMap = agentEventSeqMap;
		}

		// compute symmetry and parallelism on either FU or events (use tmp sequence for logging)
		List<Float> tmp = this.symmetry(agentSeqMap);
		if(!tmp.isEmpty()) {
			logger.info("   average symmetry: " + Stats.meanOf(tmp));
			similarityScores.addAll(tmp);
		}

		tmp = this.parallelism(agentSeqMap);
		if(!tmp.isEmpty()) {
			logger.info("   average paralellism: " + Stats.meanOf(tmp));
			similarityScores.addAll(tmp);
		}

		// overall symmetry is average: over symmetry for each character and parallelism for each character pair
		this.symmetry = Stats.meanOf(similarityScores);
		return;
	}

	private void detectOpposition(PlotDirectedSparseGraph graph, MoodMapper moodData) {
		logger.info("Start analysing opposition");
		List<Float> oppositionScores = new LinkedList<>();

		for (String agent : this.counter.agents) {
			logger.info("processing agent: " + agent);

			// find violated expectations
			logger.info("  computing violated expectation score");
			int violationIndicators = this.counter.violatedExpectationEvents.get(agent).size() + this.counter.terminatedPercepts.get(agent).size();
			int relevantEvents = this.counter.emotionalEvents.get(agent).size() + this.counter.overallPerceptNum.get(agent);
			float normalizedExpectationViolationScore = (float) violationIndicators / relevantEvents;
			logger.fine("     violationIndicators: " + violationIndicators);
			logger.fine("     relevantEvents: " + relevantEvents);
			logger.fine("     --> normalized  score: " + normalizedExpectationViolationScore);

			// find reversals in fortunes
			logger.fine("  computing reversals in fortunes score");
			Map<Long, List<Mood>> cycleMoodMap = moodData.getMoodByAgent(agent);
			List<Long> reasoningCycleNums = cycleMoodMap.keySet().stream().sorted().collect(Collectors.toList());
			ArrayList<MoodInterval> reversals = new ArrayList<>();
			for(Long cycleNum : reasoningCycleNums) {
				Mood m = moodData.sampleMood(agent, cycleNum);

				for(long i = cycleNum - FORTUNE_CHANGE_INTERVAL_LENGTH; i < cycleNum; ++i) {
					boolean intervalDetected = false;
					Mood m_i = moodData.sampleMood(agent, i);
					if (m_i == null) {
						continue;
					}

					for(String dim : Mood.DIMENSIONS) {
						if (FORTUNE_CHANGE_DELTA_MOOD_THRESHOLD < Math.abs(m_i.get(dim) - m.get(dim)) & Math.signum(m_i.get(dim)) != Math.signum(m.get(dim))) {
							reversals.add(new MoodInterval(i, m_i, cycleNum, m));
							intervalDetected = true;
							break;
						}
					}

					if (intervalDetected) {
						break;
					}
				}
			}
			logger.fine("     fortuneIntervals: " + reversals);
			logger.fine("     number of entries: " + reversals.size());

			List<MoodInterval> reversalsNoOverlap = new ArrayList<>();
			List<MoodInterval> tmpList = new ArrayList<>();
			if(!reversals.isEmpty()) {
				tmpList.add(reversals.remove(0));
			}
			while(tmpList.size() > 0) {
				MoodInterval interval = tmpList.remove(0);

				// search over all starting positions that have not been removed yet as overlaps
				Iterator<MoodInterval> it = reversals.listIterator();
				while(it.hasNext()) {
					MoodInterval nextInt = it.next();
					// if starting position p is located before the end of the currently used position, remove p
					// otherwise, transfer p into list of positions to be used to remove overlaps and abort
					// that way, we find the first p that is not to be removed, and instantly switch to using it
					if(interval.contains(nextInt)) {
						it.remove();
					} else {
						it.remove();
						tmpList.add(nextInt);
						break;
					}
				}
				// since we removed everything that overlapped with current position, we can safe it as overlap free
				reversalsNoOverlap.add(interval);
			}
			logger.fine("     fortuneIntervalsNoOverlap: " + reversalsNoOverlap);
			logger.fine("     number of entries: " + reversalsNoOverlap.size());

			long possibleIntervalNum = (moodData.latestEndTime() - moodData.latestStartTime() ) / 10;
			logger.fine("     number of possible intervals: " + possibleIntervalNum);
			float normalizedFortuneChangeScore = (float)reversalsNoOverlap.size() / possibleIntervalNum;
			logger.fine("     --> normalized score: " + normalizedFortuneChangeScore);

			// opposition score for this agent is the higher of both scores
			oppositionScores.add(Math.max(normalizedExpectationViolationScore, normalizedFortuneChangeScore));
		}

		// focus on opposition for main characters, here: one character i.e. protagonist
		this.opposition = oppositionScores.stream().mapToDouble(f -> f).max().orElse(0);
	}

	private void detectSuspense(PlotDirectedSparseGraph graph) {
		int suspense  = 0;
		for (String agent : this.counter.agents) {
			List<Pair<Vertex, Vertex>> confPairs = this.counter.productiveConflicts.get(agent);

			for (Pair<Vertex, Vertex> pair: confPairs) {
				Vertex intention = pair.getFirst();
				Vertex resolution = pair.getSecond();		//is an intention in case of t edge, or an action in case of m edge

				if (this.counter.motivationChains.contains(agent, intention)) {
					List<Vertex> motivations = this.counter.motivationChains.get(agent, intention);
					intention = motivations.get(motivations.size() - 1);
				}

				int localSuspense = resolution.getStep() - intention.getStep();

				if (suspense <= localSuspense) { // <= is important, because with == suspense we want the stuff closer to the end
					suspense = localSuspense;
					this.mostSuspensefulIntention = new Triple<>(agent, intention, resolution);
				}
			}
		}

		logger.info("Maximal suspense: " + suspense);
		if(this.mostSuspensefulIntention != null) {
			logger.info("Most suspensefull intention: " +
						this.mostSuspensefulIntention.getFirst() + "'s (" +
						this.mostSuspensefulIntention.getSecond().toString() + ", " +
						this.mostSuspensefulIntention.getThird().toString() + ")");
		}

		this.suspense = suspense;
	}

	private HashMap<String, List<String>> extractOrderedFUSequences(List<String> agentNames) {
		Map<String, Collection<Instance>> fuSequences = agentNames.stream().collect(Collectors.toMap(agName -> agName,
																									 agName -> new ArrayList<>()));

		for(Instance i : this.fUinstances) {
			Collection<Instance> agList = fuSequences.get(i.getFirstAgent());
			agList.add(i);

			if (i.getSecondAgent() != null) {
				agList = fuSequences.get(i.getSecondAgent());
				agList.add(i);
			}
		}

		HashMap<String, List<Instance>> agentFuOrderMap = new HashMap<>();
		for (String agent : fuSequences.keySet()) {
			Collection<Instance> instances = fuSequences.get(agent);
			agentFuOrderMap.put(agent, instances.stream().sorted(new FunctionalUnit.InstanceSubgraphOrderComparator(agent))
														 .collect(Collectors.toList()));
		}

		// translate FU sequences to string based representation
		HashMap<String, List<String>> agentFuStringMap =
				(HashMap<String, List<String>>) agentFuOrderMap.entrySet().stream()
						  .collect(Collectors.toMap(
									  entry -> entry.getKey(),
									  entry -> entry.getValue().stream().map(elem -> elem.toString()).collect(Collectors.toList()))
								  );

		for(String agent: agentFuStringMap.keySet()) {
			logger.fine("FU order (" + agent + "): " + agentFuStringMap.get(agent));
		}
		logger.fine("\n");

		return agentFuStringMap;
	}

	private List<Float> parallelism(HashMap<String, List<String>> agentSequenceMap) {
		// if we have only 1 agent, no parallelism can be computed
		if(agentSequenceMap.keySet().size() == 1) {
			return new ArrayList<>();
		}

		// find all possible pairings for parallelism comparison
		Set<Set<String>> pairs = Sets.combinations(agentSequenceMap.keySet(), 2);

		List<Float> parallelismScores =  new ArrayList<>();
		for(Set<String> pairSet : pairs) {
			ArrayList<String> pair = new ArrayList<>(pairSet);
			String agent1 = pair.get(0);
			String agent2 = pair.get(1);
			Float fuPara = SymmetryAnalyzer.computeParallelism(agentSequenceMap.get(agent1), agentSequenceMap.get(agent2));
			logger.info("     normalized parallelism (" + agent1 + ", " + agent2 +  "): " + fuPara);
			parallelismScores.add(fuPara);
		}

		return parallelismScores;
	}

	private List<Float> symmetry(HashMap<String, List<String>> agentSequenceMap) {
		List<Float> symmetryScores =  new ArrayList<>();
		for (String agent : agentSequenceMap.keySet()) {
			Float fuSym = SymmetryAnalyzer.computeSymmetry(agentSequenceMap.get(agent));
			logger.info("      normalized FU similarity (" + agent + "): " + fuSym);
			symmetryScores.add(fuSym);
		}
		return symmetryScores;
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
		logger.info("normalized symmetry: " + this.symmetry);
		logger.info("normalized opposition: " + this.opposition);

		double tellability = (double) this.numPolyvalentVertices / this.numAllVertices +
							 (double) this.suspense / this.plotLength +
							 this.symmetry +
							 this.opposition;
		tellability /= 4;

		double harmonizedTellability = this.harmonizeButSuspense();

		return harmonizedTellability;
	}

	@SuppressWarnings("unused")
	private double harmonizeAll() {
		double harmFP =  1 - 2 * Math.abs(0.5 - this.functionalPolyvalence);
		double harmSYM =  1 - 2 * Math.abs(0.5 - this.symmetry);
		double harmOPO =  1 - 2 * Math.abs(0.5 - this.opposition);
		double harmSUS = 1 - 2 * Math.abs(0.5 - this.suspense);

		logger.info("Harmonize all");
		logger.info("   Harmonized FP: " + harmFP);
		logger.info("   Harmonized SYM: " + harmSYM);
		logger.info("   Harmonized OPO: " + harmOPO);
		logger.info("   Harmonized SUS: " + harmSUS);

		double harmonizedTellability = (harmFP + harmSYM + harmOPO + harmSUS) / 4;
		logger.info("   Harmonized tellability: " + harmonizedTellability);
		return harmonizedTellability;
	}

	@SuppressWarnings("unused")
	private double harmonizeAve() {
		logger.info("Harmonize ave");
		double ave_tellability = (this.functionalPolyvalence + this.suspense + this.symmetry + this.opposition) / 4;
		logger.info("   Average tellability: " + ave_tellability);

		double harmonizedTellability = 1 - 2 * Math.abs(0.5 - ave_tellability);
		logger.info("   Harmonized tellability: " + harmonizedTellability);
		return harmonizedTellability;
	}

	private double harmonizeButSuspense() {
		double harmFP =  1 - 2 * Math.abs(0.5 - this.functionalPolyvalence);
		double harmSYM =  1 - 2 * Math.abs(0.5 - this.symmetry);
		double harmOPO =  1 - 2 * Math.abs(0.5 - this.opposition);

		logger.info("Harmonize all but suspense");
		logger.info("   Harmonized FP: " + harmFP);
		logger.info("   Harmonized SYM: " + harmSYM);
		logger.info("   Harmonized OPO: " + harmOPO);
		logger.info("   Unharmonized SUS: " + this.suspense);

		double harmonizedTellability = (harmFP + harmSYM + harmOPO + this.suspense) / 4;
		logger.info("   Harmonized tellability: " + harmonizedTellability);
		return harmonizedTellability;
	}

	@SuppressWarnings("unused")		// left to be able to determine symmetry statistics  of split into event types, should the need arise
	private List<Float> eventBasedSymmetry(HashMap<String, List<String>> agentEventMap, PlotDirectedSparseGraph graph) {
		List<Float> eventSyms =  new ArrayList<>();

		for (Vertex agent : graph.getRoots()) {
			List<String> emotionSequences = new ArrayList<>();
			List<String> intentionSequences = new ArrayList<>();
			List<String> beliefSequences = new ArrayList<>();
			List<String> actionSequences = new ArrayList<>();
			List<String> mentalSequences = new ArrayList<>();

			// get the character's story graph
			for(Vertex v : graph.getCharSubgraph(agent)) {
				// get the emotions of the character
				if (!v.getEmotions().isEmpty()) {
					for (String emotion : v.getEmotions()) {
						emotionSequences.add(emotion);
						mentalSequences.add(emotion);
					}
				}
				// get the intentions of character
				if (!v.getIntention().isEmpty()) {
					intentionSequences.add(v.getIntention());
					mentalSequences.add(v.getIntention());
				}
				// get the intentions of character
				if (v.getType() == Type.PERCEPT) {
					beliefSequences.add(TermParser.removeAnnots(v.getLabel()));
					mentalSequences.add(TermParser.removeAnnots(v.getLabel()));
				}
				// get the actions of the character
				if(v.getType() == Type.ACTION || v.getType() == Type.SPEECHACT) {
					actionSequences.add(v.getFunctor());
				}
			}


			// run the analysis on the different states
			float emotionSym = SymmetryAnalyzer.computeSymmetry(emotionSequences);
			float intentionSym = SymmetryAnalyzer.computeSymmetry(intentionSequences);
			float beliefSym = SymmetryAnalyzer.computeSymmetry(beliefSequences);
			float actionSym = SymmetryAnalyzer.computeSymmetry(actionSequences);
			float mentalSym = SymmetryAnalyzer.computeSymmetry(mentalSequences);
			float allSym = SymmetryAnalyzer.computeSymmetry(agentEventMap.get(agent.toString()));


			logger.fine("\n\nCharacter: " + agent + ": ");
			logger.fine("\nSequences:" +
						"\n Emotions: " + emotionSequences +
						"\n Intentions: " + intentionSequences +
						"\n Beliefs: " + beliefSequences +
						"\n All Mentall: " + mentalSequences +
						"\n Actions: " + actionSequences +
						"\n All Events: " + agentEventMap.get(agent)
						);
			logger.fine("\n");
			logger.fine("\nVertex Symmetry Anlysis: \n "
						+ emotionSym + "(Emotions),\n "
						+ intentionSym + "(Intentions),\n "
						+ beliefSym + "(Beliefs),\n "
						+ actionSym + "(Actions), \n "
						+ "----> Average 4-part vertex symmetry: "+ (emotionSym + intentionSym + beliefSym + actionSym) / 4f + "\n "
						+ mentalSym + "(all mental), \n "
						+ actionSym + "(Actions), \n "
						+ "----> Average 2-part vertex symmetry: " + (mentalSym + actionSym) / 2f + "\n "
						+ allSym + "(all events)"
					);
			eventSyms.add(allSym);
		}

		return eventSyms;
	}

	private class MoodInterval {
		public long startCycle;
		public long endCycle;
		public Mood startMood;
		public Mood endMood;

		public MoodInterval(long startCycle, Mood startMood, long endCycle, Mood endMood) {
			this.startCycle = startCycle;
			this.startMood =  startMood;
			this.endCycle = endCycle;
			this.endMood = endMood;
		}

		public String toString() {
			return this.startCycle + "|" + this.startMood + "|" + this.endCycle + "|" + this.endMood + "|||";
		}

		public boolean contains(MoodInterval other) {
			return this.startCycle <= other.startCycle & this.endCycle > other.startCycle;
		}
	}
}
