package plotmas.graph.isomorphism;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;
import plotmas.graph.Vertex.Type;
import plotmas.graph.visitor.EdgeLayoutVisitor;

/**
 * Class where all functional units are defined.
 * 
 * The helper methods in this class should be used for
 * creating the vertices and edges of the graph.
 * 
 * The vertex creation helper methods require a parameter,
 * indicating the vertex' place on the y-axis of the resulting
 * display graph of the functional unit.
 * @author Sven Wilke
 *
 */
public class FunctionalUnits {
	
	/**
	 * Holds all functional units which are relevant for computing
	 * functional polyvalence.
	 */
	public static FunctionalUnit[] ALL;
	
	public static final FunctionalUnit NESTED_GOAL;
	public static final FunctionalUnit DENIED_REQUEST;
	public static final FunctionalUnit RETALIATION;
	
	public static final FunctionalUnit HONORED_REQUEST;
	
	public static final FunctionalUnit INTENTIONAL_PROBLEM_RESOLUTION;
	public static final FunctionalUnit FORTUITOUS_PROBLEM_RESOLUTION;
	public static final FunctionalUnit SUCCESS_BORN_OF_ADVERSITY;
	public static final FunctionalUnit FLEETING_SUCCESS;
	public static final FunctionalUnit STARTING_OVER;
	public static final FunctionalUnit GIVING_UP;
	public static final FunctionalUnit SACRIFICE;
	
	public static final FunctionalUnit DEBUG_SPEECH;
	public static final FunctionalUnit DEBUG_TERMINATION;
	
	/**
	 * A graph containing all functional units in {@link #ALL ALL} next
	 * to each other to be used for displaying them.
	 */
	public static final PlotDirectedSparseGraph ALL_UNITS_GRAPH;
	
	static {
		ALL = new FunctionalUnit[11];
		
		Vertex v1, v2, v3;
		
		PlotDirectedSparseGraph deniedRequest = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeIntention(1);
		deniedRequest.addEdge(makeCommunication(), v1, v2);
		v3 = makeIntention(2);
		deniedRequest.addEdge(makeMotivation(), v2, v3);
		v2 = makeNegative(3);
		deniedRequest.addEdge(makeCommunication(), v3, v2);
		DENIED_REQUEST = new FunctionalUnit("Denied Request", deniedRequest);
		
		PlotDirectedSparseGraph nestedGoal = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeIntention(2);
		v3 = makePolyemotional(3);
		nestedGoal.addEdge(makeMotivation(), v1, v2);
		nestedGoal.addEdge(makeActualization(), v2, v3);
		NESTED_GOAL = new FunctionalUnit("Nested Goal", nestedGoal);
		
		PlotDirectedSparseGraph retaliation = new PlotDirectedSparseGraph();
		v1 = makeWildcard(1);
		v2 = makeNegative(1);
		retaliation.addEdge(makeCommunication(), v1, v2);
		v3 = makeIntention(2);
		retaliation.addEdge(makeMotivation(), v2, v3);
		v2 = makeWildcard(3);
		retaliation.addEdge(makeMotivation(), v3, v2);
		v1 = makeIntention(3);
		retaliation.addEdge(makeCommunication(), v2, v1);
		v2 = makeNegative(4);
		retaliation.addEdge(makeActualization(), v1, v2);
		RETALIATION = new FunctionalUnit("Retaliation", retaliation);
		
		PlotDirectedSparseGraph intentionalProblemResolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1);
		v2 = makeIntention(2);
		v3 = makePositive(3);
		intentionalProblemResolution.addEdge(makeMotivation(), v1, v2);
		intentionalProblemResolution.addEdge(makeActualization(), v2, v3);
		intentionalProblemResolution.addEdge(makeTermination(), v3, v1);
		INTENTIONAL_PROBLEM_RESOLUTION = new FunctionalUnit("Intentional Problem Resolution", intentionalProblemResolution);
		
