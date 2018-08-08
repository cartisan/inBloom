package plotmas.graph.isomorphism;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

/**
 * Class which wraps around a PlotDirectedSparseGraph to be used as a functional unit.
 * Provides functionality for creating a displayable graph of this unit.
 * @author Sven Wilke
 */
public class FunctionalUnit {
	
	private String name;
	private PlotDirectedSparseGraph unitGraph;
	private PlotDirectedSparseGraph displayGraph;
	
	public FunctionalUnit(String name, PlotDirectedSparseGraph graph) {
		this.name = name;
		this.unitGraph = graph;
	}
	
	/**
	 * Returns the name of this functional unit.
	 * @return String name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Retrieves the pure functional unit graph
	 * for use in subgraph isomorphism analysis.
	 * @return PlotDirectedSparseGraph functional unit graph
	 */
	public PlotDirectedSparseGraph getGraph() {
		return this.unitGraph;
	}
	
	/**
	 * Retrieves a version of this functional unit which
	 * is plottable using the default PlotGraphLayout,
	 * i.e. a version which contains root nodes and temporal
	 * edges.
	 * This displayable graph is created on first use of this
	 * method and then cached for later retrieval.
	 * @return PlotDirectedSparseGraph displayable functional unit graph
	 */
	public PlotDirectedSparseGraph getDisplayGraph() {
		if(displayGraph == null) {
			this.createDisplayGraph();
		}
		return this.displayGraph;
	}
	
	private void createDisplayGraph() {
		displayGraph = unitGraph.clone();
		Vertex root2 = null;
		Vertex[] roots = new Vertex[] {
			displayGraph.addRoot(this.name + " Agent 1"),
			root2 = displayGraph.addRoot(this.name + " Agent 2")
		};
		
		Vertex startVertex = null;
		for(Vertex v : displayGraph.getVertices()) {
			if(isStartVertex(v, displayGraph)) {
				startVertex = v;
				break;
			}
		}
		
		assert startVertex != null;
		
		connect(startVertex, roots, 0);
		
		if(displayGraph.getIncidentEdges(root2).isEmpty()) {
			displayGraph.removeVertex(root2);
			displayGraph.getRoots().remove(root2);
		} else {
			Logger.getGlobal().info("++++++++++++ root " + root2.toString());
			for (Edge e: displayGraph.getIncidentEdges(root2)) {
				Logger.getGlobal().info("type " + e.getType().toString());
				Logger.getGlobal().info("src " + displayGraph.getSource(e).toString());
				Logger.getGlobal().info("dest " + displayGraph.getDest(e).toString());
			}
		}
		
		displayGraph.setName(this.getName());
	}
	
	/**
	 * Connects v with con[agent] if v != con[agent] and then
	 * recursively connects the whole graph with temporal
	 * edges.
	 */
	private void connect(Vertex v, Vertex[] con, int agent) {
		Collection<Edge> outEdges = displayGraph.getOutEdges(v);
		if(con[agent] != v) {
			displayGraph.addEdge(new Edge(con[agent].getType() == Vertex.Type.ROOT ? Edge.Type.ROOT : Edge.Type.TEMPORAL), con[agent], v);
			con[agent] = v;
		}
		
		Map<Edge.Type, Collection<Edge>> typedEdges = new HashMap<>();
		for(Edge.Type type : Edge.Type.values()) {
			typedEdges.put(type, new HashSet<Edge>());
		}
		for(Edge e : outEdges) {
			typedEdges.get(e.getType()).add(e);
		}
		
		for(Edge e : typedEdges.get(Edge.Type.ACTUALIZATION)) {
			Vertex w = displayGraph.getDest(e);
			displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), con[agent], w);
			con[agent] = w;
		}
		
		for(Edge e : typedEdges.get(Edge.Type.MOTIVATION)) {
			Vertex w = displayGraph.getDest(e);
			displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), con[agent], w);
			con[agent] = w;
			connect(w, con, agent);
		}
		
		for(Edge e : typedEdges.get(Edge.Type.COMMUNICATION)) {
			Vertex w = displayGraph.getDest(e);
			connect(w, con, agent == 0 ? 1 : 0);
		}
	}
	
	private boolean isStartVertex(Vertex v, PlotDirectedSparseGraph g) {
		if ((v.getType()==Vertex.Type.AXIS_LABEL) | (v.getType()==Vertex.Type.ROOT)) {
			return false;
		}
		
		Collection<Edge> inEdges = g.getInEdges(v);
		if(!inEdges.isEmpty()) {
			for(Edge e : inEdges) {
				if(e.getType() != Edge.Type.TERMINATION) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
