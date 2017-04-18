package little_red_hen.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Storyplot aware graph implementation, that does not consider
 * edges of type {@code Edge.Type.COMMUNICATION} as linking a 
 * vertex to its successor.
 * 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseMultigraph extends DirectedSparseMultigraph<Vertex, Edge> {
  

	@Override
	public Collection<Vertex> getSuccessors(Vertex vertex) {
        if (!containsVertex(vertex))
            return null;
        
        Set<Vertex> succs = new HashSet<Vertex>();
        for (Edge edge : getOutgoing_internal(vertex))
        	if (edge.getType() != Edge.Type.COMMUNICATION) {
        		succs.add(this.getDest(edge));
        	}
        
        return Collections.unmodifiableCollection(succs);
    }
}
