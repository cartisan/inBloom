package plotmas.graph.visitor;

import java.util.Collection;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

public class EdgeLayoutVisitor implements PlotGraphVisitor {
	
	private static final int EDGE_SPACING = 9;
	
	private PlotDirectedSparseGraph graph;
	private Vertex[] occupanceLeft;
	private Vertex[] occupanceRight;
	
	public EdgeLayoutVisitor(PlotDirectedSparseGraph g, int numLanes) {
		this.graph = g;
		this.occupanceLeft = new Vertex[numLanes];
		this.occupanceRight = new Vertex[numLanes];
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
			}
		}
	}
	
	@Override
	public EdgeVisitResult visitEdge(Edge edge) {
		Edge.Type type = edge.getType();
		if(type == Edge.Type.TEMPORAL || type == Edge.Type.ROOT) {
			return EdgeVisitResult.CONTINUE;
		}
		if(type == Edge.Type.MOTIVATION) {
			int lane = getFreeLaneLeft();
			this.occupanceLeft[lane] = graph.getDest(edge);
			int offset = EDGE_SPACING + lane * EDGE_SPACING;
			edge.setOffset(offset);
			Vertex vertex = this.graph.getDest(edge);
			vertex.minWidth = Math.max(vertex.minWidth, offset);
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
}
