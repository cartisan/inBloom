package little_red_hen.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Storyplot aware graph implementation, that does not consider
 * edges of type {@code Edge.Type.COMMUNICATION} as linking a 
 * vertex to its successor.
 * 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseGraph extends DirectedSparseGraph<Vertex, Edge> {
  
	private List<Vertex> roots = Lists.newArrayList();

    public List<Vertex> getRoots() {
		if (roots.size() > 0) {
			return roots;
		}
		
		throw new IllegalStateException("Graph was not initialised with root nodes") ;
	}

	public boolean addRoot(Vertex vertex) {
    	boolean result = this.addVertex(vertex);
    	if (result) {
    		this.roots.add(vertex);
    	}
    	
    	return result;
    }

	/* 
	 * Returns only those vertices, that are successors in a plot sense,
	 * i.e. vertices that pertain to the same character column.This excludes
	 * vertices connected by communication edges.
	 *  
	 */
	public Collection<Vertex> getCharSuccessors(Vertex vertex) {
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
