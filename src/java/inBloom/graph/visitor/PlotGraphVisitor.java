package inBloom.graph.visitor;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Defines the interface for visiting a {@link PlotDirectedSparseGraph}.  Provides different methods for visiting the
 * different vertex types, as well as a method {@link #visitEdge(Edge) visitEdge} to control the visitation. </br>
 * Visits and returns a clone of the graph provided to {@link #apply(PlotDirectedSparseGraph)}, unless the executed
 * visitor subclass overrides the apply method.
 * @author Sven Wilke
 */
public abstract class PlotGraphVisitor {
	protected PlotDirectedSparseGraph graph;

	public abstract void visitRoot			(Vertex vertex);
	public abstract void visitEvent			(Vertex vertex);
	public abstract void visitAction		(Vertex vertex);
	public abstract void visitEmotion		(Vertex vertex);
	public abstract void visitPercept		(Vertex vertex);
	public abstract void visitSpeech		(Vertex vertex);
	public abstract void visitListen		(Vertex vertex);
	public abstract void visitIntention		(Vertex vertex);

	/**
	 * Called when visiting an edge.
	 * Used by {@link inBloom.graph.PlotDirectedSparseGraph#accept(PlotGraphVisitor) PlotDirectedSparseGraph#accept(PlotGraphVisitor)}
	 * to determine how to continue the visitation.
	 * @param edge Edge to be visited
	 * @return EdgeVisitResult defining how the visitation behaves. {@see inBloom.graph.visitor.EdgeVisitResult}
	 */
	public EdgeVisitResult visitEdge(Edge edge) {
		switch(edge.getType()) {
			case ROOT:
			case TEMPORAL:
				return EdgeVisitResult.CONTINUE;
			default:
				return EdgeVisitResult.TERMINATE;
		}
	}

	/**
	 * Initiates the visiting process on graph. Usually implemented via double dispatch by calling {@code graph.accept(this)}.
	 * @param graph the graph to be visited
	 * @return the visited graph with all resulting modifications
	 */
	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph.clone();
		this.graph.accept(this);
		return this.graph;
	}
}
