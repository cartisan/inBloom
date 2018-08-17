package plotmas.test.story;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.FunctionalUnit;

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
		v1 = makeIntention(1);
		v2 = makeIntention(2);
		motivation.addEdge(makeMotivation(), v1, v2);
		MOTIVATION = new FunctionalUnit("Motivation", motivation);
		
		PlotDirectedSparseGraph resolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1);
		v2 = makePositive(2);
		resolution.addEdge(makeTermination(), v2, v1);
		RESOLUTION = new FunctionalUnit("Resolution", resolution);
		
		PlotDirectedSparseGraph loss = new PlotDirectedSparseGraph();
		v1 = makePositive(1);
		v2 = makeNegative(2);
		loss.addEdge(makeTermination(), v2, v1);
		LOSS = new FunctionalUnit("Loss", loss);
		
		PlotDirectedSparseGraph perseverance = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeIntention(2);
		perseverance.addEdge(makeEquivalence(), v2, v1);
		PERSEVERANCE = new FunctionalUnit("Perseverance", perseverance);
		
		PlotDirectedSparseGraph positiveTradeoff = new PlotDirectedSparseGraph();
		v1 = makePositive(1);
		v2 = makePositive(2);
		positiveTradeoff.addEdge(makeTermination(), v2, v1);
		POSITIVE_TRADEOFF = new FunctionalUnit("Positive Tradeoff", positiveTradeoff);
		
		PlotDirectedSparseGraph negativeTradeoff = new PlotDirectedSparseGraph();
		v1 = makeNegative(1);
		v2 = makeNegative(2);
		negativeTradeoff.addEdge(makeTermination(), v2, v1);
		NEGATIVE_TRADEOFF = new FunctionalUnit("Negative Tradeoff", negativeTradeoff);
	}
	
	private static Vertex makeIntention(int step) {
		return new Vertex("!intention", Vertex.Type.INTENTION, step);
	}
	
	private static Vertex makePositive(int step) {
		Vertex vertex = new Vertex("+", step);
		vertex.addEmotion("love");
		return vertex;
	}
	
	private static Vertex makeNegative(int step) {
		Vertex vertex = new Vertex("-", step);
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makeWild(int step) {
		Vertex vertex = new Vertex("*", step);
		vertex.addEmotion("love");
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Edge makeCommunication() {
		return new Edge(Edge.Type.COMMUNICATION);
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
