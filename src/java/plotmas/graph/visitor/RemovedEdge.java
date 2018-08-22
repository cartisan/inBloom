package plotmas.graph.visitor;

import plotmas.graph.Edge;
import plotmas.graph.Vertex;

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