		PlotDirectedSparseGraph fortuitousProblemResolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1);
		v2 = makeIntention(2);
		v3 = makePositive(3);
		fortuitousProblemResolution.addEdge(makeMotivation(), v1, v2);
		fortuitousProblemResolution.addEdge(makeTermination(), v3, v1);
		FORTUITOUS_PROBLEM_RESOLUTION = new FunctionalUnit("Fortuitous Problem Resolution", fortuitousProblemResolution);
		
		PlotDirectedSparseGraph successBornOfAdversity = new PlotDirectedSparseGraph();
		v1 = makeNegative(1);
		v2 = makeIntention(2);
		v3 = makePositive(3);
		successBornOfAdversity.addEdge(makeMotivation(), v1, v2);
		successBornOfAdversity.addEdge(makeActualization(), v2, v3);
		SUCCESS_BORN_OF_ADVERSITY = new FunctionalUnit("Success born of Adversity", successBornOfAdversity);
		
		PlotDirectedSparseGraph fleetingSuccess = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makePositive(2);
		v3 = makeNegative(3);
		fleetingSuccess.addEdge(makeActualization(), v1, v2);
		fleetingSuccess.addEdge(makeTermination(), v3, v2);
		FLEETING_SUCCESS = new FunctionalUnit("Fleeting Success", fleetingSuccess);
		
		PlotDirectedSparseGraph startingOver = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makePositive(2);
		v3 = makeNegative(3);
		startingOver.addEdge(makeActualization(), v1, v2);
		startingOver.addEdge(makeTermination(), v3, v2);
		v2 = makeIntention(4);
		startingOver.addEdge(makeMotivation(), v3, v2);
		startingOver.addEdge(makeEquivalence(), v2, v1);
		STARTING_OVER = new FunctionalUnit("Starting Over", startingOver);
		
		PlotDirectedSparseGraph givingUp = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeNegative(2);
		v3 = makeIntention(3);
		givingUp.addEdge(makeActualization(), v1, v2);
		givingUp.addEdge(makeMotivation(), v2, v3);
		givingUp.addEdge(makeTermination(), v3, v1);
		GIVING_UP = new FunctionalUnit("Giving Up", givingUp);
		
		PlotDirectedSparseGraph sacrifice = new PlotDirectedSparseGraph();
		v1 = makePositive(1);
		v2 = makeIntention(2);
		v3 = makePositive(3);
		sacrifice.addEdge(makeActualization(), v2, v3);
		sacrifice.addEdge(makeTermination(), v3, v1);
		SACRIFICE = new FunctionalUnit("Sacrifice", sacrifice);
		
		PlotDirectedSparseGraph honoredRequest = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makeIntention(1);
		honoredRequest.addEdge(makeCommunication(), v1, v2);

		v3 = makeIntention(2);
		honoredRequest.addEdge(makeMotivation(), v2, v3);

		v2 = makePositive(3);
		honoredRequest.addEdge(makeCommunication(), v3, v2);
		HONORED_REQUEST = new FunctionalUnit("Honored Request", honoredRequest);
		
		ALL[0] = DENIED_REQUEST;
		ALL[1] = NESTED_GOAL;
		ALL[2] = RETALIATION;
		ALL[3] = INTENTIONAL_PROBLEM_RESOLUTION;
		ALL[4] = FORTUITOUS_PROBLEM_RESOLUTION;
		ALL[5] = SUCCESS_BORN_OF_ADVERSITY;
		ALL[6] = FLEETING_SUCCESS;
		ALL[7] = STARTING_OVER;
		ALL[8] = GIVING_UP;
		ALL[9] = SACRIFICE;
		ALL[10] = HONORED_REQUEST;
		
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
		v2 = makePolyemotional(2);
		speech.addEdge(makeCommunication(), v1, v2);
		DEBUG_SPEECH = new FunctionalUnit("Debug Speech", speech);
		
		PlotDirectedSparseGraph termination = new PlotDirectedSparseGraph();
		v1 = makeIntention(1);
		v2 = makePolyemotional(2);
		termination.addEdge(makeTermination(), v2, v1);
		DEBUG_TERMINATION = new FunctionalUnit("Debug Termination", termination);
	}
	
	private static Vertex makeIntention(int step) {
		return new Vertex("!intention", Vertex.Type.INTENTION, step);
	}
	
	private static Vertex makePositive(int step) {
		Vertex vertex = new Vertex("+", Type.PERCEPT, step);
		vertex.addEmotion("love");
		return vertex;
	}
	
	private static Vertex makeNegative(int step) {
		Vertex vertex = new Vertex("-", Type.PERCEPT, step);
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makePolyemotional(int step) {
		Vertex vertex = new Vertex("*", Type.PERCEPT, step);
		vertex.addEmotion("love");
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makeWildcard(int step) {
		Vertex vertex = new Vertex("?", Vertex.Type.WILDCARD, step);
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
