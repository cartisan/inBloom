package inBloom.graph.visitor;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Adapter of PlotGraphVisitor interface with an empty implementation for each method.
 * @author Sven Wilke
 */
public abstract class PlotGraphVisitorAdapter implements PlotGraphVisitor {

	@Override
	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		graph.accept(this);
		return graph;
	}
	
	public void visitVertex(Vertex vertex) {}
	
	@Override
	public void visitRoot(Vertex vertex) {}

	@Override
	public void visitEvent(Vertex vertex) {}

	@Override
	public void visitEmotion(Vertex vertex) {}

	@Override
	public void visitPercept(Vertex vertex) {}

	@Override
	public void visitSpeech(Vertex vertex) {}

	@Override
	public void visitListen(Vertex vertex) {}

	@Override
	public void visitIntention(Vertex vertex) {}

	@Override
	public abstract EdgeVisitResult visitEdge(Edge edge);
}
