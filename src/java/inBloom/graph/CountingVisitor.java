package inBloom.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import jason.util.Pair;

import inBloom.graph.Vertex.Type;
import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;

/**
 * Performs counting in order to compute plot statistics. Unlike common visitors, this does not modify the graph.
 * @see inBloom.graph.visitor.PlotGraphVisitor#apply(inBloom.graph.PlotDirectedSparseGraph)
 */
public class CountingVisitor extends PlotGraphVisitor {

	protected static Logger logger = Logger.getLogger(CountingVisitor.class.getName());

	public List<String> agents;
	public HashMap<String, Integer> conflictCounter;						 // agentName --> conflictNum
	public HashMap<String, List<Pair<Vertex, Vertex>>> productiveConflicts;  // agentName --> [(Intention, Resolution), (...), ...]
	public HashMap<String, List<Vertex>> terminatedPercepts;				 // agentName --> [vertex1, vertex2, ...]
	public HashMap<String, List<Vertex>> violatedExpectationEvents;		 // agentName --> [vertex1, vertex2, ...]
	public HashMap<String, List<Vertex>> emotionalEvents;					 // agentName --> [vertex1, vertex2, ...]
	public HashMap<String, Integer> overallPerceptNum;						 // agentName --> int
	public Table<String, Vertex, List<Vertex>> motivationChains = HashBasedTable.create();

	private int lowestStep = Integer.MAX_VALUE;		// lowest environment step encountered in story (= plot start in steps)
	private int highestStep = Integer.MIN_VALUE;	// highest environment step encountered in story (= plot end in steps)
	private int vertexNum = 0;					    // overall number of events in the plot

	private String currentRoot;





	public CountingVisitor() {
		this.agents = new ArrayList<>();
		this.conflictCounter = new HashMap<>();
		this.productiveConflicts = new HashMap<>();
		this.terminatedPercepts = new HashMap<>();
		this.violatedExpectationEvents = new HashMap<>();
		this.emotionalEvents = new HashMap<>();
		this.overallPerceptNum = new HashMap<>();
	}

	@Override
	public void visitRoot(Vertex vertex) {
		// new character, add it to all counters and note that we are processing it's subtree
		this.currentRoot = vertex.getLabel();
		this.agents.add(this.currentRoot);

		this.conflictCounter.put(this.currentRoot, 0);
		this.productiveConflicts.put(this.currentRoot, new ArrayList<>());
		this.terminatedPercepts.put(this.currentRoot, new ArrayList<>());
		this.violatedExpectationEvents.put(this.currentRoot, new ArrayList<>());
		this.emotionalEvents.put(this.currentRoot, new ArrayList<>());
		this.overallPerceptNum.put(this.currentRoot, 0);
	}

	@Override
	public void visitIntention(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);

		// each intention demonstrates a conflict
		this.conflictCounter.put(this.currentRoot,
							this.conflictCounter.get(this.currentRoot) + 1);
	}

	@Override
	public void visitEvent(Vertex vertex) {
		logger.severe("No EVENT vertices should be left by this stage of preprocessing, found: " + vertex.getLabel());
		logger.severe("Count of vertices might be not rliable anymore");
	}

	@Override
	public void visitAction(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitPercept(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);

		int newCount = this.overallPerceptNum.get(this.currentRoot) + 1;
		this.overallPerceptNum.put(this.currentRoot, newCount);
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitListen(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		logger.severe("No EMOTION vertices should be left by this stage of preprocessing, found: " + vertex.getLabel());
	}

	@Override
	public EdgeVisitResult visitEdge(Edge edge) {
		Edge.Type type = edge.getType();
		if(type == Edge.Type.TEMPORAL || type == Edge.Type.ROOT) {
			return EdgeVisitResult.CONTINUE;
		}

		if(type == Edge.Type.ACTUALIZATION) {	// [I] -a->[+/-]
			this.productiveConflicts.get(this.currentRoot).add(new Pair<>(this.graph.getSource(edge), this.graph.getDest(edge)));
		}

		else if(type == Edge.Type.TERMINATION) {	// [I] <-t- [I]
			Vertex destination = this.graph.getDest(edge);
			if(destination.getType() == Type.INTENTION) {
				this.productiveConflicts.get(this.currentRoot).add(new Pair<>(destination, this.graph.getSource(edge)));
			}

			else if(destination.getType() == Type.PERCEPT) { 	// [P] <-t- [*]
				this.terminatedPercepts.get(this.currentRoot).add(destination);
			}
		}

		else if(type == Edge.Type.MOTIVATION & this.graph.getSource(edge).getType() == Type.INTENTION ) {
			Vertex src =  this.graph.getSource(edge);
			Vertex dest = this.graph.getDest(edge);

			// note the motivator of this vertex
			LinkedList<Vertex> motivators = new LinkedList<>();
			motivators.add(src);

			// append all motivators, that the motivator might have had
			// this is where recursive intentions like !relax in RedHen get filtered out: src in that case is the
			// later intention, whose motivators where not yet processes
			if (this.motivationChains.contains(this.currentRoot, src)) {
				List<Vertex> previousMotivators = this.motivationChains.get(this.currentRoot, src);
				motivators.addAll(previousMotivators);
			}

			this.motivationChains.put(this.currentRoot, dest, motivators);
		}
		return EdgeVisitResult.TERMINATE;
	}

	/**
	 * Returns the number of conflicts in a plot, equivalent to the number of intentions of all agents.
	 * @return number of conflicts in the plot
	 */
	public int getConflictNumber() {
		int confNum = 0;

		for (int num : this.conflictCounter.values()) {
			confNum += num;
		}

		return confNum;
	}

	/**
	 * Returns the number of conflicts in a plot, equivalent to the number of intentions of all agents that have been
	 * either actualized or terminated.
	 * @return number of productive conflicts in the plot
	 */
	public int getProductiveConflictNumber() {
		int confNum = 0;

		for (List<?> l : this.productiveConflicts.values()) {
			confNum += l.size();
		}

		logger.info("Productive conflicts: " + confNum);
		return confNum;
	}

	public int getVertexNum() {
		return this.vertexNum;
	}

	public int getPlotLength() {
		return this.highestStep - this.lowestStep;
	}


	/**
	 * Updates all counts that aggregate statistics over all vertices in an analyzed plot graph
	 * @param vertex Vertex to be included into simple plot statistics
	 */
	private void updateSimpleVertexCounts(Vertex vertex) {
		this.vertexNum += 1;
		if (vertex.getStep() > this.highestStep) {
			this.highestStep = vertex.getStep();
		}

		if (vertex.getStep() < this.lowestStep) {
			this.lowestStep = vertex.getStep();
		}

		if(vertex.hasEmotion("disappointment") || vertex.hasEmotion("relief")) {
			this.violatedExpectationEvents.get(this.currentRoot).add(vertex);
		}

		if(vertex.hasEmotion()) {
			this.emotionalEvents.get(this.currentRoot).add(vertex);
		}
	}

}
