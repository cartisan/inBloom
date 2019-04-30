package inBloom.graph.visitor;

import inBloom.graph.Edge;
import inBloom.graph.Vertex;

/**
 * Helper class used in {@link inBloom.graph.PlotDirectedSparseGraph#accept(PlotGraphVisitor) accept} to access
 * edges which were removed from the graph during the visitation.
 * @author Sven Wilke
 */
public class RemovedEdge {
	
	private Edge edge;
	private Vertex dest;
	
	public RemovedEdge(Edge edge, Vertex dest) {
		this.edge = edge;
		this.dest = dest;
	}
	
	public Edge getEdge() {
		return this.edge;
	}
	
	public Vertex getDest() {
		return this.dest;
	}
}
