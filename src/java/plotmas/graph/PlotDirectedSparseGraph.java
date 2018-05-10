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
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.graph.visitor.RemovedEdge;

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

    /**
     * Returns a list of nodes that represent the roots of each character subgraph.
     * @return
     */
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
	
	public synchronized Vertex addEvent(String root, String event, Vertex.Type eventType, Edge.Type linkType) {
		Vertex newVertex = new Vertex(event, eventType);
		Vertex parent = lastVertexMap.get(root);
		
		if (parent.getType() == Vertex.Type.ROOT) {
			linkType = Edge.Type.ROOT;
		}
		
		this.addEdge(new Edge(linkType), parent, newVertex);
		lastVertexMap.put(root, newVertex);
		
		return newVertex;
	}
	
	public Vertex addMsgSend(String sender, String message) {
		Vertex sendV;
		// if same message was already send before, use old vertex
		// helps reusing vertex when multiple recipients
		if(senderMap.contains(sender, message)) {
			sendV = senderMap.get(sender, message);
		} else {
			sendV = addEvent(sender, message, Vertex.Type.SPEECHACT, Edge.Type.TEMPORAL);
			senderMap.put(sender, message, sendV);
		}
		
		return sendV;
	}
	
	public Vertex addMsgReceive(String receiver, String message, Vertex sendV) {
		Vertex recV = addEvent(receiver, message, Vertex.Type.LISTEN,  Edge.Type.TEMPORAL);
		this.addEdge(new Edge(Edge.Type.COMMUNICATION), sendV, recV);
		
		return recV;
	}
	
	/**
	 * Returns vertice, that is successors in a plot sense, i.e. vertices that pertain to the same character column.
	 * This excludes vertices connected by communication edges.
	 * @param vertex for which char-successor is sought
	 * @return successor vertex if present, or null
	 */
	public Vertex getCharSuccessor(Vertex vertex) {
        if (!containsVertex(vertex))
            return null;
        
        for (Edge edge : getOutgoing_internal(vertex))
        	//if (edge.getType() != Edge.Type.COMMUNICATION && edge.getType() != Edge.Type.MOTIVATION) {
        	if(edge.getType() == Edge.Type.TEMPORAL || edge.getType() == Edge.Type.ROOT) {
        		return this.getDest(edge);
        	}
        
        return null;
    }
	
	/**
	 * Returns the subgraph of a character, i.e. all vertices connected to a certain root node.
	 * @param root
	 * @return
	 */
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
		// BEWARE: lastVertexMap is not cloned, the returned graph is not useable for continuing plotting 
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
	
	/**
	 * Removes a vertex from this graph and connects its successor with the provided predecessor. This is necessary,
	 * in case several predecessors of {@code toRemove} are to be removed as well.
	 * @param toRemove vertex instance to be removed
	 * @param lastV vertex instance that should be used as predecessor
	 */
	public void removeVertexAndPatchGraph(Vertex toRemove, Vertex lastV) {
		// patch up hole from removal
		Vertex nextV = this.getCharSuccessor(toRemove);
		Edge.Type type = Edge.Type.TEMPORAL;
		if(lastV.getType() == Vertex.Type.ROOT) {
			type = Edge.Type.ROOT;
		}
		if(nextV != null) {
			this.addEdge(new Edge(type), lastV, nextV);
		}
		
		// remove perception
		this.removeVertex(toRemove);
	}
	
	public void removeVertexAndPatchGraphAuto(Vertex root, Vertex toRemove) {
		Vertex lastV = root;
		Vertex currentV = root;
		while(currentV != toRemove) {
			Vertex v = lastV;
			lastV = currentV;
			currentV = this.getCharSuccessor(v);
		}
		
		this.removeVertexAndPatchGraph(toRemove, lastV);
	}

	public void accept(PlotGraphVisitor visitor) {
		// Queue which contains the objects to be visited.
		// Objects may only be of type Vertex, Edge or RemovedEdge.
		// For safety, one may consider adding a marker interface (e.g. GraphElement).
		LinkedList<Object> visitQueue = new LinkedList<Object>();

		for(Vertex root : this.roots) {
			visitQueue.clear();
			visitQueue.add(root);
			while(!visitQueue.isEmpty()) {
				Object o = visitQueue.removeFirst();
				if(o == null) {
					continue;
				}
				if(o instanceof Vertex) {
					Collection<Edge> edges = this.getOutEdges((Vertex)o);
					List<RemovedEdge> remEdges = new LinkedList<RemovedEdge>();
					for(Edge e : edges) {
						remEdges.add(new RemovedEdge(e, this.getDest(e)));
					}
					this.acceptVertex((Vertex)o, visitor);
					if(this.containsVertex((Vertex)o)) {
						for(Edge e : edges) {
							visitQueue.addFirst(e);
						}
					} else {
						for(RemovedEdge e : remEdges) {
							visitQueue.addFirst(e);
						}
					}
				} else
				if(o instanceof Edge) {
					
					switch(visitor.visitEdge((Edge)o)) {
						case CONTINUE:
							visitQueue.addLast(this.getDest((Edge)o));
							break;
						case DIRECT:
							visitQueue.addFirst(this.getDest((Edge)o));
							break;
						case TERMINATE:
						default:
							break;
					}
				} else
				if(o instanceof RemovedEdge) {
					switch(visitor.visitEdge(((RemovedEdge)o).getEdge())) {
						case CONTINUE:
							visitQueue.addLast(((RemovedEdge)o).getDest());
							break;
						case DIRECT:
							visitQueue.addFirst(((RemovedEdge)o).getDest());
							break;
						case TERMINATE:
						default:
							break;
					}
				} else {
					throw new RuntimeException("Undesired object type! Queue should only contain Edge and Vertex objects!");
				}
			}
		}
	
	}
	
	private void acceptVertex(Vertex vertex, PlotGraphVisitor visitor) {
		switch(vertex.getType()) {
		case ROOT: 		visitor.visitRoot(vertex); 		break;
		case EVENT: 	visitor.visitEvent(vertex); 	break;
		case EMOTION: 	visitor.visitEmotion(vertex); 	break;
		case PERCEPT: 	visitor.visitPercept(vertex); 	break;
		case SPEECHACT: visitor.visitSpeech(vertex); 	break;
		case LISTEN: 	visitor.visitListen(vertex); 	break;
		case INTENTION: visitor.visitIntention(vertex); break;
		default:
			throw new RuntimeException("Unknown vertex type. Aborting visit!");
		}
		
	}
}
