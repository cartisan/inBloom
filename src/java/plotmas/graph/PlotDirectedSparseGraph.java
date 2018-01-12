package plotmas.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * Storyplot-aware graph implementation, that does not consider edges of type {@code Edge.Type.COMMUNICATION} as
 * linking a vertex to its successor.
 * 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseGraph extends DirectedSparseGraph<Vertex, Edge> {
  
	private List<Vertex> roots = Lists.newArrayList();
	private HashMap<String, Vertex> lastVertexMap = new HashMap<>();
	private HashMap<String, Map<String, Vertex>> senderMap = new HashMap<>(); 	//form: agentName -> (message -> vertex)

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
    	this.senderMap.put(vertex.getLabel(), new HashMap<>());
    	
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
		if(senderMap.get(sender).containsKey(message)) {
			sendV = senderMap.get(sender).get(message);
		} else {
			sendV = addEvent(sender, message, Vertex.Type.SPEECHACT, Edge.Type.TEMPORAL);
			senderMap.get(sender).put(message, sendV);
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
        if (!containsVertex(root))
            return null;
        
        List<Vertex> succs = new LinkedList<Vertex>();
        
        Vertex succ = this.getCharSuccessor(root);
        while(!(succ == null)) {
        	succs.add(succ);
        	succ = this.getCharSuccessor(succ);
        }
        
        return succs;
    }
	
	@Override
	public PlotDirectedSparseGraph clone() {
		PlotDirectedSparseGraph dest = new PlotDirectedSparseGraph();
		
		for(Vertex r: this.getRoots()) {
			dest.roots.add(r);
		}
		dest.lastVertexMap = (HashMap<String,Vertex>) this.lastVertexMap.clone();
		

	    for (Vertex v : this.getVertices())
	        dest.addVertex(v);

	    for (Edge e : this.getEdges())
	        dest.addEdge(e, this.getIncidentVertices(e));
	    
	    return dest;
	}
}
