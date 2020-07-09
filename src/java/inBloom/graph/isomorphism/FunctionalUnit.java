package inBloom.graph.isomorphism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jason.util.Pair;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Class which wraps around a PlotDirectedSparseGraph to be used as a functional unit.
 * Provides functionality for creating a displayable graph of this unit.
 * @author Sven Wilke
 */
public class FunctionalUnit {

	static Logger logger = Logger.getLogger(FunctionalUnit.class.getName());

	private String name;
	private PlotDirectedSparseGraph unitGraph;			  // graph of the functional unit
	private PlotDirectedSparseGraph displayGraph;		  // lazyly created graph that can be used to display this FU, containing roots and such
	private List<Integer> startSteps = new ArrayList<>(); // contains the step number of the vertices that are the first in each subgraph, can have 0..2 elements

	private boolean isPrimitive = false;

	private Pair<Vertex, String> subject;

	/**
	 * Creates a functional unit based on graph. The graph should contain vertices with plot edges, but no root or temporal edges.
	 * Each vertex should have a unique step so that a reproducible total order is ensured even when cloning. <br>
	 * To be able to display the FU properly, the first vertices in each char subgraph need to be indicated, by providing
	 * their respective step number. These vertices will be connected with root nodes on display.<br>
	 * If no startStep is provided, the start step is assumed to be 1.
	 *
	 * @param name name of the FU
	 * @param graph the graph of the FU
	 * @param startSteps the step numbers of the vertices that are to be used as first vertex in each char subgraph, i.e. to be connected with the roots.
	 */
	public FunctionalUnit(String name, PlotDirectedSparseGraph graph, Integer... startSteps) {
		this.name = name;
		this.unitGraph = graph;

		if(startSteps.length > 2) {
			throw new RuntimeException("Only 2 start vertices allowed in FU");
		}

		this.startSteps = new ArrayList<>(Arrays.asList(startSteps));

		// if no start steps were given, assume graph starts at step 1
		if (this.startSteps.size() == 0) {
			this.startSteps.add(1);
		}
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
		if(this.displayGraph == null) {
			this.createDisplayGraph();
		}
		return this.displayGraph;
	}

	private void createDisplayGraph() {
		this.displayGraph = this.unitGraph.clone();

		Vertex root2 = null;
		Vertex[] roots = new Vertex[] {
			this.displayGraph.addRoot(this.name + " Ag 1"),
			root2 = this.displayGraph.addRoot(this.name + " Ag 2")
		};

		this.connect(roots);

		if(this.displayGraph.getIncidentEdges(root2).isEmpty()) {
			this.displayGraph.removeVertex(root2);
			this.displayGraph.getRoots().remove(root2);
		}

		this.displayGraph.setName(this.getName());
	}

