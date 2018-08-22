package plotmas.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.util.Pair;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;

public class ConflictVisitor implements PlotGraphVisitor {
    
	protected static Logger logger = Logger.getLogger(ConflictVisitor.class.getName());
	
	public HashMap<String, Integer> conflictCounter;
	public HashMap<String, List<Pair<Vertex, Vertex>>> productiveConflicts;  // agentName --> [(Intention, Resolution), (...), ...]
	
	private String currentRoot;
	private PlotDirectedSparseGraph graph;
	
	public ConflictVisitor apply(PlotDirectedSparseGraph graph) {
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
		// each intention demonstrates a conflict
		conflictCounter.put(this.currentRoot,
							conflictCounter.get(this.currentRoot) + 1);
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
		
		if(type == Edge.Type.ACTUALIZATION || type == Edge.Type.TERMINATION) {
			this.productiveConflicts.get(this.currentRoot).add(new Pair<>(graph.getDest(edge), graph.getSource(edge)));
		}
		
		return EdgeVisitResult.TERMINATE;		
	}

	@Override
	public void visitEvent(Vertex vertex) {
		// Nothing to do here
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		// Nothing to do here
	}

	@Override
	public void visitPercept(Vertex vertex) {
		// Nothing to do here
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		// Nothing to do here
	}

	@Override
	public void visitListen(Vertex vertex) {
		// Nothing to do here
	}
	
	public int getConflictNumber() {
		int confNum = 0;
		
		for (int num : this.conflictCounter.values()) {
			confNum += num;
		}
			
		return confNum;
	}

	public int getProductiveConflictNumber() {
		int confNum = 0;
		
		for (List<?> l : this.productiveConflicts.values()) {
			confNum += l.size();
		}
		
		logger.info("Productive conflicts: " + confNum);
		return confNum;
	}
	
	public int getSuspense(){
		int suspense  = 0;
		
		for (List<Pair<Vertex, Vertex>> confPairs : this.productiveConflicts.values()) {
			int localSuspense = confPairs.stream().mapToInt(pair -> pair.getSecond().getStep() - pair.getFirst().getStep()).max().orElse(0);
			suspense = (suspense > localSuspense ? suspense : localSuspense);
		}
		
		logger.info("Maximal suspense: " + suspense);
		return suspense;
	}
}
