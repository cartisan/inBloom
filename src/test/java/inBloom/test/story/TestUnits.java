package inBloom.test.story;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.visitor.EdgeLayoutVisitor;

@SuppressWarnings("unused")
public class TestUnits {
	
	public static FunctionalUnit[] ALL;
	public static PlotDirectedSparseGraph ALL_UNITS_GRAPH;
	
	public static final FunctionalUnit MOTIVATION;
	public static final FunctionalUnit PERSEVERANCE;
	public static final FunctionalUnit RESOLUTION;
	public static final FunctionalUnit LOSS;
	public static final FunctionalUnit POSITIVE_TRADEOFF;
	public static final FunctionalUnit NEGATIVE_TRADEOFF;
	public static final FunctionalUnit PROBLEM;
	public static final FunctionalUnit ENABLEMENT;
	public static final FunctionalUnit SUCCESS;
	public static final FunctionalUnit FAILURE;
	public static final FunctionalUnit CHANGE_OF_MIND;
	public static final FunctionalUnit COMPLEX_POS_EVENT;
	
	static {
		ALL = new FunctionalUnit[12];
		
		Vertex v1, v2;
		
		PlotDirectedSparseGraph motivation = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, motivation);
		v2 = makeIntention(2, motivation);
		motivation.addEdge(makeMotivation(), v1, v2);
		MOTIVATION = new FunctionalUnit("Motivation", motivation);
		
		PlotDirectedSparseGraph resolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, resolution);
		v2 = makePositive(2, resolution);
		resolution.addEdge(makeTermination(), v2, v1);
		RESOLUTION = new FunctionalUnit("Resolution", resolution);
		
		PlotDirectedSparseGraph loss = new PlotDirectedSparseGraph();
		v1 = makePositive(1, loss);
		v2 = makeNegative(2, loss);
		loss.addEdge(makeTermination(), v2, v1);
		LOSS = new FunctionalUnit("Loss", loss);
		
		PlotDirectedSparseGraph perseverance = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, perseverance);
		v2 = makeIntention(2, perseverance);
		perseverance.addEdge(makeEquivalence(), v2, v1);
		PERSEVERANCE = new FunctionalUnit("Perseverance", perseverance);
		
		PlotDirectedSparseGraph positiveTradeoff = new PlotDirectedSparseGraph();
		v1 = makePositive(1, positiveTradeoff);
		v2 = makePositive(2, positiveTradeoff);
		positiveTradeoff.addEdge(makeTermination(), v2, v1);
		POSITIVE_TRADEOFF = new FunctionalUnit("Positive Tradeoff", positiveTradeoff);
		
		PlotDirectedSparseGraph negativeTradeoff = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, negativeTradeoff);
		v2 = makeNegative(2, negativeTradeoff);
		negativeTradeoff.addEdge(makeTermination(), v2, v1);
		NEGATIVE_TRADEOFF = new FunctionalUnit("Negative Tradeoff", negativeTradeoff);
		
		PlotDirectedSparseGraph problem = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, problem);
		v2 = makeIntention(2, problem);
		problem.addEdge(makeMotivation(), v1, v2);
		PROBLEM = new FunctionalUnit("Problem", problem);
		
		PlotDirectedSparseGraph enablement = new PlotDirectedSparseGraph();
		v1 = makePositive(1, enablement);
		v2 = makeIntention(2, enablement);
		enablement.addEdge(makeMotivation(), v1, v2);
		ENABLEMENT = new FunctionalUnit("Enablement", enablement);
		
		PlotDirectedSparseGraph success = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, success);
		v2 = makePositive(2, success);
		success.addEdge(makeActualization(), v1, v2);
		SUCCESS = new FunctionalUnit("Success", success);
		
		PlotDirectedSparseGraph failure = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, failure);
		v2 = makeNegative(2, failure);
		failure.addEdge(makeActualization(), v1, v2);
		FAILURE = new FunctionalUnit("Failure", failure);
		
		PlotDirectedSparseGraph changeMind = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, changeMind);
		v2 = makeIntention(2, changeMind);
		changeMind.addEdge(makeTermination(), v1, v2);
		CHANGE_OF_MIND = new FunctionalUnit("Change of Mind", changeMind);
		
		PlotDirectedSparseGraph complexPos = new PlotDirectedSparseGraph();
		v1 = makePositive(1, complexPos);
		v2 = makePositive(2, complexPos);
		complexPos.addEdge(makeCausality(), v1, v2);
		COMPLEX_POS_EVENT = new FunctionalUnit("Complex Pos Ev", complexPos);
		
		// Set up all primitive units so they can be displayed as graph in PrimitiveUniteTest
		ALL[0] = MOTIVATION;
		ALL[1] = RESOLUTION;
		ALL[2] = LOSS;
		ALL[3] = PERSEVERANCE;
		ALL[4] = POSITIVE_TRADEOFF;
		ALL[5] = NEGATIVE_TRADEOFF;
		ALL[6] = PROBLEM;
		ALL[7] = ENABLEMENT;
		ALL[8] = SUCCESS;
		ALL[9] = FAILURE;
		ALL[10] = CHANGE_OF_MIND;
		ALL[11] = COMPLEX_POS_EVENT;
		
		PlotDirectedSparseGraph allUnitsGraph = ALL[0].getDisplayGraph();
		for(int i = 1; i < ALL.length; i++) {
			ALL[i].getDisplayGraph().cloneInto(allUnitsGraph);
		}

		allUnitsGraph.setName("Primitive Units");
		ALL_UNITS_GRAPH = allUnitsGraph;
	}
	
	private static Vertex makeIntention(int step, PlotDirectedSparseGraph graph) {
		return new Vertex("!intention", Vertex.Type.INTENTION, step, graph);
	}
	
	private static Vertex makePositive(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("+", Vertex.Type.PERCEPT, step, graph);
		vertex.addEmotion("love");
		return vertex;
	}
	
	private static Vertex makeNegative(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("-",  Vertex.Type.PERCEPT, step, graph);
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makeWild(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("*",  Vertex.Type.PERCEPT, step, graph);
		vertex.addEmotion("love");
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Edge makeCommunication() {
		return new Edge(Edge.Type.CROSSCHARACTER);
	}
	
	private static Edge makeMotivation() {
		return new Edge(Edge.Type.MOTIVATION);
	}
	
	private static Edge makeActualization() {
		return new Edge(Edge.Type.ACTUALIZATION);
	}
	
	private static Edge makeTermination() {
		return new Edge(Edge.Type.TERMINATION);
	}
	
	private static Edge makeEquivalence() {
		return new Edge(Edge.Type.EQUIVALENCE);
	}
	
	private static Edge makeCausality() {
		return new Edge(Edge.Type.CAUSALITY);
	}
}
