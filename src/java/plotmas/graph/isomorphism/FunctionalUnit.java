package plotmas.graph.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
	
	static Logger logger = Logger.getLogger(FunctionalUnit.class.getName());
	
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

		connect(roots);

		if(displayGraph.getIncidentEdges(root2).isEmpty()) {
			displayGraph.removeVertex(root2);
			displayGraph.getRoots().remove(root2);
		}
		
		displayGraph.setName(this.getName());
	}
	
	/**
	 * Connects the display graph temporally using the steps
	 * of the vertices. Assumes that for each character,
	 * exactly one vertex at step 1 exists.
	 * @param roots
	 */
	private void connect(Vertex[] roots) {
		Vertex[] starts = {null, null};
		for(Vertex v : displayGraph.getVertices()) {
			if(v.getType() != Vertex.Type.AXIS_LABEL && v.getStep() == 1) {
				if(starts[0] == null) {
					starts[0] = v;
				} else {
					starts[1] = v;
					break;
				}
			}
		}
		List<Vertex> vertexDump0 = new ArrayList<Vertex>();
		List<Vertex> vertexDump1 = new ArrayList<Vertex>();
		vertexDump0.add(starts[0]);
		vertexDump1.add(starts[1]);
		Set<Vertex> vertices0 = new HashSet<Vertex>();
		Set<Vertex> vertices1 = new HashSet<Vertex>();
		boolean allIn0, allIn1;
		do {
			allIn0 = true;
			allIn1 = true;
			for(Vertex v : vertexDump0) {
				if(!vertices0.contains(v)) {
					allIn0 = false;
					collectSubVertices(v, vertices0, vertexDump1);
				}
			}
			if(starts[1] != null) {
				for(Vertex v : vertexDump1) {
					if(!vertices1.contains(v)) {
						allIn1 = false;
						collectSubVertices(v, vertices1, vertexDump0);
					}
				}
			}
		} while(!(allIn0 && allIn1));
		
		Comparator<Vertex> stepComparator = new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				return o1.getStep() - o2.getStep();
			}
		};
		
		Vertex[] sortVertices0 = new Vertex[vertices0.size()];
		Vertex[] sortVertices1 = new Vertex[vertices1.size()];
		vertices0.toArray(sortVertices0);
		if(starts[1] != null)
			vertices1.toArray(sortVertices1);
		Arrays.sort(sortVertices0, stepComparator);
		if(starts[1] != null)
			Arrays.sort(sortVertices1, stepComparator);
		
		for(int i = 0; i < sortVertices0.length; i++) {
			if(i == 0) {
				displayGraph.addEdge(new Edge(Edge.Type.ROOT), roots[0], sortVertices0[0]);
			} else {
				displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), sortVertices0[i - 1], sortVertices0[i]);
			}
		}
		
		if(starts[1] != null)
			for(int i = 0; i < sortVertices1.length; i++) {
				if(i == 0) {
					displayGraph.addEdge(new Edge(Edge.Type.ROOT), roots[1], sortVertices1[0]);
				} else {
					displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), sortVertices1[i - 1], sortVertices1[i]);
				}
			}
	}
	
	private void collectSubVertices(Vertex start, Set<Vertex> vertices, List<Vertex> dump) {
		Queue<Vertex> queue = new LinkedList<Vertex>();
		queue.add(start);
		while(!queue.isEmpty()) {
			Vertex vert = queue.remove();
			if(!vertices.contains(vert)) {
				vertices.add(vert);
				for(Edge e : displayGraph.getIncidentEdges(vert)) {
					
					Vertex toAdd = displayGraph.getSource(e);
					if(toAdd == vert) {
						toAdd = displayGraph.getDest(e);
					}
					if(e.getType() == Edge.Type.COMMUNICATION) {
						dump.add(toAdd);
					} else {
						queue.add(toAdd);
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
