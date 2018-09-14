package plotmas.graph.visitor;

/**
 * Used by {@link PlotGraphVisitor#visitEdge(Edge) PlotGraphVisitor#visitEdge}, handled in {@link PlotDirectedSparseGraph#accept(PlotGraphVisitor) PlotDirectedSparseGraph#accept}.
 * 
 * CONTINUE 	puts the destination of the edge into a queue to be processed later,				(breadth-first search)
 * DIRECT 		put the destination of the edge at the head of the queue for direct processing	(depth-first search)
 * TERMINATE 	stops the visitation along this edge.
 * @author Sven Wilke
 */
public enum EdgeVisitResult {
	CONTINUE, DIRECT, TERMINATE
}
