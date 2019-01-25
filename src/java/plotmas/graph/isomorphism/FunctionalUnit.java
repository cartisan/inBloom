package plotmas.graph.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.util.Pair;
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
	
	private boolean isPrimitive = false;
	
	private Pair<Vertex, String> subject;
	
	public FunctionalUnit(String name, PlotDirectedSparseGraph graph) {
		this.name = name;
		this.unitGraph = graph;
	}
	
	/**
	 * Marks this functional unit as a primitive unit.
	 */
	public void setPrimitive() {
		this.isPrimitive = true;
	}
	
	/**
	 * Checks whether this unit is primitive or complex
	 * @return true if this is a primitive unit, false otherwise.
	 */
	public boolean isPrimitive() {
		return this.isPrimitive;
	}
	
	/**
	 * Sets the subject of the vertex.
	 * @param subject 	Pair of vertex to be used as a subject and
	 * 					regex string to use for parsing the vertex' content
	 * 					(matching group 1 will be used as subject).
	 */
	public void setSubject(Pair<Vertex, String> subject) {
		if(!this.unitGraph.containsVertex(subject.getFirst())) {
			throw new IllegalArgumentException("Can not mark a vertex as subject which is not part of the FunctionalUnit.");
		}
		this.subject = subject;
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
	
	/**
	 * Represents an instance of a functional unit.
	 * Holds the partaking agents as well as the subject,
	 * along with additional temporal information.
	 * Can be compared to achieve a temporal order.
	 * @author Sven
	 */
	public class Instance implements Comparable<FunctionalUnit.Instance>{
		
		private PlotDirectedSparseGraph graph;
		private Collection<Vertex> vertices;
		private int start = Integer.MAX_VALUE;
		private int end = Integer.MIN_VALUE;
		
		private String firstAgent;
		private String secondAgent;
		private String subject;
		private boolean firstPlural = false;
		private boolean secondPlural = false;
		
		/**
		 * Creates an instance of a functional unit, given a graph and a collection
		 * of vertices contained in the plot graph that are part of this instance.
		 * @param graph
		 * @param vertices
		 */
		public Instance(PlotDirectedSparseGraph graph, Collection<Vertex> vertices) {
			this.graph = graph;
			this.vertices = vertices;
			for(Vertex v : vertices) {
				start = Math.min(start, v.getStep());
				end = Math.max(end, v.getStep());
				if(graph != null) {
					if(firstAgent != null && !graph.getAgent(v).equals(firstAgent)) {
						secondAgent = graph.getAgent(v);
					} else
					if(firstAgent == null) {
						boolean containsAnyPredecessors = false;
						for(Vertex p : graph.getRealPredecessors(v)) {
							if(vertices.contains(p)) {
								containsAnyPredecessors = true;
								break;
							}
						}
						if(!containsAnyPredecessors) {
							firstAgent = graph.getAgent(v);
						}
					}
				}
			}
			if(graph != null && secondAgent == null) {
				for(Vertex v : vertices) {
					if(!graph.getAgent(v).equals(firstAgent)) {
						secondAgent = graph.getAgent(v);
						break;
					}
				}
			}
			if(firstAgent != null) {
				firstAgent = "the " + firstAgent;
			}
			if(secondAgent != null) {
				secondAgent = "the " + secondAgent;
			}
		}
		
		/**
		 * Given a mapping of functional unit vertices to plot vertices,
		 * this computes the subject of the instance and stores it.
		 * @param unitMapping Mapping as created by UnitFinder
		 */
		public void identifySubject(Map<Vertex, Vertex> unitMapping) {
			if(getUnit().subject == null) {
				return;
			}
			for(Map.Entry<Vertex, Vertex> kvp : unitMapping.entrySet()) {
				if(kvp.getValue() == getUnit().subject.getFirst()) {
					Pattern p = Pattern.compile(getUnit().subject.getSecond());
					Matcher m = p.matcher(kvp.getKey().getLabel());
					m.find();
					this.subject = m.group(1);
				}
			}
		}
		
		/**
		 * Returns the subject. Note that this is null if either identifySubject has
		 * not been called before, or the subject of the FunctionalUnit was not set.
		 * @return
		 */
		public String getSubject() {
			return this.subject;
		}
		
		/**
		 * Allows manual setting of the subject
		 * (to allow instance merging from outside).
		 * @param subject the subject to set.
		 */
		public void setSubject(String subject) {
			if(this.graph != null || this.subject != null) {
				throw new RuntimeException("Can not set subject on a FunctionalUnit$Instance,"
						+ " if that instance has a graph assigned or already has a subject set.");
			}
			this.subject = subject;
		}
		
		/**
		 * Checks whether a given vertex is part of this
		 * functional unit instance.
		 * @param v Vertex to check for inclusion of.
		 * @return true if v is contained in this unit, false otherwise.
		 */
		public boolean contains(Vertex v) {
			return vertices.contains(v);
		}
		
		/**
		 * Retrieves the collection of vertices that are
		 * part of this functional unit instance.
		 * Note: modifying the returned collection modifies
		 * the collection in the functional unit instance.
		 * @return Collection of vertices
		 */
		public Collection<Vertex> getVertices() {
			return this.vertices;
		}
		
		/**
		 * Gets the functional unit this is an instance of.
		 * @return FunctionalUnit type of this instance.
		 */
		public FunctionalUnit getUnit() {
			return FunctionalUnit.this;
		}
		
		/**
		 * Returns an abbreviated version of this instance's unit's name.
		 * (All capital letters if the name is several words, or first
		 * three letters if the name is a single word)
		 */
		public String toString() {
			return FunctionalUnit.shortenName(getUnit().getName());
		}
		
		public String getFirstAgent() {
			return firstAgent;
		}

		public boolean isFirstPlural() {
			return firstPlural;
		}
		
		public void setFirstPlural() {
			firstPlural = true;
		}
		
		
		public String getSecondAgent() {
			return secondAgent;
		}
		
		public boolean isSecondPlural() {
			return secondPlural;
		}
		
		public void setSecondPlural() {
			secondPlural = true;
		}
		
		/**
		 * Allows manual setting of the first agent.
		 * (to allow instance merging from outside).
		 * @param firstAgent agent to set as the first agent.
		 */
		public void setFirstAgent(String firstAgent) {
			if(this.graph != null || this.firstAgent != null) {
				throw new RuntimeException("Can not set first agent on a FunctionalUnit$Instance,"
						+ " if that instance has a graph assigned or already has a first agent set.");
			}
			this.firstAgent = firstAgent;
		}
		
		/**
		 * Allows manual setting of the second agent.
		 * (to allow instance merging from outside).
		 * @param secondAgent agent to set as the second agent.
		 */
		public void setSecondAgent(String secondAgent) {
			if(this.graph != null || this.secondAgent != null) {
				throw new RuntimeException("Can not set second agent on a FunctionalUnit$Instance,"
						+ " if that instance has a graph assigned or already has a second agent set.");
			}
			this.secondAgent = secondAgent;
		}
		
		/**
		 * Computes the temporal length of this functional unit.
		 * This is the time step of the last vertex - the time step
		 * of the first vertex.
		 * @return
		 */
		public int getSpan() {
			return end - start;
		}
		
		/**
		 * Retrieves the time step of the first vertex of this unit.
		 * @return
		 */
		public int getStart() {
			return start;
		}

		/**
		 * Orders functional units by their starting time.
		 */
		@Override
		public int compareTo(Instance arg0) {
			if(this.graph == null || arg0.graph == null) {
				throw new IllegalArgumentException("Can not compare two FunctionalUnit$Instance objects when either one"
						+ " has no PlotDirectedSparseGraph!");
			}
			if(arg0.graph != this.graph) {
				throw new IllegalArgumentException("Can not compare two FunctionalUnit$Instance objects which belong to"
						+ " two different PlotDirectedSparseGraph!");
			}
			
			int stepComparison = this.getStart() - arg0.getStart();
			int minThis = Integer.MAX_VALUE;
			int minOther = Integer.MAX_VALUE;
			if(stepComparison == 0) {
				for(Vertex v : vertices) {
					if(v.getStep() == this.getStart()) {
						minThis = Math.min(minThis, this.graph.getInnerStep(v));
					}
				}
				for(Vertex v : arg0.vertices) {
					if(v.getStep() == arg0.getStart()) {
						minOther = Math.min(minOther, this.graph.getInnerStep(v));
					}
				}
				return minThis - minOther;
			}
			return stepComparison;
		}
	}
	
	static private String shortenName(String name) {
		String result = "";
		if(name.contains(" ")) {
			for(char c : name.toCharArray()) {
				if(c >= 'A' && c <= 'Z') {
					result += c;
				}
			}
		} else {
			result = name.substring(0, 3);
		}
		return result;
	}
}
