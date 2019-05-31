package inBloom.test.story;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnit;

@SuppressWarnings("unused")
public class TestUnits {
	
	public static final FunctionalUnit MOTIVATION;
	public static final FunctionalUnit PERSEVERANCE;
	public static final FunctionalUnit RESOLUTION;
	public static final FunctionalUnit LOSS;
	public static final FunctionalUnit POSITIVE_TRADEOFF;
	public static final FunctionalUnit NEGATIVE_TRADEOFF;
	
	static {
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
}
