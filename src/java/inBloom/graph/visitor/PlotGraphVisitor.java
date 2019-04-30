package inBloom.graph.visitor;

import inBloom.graph.Edge;
import inBloom.graph.Vertex;

/**
 * Interface for visiting a {@link PlotDirectedSparseGraph}.
 * Provides different methods for visiting the different vertex types,
 * as well as a method {@link #visitEdge(Edge) visitEdge} to control
 * the visitation.
 * @author Sven Wilke
 */
public interface PlotGraphVisitor {

	public void visitRoot		(Vertex vertex);
	public void visitEvent		(Vertex vertex);
	public void visitAction		(Vertex vertex);
	public void visitEmotion	(Vertex vertex);
	public void visitPercept	(Vertex vertex);
	public void visitSpeech		(Vertex vertex);
	public void visitListen		(Vertex vertex);
	public void visitIntention	(Vertex vertex);
	
	/**
	 * Called when visiting an edge.
	 * Used by {@link inBloom.graph.PlotDirectedSparseGraph#accept(PlotGraphVisitor) PlotDirectedSparseGraph} to determine how to continue the visitation.
	 * @param edge Edge to be visited
	 * @return EdgeVisitResult defining how the visitation behaves. {@see inBloom.graph.visitor.EdgeVisitResult}
	 */
	public EdgeVisitResult visitEdge(Edge edge);
}
