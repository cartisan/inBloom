package inBloom.helper;

import java.util.Comparator;
import java.util.LinkedList;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Comparator for vertex order in a plot graph. Orders vertices by step, in case of equality by plot-edges, and in case
 * of further equality by label.
 *
 * @author Leonid Berov
 */
public class VertexOrderComparator implements Comparator<Vertex> {

	private PlotDirectedSparseGraph graph;

	public VertexOrderComparator(PlotDirectedSparseGraph graph) {
		this.graph = graph;
	}

	@Override
	public int compare(Vertex v1, Vertex v2) {
		if (!this.graph.containsVertex(v1) || !this.graph.containsVertex(v2)) {
			throw new RuntimeException("Vertices to be compared by order not part of the same graph");
		}

		int stepOrder = Comparator.comparingInt(Vertex::getStep).compare(v1, v2);
		// if vertices differ in step, their order is based on steps
		if (stepOrder != 0) {
			return stepOrder;
		}
		// if step is the same, check if v2 is predecessor of v1 by following in-edges
		LinkedList<Vertex> predsV1 = new LinkedList<>(this.graph.getRealPredecessors(v1));
		while(!predsV1.isEmpty()) {
			Vertex pred = predsV1.remove();
			if(pred.equals(v2)) {
				return 1;
			}
			predsV1.addAll(this.graph.getRealPredecessors(pred));
		}

		// check if v1 is predecessor of v2 by following in-edges
		LinkedList<Vertex> predsV2 = new LinkedList<>(this.graph.getRealPredecessors(v2));
		while(!predsV2.isEmpty()) {
			Vertex pred = predsV2.remove();
			if(pred.equals(v1)) {
				return -1;
			}
			predsV2.addAll(this.graph.getRealPredecessors(pred));
		}

		// if this fails too, order by label
		return v1.getLabel().compareTo(v2.getLabel());
	}
}
