package inBloom.helper;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

		// if step is the same, check if v2 is predecessor of v1 by following in-edges
		LinkedList<Vertex> predsV1 = new LinkedList<>(this.graph.getRealPredecessors(v1));
		while(!predsV1.isEmpty()) {
			Vertex pred = predsV1.remove();
			if(pred.equals(v2)) {
				logger.fine("Result based on v1 predicessor search");
				return 1;
			}
			List<Vertex> sameStepPreds = this.graph.getRealPredecessors(pred).stream()
																			 .filter(v -> v.getStep() == v1.getStep())
																			 .collect(Collectors.toList());
			predsV1.addAll(sameStepPreds);
		}

		// check if v1 is predecessor of v2 by following in-edges
		LinkedList<Vertex> predsV2 = new LinkedList<>(this.graph.getRealPredecessors(v2));
		while(!predsV2.isEmpty()) {
			Vertex pred = predsV2.remove();
			if(pred.equals(v1)) {
				logger.fine("Result based on v2 predicessor search");
				return -1;
			}
			List<Vertex> sameStepPreds = this.graph.getRealPredecessors(pred).stream()
																			 .filter(v -> v.getStep() == v2.getStep())
																			 .collect(Collectors.toList());
			predsV2.addAll(sameStepPreds);
		}

		// if this fails too, order by id
		logger.fine("Comparing by id");
		return v1.getId().compareTo(v2.getId());
	}
}