	/**
	 * Connects the display graph temporally using the steps
	 * of the vertices. Assumes that for each character,
	 * exactly one vertex at step 1 exists.
	 * @param roots
	 */
	private void connect(Vertex[] roots) {
		Vertex[] starts = {null, null};
		int pos = 0;
		ArrayList<Vertex> allV = new ArrayList<>(this.displayGraph.getVertices());
		for(Integer startStep : this.startSteps) {
			Iterator<Vertex> i = allV.iterator();
			while(i.hasNext()) {
				Vertex v = i.next();
				if(v.getStep() == startStep) {
					starts[pos] = v;
					i.remove();			// remove v from allV, so that it won't be returned again in cases when the startSteps are the same
					break;
				}
			}

			if(starts[pos] == null) {throw new RuntimeException("Couldn't find vertex with step: " + startStep + " in FU " + this.name);}

			pos += 1;
		}

		List<Vertex> vertexDump0 = new ArrayList<>();
		List<Vertex> vertexDump1 = new ArrayList<>();
		vertexDump0.add(starts[0]);
		vertexDump1.add(starts[1]);
		Set<Vertex> vertices0 = new HashSet<>();
		Set<Vertex> vertices1 = new HashSet<>();
		boolean allIn0, allIn1;
		do {
			allIn0 = true;
			allIn1 = true;
			for(Vertex v : vertexDump0) {
				if(!vertices0.contains(v)) {
					allIn0 = false;
					this.collectSubVertices(v, vertices0, vertexDump1);
				}
			}
			if(starts[1] != null) {
				for(Vertex v : vertexDump1) {
					if(!vertices1.contains(v)) {
						allIn1 = false;
						this.collectSubVertices(v, vertices1, vertexDump0);
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
		if(starts[1] != null) {
			vertices1.toArray(sortVertices1);
		}
		Arrays.sort(sortVertices0, stepComparator);
		if(starts[1] != null) {
			Arrays.sort(sortVertices1, stepComparator);
		}

		for(int i = 0; i < sortVertices0.length; i++) {
			if(i == 0) {
				this.displayGraph.addEdge(new Edge(Edge.Type.ROOT), roots[0], sortVertices0[0]);
			} else {
				this.displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), sortVertices0[i - 1], sortVertices0[i]);
			}
		}

		if(starts[1] != null) {
			for(int i = 0; i < sortVertices1.length; i++) {
				if(i == 0) {
					this.displayGraph.addEdge(new Edge(Edge.Type.ROOT), roots[1], sortVertices1[0]);
				} else {
					this.displayGraph.addEdge(new Edge(Edge.Type.TEMPORAL), sortVertices1[i - 1], sortVertices1[i]);
				}
			}
		}
	}

	private void collectSubVertices(Vertex start, Set<Vertex> vertices, List<Vertex> dump) {
		Queue<Vertex> queue = new LinkedList<>();
		queue.add(start);
		while(!queue.isEmpty()) {
			Vertex vert = queue.remove();
			if(!vertices.contains(vert)) {
				vertices.add(vert);
				for(Edge e : this.displayGraph.getIncidentEdges(vert)) {

					Vertex toAdd = this.displayGraph.getSource(e);
					if(toAdd == vert) {
						toAdd = this.displayGraph.getDest(e);
					}
					if(e.getType() == Edge.Type.CROSSCHARACTER) {
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

		public PlotDirectedSparseGraph graph;
		private Collection<Vertex> vertices;
		private int start = Integer.MAX_VALUE;
		private int end = Integer.MIN_VALUE;
		private String type;

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
		public Instance(PlotDirectedSparseGraph graph, Collection<Vertex> vertices, String type) {
			this.graph = graph;
			this.vertices = vertices;
			this.type = type;

			for(Vertex v : vertices) {
				this.start = Math.min(this.start, v.getStep());
				this.end = Math.max(this.end, v.getStep());
				if(graph != null) {
					if(this.firstAgent != null && !graph.getAgent(v).equals(this.firstAgent)) {
						this.secondAgent = graph.getAgent(v);
					} else
					if(this.firstAgent == null) {
						boolean containsAnyPredecessors = false;
						for(Vertex p : graph.getRealPredecessors(v)) {
							if(vertices.contains(p)) {
								containsAnyPredecessors = true;
								break;
							}
						}
						if(!containsAnyPredecessors) {
							this.firstAgent = graph.getAgent(v);
						}
					}
				}
			}
			if(graph != null && this.secondAgent == null) {
				for(Vertex v : vertices) {
					if(!graph.getAgent(v).equals(this.firstAgent)) {
						this.secondAgent = graph.getAgent(v);
						break;
					}
				}
			}
		}

		/**
		 * Given a mapping of functional unit vertices to plot vertices,
		 * this computes the subject of the instance and stores it.
		 * @param unitMapping Mapping as created by UnitFinder
		 */
		public void identifySubject(Map<Vertex, Vertex> unitMapping) {
			if(this.getUnit().subject == null) {
				return;
			}
			for(Map.Entry<Vertex, Vertex> kvp : unitMapping.entrySet()) {
				if(kvp.getValue() == this.getUnit().subject.getFirst()) {
					Pattern p = Pattern.compile(this.getUnit().subject.getSecond());
					Matcher m = p.matcher(kvp.getKey().getLabel());
					if (m.find()) {
						this.subject = m.group(1);
					} else {
						logger.severe("Couldn't identify subject of FU " + this.type + " using pattern:" + this.getUnit().subject.getSecond());
					}
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

		public String getType() {
			return this.type;
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
			return this.vertices.contains(v);
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
			return FunctionalUnit.shortenName(this.getUnit().getName());
		}

		public String getFirstAgent() {
			return this.firstAgent;
		}

		public boolean isFirstPlural() {
			return this.firstPlural;
		}

		public void setFirstPlural() {
			this.firstPlural = true;
		}


		public String getSecondAgent() {
			return this.secondAgent;
		}

		public boolean isSecondPlural() {
			return this.secondPlural;
		}

		public void setSecondPlural() {
			this.secondPlural = true;
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
			return this.end - this.start;
		}

		/**
		 * Retrieves the time step of the first vertex of this unit.
		 * @return
		 */
		public int getStart() {
			return this.start;
		}

		/**
		 * Retrieves the time step of the last vertex of this unit.
		 * @return
		 */
		public int getEnd() {
			return this.end;
		}

		/**
		 * Orders functional units by their starting step.
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
				for(Vertex v : this.vertices) {
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

	/**
	 * Comparator that orders instances according to the order in which they appear in the subgraph of an agent. The order
	 * is determined on the plot step of the last vertex, then on the order inside the step of the last vertex, then on
	 * the plot step of the first vertex, then on the order inside the step of the first vertex.
	 * @author Leonid Berov
	 */
	public static class InstanceSubgraphOrderComparator implements Comparator<Instance> {

		private String agent;

		/**
		 * Initializes the comparator with the label of the root vertex that starts the subgraph in which order is to be
		 * determined.
		 * @param agent name of the agent
		 */
		public InstanceSubgraphOrderComparator(String agent) {
			this.agent = agent;
		}

		@Override
		public int compare(Instance instance1, Instance instance2) {
			logger.fine("Comparing: " + instance1 + " and " + instance2);

			// project instances on subgraph of currently processed agent
			List<Vertex> thisAgentInstance1Vertices = instance1.getVertices().stream().filter(v -> v.getRoot().toString().equals(this.agent)).collect(Collectors.toList());
			List<Vertex> thisAgentInstance2Vertices = instance2.getVertices().stream().filter(v -> v.getRoot().toString().equals(this.agent)).collect(Collectors.toList());

			// Comparing based on step of last vertex of this instance in this subgraph
			int maxStepThisAgent = thisAgentInstance1Vertices.stream().map(v -> v.getStep()).mapToInt(step -> step).max().getAsInt();
			final int maxStepInstance1 = new Integer(maxStepThisAgent);

			maxStepThisAgent = thisAgentInstance2Vertices.stream().map(v -> v.getStep()).mapToInt(step -> step).max().getAsInt();
			final int maxStepInstance2 = new Integer(maxStepThisAgent);


			int stepComparison = maxStepInstance1 - maxStepInstance2;
			logger.fine("   last step comparison: " + stepComparison + "(" + maxStepInstance1 + ", " + maxStepInstance2 +")");
			if(stepComparison != 0) {
				return stepComparison;
			}

			// in case of same step, compare based on 'inner step', i.e. absolute position inside step
			int maxInstance1Inner = thisAgentInstance1Vertices.stream().filter(v -> v.getStep() == maxStepInstance1)
														      .map(v -> instance1.graph.getInnerStep(v))
														      .mapToInt(innerStep -> innerStep)
														      .max().getAsInt();

			int maxInstance2Inner = thisAgentInstance2Vertices.stream().filter(v -> v.getStep() == maxStepInstance2)
															  .map(v -> instance1.graph.getInnerStep(v))
															  .mapToInt(innerStep -> innerStep)
															  .max().getAsInt();

			int innerStepComparison = maxInstance1Inner - maxInstance2Inner;
			logger.fine("   last inner step comparison: " + innerStepComparison + "(" + maxInstance1Inner + ", " + maxInstance2Inner +")");
			if(innerStepComparison != 0) {
				return innerStepComparison;
			}

			// in case of same inner step (e.g. instances ending in same vertex), compare based on starting step and starting inner step
			int minStepThisAgent = thisAgentInstance1Vertices.stream().map(v -> v.getStep()).mapToInt(step -> step).min().getAsInt();
			final int minStepInstance1 = new Integer(minStepThisAgent);

			minStepThisAgent = thisAgentInstance2Vertices.stream().map(v -> v.getStep()).mapToInt(step -> step).min().getAsInt();
			final int minStepInstance2 = new Integer(minStepThisAgent);


			stepComparison = minStepInstance1 - minStepInstance2;
			logger.fine("   start step comparison: " + stepComparison + "(" + minStepInstance1 + ", " + minStepInstance2 +")");
			if(stepComparison != 0) {
				return stepComparison;
			}

			// in case of same step, compare based on 'inner step', i.e. absolute position inside step
			int minInstance1Inner = thisAgentInstance1Vertices.stream().filter(v -> v.getStep() == minStepInstance1)
														      .map(v -> instance1.graph.getInnerStep(v))
														      .mapToInt(innerStep -> innerStep)
														      .min().getAsInt();

			int minInstance2Inner = thisAgentInstance2Vertices.stream().filter(v -> v.getStep() == minStepInstance2)
															  .map(v -> instance1.graph.getInnerStep(v))
															  .mapToInt(innerStep -> innerStep)
															  .min().getAsInt();

			innerStepComparison = minInstance1Inner - minInstance2Inner;
			logger.fine("   start inner step comparison: " + innerStepComparison + "(" + minInstance1Inner + ", " + minInstance2Inner +")");
			return innerStepComparison;
		}
	}
}
