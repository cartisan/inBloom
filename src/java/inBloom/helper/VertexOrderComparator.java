package inBloom.helper;

import java.util.Comparator;
import java.util.logging.Logger;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Comparator for vertex order in a plot graph. Orders vertices by step, in case of equality by plot-edges, and in case
 * of further equality by label.
 *
 * @author Leonid Berov
 */
public class VertexOrderComparator implements Comparator<Vertex> {
    static Logger logger = Logger.getLogger(VertexOrderComparator.class.getName());

	private PlotDirectedSparseGraph graph;

	public VertexOrderComparator(PlotDirectedSparseGraph graph) {
		this.graph = graph;
	}

	@Override
	public int compare(Vertex v1, Vertex v2) {
		if (!this.graph.containsVertex(v1) || !this.graph.containsVertex(v2)) {
			throw new RuntimeException("Vertices to be compared by order not part of the same graph");
		}

		logger.fine("Comparing: " + v1 + " and " + v2);

		int stepOrder = Comparator.comparingInt(Vertex::getStep).compare(v1, v2);
		// if vertices differ in step, their order is based on steps
		if (stepOrder != 0) {
			logger.fine("Result based on step order comparison: " + stepOrder);
			return stepOrder;
		}

		// Attention: FU graphs don't have temporal edges, so this will always return 0, in FUs we rely on subsequent vertices having a different step
		int innerStepOrder = Comparator.comparingInt(this.graph::getInnerStep).compare(v1, v2);
		if (innerStepOrder != 0) {
			logger.fine("Result based on inner step order comparison: " + innerStepOrder);
			return innerStepOrder;
		}

		// if this fails too, order by id
		logger.fine("Comparing by id");
		return v1.getId().compareTo(v2.getId());
	}
}
