package plotmas.graph.isomorphism;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;
import plotmas.graph.visitor.EdgeLayoutVisitor;

public class FunctionalUnits {
	
	public static FunctionalUnit[] ALL;
	
	public static final FunctionalUnit NESTED_GOAL;
	public static final FunctionalUnit DENIED_REQUEST;
	public static final FunctionalUnit RETALIATION;
	
	public static final FunctionalUnit DEBUG_SPEECH;
	public static final FunctionalUnit DEBUG_TERMINATION;
	
	public static final PlotDirectedSparseGraph ALL_UNITS_GRAPH;
	
	static {
		ALL = new FunctionalUnit[3];
		
		Vertex v1, v2, v3;
		
		PlotDirectedSparseGraph deniedRequest = new PlotDirectedSparseGraph();

		v1 = makeIntention(1);
		v2 = makeNegative(1);
		deniedRequest.addEdge(makeCommunication(), v1, v2);

		v3 = makeIntention(2);
		deniedRequest.addEdge(makeMotivation(), v2, v3);

		v2 = makeIntention(3);
		deniedRequest.addEdge(makeMotivation(), v3, v2);

		v3 = makeNegative(3);
		deniedRequest.addEdge(makeCommunication(), v2, v3);
		deniedRequest.addEdge(makeTermination(), v3, v1);
		DENIED_REQUEST = new FunctionalUnit("Denied Request", deniedRequest);
		
		PlotDirectedSparseGraph nestedGoal = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeIntention(2);
		v3 = makeWild(3);
		nestedGoal.addEdge(makeMotivation(), v1, v2);
		nestedGoal.addEdge(makeActualization(), v2, v3);
		NESTED_GOAL = new FunctionalUnit("Nested Goal", nestedGoal);
		
		PlotDirectedSparseGraph retaliation = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeNegative(1);
		retaliation.addEdge(makeCommunication(), v1, v2);
		v1 = makeIntention(2);
		retaliation.addEdge(makeMotivation(), v2, v1);
		v2 = makeIntention(3);
		v3 = makeIntention(5);
		retaliation.addEdge(makeMotivation(), v1, v2);
		retaliation.addEdge(makeMotivation(), v1, v3);
		v1 = makePositive(4);
		retaliation.addEdge(makeActualization(), v2, v1);
		v1 = makeNegative(5);
		retaliation.addEdge(makeCommunication(), v3, v1);
		RETALIATION = new FunctionalUnit("Retaliation", retaliation);
		
		ALL[0] = DENIED_REQUEST;
		ALL[1] = NESTED_GOAL;
		ALL[2] = RETALIATION;
		
		PlotDirectedSparseGraph allUnitsGraph = ALL[0].getDisplayGraph();
		for(int i = 1; i < ALL.length; i++) {
			ALL[i].getDisplayGraph().cloneInto(allUnitsGraph);
		}
		EdgeLayoutVisitor elv = new EdgeLayoutVisitor(allUnitsGraph, 9);
		allUnitsGraph.accept(elv);
		allUnitsGraph.setName("Functional Units");
		ALL_UNITS_GRAPH = allUnitsGraph;
		
		/**
		 * DEBUG UNITS
		 */
		PlotDirectedSparseGraph speech = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeWild(2);
		speech.addEdge(makeCommunication(), v1, v2);
		DEBUG_SPEECH = new FunctionalUnit("Debug Speech", speech);
		
		PlotDirectedSparseGraph termination = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeWild(2);
		termination.addEdge(makeTermination(), v2, v1);
		DEBUG_TERMINATION = new FunctionalUnit("Debug Termination", termination);
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
}
