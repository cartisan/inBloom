package inBloom.graph.visitor;

import java.util.Collection;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Computes and sets the offsets of a graph's edges for later display.
 * @author Sven Wilke
 */
public class EdgeLayoutVisitor implements PlotGraphVisitor {
	
	/**
	 * Number of pixels between two edges.
	 */
	private static final int EDGE_SPACING = 9;
	
	private PlotDirectedSparseGraph graph;
	private Vertex[] occupanceLeft;
	private Vertex[] occupanceRight;
	
	/**
	 * Creates a visitor for a given graph, with maximally <i>numLanes</i> columns
	 * in either direction. 
	 * @param g
	 * @param numLanes
	 */
	public EdgeLayoutVisitor(int numLanes) {
		this.occupanceLeft = new Vertex[numLanes];
		this.occupanceRight = new Vertex[numLanes];
	}
	

	@Override
	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph;
		this.graph.accept(this);
		return this.graph;
	}
	
	private void visitVertex(Vertex vertex) {
		for(int i = 0; i < this.occupanceLeft.length; i++) {
			if(this.occupanceLeft[i] == vertex) {
				this.occupanceLeft[i] = null;
			}
			if(this.occupanceRight[i] == vertex) {
				this.occupanceRight[i] = null;
			}
		}
		Collection<Edge> termEdges = this.graph.getInEdges(vertex);
		for(Edge tEdge : termEdges) {
			if(tEdge.getType() == Edge.Type.TERMINATION || tEdge.getType() == Edge.Type.EQUIVALENCE) {
				Vertex terminationSource = this.graph.getSource(tEdge);
				int lane = this.getFreeLaneRight();
				this.occupanceRight[lane] = terminationSource;
				int offset = EDGE_SPACING + lane * EDGE_SPACING;
				tEdge.setOffset(offset);
				vertex.minWidth = Math.max(vertex.minWidth, offset);
				terminationSource.minWidth = Math.max(terminationSource.minWidth, offset);
			}
		}
	}
	
	@Override
	public EdgeVisitResult visitEdge(Edge edge) {
		Edge.Type type = edge.getType();
		if(type == Edge.Type.TEMPORAL || type == Edge.Type.ROOT) {
			return EdgeVisitResult.CONTINUE;
		}
		if(type == Edge.Type.MOTIVATION || type == Edge.Type.CAUSALITY) {
			int lane = getFreeLaneLeft();
			this.occupanceLeft[lane] = graph.getDest(edge);
			int offset = EDGE_SPACING + lane * EDGE_SPACING;
			edge.setOffset(offset);
			Vertex vertexA = this.graph.getDest(edge);
			Vertex vertexB = this.graph.getSource(edge);
			vertexA.minWidth = Math.max(vertexA.minWidth, offset);
			vertexB.minWidth = Math.max(vertexB.minWidth, offset);
		}
		return EdgeVisitResult.TERMINATE;
	}
	
	private int getFreeLaneLeft() {
		for(int i = 0; i < this.occupanceLeft.length; i++) {
			if(this.occupanceLeft[i] == null) {
				return i;
			}
		}
		return 0;
	}
	
	private int getFreeLaneRight() {
		for(int i = 0; i < this.occupanceRight.length; i++) {
			if(this.occupanceRight[i] == null) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void visitRoot(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitEvent(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitPercept(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitListen(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitIntention(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitAction(Vertex vertex) {
		this.visitVertex(vertex);
	}
}
