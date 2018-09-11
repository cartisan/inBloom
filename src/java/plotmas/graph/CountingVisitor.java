package plotmas.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import jason.util.Pair;
import plotmas.graph.Vertex.Type;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.helper.Triple;

public class CountingVisitor implements PlotGraphVisitor {
    
	protected static Logger logger = Logger.getLogger(CountingVisitor.class.getName());
	
	public HashMap<String, Integer> conflictCounter;						 // agentName --> conflictNum
	public HashMap<String, List<Pair<Vertex, Vertex>>> productiveConflicts;  // agentName --> [(Intention, Resolution), (...), ...]
	public Table<String, Vertex, List<Vertex>> motivationChains = HashBasedTable.create();
	public Triple<String, Vertex, Vertex> mostSuspensefulIntention;			 // (agent, intention, action)
	
	private int highestStep = 0;		// highest environment step encountered in story (= plot length in steps)
	private int vertexNum = 0;		// overall number of events in the plot  
	
	private String currentRoot;
	private PlotDirectedSparseGraph graph;
	
	public CountingVisitor apply(PlotDirectedSparseGraph graph) {
		conflictCounter = new HashMap<>();
		productiveConflicts = new HashMap<>();
		
		this.graph = graph;
		this.graph.accept(this);
		return this;
	}
	

	@Override
	public void visitRoot(Vertex vertex) {
		// new character, add it to all counters and note that we are processing it's subtree
		this.currentRoot = vertex.getLabel();
		
		conflictCounter.put(currentRoot, 0);
		productiveConflicts.put(currentRoot, new LinkedList<>());
	}

	@Override
	public void visitIntention(Vertex vertex) {
		updateSimpleVertexCounts(vertex);
		
		// each intention demonstrates a conflict
		conflictCounter.put(this.currentRoot,
							conflictCounter.get(this.currentRoot) + 1);
	}

	@Override
	public void visitEvent(Vertex vertex) {
		logger.severe("No EVENT vertices should be left by this stage of preprocessing: " + vertex.getLabel());
		logger.severe("Count of vertices might be not rliable anymore");
	}

	@Override
	public void visitAction(Vertex vertex) {
		updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitPercept(Vertex vertex) {
		updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		updateSimpleVertexCounts(vertex);
	}

	@Override
	public void visitListen(Vertex vertex) {
		updateSimpleVertexCounts(vertex);
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
			this.productiveConflicts.get(this.currentRoot).add(new Pair<>(graph.getSource(edge), graph.getDest(edge)));
		}

		if(type == Edge.Type.TERMINATION) {
			if(graph.getDest(edge).getType() == Type.INTENTION) {
				this.productiveConflicts.get(this.currentRoot).add(new Pair<>(graph.getDest(edge), graph.getSource(edge)));
			}
		}

		if(type == Edge.Type.MOTIVATION) {
			Vertex src =  graph.getSource(edge);
			Vertex dest = graph.getDest(edge);
			
			// note the motivator of this vertex
			LinkedList<Vertex> motivators = new LinkedList<>();
			motivators.add(src);
			
			// append all motivators, that the motivator might have had
			// this is where recursive intentions like !relax in RedHen get filtered out: src in that case is the
			// later intention, whose motivators where not yet processes
			if (motivationChains.contains(this.currentRoot, src)) {
				List<Vertex> previousMotivators = motivationChains.get(this.currentRoot, src);
				motivators.addAll(previousMotivators);
			}
			
			motivationChains.put(this.currentRoot, dest, motivators);
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
				
				if (motivationChains.contains(agent, intention)) {
					List<Vertex> motivations = motivationChains.get(agent, intention); 
					intention = motivations.get(motivations.size() - 1);
				}
				
				int localSuspense = action.getStep() - intention.getStep();
				
				if (suspense < localSuspense) {
					suspense = localSuspense;
					mostSuspensefulIntention = new Triple<>(agent, intention, action);
				}
			}
		}
		
		logger.info("Maximal suspense: " + suspense + ": " +
					mostSuspensefulIntention.getFirst() + "'s (" + 
					mostSuspensefulIntention.getSecond().toString() + ", " +
					mostSuspensefulIntention.getThird().toString() + ")");
		
		return suspense;
	}

	public int getVertexNum() {
		return this.vertexNum;
	}
	
	public int getPlotLength() {
		return this.highestStep;
	}
	

	/**
	 * Updates all counts that aggregate statistics over all vertices in an analyzed plot graph
	 * @param vertex Vertex to be included into simple plot statistics
	 */
	private void updateSimpleVertexCounts(Vertex vertex) {
		this.vertexNum += 1;
		if (vertex.getStep() > this.highestStep)
			this.highestStep = vertex.getStep();
	}

}
