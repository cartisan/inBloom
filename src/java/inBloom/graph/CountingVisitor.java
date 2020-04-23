package inBloom.graph;

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
import inBloom.helper.Triple;

/**
 * Performs counting in order to compute plot statistics. Unlike common visitors, this does not modify the graph.
 * @see inBloom.graph.visitor.PlotGraphVisitor#apply(inBloom.graph.PlotDirectedSparseGraph)
 */
public class CountingVisitor extends PlotGraphVisitor {

	protected static Logger logger = Logger.getLogger(CountingVisitor.class.getName());

	public HashMap<String, Integer> conflictCounter;						 // agentName --> conflictNum
	public HashMap<String, List<Pair<Vertex, Vertex>>> productiveConflicts;  // agentName --> [(Intention, Resolution), (...), ...]
	public Table<String, Vertex, List<Vertex>> motivationChains = HashBasedTable.create();
	public Triple<String, Vertex, Vertex> mostSuspensefulIntention;			 // (agent, intention, action)

	private int lowestStep = Integer.MAX_VALUE;		// lowest environment step encountered in story (= plot start in steps)
	private int highestStep = Integer.MIN_VALUE;	// highest environment step encountered in story (= plot end in steps)
	private int vertexNum = 0;					    // overall number of events in the plot

	private String currentRoot;


	public CountingVisitor() {
		this.conflictCounter = new HashMap<>();
		this.productiveConflicts = new HashMap<>();
	}

	@Override
	public void visitRoot(Vertex vertex) {
		// new character, add it to all counters and note that we are processing it's subtree
		this.currentRoot = vertex.getLabel();

		this.conflictCounter.put(this.currentRoot, 0);
		this.productiveConflicts.put(this.currentRoot, new LinkedList<>());
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
		logger.severe("No EVENT vertices should be left by this stage of preprocessing: " + vertex.getLabel());
		logger.severe("Count of vertices might be not rliable anymore");
	}

	@Override
	public void visitAction(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitPercept(Vertex vertex) {
		this.updateSimpleVertexCounts(vertex);
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
		// Nothing to do here, war emotions should not be found in analyzed graphs
	}

	@Override
	public EdgeVisitResult visitEdge(Edge edge) {
		Edge.Type type = edge.getType();
		if(type == Edge.Type.TEMPORAL || type == Edge.Type.ROOT) {
			return EdgeVisitResult.CONTINUE;
		}

		if(type == Edge.Type.ACTUALIZATION) {
			this.productiveConflicts.get(this.currentRoot).add(new Pair<>(this.graph.getSource(edge), this.graph.getDest(edge)));
		}

		if(type == Edge.Type.TERMINATION) {
			if(this.graph.getDest(edge).getType() == Type.INTENTION) {
				this.productiveConflicts.get(this.currentRoot).add(new Pair<>(this.graph.getDest(edge), this.graph.getSource(edge)));
			}
		}

		if(type == Edge.Type.MOTIVATION) {
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

	/**
	 * Returns the biggest number of environment steps that was necessary to resolve an intention.
	 * @return
	 */
	public int getSuspense(){
		int suspense  = 0;

		for (String agent : this.productiveConflicts.keySet()) {
			List<Pair<Vertex, Vertex>> confPairs = this.productiveConflicts.get(agent);

			for (Pair<Vertex, Vertex> pair: confPairs) {
				Vertex intention = pair.getFirst();
				Vertex action = pair.getSecond();

				if (this.motivationChains.contains(agent, intention)) {
					List<Vertex> motivations = this.motivationChains.get(agent, intention);
					intention = motivations.get(motivations.size() - 1);
				}

				int localSuspense = action.getStep() - intention.getStep();

				if (suspense < localSuspense) {
					suspense = localSuspense;
					this.mostSuspensefulIntention = new Triple<>(agent, intention, action);
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

		return suspense;
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
	}

}
