package plotmas.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import plotmas.graph.Vertex.Type;

/**
 * Storyplot-aware graph implementation, that does not consider edges of type {@code Edge.Type.COMMUNICATION} as
 * linking a vertex to its successor.
 * 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseGraph extends DirectedSparseGraph<Vertex, Edge> implements Cloneable {
  
    static Logger logger = Logger.getLogger(PlotDirectedSparseGraph.class.getName());
	
	private List<Vertex> roots = Lists.newArrayList();
	private HashMap<String, Vertex> lastVertexMap = new HashMap<>();			// maps: agentName --> vertex
	private Table<String, String, Vertex> senderMap = HashBasedTable.create();	// maps: agentName, message --> vertex

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
    	
    	this.lastVertexMap.put(vertex.getLabel(), vertex);
    	
    	return result;
    }
	
	public Vertex addEvent(String root, String event, Vertex.Type eventType, Edge.Type linkType) {
		Vertex newVertex = new Vertex(event, eventType);
		Vertex parent = lastVertexMap.get(root);
		
		if (parent.getType() == Vertex.Type.ROOT) {
			linkType = Edge.Type.ROOT;
		}
		
		this.addEdge(new Edge(linkType), parent, newVertex);
		lastVertexMap.put(root, newVertex);
		
		return newVertex;
	}
	
	public void addRequest(String sender, String receiver, String message) {
		Vertex sendV;
		// if same message was already send before, use old vertex
		// helps reusing vertex when multiple recipients
		if(senderMap.contains(sender, message)) {
			sendV = senderMap.get(sender, message);
		} else {
			sendV = addEvent(sender, message, Vertex.Type.SPEECHACT, Edge.Type.TEMPORAL);
			senderMap.put(sender, message, sendV);
		}

		Vertex recV = addEvent(receiver, message, Vertex.Type.LISTEN,  Edge.Type.TEMPORAL);
		this.addEdge(new Edge(Edge.Type.COMMUNICATION), sendV, recV);
	}
	
	/**
	 * Returns vertice, that is successors in a plot sense,
	 * i.e. vertices that pertain to the same character column.This excludes
	 * vertices connected by communication edges.
	 * @param vertex for which char-successor is sought
	 * @return successor vertex if present, or null
	 */
	public Vertex getCharSuccessor(Vertex vertex) {
        if (!containsVertex(vertex))
            return null;
        
        for (Edge edge : getOutgoing_internal(vertex))
        	if (edge.getType() != Edge.Type.COMMUNICATION) {
        		return this.getDest(edge);
        	}
        
        return null;
    }
	
	public List<Vertex> getCharSubgraph(Vertex root) {
		List<Vertex> succs = new LinkedList<Vertex>();

		if (!containsVertex(root)) {
//			System.out.println("Subgraph for character " + root.getLabel() + " not found. Vertex: "  + root.toString());
            return succs;
		}
        
        Vertex succ = this.getCharSuccessor(root);
        while(!(succ == null)) {
        	succs.add(succ);
        	succ = this.getCharSuccessor(succ);
        }
        
        return succs;
    }
	
	@Override
	public PlotDirectedSparseGraph clone() {
		// FIXME: lastVertexMap is not cloned, the returned graph is not useable for continuing plotting 
		PlotDirectedSparseGraph dest = new PlotDirectedSparseGraph();
		
		// clone vertices and add them to cloned graph
		HashMap<Vertex,Vertex> cloneMap = new HashMap<>();		// maps old vertex -> cloned vertex
	    for (Vertex v : this.getVertices()) {
	        Vertex clone = v.clone();
	    	dest.addVertex(clone);
    		cloneMap.put(v, clone);
    		
    		// if cloned vertex is a root, note that in roots list 
    		if(clone.getType()==Type.ROOT)
    			dest.roots.add(clone);
    		
	    }

	    // clone edges, make sure that incident vertices are translated into their cloned counterparts
	    for (Edge e : this.getEdges()) {
	    	Collection<Vertex> vClones = this.getIncidentVertices(e).stream().map( v -> cloneMap.get(v)).collect(Collectors.toList());
	        dest.addEdge(e.clone(), vClones);
	    }
	    
	    return dest;
	}
}
