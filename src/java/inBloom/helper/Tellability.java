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
	public static final int SIMILARITY_FU_THRESHOLD = 3;

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
	public double absoluteFunctionalPolyvalence;
	public double balancedFunctionalPolyvalence;

	// Semantic Symmetry
	public int charNum;
	public Collection<Instance> fUinstances = new ArrayList<>();
	public double absoluteSymmetry;
	public double balancedSymmetry;

	// Semantic Opposition
	public double absoluteOpposition;
	public double balancedOpposition;

	// Suspense
	public Triple<String, Vertex, Vertex> mostSuspensefulIntention;			 // (agent, intention, action)
	public int plotLength;
	public double absoluteSuspense;
	public double balancedSuspense;

	// Overall
	public double value;


	/**
	 * Takes an analyzed graph and computes all necessary statistics of the plot to compute tellability.
	 * @param graph whose tellability needs to be determined and that has been been processed by
	 * {@link VertexMergingPPVisitor} and  {@link EdgeGenerationPPVisitor}.
	 */
	public Tellability(PlotDirectedSparseGraph graph, MoodMapper moodData) {
//		logger.setLevel(Level.FINE);
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

		logger.info("normalized polyvalence: " + this.absoluteFunctionalPolyvalence);
		logger.info("normalized absoluteSymmetry: " + this.absoluteSymmetry);
		logger.info("normalized absoluteOpposition: " + this.absoluteOpposition);
		logger.info("normalized absoluteSuspense: " + this.absoluteSuspense);
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
		logger.info("Start analysing polyvalence");
		Map<Vertex, Integer> vertexUnitCount = new HashMap<>();

		UnitFinder finder = new UnitFinder();
		int polyvalentVertices = 0;
		int unitInstances = 0;
		Set<Vertex> polyvalentVertexSet = new HashSet<>();

		this.connectivityGraph = new ConnectivityGraph(graph);

		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			logger.info("      Finding units of type: '" + unit.getName() + "'...");
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

		Integer maxPolyCount = vertexUnitCount.values().stream().mapToInt(i -> i).max().orElse(-1);
		List<Vertex> mostPolyVertices = vertexUnitCount.entrySet().stream().filter(entry -> entry.getValue() == maxPolyCount)
																		   .map(entry -> entry.getKey())
																	       .collect(Collectors.toList());

		this.numFunctionalUnits = unitInstances;
		String foundUnits = this.functionalUnitCount.entrySet().stream().filter(entry -> entry.getValue() > 0)
													.map(entry -> entry.getKey().getName() + ": " + entry.getValue())
													.sorted()
													.reduce( (a,b) -> a = a + ", " + b)
													.orElse("<none>");
		logger.info("   --> Found units: " + foundUnits);

		this.numPolyvalentVertices = polyvalentVertices;
		logger.info("   Number of vertices that are part of at least one FU: " + vertexUnitCount.size());
		logger.info("   Number of polyvalent vertices: " + this.numPolyvalentVertices);
		logger.info("   Most polyvalent vertices (score=" + maxPolyCount + "): " + mostPolyVertices);
		logger.fine("   Vertx:polyValenceValue-count" + vertexUnitCount);
		logger.info("   Number of all vertices: " + this.numAllVertices);
		this.absoluteFunctionalPolyvalence = (double) this.numPolyvalentVertices / this.numAllVertices;
		logger.info("   --> Functional Polyvalence: " + this.absoluteFunctionalPolyvalence);

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
		logger.info("Start analysing p and s");
		HashMap<String, List<String>> agentFuSeqMap = this.extractOrderedFUSequences(graph.getRoots().stream().map(v -> v.toString()).collect(Collectors.toList()));
		boolean sufficientFUPresent = agentFuSeqMap.entrySet().stream().map( entry -> entry.getValue().size() )
																	   .mapToInt(size -> size)
																	   .min().orElse(0) >= SIMILARITY_FU_THRESHOLD;

		HashMap<String, List<String>> agentSeqMap;
		List<Float> similarityScores =  new ArrayList<>();
		if (sufficientFUPresent) {
			logger.info("   Perform FU based symmetry and parallelism analysis");
			agentSeqMap = agentFuSeqMap;
		} else {
			logger.info("   Not sufficient number of FU present in any of the sub graphs, fall back on event based absoluteSymmetry and parallelism analysis");

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
		this.absoluteSymmetry = Stats.meanOf(similarityScores);
		return;
	}

	private void detectOpposition(PlotDirectedSparseGraph graph, MoodMapper moodData) {
		logger.info("Start analysing opposition");
		List<Float> oppositionScores = new LinkedList<>();

		for (String agent : this.counter.agents) {
			logger.info("   Processing agent: " + agent);

			// find violated expectations
			logger.info("   computing violated expectation score");
			int violationIndicators = this.counter.violatedExpectationEvents.get(agent).size() + this.counter.terminatedPercepts.get(agent).size();
			int relevantEvents = this.counter.emotionalEvents.get(agent).size() + this.counter.overallPerceptNum.get(agent);
			float normalizedExpectationViolationScore = 0;
			if (relevantEvents != 0) {
				normalizedExpectationViolationScore = (float) violationIndicators / relevantEvents;
			}

			logger.fine("      violated expectations: " + this.counter.violatedExpectationEvents.get(agent));
			logger.fine("      terminated beliefs: " + this.counter.terminatedPercepts.get(agent));
			logger.info("      number of violation indicators: " + violationIndicators);
			logger.info("      relevantEvents: " + relevantEvents);
			logger.info("   --> normalized  score: " + normalizedExpectationViolationScore);

			// find reversals in fortunes
			logger.info("   computing reversals in fortunes score");
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
			logger.fine("      fortuneIntervals: " + reversals);
			logger.fine("      number of entries: " + reversals.size());

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
			logger.fine("      fortuneIntervalsNoOverlap: " + reversalsNoOverlap);
			logger.info("      number of entries: " + reversalsNoOverlap.size());

			long possibleIntervalNum = (moodData.latestEndTime() - moodData.latestStartTime() ) / 10;
			logger.info("      number of possible intervals: " + possibleIntervalNum);

			float normalizedFortuneChangeScore = 0;
			if (possibleIntervalNum != 0 & reversalsNoOverlap.size() != 0) {
				normalizedFortuneChangeScore = (float)reversalsNoOverlap.size() / possibleIntervalNum;
			}
			logger.info("   --> normalized score: " + normalizedFortuneChangeScore);

			// opposition score for this agent is the higher of both scores
			oppositionScores.add(Math.max(normalizedExpectationViolationScore, normalizedFortuneChangeScore));
		}

		// focus on opposition for main characters, here: one character i.e. protagonist
		this.absoluteOpposition = oppositionScores.stream().mapToDouble(f -> f).max().orElse(0);
	}

	private void detectSuspense(PlotDirectedSparseGraph graph) {
		logger.info("Start analysing suspense");
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

		logger.info("   maximal suspense: " + suspense);
		if(this.mostSuspensefulIntention != null) {
			logger.info("   most suspensefull intention: " +
						this.mostSuspensefulIntention.getFirst() + "'s (" +
						this.mostSuspensefulIntention.getSecond().toString() + ", " +
						this.mostSuspensefulIntention.getThird().toString() + ")");
		}

		logger.info("   plot length: " + this.plotLength);
		this.absoluteSuspense = (double) suspense / this.plotLength;
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
			logger.fine("   FU order (" + agent + "): " + agentFuStringMap.get(agent));
		}

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
			logger.info("      normalized parallelism (" + agent1 + ", " + agent2 +  "): " + fuPara);
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
	 * Computes the overall tellability score, by balancing and averaging the respective tellability principles.
	 * @return
	 */
	public double compute() {
		if (this.productiveConflicts < 1) {
			// need at least one conflict and one attempt at resolution, for this to be a plot
			return 0;
		}

		double aveTellability = (this.absoluteFunctionalPolyvalence + this.absoluteSuspense + this.absoluteSymmetry + this.absoluteOpposition) / 4;
		logger.info("(Average tellability: " + aveTellability + ")");

		this.value = this.balanceButSuspense();
		logger.info("Balanced tellability: " + this.value);

		return this.value;
	}

	@SuppressWarnings("unused")
	private double balanceAll() {
		this.balancedFunctionalPolyvalence =  1 - 2 * Math.abs(0.5 - this.absoluteFunctionalPolyvalence);
		this.balancedSymmetry =  1 - 2 * Math.abs(0.5 - this.absoluteSymmetry);
		this.balancedOpposition =  1 - 2 * Math.abs(0.5 - this.absoluteOpposition);
		this.balancedSuspense = 1 - 2 * Math.abs(0.5 - this.absoluteSuspense);

		logger.info("Balance all");
		logger.info("   Balanced FP: " + this.balancedFunctionalPolyvalence);
		logger.info("   Balanced SYM: " + this.balancedSymmetry);
		logger.info("   Balanced OPO: " + this.balancedOpposition);
		logger.info("   Balanced SUS: " + this.balancedSuspense);

		return (this.balancedFunctionalPolyvalence + this.balancedSymmetry + this.balancedOpposition + this.balancedSuspense) / 4;
	}

	@SuppressWarnings("unused")
	private double balanceAve() {
		logger.info("Balance ave");
		double ave_tellability = (this.absoluteFunctionalPolyvalence + this.absoluteSuspense + this.absoluteSymmetry + this.absoluteOpposition) / 4;
		logger.info("   Average tellability: " + ave_tellability);

		return 1 - 2 * Math.abs(0.5 - ave_tellability);
	}

	private double balanceButSuspense() {
		this.balancedFunctionalPolyvalence =  1 - 2 * Math.abs(0.5 - this.absoluteFunctionalPolyvalence);
		this.balancedSymmetry =  1 - 2 * Math.abs(0.5 - this.absoluteSymmetry);
		this.balancedOpposition =  1 - 2 * Math.abs(0.5 - this.absoluteOpposition);
		this.balancedSuspense = this.absoluteSuspense;

		logger.info("Balance all but absoluteSuspense");
		logger.info("   Balanced FP: " + this.balancedFunctionalPolyvalence);
		logger.info("   Balanced SYM: " + this.balancedSymmetry);
		logger.info("   Balanced OPO: " + this.balancedOpposition);
		logger.info("   Unbalanced SUS: " + this.balancedSuspense);

		return (this.balancedFunctionalPolyvalence + this.balancedSymmetry + this.balancedOpposition + this.balancedSuspense) / 4;
	}

	@SuppressWarnings("unused")		// left to be able to determine absoluteSymmetry statistics  of split into event types, should the need arise
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
						+ "----> Average 4-part vertex absoluteSymmetry: "+ (emotionSym + intentionSym + beliefSym + actionSym) / 4f + "\n "
						+ mentalSym + "(all mental), \n "
						+ actionSym + "(Actions), \n "
						+ "----> Average 2-part vertex absoluteSymmetry: " + (mentalSym + actionSym) / 2f + "\n "
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
