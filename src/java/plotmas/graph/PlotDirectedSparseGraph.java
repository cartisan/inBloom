package plotmas.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import plotmas.graph.Vertex.Type;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.graph.visitor.RemovedEdge;

/**
 * Storyplot-aware graph implementation, that does not consider edges of type {@code Edge.Type.COMMUNICATION} as
 * linking a vertex to its successor.
 * 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseGraph extends DirectedSparseMultigraph<Vertex, Edge> implements Cloneable {
  
    static Logger logger = Logger.getLogger(PlotDirectedSparseGraph.class.getName());
	
	private List<Vertex> roots = Lists.newArrayList();
	private HashMap<String, Vertex> lastVertexMap = new HashMap<>();			// maps: agentName --> vertex
	private Table<String, String, Vertex> senderMap = HashBasedTable.create();	// maps: agentName, message --> vertex
	
	/**
	 * The name of this graph.
	 * Used as a string representation
	 */
	private String name;
	
	/**
	 * An array containing all vertices of this graph.
	 * Used to identify vertices by id in plotmas.graph.isomorphism.State
	 * This is generated whenever a change to the vertices was made and
	 * the method getVertex or getVertexId is called.
	 */
	private Vertex[] vertexArray;
	
	/**
	 * A flag which is set to true whenever the graph changed.
	 * Used to identify whether or not vertexArray needs to be generated.
	 */
	private boolean isDirty = true;
	
	/**
	 * Map used for quick access to the agent name of the subgraph a
	 * vertex is in. This map is cloned to a clone of this graph.
	 */
	private HashMap<Vertex, String> vertexAgentMap = new HashMap<>();			// maps: vertex --> agentName


	private Map<FunctionalUnit, Set<Vertex>> unitVertexGroups = new HashMap<>(); // stores all vertices belonging to a functional unit
	
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
    	
    	this.vertexAgentMap.put(vertex, vertex.getLabel());
    	
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
		
		this.vertexAgentMap.put(newVertex, root);
		
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
		this.vertexAgentMap.put(recV, receiver);
		return recV;
	}
	
	/**
	 * Overriden method call to addEdge, in order to set isDirty flag.
	 */
	@Override
	public boolean addEdge(Edge edge, Vertex from, Vertex to) {
		isDirty = true;
		return super.addEdge(edge, from, to);
	}
	
	/**
	 * Returns the name of the agent the given
	 * vertex is in the subgraph of.
	 * @param vertex
	 * @return name of agent
	 */
	public String getAgent(Vertex vertex) {
		if(!vertexAgentMap.containsKey(vertex)) {
			return "none";
		}
		return vertexAgentMap.get(vertex);
	}
	
	/**
	 * Returns the vertex identified by the
	 * given id.
	 * Generates vertexArray if needed.
	 * @param vertexId
	 * @return Vertex
	 */
	public Vertex getVertex(int vertexId) {
		if(isDirty) {
			vertexArray = new Vertex[this.getVertexCount()];
			vertices.keySet().toArray(vertexArray);
			isDirty = false;
		}
		if(vertexId < 0 || vertexId >= vertexArray.length) {
			return null;
		}
		return vertexArray[vertexId];
	}
	
	/**
	 * Finds the id of a given vertex.
	 * Generates vertexArray if needed.
	 * @param vertex
	 * @return int vertexId
	 */
	public int getVertexId(Vertex vertex) {
		if(isDirty) {
			vertexArray = new Vertex[this.getVertexCount()];
			vertices.keySet().toArray(vertexArray);
			isDirty = false;
		}
		for(int i = 0; i < vertexArray.length; i++) {
			if(vertexArray[i] == vertex) {
				return i;
			}
		}
		return -1;
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
	
	/**
	 * Adds a given vertex to the set of vertices belonging to the
	 * provided functional unit.
	 * @param v Vertex to add
	 * @param unit Functional unit the vertex belongs to
	 */
	public void markVertexAsUnit(Vertex v, FunctionalUnit unit) {
		if(!unitVertexGroups.containsKey(unit)) {
			unitVertexGroups.put(unit, new HashSet<Vertex>());
		}
		
		unitVertexGroups.get(unit).add(v);
	}
	
	/**
	 * Returns all vertices belonging to the provided unit.
	 * @param unit to retrieve the vertices of
	 * @return Set of vertices
	 */
	public Set<Vertex> getUnitVertices(FunctionalUnit unit) {
		if(!unitVertexGroups.containsKey(unit)) {
			unitVertexGroups.put(unit, new HashSet<Vertex>());
		}
		return unitVertexGroups.get(unit);
	}
	
	@Override
	public PlotDirectedSparseGraph clone() {
		return cloneInto(new PlotDirectedSparseGraph());
	}

	/**
	 * Clones the vertices and edges of this graph into the provided graph.
	 * @param dest
	 * @return the provided graph with vertices and edges of this one added.
	 */
	public PlotDirectedSparseGraph cloneInto(PlotDirectedSparseGraph dest) {
		// BEWARE: lastVertexMap is not cloned, the returned graph is not useable for continuing plotting 
		
		// clone vertices and add them to cloned graph
		HashMap<Vertex,Vertex> cloneMap = new HashMap<>();		// maps old vertex -> cloned vertex
	    for (Vertex v : this.getVertices()) {
	        Vertex clone = v.clone();
	    	dest.addVertex(clone);
    		cloneMap.put(v, clone);
    		
    		// if cloned vertex is a root, note that in roots list 
    		if(clone.getType()==Type.ROOT)
    			dest.roots.add(clone);
    		
    		dest.vertexAgentMap.put(clone, vertexAgentMap.get(v));
	    }

	    // clone edges, make sure that incident vertices are translated into their cloned counterparts
	    for (Edge e : this.getEdges()) {
	    	Collection<Vertex> vClones = this.getIncidentVertices(e).stream().map( v -> cloneMap.get(v)).collect(Collectors.toList());
	        dest.addEdge(e.clone(), vClones);
	    }
	    
	    for(Map.Entry<FunctionalUnit, Set<Vertex>> entry : unitVertexGroups.entrySet()) {
	    	FunctionalUnit fu = entry.getKey();
	    	for(Vertex v : entry.getValue()) {
	    		dest.markVertexAsUnit(cloneMap.get(v), fu);
	    	}
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
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
