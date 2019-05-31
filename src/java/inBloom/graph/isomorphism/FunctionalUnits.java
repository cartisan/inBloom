package inBloom.graph.isomorphism;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.Vertex.Type;
import inBloom.graph.visitor.EdgeLayoutVisitor;
import jason.util.Pair;

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
	
	/**
	 * Holds all primitive functional units.
	 */
	public static FunctionalUnit[] PRIMITIVES;

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
	
	
	
	/**
	 * A graph containing all functional units in {@link #ALL ALL} next
	 * to each other to be used for displaying them.
	 */
	public static final PlotDirectedSparseGraph ALL_UNITS_GRAPH;
	
	static {
		ALL = new FunctionalUnit[11];
		
		Vertex v1, v2, v3;
		
		PlotDirectedSparseGraph deniedRequest = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, deniedRequest);
		v2 = makeNegative(1, deniedRequest);
		deniedRequest.addEdge(makeCommunication(), v1, v2);
		v3 = makeIntention(2, deniedRequest);
		deniedRequest.addEdge(makeMotivation(), v2, v3);
		v2 = makeIntention(3, deniedRequest);
		deniedRequest.addEdge(makeMotivation(), v3, v2);		
		v3 = makeNegative(4, deniedRequest);
		deniedRequest.addEdge(makeCommunication(), v2, v3);
		DENIED_REQUEST = new FunctionalUnit("Denied Request", deniedRequest);
		DENIED_REQUEST.setSubject(new Pair<Vertex, String>(v1, "request\\((.*)\\)"));
		
		PlotDirectedSparseGraph nestedGoal = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, nestedGoal);
		v2 = makeIntention(2, nestedGoal);
		v3 = makePolyemotional(3, nestedGoal);
		nestedGoal.addEdge(makeMotivation(), v1, v2);
		nestedGoal.addEdge(makeActualization(), v2, v3);
		NESTED_GOAL = new FunctionalUnit("Nested Goal", nestedGoal);
		NESTED_GOAL.setSubject(new Pair<Vertex, String>(v1, "(.*)"));
		
		PlotDirectedSparseGraph retaliation = new PlotDirectedSparseGraph();
		v1 = makeWildcard(1, retaliation);
		v2 = makeNegative(1, retaliation);
		retaliation.addEdge(makeCommunication(), v1, v2);
		v3 = makeIntention(2, retaliation);
		retaliation.addEdge(makeMotivation(), v2, v3);
		v2 = makeWildcard(3, retaliation);
		retaliation.addEdge(makeMotivation(), v3, v2);
		v1 = makeIntention(3, retaliation);
		retaliation.addEdge(makeCommunication(), v2, v1);
		v2 = makeNegative(4, retaliation);
		retaliation.addEdge(makeActualization(), v1, v2);
		RETALIATION = new FunctionalUnit("Retaliation", retaliation);
		RETALIATION.setSubject(new Pair<Vertex, String>(v3, "(.*)"));
		
		PlotDirectedSparseGraph intentionalProblemResolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, intentionalProblemResolution);
		v2 = makeIntention(2, intentionalProblemResolution);
		v3 = makePositive(3, intentionalProblemResolution);
		intentionalProblemResolution.addEdge(makeMotivation(), v1, v2);
		intentionalProblemResolution.addEdge(makeActualization(), v2, v3);
		intentionalProblemResolution.addEdge(makeTermination(), v3, v1);
		INTENTIONAL_PROBLEM_RESOLUTION = new FunctionalUnit("Intentional Problem Resolution", intentionalProblemResolution);
		
		PlotDirectedSparseGraph fortuitousProblemResolution = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, fortuitousProblemResolution);
		v2 = makeIntention(2, fortuitousProblemResolution);
		v3 = makePositive(3, fortuitousProblemResolution);
		fortuitousProblemResolution.addEdge(makeMotivation(), v1, v2);
		fortuitousProblemResolution.addEdge(makeTermination(), v3, v1);
		FORTUITOUS_PROBLEM_RESOLUTION = new FunctionalUnit("Fortuitous Problem Resolution", fortuitousProblemResolution);
		
		PlotDirectedSparseGraph successBornOfAdversity = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, successBornOfAdversity);
		v2 = makeIntention(2, successBornOfAdversity);
		v3 = makePositive(3, successBornOfAdversity);
		successBornOfAdversity.addEdge(makeMotivation(), v1, v2);
		successBornOfAdversity.addEdge(makeActualization(), v2, v3);
		SUCCESS_BORN_OF_ADVERSITY = new FunctionalUnit("Success born of Adversity", successBornOfAdversity);
		
		PlotDirectedSparseGraph fleetingSuccess = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, fleetingSuccess);
		v2 = makePositive(2, fleetingSuccess);
		v3 = makeNegative(3, fleetingSuccess);
		fleetingSuccess.addEdge(makeActualization(), v1, v2);
		fleetingSuccess.addEdge(makeTermination(), v3, v2);
		FLEETING_SUCCESS = new FunctionalUnit("Fleeting Success", fleetingSuccess);
		
		PlotDirectedSparseGraph startingOver = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, startingOver);
		v2 = makePositive(2, startingOver);
		v3 = makeNegative(3, startingOver);
		startingOver.addEdge(makeActualization(), v1, v2);
		startingOver.addEdge(makeTermination(), v3, v2);
		v2 = makeIntention(4, startingOver);
		startingOver.addEdge(makeMotivation(), v3, v2);
		startingOver.addEdge(makeEquivalence(), v2, v1);
		STARTING_OVER = new FunctionalUnit("Starting Over", startingOver);
		
		PlotDirectedSparseGraph givingUp = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, givingUp);
		v2 = makeNegative(2, givingUp);
		v3 = makeIntention(3, givingUp);
		givingUp.addEdge(makeActualization(), v1, v2);
		givingUp.addEdge(makeMotivation(), v2, v3);
		givingUp.addEdge(makeTermination(), v3, v1);
		GIVING_UP = new FunctionalUnit("Giving Up", givingUp);
		
		PlotDirectedSparseGraph sacrifice = new PlotDirectedSparseGraph();
		v1 = makePositive(1, sacrifice);
		v2 = makeIntention(2, sacrifice);
		v3 = makePositive(3, sacrifice);
		sacrifice.addEdge(makeActualization(), v2, v3);
		sacrifice.addEdge(makeTermination(), v3, v1);
		SACRIFICE = new FunctionalUnit("Sacrifice", sacrifice);
		
		PlotDirectedSparseGraph honoredRequest = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, honoredRequest);
		v2 = makePositive(1, honoredRequest);
		honoredRequest.addEdge(makeCommunication(), v1, v2);
		v3 = makeIntention(2, honoredRequest);
		honoredRequest.addEdge(makeMotivation(), v2, v3);
		v2 = makeIntention(3, honoredRequest);
		honoredRequest.addEdge(makeMotivation(), v3, v2);		
		v3 = makePositive(4, honoredRequest);
		honoredRequest.addEdge(makeCommunication(), v2, v3);
		HONORED_REQUEST = new FunctionalUnit("Honored Request", honoredRequest);
		HONORED_REQUEST.setSubject(new Pair<Vertex, String>(v1, "request\\((.*)\\)"));
		
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
		allUnitsGraph = new EdgeLayoutVisitor(9).apply(allUnitsGraph);
		allUnitsGraph.setName("Functional Units");
		ALL_UNITS_GRAPH = allUnitsGraph;
		
		/**
		 * Primitive Units
		 */
		PRIMITIVES = new FunctionalUnit[24];
		
		PlotDirectedSparseGraph g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeMotivation(), v1, v2);
		PRIMITIVES[0] = new FunctionalUnit("Motivation", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeTermination(), v2, v1);
		PRIMITIVES[1] = new FunctionalUnit("Change of Mind", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeEquivalence(), v2, v1);
		PRIMITIVES[2] = new FunctionalUnit("Perseverance", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeMotivation(), v1, v2);
		PRIMITIVES[3] = new FunctionalUnit("Enablement", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeMotivation(), v1, v2);
		PRIMITIVES[4] = new FunctionalUnit("Problem", g);

		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeActualization(), v1, v2);
		PRIMITIVES[5] = new FunctionalUnit("Success", g);

		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeActualization(), v1, v2);
		PRIMITIVES[6] = new FunctionalUnit("Failure", g);

		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeTermination(), v2, v1);
		PRIMITIVES[7] = new FunctionalUnit("Loss", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeTermination(), v2, v1);
		PRIMITIVES[8] = new FunctionalUnit("Resolution", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeTermination(), v2, v1);
		PRIMITIVES[9] = new FunctionalUnit("Negative Trade-Off", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeTermination(), v2, v1);
		PRIMITIVES[10] = new FunctionalUnit("Positive Trade-Off", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeEquivalence(), v2, v1);
		PRIMITIVES[11] = new FunctionalUnit("Mixed Blessing", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeEquivalence(), v2, v1);
		PRIMITIVES[12] = new FunctionalUnit("Hidden Blessing", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeEquivalence(), v2, v1);
		PRIMITIVES[13] = new FunctionalUnit("Complex Positive Event", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeEquivalence(), v2, v1);
		PRIMITIVES[14] = new FunctionalUnit("Complex Negative Event", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[15] = new FunctionalUnit("Request", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[16] = new FunctionalUnit("CC Enablement", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makeIntention(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[17] = new FunctionalUnit("CC Motivation", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[18] = new FunctionalUnit("CC Threat", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeIntention(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[19] = new FunctionalUnit("CC Promise", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[20] = new FunctionalUnit("Shared Positive Event", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[21] = new FunctionalUnit("Shared Negative Event", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makePositive(1, g);
		v2 = makeNegative(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[22] = new FunctionalUnit("CC Mixed Blessing", g);
		
		g = new PlotDirectedSparseGraph();
		v1 = makeNegative(1, g);
		v2 = makePositive(2, g);
		g.addEdge(makeCommunication(), v1, v2);
		PRIMITIVES[23] = new FunctionalUnit("CC Hidden Blessing", g);
		
		for(FunctionalUnit unit : PRIMITIVES) {
			unit.setPrimitive();
		}
	}
	
	private static Vertex makeIntention(int step, PlotDirectedSparseGraph graph) {
		return new Vertex("!intention", Vertex.Type.INTENTION, step, graph);
	}
	
	private static Vertex makePositive(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("+", Type.PERCEPT, step, graph);
		vertex.addEmotion("love");
		return vertex;
	}
	
	private static Vertex makeNegative(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("-", Type.PERCEPT, step, graph);
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makePolyemotional(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("*", Type.PERCEPT, step, graph);
		vertex.addEmotion("love");
		vertex.addEmotion("hate");
		return vertex;
	}
	
	private static Vertex makeWildcard(int step, PlotDirectedSparseGraph graph) {
		Vertex vertex = new Vertex("?", Vertex.Type.WILDCARD, step, graph);
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
