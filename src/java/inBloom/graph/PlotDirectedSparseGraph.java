package inBloom.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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

import inBloom.graph.Vertex.Type;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.visitor.PlotGraphVisitor;
import inBloom.graph.visitor.RemovedEdge;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Storyplot-aware graph implementation, that does not consider edges of type {@code Edge.Type.CROSSCHARACTER} as
 * linking a vertex to its successor.
 *
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotDirectedSparseGraph extends DirectedSparseMultigraph<Vertex, Edge> implements Cloneable {

    static Logger logger = Logger.getLogger(PlotDirectedSparseGraph.class.getName());
    public static final String AXIS_LABEL = "plot step";

	private ArrayList<Vertex> roots = Lists.newArrayList();
	private HashMap<String, Vertex> lastVertexMap = new HashMap<>();			// maps: agentName --> vertex
	private Table<String, String, Vertex> senderMap = HashBasedTable.create();	// maps: agentName, message --> vertex
	public HashMap<Integer, Vertex> yAxis = new HashMap<>();					// maps: time step --> vertex

	/**
	 * The name of this graph.
	 * Used as a string representation
	 */
	private String name;

	/**
	 * An array containing all vertices of this graph sorted by step in a reproducible way (if multiple vertices
	 * per step are present, order inside step is not guaranteed). Used to identify vertices by id in
	 * {@link inBloom.graph.isomorphism.State}.
	 * This is generated whenever a change to the vertices was made and the list was accessed.
	 */
	private Vertex[] vertexArray;

	/**
	 * A flag which is set to true whenever the graph changed. Used to identify whether or not orderedVertexList needs
	 * to be regenerated.
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
    public ArrayList<Vertex> getRoots() {
		if (this.roots.size() > 0) {
			return this.roots;
		}
		throw new IllegalStateException("Graph was not initialised with root nodes") ;
	}

    public Collection<Vertex> getAxisVertices() {
    	return this.yAxis.values();
    }

	/**
	 * Creates a new vertex that will be used to represent time-step labels
	 */
	private void addToAxis(int step, String label) {
		Vertex labelV = new Vertex(label, Type.AXIS_LABEL, step, this);
		this.yAxis.put(step, labelV);
		this.addVertex(labelV);
	}

	public Vertex addRoot(String agName) {
		Vertex root = new Vertex(agName, Type.ROOT, 0, this);

    	boolean result = this.addVertex(root);
    	if (result) {
    		this.roots.add(root);
    		this.lastVertexMap.put(root.getLabel(), root);

    		// add appropriate label to yAxis
    		if (!this.yAxis.containsKey(root.getStep())) {
    			this.addToAxis(root.getStep(), AXIS_LABEL);
    	}

    		return root;
    	}

    	this.vertexAgentMap.put(root, root.getLabel());

    	return null;
    }

	public synchronized Vertex addEvent(String root, String event, int step, Vertex.Type eventType, Edge.Type linkType) {
		Vertex newVertex = new Vertex(event, eventType, step, this);
		Vertex parent = this.lastVertexMap.get(root);

		if (parent.getType() == Vertex.Type.ROOT) {
			linkType = Edge.Type.ROOT;
		}

		this.addEdge(new Edge(linkType), parent, newVertex);
		this.lastVertexMap.put(root, newVertex);

		this.vertexAgentMap.put(newVertex, root);

		// add appropriate label to yAxis
		if (!this.yAxis.containsKey(step)) {
			this.addToAxis(step, String.valueOf(step));
		}


		return newVertex;
	}

	public Vertex addMsgSend(String sender, String message, int step) {
		Vertex sendV;
		// if same message was already send before, use old vertex
		// helps reusing vertex when multiple recipients
		if(this.senderMap.contains(sender, message)) {
			sendV = this.senderMap.get(sender, message);
		} else {
			sendV = this.addEvent(sender, message, step, Vertex.Type.SPEECHACT, Edge.Type.TEMPORAL);
			this.senderMap.put(sender, message, sendV);
		}

		return sendV;
	}

	public Vertex addMsgReceive(String receiver, String message, Vertex sendV, int step) {
		Vertex recV = this.addEvent(receiver, message, step, Vertex.Type.LISTEN,  Edge.Type.TEMPORAL);
		this.addEdge(new Edge(Edge.Type.CROSSCHARACTER), sendV, recV);
		this.vertexAgentMap.put(recV, receiver);
		return recV;
	}

	/**
	 * Overrides method call to addEdge, in order to set isDirty flag.
	 */
	@Override
	public boolean addEdge(Edge edge, Vertex from, Vertex to) {
		this.isDirty = true;
		return super.addEdge(edge, from, to);
	}

	/**
	 * Overrides method call to removeEdge, in order to set isDirty flag.
	 */
	@Override
	public boolean removeEdge(Edge edge) {
		this.isDirty = true;
		return super.removeEdge(edge);
	}

	/**
	 * Returns the name of the agent the given
	 * vertex is in the subgraph of.
	 * @param vertex
	 * @return name of agent
	 */
	public String getAgent(Vertex vertex) {
		if(!this.vertexAgentMap.containsKey(vertex)) {
			return "none";
		}
		return this.vertexAgentMap.get(vertex);
	}

	/**
	 * Returns the vertex at position id in orderedVertexList. Generates orderedVertexList if needed.
	 * @param vertexId
	 * @return Vertex
	 */
	public Vertex getVertex(int vertexId) {
		if(this.isDirty) {
			this.regenerateVertexArray();
		}
		if(vertexId < 0 || vertexId >= this.vertexArray.length) {
			return null;
		}
		return this.vertexArray[vertexId];
	}

	/**
	 * Finds the id of a given vertex. Generates orderedVertexList if needed.
	 * @param vertex
	 * @return int vertexId
	 */
	public int getVertexId(Vertex vertex) {
		if(this.isDirty) {
			this.regenerateVertexArray();
		}
		for(int i = 0; i < this.vertexArray.length; i++) {
			if(this.vertexArray[i] == vertex) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns orderedVertexList, regenerates it if dirty flag was set.
	 * @return
	 */
	public List<Vertex> getOrderedVertexList() {
		if(this.isDirty) {
			this.regenerateVertexArray();
		}
		return Arrays.asList(this.vertexArray);
	}

	private void regenerateVertexArray() {
		this.vertexArray = new Vertex[this.getPlotVertexCount()];
		this.vertices.keySet().stream()
		   .filter(v -> !this.roots.contains(v) & !this.yAxis.values().contains(v))
	 	   .sorted(Comparator.comparingInt(Vertex::getStep))
	 	   .collect(Collectors.toList()).toArray(this.vertexArray);
		this.isDirty = false;
	}

	/**
	 * Returns the number of vertices related directly to plot, that is, excluding roots and axis.
	 * @return
	 */
	public int getPlotVertexCount() {
		return (int) this.vertices.keySet().stream()
										   .filter(v -> !this.roots.contains(v) & !this.yAxis.values().contains(v))
										   .count();
	}

	/**
	 * Returns vertex, that is a successor in a temporal sense, i.e. vertices that pertain to the same character column
	 * and is connected via temporal edge. This excludes vertices connected by communication edges.
	 * @param vertex for which char-successor is sought
	 * @return successor vertex if present, or null
	 */
	public Vertex getCharSuccessor(Vertex vertex) {
        if (!this.containsVertex(vertex)) {
			return null;
		}

        for (Edge edge : this.getOutgoing_internal(vertex)) {
			if(edge.getType() == Edge.Type.TEMPORAL || edge.getType() == Edge.Type.ROOT) {
        		return this.getDest(edge);
        	}
		}

        return null;
    }

	/**
	 * Returns vertex, that is a successor in a plot sense, i.e. vertices that pertain to the same character column
	 * and has a causal relation. This excludes vertices connected by communication edges.
	 * @param vertex for which plot-successor is sought
	 * @return successor vertex if present, or null
	 */
	public Vertex getPlotSuccessor(Vertex vertex) {
		if (!this.containsVertex(vertex)) {
			return null;
		}

		if (this.getOutgoing_internal(vertex).size() > 1) {
			throw new RuntimeException("Vertex" + vertex.toString() + "has multiple plot successors");
		}

		for (Edge edge : this.getOutgoing_internal(vertex)) {
			if(edge.getType() == Edge.Type.MOTIVATION || edge.getType() == Edge.Type.ACTUALIZATION
					|| edge.getType() == Edge.Type.CAUSALITY || edge.getType() == Edge.Type.EQUIVALENCE) {
				return this.getDest(edge);
			}
		}

		return null;
	}

	/**
	 * Returns vertex, that is a predecessor in a plot sense, i.e. vertices that pertain to the same character column.
	 * This excludes vertices connected by communication edges.
	 * @param vertex for which char-predecessor is sought
	 * @return successor vertex if present, or null
	 */
	public Vertex getCharPredecessor(Vertex vertex) {
		if(!this.containsVertex(vertex)) {
			return null;
		}

		for(Edge edge : this.getIncoming_internal(vertex)) {
			if(edge.getType() == Edge.Type.TEMPORAL || edge.getType() == Edge.Type.ROOT) {
				return this.getSource(edge);
			}
		}
		return null;
	}

	/**
	 * Returns the position of this vertex inside the step.
	 * 0 means this vertex is the first of its step, 1 is the second,
	 * 2 the third, and so on.
	 * @param vertex
	 * @return position
	 */
	public int getInnerStep(Vertex vertex) {
		int step = vertex.getStep();
		Vertex pred = this.getCharPredecessor(vertex);
		int c = 0;
		while(pred != null && pred.getStep() == step) {
			c++;
			pred = this.getCharPredecessor(pred);
		}
		return c;
	}

	/**
	 * Returns all vertices which are a real predecessor of the given vertex.
	 * This means, that all vertices this method returns happened at the same
	 * time, or earlier, than the vertex in question.
	 * This is effectively done by excluding EQUIVALENCE, ROOT and TERMINATION
	 * edges in looking for predecessors. This assumes that they will never be
	 * used in a forward-connecting way (except for ROOT).
	 * Should this change, this method needs to be changed to look
	 * for getStep and getInnerStep.
	 * @param vertex for which real predecessors are sought
	 * @return all real predecessors of the vertex, or an empty collection if none were found
	 */
	public Collection<Vertex> getRealPredecessors(Vertex vertex) {
		if(!this.containsVertex(vertex)) {
			return null;
		}

		Set<Vertex> vertices = new HashSet<>();
		for(Edge edge : this.getIncoming_internal(vertex)) {
			if(edge.getType() != Edge.Type.EQUIVALENCE
			&& edge.getType() != Edge.Type.TERMINATION
			&& edge.getType() != Edge.Type.ROOT) {
				vertices.add(this.getSource(edge));
			}
		}
		return vertices;
	}

	/**
	 * Returns the subgraph of a character, i.e. all vertices connected to a certain root node.
	 * @param root
	 * @return
	 */
	public List<Vertex> getCharSubgraph(Vertex root) {
		List<Vertex> succs = new LinkedList<>();

		if (!this.containsVertex(root)) {
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
		if(!this.unitVertexGroups.containsKey(unit)) {
			this.unitVertexGroups.put(unit, new HashSet<Vertex>());
		}

		this.unitVertexGroups.get(unit).add(v);
	}

	/**
	 * Returns all vertices belonging to the provided unit.
	 * @param unit to retrieve the vertices of
	 * @return Set of vertices
	 */
	public Set<Vertex> getUnitVertices(FunctionalUnit unit) {
		if(!this.unitVertexGroups.containsKey(unit)) {
			this.unitVertexGroups.put(unit, new HashSet<Vertex>());
		}
		return this.unitVertexGroups.get(unit);
	}

	@Override
	public PlotDirectedSparseGraph clone() {
		return this.cloneInto(new PlotDirectedSparseGraph());
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
		dest.name = this.name;

		// clone roots in right order
		for (Vertex root : this.roots) {
	        Vertex clone = root.clone(dest);
	    	dest.addVertex(clone);
    		cloneMap.put(root, clone);

			dest.roots.add(clone);
			dest.vertexAgentMap.put(clone, this.vertexAgentMap.get(root));
		}
		synchronized(this.vertices) {
		    for (Vertex v : this.getVertices()) {
		    	if (!(v.getType() == Type.ROOT)) {
		    		Vertex clone = v.clone(dest);

		    		// if cloned vertex is an axis, note that in axis map
		    		if(this.yAxis.containsValue(v)) {
		    			if(!dest.yAxis.containsKey(v.getStep())) {
		    				dest.yAxis.put(v.getStep(), clone);
		    			} else {
		    				cloneMap.put(v, v);
		    				continue;
		    			}
		    		}
		    		dest.addVertex(clone);
	    			cloneMap.put(v, clone);

	    			dest.vertexAgentMap.put(clone, this.vertexAgentMap.get(v));
	    		}
		    }
		}

	    // clone edges, make sure that incident vertices are translated into their cloned counterparts
	    synchronized(this.edges) {
		    for (Edge e : this.getEdges()) {
		    	Collection<Vertex> vClones = this.getIncidentVertices(e).stream().map( v -> cloneMap.get(v)).collect(Collectors.toList());
		        dest.addEdge(e.clone(), vClones);
		    }
	    }

	    for(Map.Entry<FunctionalUnit, Set<Vertex>> entry : this.unitVertexGroups.entrySet()) {
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

	/**
	 * Removes a vertex from this graph and automatically finds the predecessor of the
	 * removed node and connects it to the removed node's successor.
	 * @param root Root of the subgraph the vertex to be removed belongs to
	 * @param toRemove Vertex to be removed
	 */
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

	/**
	 * Runs a PlotGraphVisitor over this graph.
	 * @param visitor to run
	 */
	public void accept(PlotGraphVisitor visitor) {
		// Queue which contains the objects to be visited.
		// Objects may only be of type Vertex, Edge or RemovedEdge.
		// For safety, one may consider adding a marker interface (e.g. GraphElement).
		LinkedList<Object> visitQueue = new LinkedList<>();

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
					List<RemovedEdge> remEdges = new LinkedList<>();
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
		case WILDCARD:	visitor.visitEvent(vertex);		break;
		case ACTION: 	visitor.visitAction(vertex); 	break;
		case EMOTION: 	visitor.visitEmotion(vertex); 	break;
		case PERCEPT: 	visitor.visitPercept(vertex); 	break;
		case SPEECHACT: visitor.visitSpeech(vertex); 	break;
		case LISTEN: 	visitor.visitListen(vertex); 	break;
		case INTENTION: visitor.visitIntention(vertex); break;
		case AXIS_LABEL: break;
		default:
			throw new RuntimeException("Unknown vertex type. Aborting visit!");
		}

	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		if (this.name != null) {
			return this.name;
		}
		return this.getOrderedVertexList().toString();
	}
}
