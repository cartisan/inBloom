package plotmas.graph.visitor;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

public class EdgeLayoutVisitor implements PlotGraphVisitor {
	
	private PlotDirectedSparseGraph graph;
	private Vertex[] occupance;
	private int offsetStart;
	private int offsetStep;
	
	public EdgeLayoutVisitor(PlotDirectedSparseGraph g, int numLanes) {
		this.graph = g;
		this.occupance = new Vertex[numLanes];
		int totalWidth = 80;
		this.offsetStep = -totalWidth / numLanes;
		this.offsetStart = -(int)((float)this.offsetStep * ((float)numLanes / 2f));
	}
	
	private void visitVertex(Vertex vertex) {
		for(int i = 0; i < this.occupance.length; i++) {
			if(this.occupance[i] == vertex) {
				this.occupance[i] = null;
			}
		}
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
	public EdgeVisitResult visitEdge(Edge edge) {
		Edge.Type type = edge.getType();
		if(type == Edge.Type.TEMPORAL || type == Edge.Type.ROOT) {
			return EdgeVisitResult.CONTINUE;
		}
		if(type == Edge.Type.MOTIVATION) {
			int lane = getFreeLane();
			this.occupance[lane] = graph.getDest(edge);
			edge.setOffset(getOffset(lane));
		}
		return EdgeVisitResult.TERMINATE;
	}
	
	private int getOffset(int lane) {
		return this.offsetStart + this.offsetStep * lane;
	}
	
	private int getFreeLane() {
		for(int i = 0; i < this.occupance.length; i++) {
			if(this.occupance[i] == null) {
				return i;
			}
		}
		return 0;
	}

}
