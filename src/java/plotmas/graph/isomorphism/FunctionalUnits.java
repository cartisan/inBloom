package plotmas.graph.isomorphism;

import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

import plotmas.graph.Edge;

public class FunctionalUnits {
	
	public static PlotDirectedSparseGraph[] ALL;
	
	public static final PlotDirectedSparseGraph NESTED_GOAL;
	public static final PlotDirectedSparseGraph DENIED_REQUEST;
	public static final PlotDirectedSparseGraph RETALIATION;
	
	public static final PlotDirectedSparseGraph DEBUG_SPEECH;
	public static final PlotDirectedSparseGraph DEBUG_TERMINATION;
	
	static {
		ALL = new PlotDirectedSparseGraph[3];
		
		Vertex v1, v2, v3;
		
		PlotDirectedSparseGraph deniedRequest = new PlotDirectedSparseGraph();

		v1 = makeIntention();
		v2 = makeNegative();
		deniedRequest.addEdge(makeCommunication(), v1, v2);

		v3 = makeIntention();
		deniedRequest.addEdge(makeMotivation(), v2, v3);

		v2 = makeIntention();
		deniedRequest.addEdge(makeMotivation(), v3, v2);

		v3 = makeNegative();
		deniedRequest.addEdge(makeCommunication(), v2, v3);
		deniedRequest.addEdge(makeTermination(), v3, v1);
		DENIED_REQUEST = deniedRequest;
		
		PlotDirectedSparseGraph nestedGoal = new PlotDirectedSparseGraph();
		v1 = makeIntention();
		v2 = makeIntention();
		v3 = makeWild();
		nestedGoal.addEdge(makeMotivation(), v1, v2);
		nestedGoal.addEdge(makeActualization(), v2, v3);
		NESTED_GOAL = nestedGoal;
		
		PlotDirectedSparseGraph retaliation = new PlotDirectedSparseGraph();
		v1 = makeIntention();
		v2 = makeNegative();
		retaliation.addEdge(makeCommunication(), v1, v2);
		v1 = makeIntention();
		retaliation.addEdge(makeMotivation(), v2, v1);
		v2 = makeIntention();
		v3 = makeIntention();
		retaliation.addEdge(makeMotivation(), v1, v2);
		retaliation.addEdge(makeMotivation(), v1, v3);
		v1 = makePositive();
		retaliation.addEdge(makeActualization(), v2, v1);
		v1 = makeNegative();
		retaliation.addEdge(makeCommunication(), v3, v1);
		RETALIATION = retaliation;
		
		ALL[0] = DENIED_REQUEST;
		ALL[1] = NESTED_GOAL;
		ALL[2] = RETALIATION;
		
		/**
		 * DEBUG UNITS
		 */
		PlotDirectedSparseGraph speech = new PlotDirectedSparseGraph();
		v1 = makeIntention();
		v2 = makeWild();
		speech.addEdge(makeCommunication(), v1, v2);
		DEBUG_SPEECH = speech;
		
		PlotDirectedSparseGraph termination = new PlotDirectedSparseGraph();
		v1 = makeIntention();
		v2 = makeWild();
		termination.addEdge(makeTermination(), v2, v1);
		DEBUG_TERMINATION = termination;
	}
	
	private static Vertex makeIntention() {
		return new Vertex("!intention", Vertex.Type.INTENTION);
	}
	
	private static Vertex makePositive() {
		return new Vertex("[(+)]");
	}
	
	private static Vertex makeNegative() {
		return new Vertex("[(-)]");
	}
	
	private static Vertex makeWild() {
		return new Vertex("[(*)]");
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
