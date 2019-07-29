package inBloom.graph.isomorphism;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Class used to hold the state of partial mappings in the
 * subgraph isomorphism algorithm.
 * @author Sven Wilke
 */
public class State {

	private static final int NULL_NODE = -1;

	private PlotDirectedSparseGraph g1, g2;
	private int n1, n2;
	private int[] core1, core2, in1, in2, out1, out2;

	private int depth;

	public int candidateV1, candidateV2;

	private Map<String, Integer> agentNodeCounts;

	private boolean isCandidate;

	/**
	 * Creates a new State for the given plot graph and functional unit graph.
	 * @param plotGraph
	 * @param unitGraph
	 */
	public State(PlotDirectedSparseGraph plotGraph, PlotDirectedSparseGraph unitGraph) {
		this.g1 = plotGraph;
		this.g2 = unitGraph;
		this.depth = 0;
		this.candidateV1 = NULL_NODE;
		this.candidateV2 = NULL_NODE;
		this.n1 = this.g1.getVertexCount();
		this.n2 = this.g2.getVertexCount();
		assert this.n1 >= this.n2;
		this.core1 = new int[this.n1];
		this.core2 = new int[this.n2];
		this.in1 = new int[this.n1];
		this.in2 = new int[this.n2];
		this.out1 = new int[this.n1];
		this.out2 = new int[this.n2];

		this.agentNodeCounts = new HashMap<>();

		this.isCandidate = false;

		Arrays.fill(this.core1, NULL_NODE);
		Arrays.fill(this.core2, NULL_NODE);
		Arrays.fill(this.in1, NULL_NODE);
		Arrays.fill(this.in2, NULL_NODE);
		Arrays.fill(this.out1, NULL_NODE);
		Arrays.fill(this.out2, NULL_NODE);
	}

	/**
	 * Creates a candidate state, based on a candidate mapping and the previous state
	 * @param other State to copy.
	 */
	public State(State other, int v1, int v2) {
		// Save candidate mapping
		this.candidateV1 = v1;
		this.candidateV2 = v2;
		this.isCandidate = true;

		// take over previous state
		this.g1 = other.g1;
		this.g2 = other.g2;
		this.depth = other.depth + 1;
		this.n1 = other.n1;
		this.n2 = other.n2;
		this.core1 = other.core1;
		this.core2 = other.core2;
		this.in1 = other.in1;
		this.in2 = other.in2;
		this.out1 = other.out1;
		this.out2 = other.out2;

		this.agentNodeCounts = other.agentNodeCounts;
	}

	/**
	 * Checks whether this state is a goal state,
	 * i.e. whether all vertices of the unit graph
	 * were successfully mapped.
	 * @return true if goal state, false otherwise
	 */
	public boolean isGoal() {
		return this.depth == this.n2;
	}


	/**
	 * Adds the candidate mapping to this state, turning it from candidate into an actual SSR state.
	 */
	public void addCandidateMapping() {
		this.addMapping(this.candidateV1, this.candidateV2);
		this.isCandidate = false;
	}

	/**
	 * Adds a mapping between two vertices.
	 * @param v1 Index of the vertex in the plot graph
	 * @param v2 Index of the vertex in the functional unit graph
	 */
	public void addMapping(int v1, int v2) {
		assert this.core1[v1] == NULL_NODE;
		assert this.core2[v2] == NULL_NODE;

		this.core1[v1] = v2;
		this.core2[v2] = v1;

		this.countNodeForAgent(this.g1.getAgent(this.g1.getVertex(v1)));

		Set<Integer> in1Set = this.getPredecessors(this.g1, v1);
		Set<Integer> out1Set = this.getSuccessors(this.g1, v1);
		Set<Integer> in2Set = this.getPredecessors(this.g2, v2);
		Set<Integer> out2Set = this.getSuccessors(this.g2, v2);

		for(int in1Node : in1Set) {
			this.in1[in1Node] = this.depth;
		}
		for(int out1Node : out1Set) {
			this.out1[out1Node] = this.depth;
		}
		for(int in2Node : in2Set) {
			this.in2[in2Node] = this.depth;
		}
		for(int out2Node : out2Set) {
			this.out2[out2Node] = this.depth;
		}
	}

	/**
	 * Increases the number of vertices of the matching
	 * belonging to the given agent by 1.
	 * @param agent
	 */
	private void countNodeForAgent(String agent) {
		int current = 0;
		if(this.agentNodeCounts.containsKey(agent)) {
			current = this.agentNodeCounts.get(agent);
		}
		this.agentNodeCounts.put(agent, current + 1);
	}

	/**
	 * Decreases the number of vertices of the matching
	 * belonging to the given agent by 1.
	 * @param agent
	 */
	private void uncountNodeForAgent(String agent) {
		this.agentNodeCounts.put(agent, this.agentNodeCounts.get(agent) - 1);
	}

	/**
	 * Removes the last mapping and undoes all changes caused by the
	 * creation of that mapping.
	 */
	public void backtrack() {
		assert this.candidateV1 != NULL_NODE;
		assert this.candidateV1 != NULL_NODE;

		this.core1[this.candidateV1] = NULL_NODE;
		this.core2[this.candidateV2] = NULL_NODE;

		this.uncountNodeForAgent(this.g1.getAgent(this.g1.getVertex(this.candidateV1)));

		Set<Integer> in1Set = this.getPredecessors(this.g1, this.candidateV1);
		Set<Integer> out1Set = this.getSuccessors(this.g1, this.candidateV1);
		Set<Integer> in2Set = this.getPredecessors(this.g2, this.candidateV2);
		Set<Integer> out2Set = this.getSuccessors(this.g2, this.candidateV2);

		for(int in1Node : in1Set) {
			this.in1[in1Node] = NULL_NODE;
		}
		for(int out1Node : out1Set) {
			this.out1[out1Node] = NULL_NODE;
		}
		for(int in2Node : in2Set) {
			this.in2[in2Node] = NULL_NODE;
		}
		for(int out2Node : out2Set) {
			this.out2[out2Node] = NULL_NODE;
		}

		this.isCandidate = true;
	}

	/**
	 * Returns the mapping of vertices from the FU graph to the plot graph represented by the current state.
	 * @return HashMap containing current vertex mappings (FU vertex -> plot graph vertex)
	 */
	public Map<Vertex, Vertex> getMapping() {
		HashMap<Vertex, Vertex> mapping = new HashMap<>();
		for(int i = 0; i < this.n2; i++) {
			if(this.core2[i] != NULL_NODE) {
				mapping.put(this.g2.getVertex(i), this.g1.getVertex(this.core2[i]));
			}
		}
		return mapping;
	}

	/**
	 * Computes the set of possible mappings.
	 */
	public Set<State> getCandidates() {
		// TODO: create candidates based on split nodes in g2
		// TODO: Optimize by saving how many entries are in the sets
		//		 out1, out2, in1 and in2 and abort early

		// in s0 the candidates are generated by attempting to pair an initial v0 in g2 (FU-graph) with every v0 in g1 (plot graph)
		if (this.depth == 0) {
			HashSet<State> t0 = new HashSet<>();
			int g2v0 = 0;  // first vertex in g2
			for(int g1v0 = 0; g1v0 < this.n1; g1v0++) { // all possible candidates for mapping first vertex
					t0.add(new State(this, g1v0, g2v0));
			}

			return t0;
		}

		// Consider successor pairs
		HashSet<State> tOut = new HashSet<>();
		for(int currentOut1 = 0; currentOut1 < this.n1; currentOut1++) {
			if(this.out1[currentOut1] != NULL_NODE && this.core1[currentOut1] == NULL_NODE) {
				for(int currentOut2 = 0; currentOut2 < this.n2; currentOut2++) {
					if(this.out2[currentOut2] != NULL_NODE && this.core2[currentOut2] == NULL_NODE) {
						tOut.add(new State(this, currentOut1, currentOut2));
					}
				}
			}
		}
		if(!tOut.isEmpty()) {
			return tOut;
		}

		// Consider predecessor pairs
		HashSet<State> tIn = new HashSet<>();
		for(int currentIn1 = 0; currentIn1 < this.n1; currentIn1++) {
			if(this.in1[currentIn1] != NULL_NODE && this.core1[currentIn1] == NULL_NODE) {
				for(int currentIn2 = 0; currentIn2 < this.n2; currentIn2++) {
					if(this.in2[currentIn2] != NULL_NODE && this.core2[currentIn2] == NULL_NODE) {
						tIn.add(new State(this, currentIn1, currentIn2));
					}
				}
			}
		}
		if(!tIn.isEmpty()) {
			return tIn;
		}

		if(this.depth > 0) {
			return Collections.emptySet();
		}

		HashSet<State> tall = new HashSet<>();
		for(int current1 = 0; current1 < this.n1; current1++) {
			if(this.core1[current1] == NULL_NODE) {
				for(int current2 = 0; current2 < this.n2; current2++) {
					if(this.core2[current2] == NULL_NODE) {
						tall.add(new State(this, current1, current2));
					}
				}
			}
		}

		return tall;
	}

	/**
	 * Checks whether a matching of two vertices
	 * is feasible both syntactically and semantically.
	 * @param candidateV1 Index of the vertex in the plot graph
	 * @param candidateV2 Index of the vertex in the functional unit graph
	 * @return true if the matching is feasible.
	 */
	public boolean isFeasible() {
		assert this.candidateV1 < this.n1;
		assert this.candidateV2 < this.n2;
		assert this.core1[this.candidateV1] == NULL_NODE;
		assert this.core2[this.candidateV2] == NULL_NODE;

		return this.isSynFeasible() && this.isSemFeasible();
	}

	/**
	 * Checks if a potential matching is syntactically feasible.
	 * @param candidateV1 Index of the vertex in the plot graph
	 * @param candidateV2 Index of the vertex in the functional unit graph
	 * @return true if the matching is syntactically feasible.
	 */
	private boolean isSynFeasible() {

		Set<Integer> pred1 = this.getPredecessors(this.g1, this.candidateV1);
		Set<Integer> pred2 = this.getPredecessors(this.g2, this.candidateV2);
		Set<Integer> succ1 = this.getSuccessors(this.g1, this.candidateV1);
		Set<Integer> succ2 = this.getSuccessors(this.g2, this.candidateV2);

		// Calculate whether R_Pred and R_Succ hold.
		boolean rPred = true;
		boolean rSucc = true;

		for(int n : pred1) {
			if(this.core1[n] != NULL_NODE) {
				if(!pred2.contains(this.core1[n])) {
					rPred = false;
					break;
				}
			}
		}

		for(int n : succ1) {
			if(this.core1[n] != NULL_NODE) {
				if(!succ2.contains(this.core1[n])) {
					rSucc = false;
					break;
				}
			}
		}

		for(int n : pred2) {
			if(this.core2[n] != NULL_NODE) {
				if(!pred1.contains(this.core2[n])) {
					rPred = false;
					break;
				}
			}
		}

		for(int n : succ2) {
			if(this.core2[n] != NULL_NODE) {
				if(!succ1.contains(this.core2[n])) {
					rSucc = false;
					break;
				}
			}
		}

		if(!rPred || !rSucc) {
			return false;
		}

		// Calculate whether R_In and R_Out hold.
		boolean rIn;
		boolean rOut;

		int cardSuccIn1 = 0;
		int cardSuccIn2 = 0;
		int cardPredIn1 = 0;
		int cardPredIn2 = 0;
		int cardSuccOut1 = 0;
		int cardSuccOut2 = 0;
		int cardPredOut1 = 0;
		int cardPredOut2 = 0;

		for(int n = 0; n < this.n1; n++) {
			if(this.in1[n] != NULL_NODE) {
				if(succ1.contains(n)) {
					cardSuccIn1++;
				}
				if(pred1.contains(n)) {
					cardPredIn1++;
				}
			}
			if(this.out1[n] != NULL_NODE) {
				if(succ1.contains(n)) {
					cardSuccOut1++;
				}
				if(pred1.contains(n)) {
					cardPredOut1++;
				}
			}
		}
		for(int n = 0; n < this.n2; n++) {
			if(this.in2[n] != NULL_NODE) {
				if(succ2.contains(n)) {
					cardSuccIn2++;
				}
				if(pred2.contains(n)) {
					cardPredIn2++;
				}
			}
			if(this.out2[n] != NULL_NODE) {
				if(succ2.contains(n)) {
					cardSuccOut2++;
				}
				if(pred2.contains(n)) {
					cardPredOut2++;
				}
			}
		}

		rIn = cardSuccIn1 >= cardSuccIn2 && cardPredIn1 >= cardPredIn2;
		rOut = cardSuccOut1 >= cardSuccOut2 && cardPredOut1 >= cardPredOut2;

		if(!(rIn && rOut)) {
			return false;
		}

		// Calculate whether R_New holds.
		boolean rNew;

		int cardN1Pred = 0;
		int cardN2Pred = 0;
		int cardN1Succ = 0;
		int cardN2Succ = 0;

		for(int n = 0; n < this.n1; n++) {
			if(   this.core1[n] != NULL_NODE
				&&  this.in1[n] != NULL_NODE
				&& this.out1[n] != NULL_NODE) {
				if(pred1.contains(n)) {
					cardN1Pred++;
				}
				if(succ1.contains(n)) {
					cardN1Succ++;
				}
			}
		}
		for(int n = 0; n < this.n2; n++) {
			if(   this.core2[n] != NULL_NODE
				&&  this.in2[n] != NULL_NODE
				&& this.out2[n] != NULL_NODE) {
				if(pred2.contains(n)) {
					cardN2Pred++;
				}
				if(succ2.contains(n)) {
					cardN2Succ++;
				}
			}
		}

		rNew = cardN1Pred >= cardN2Pred && cardN1Succ >= cardN2Succ;

		return rNew;
	}

	/**
	 * Checks if a potential matching is semantically feasible
	 * by checking for vertex and edge compatibility.
	 * @param candidateV1 Index of the vertex in the plot graph
	 * @param candidateV2 Index of the vertex in the functional unit graph
	 * @return true if the matching is semantically feasible.
	 */
	private boolean isSemFeasible() {
		return this.checkVertexCompatibility()
			&& this.checkEdgeCompatibility();
	}

	/**
	 * Checks whether the vertices of a vertex matching
	 * are compatible.
	 * @param v1 Index of the vertex in the plot graph
	 * @param v2 Index of the vertex in the functional unit graph
	 * @return true if the vertices are compatible.
	 */
	private boolean checkVertexCompatibility() {
		Vertex plotVertex = this.g1.getVertex(this.candidateV1);
		Integer currentCount = this.agentNodeCounts.get(this.g1.getAgent(plotVertex));
		if(currentCount == null || currentCount == 0) {
			int involvedAgents = 0;
			for(int nodeCount : this.agentNodeCounts.values()) {
				if(nodeCount > 0) {
					involvedAgents++;
				}
			}
			if(involvedAgents >= 2) {
				return false;
			}
		}
		UnitVertexType t1 = UnitVertexType.typeOf(plotVertex);
        UnitVertexType t2 = UnitVertexType.typeOf(this.g2.getVertex(this.candidateV2));
        if(t2 == UnitVertexType.WILDCARD) {
        	return true;
        }
        if(t2 == UnitVertexType.POLYEMOTIONAL) {
        	return t1 != UnitVertexType.INTENTION;
        }
        if(t1 == UnitVertexType.POLYEMOTIONAL) {
        	return t2 != UnitVertexType.INTENTION;
        }
        return t1 != UnitVertexType.NONE && t1 == t2;
	}

	/**
	 * Checks whether the edges of a vertex matching
	 * are compatible.
	 * @param v1 Index of the vertex in the plot graph
	 * @param v2 Index of the vertex in the functional unit graph
	 * @return true if the edges are compatible.
	 */
	private boolean checkEdgeCompatibility() {
		for(int m = 0; m < this.n2; m++) {
			if(this.core2[m] != NULL_NODE) {
				Vertex vn1 = this.g1.getVertex(this.candidateV1);
				Vertex vn2 = this.g1.getVertex(this.core2[m]);
				Vertex vm1 = this.g2.getVertex(this.candidateV2);
				Vertex vm2 = this.g2.getVertex(m);

				boolean isWildcard = UnitVertexType.typeOf(vm1) == UnitVertexType.WILDCARD
								  || UnitVertexType.typeOf(vm2) == UnitVertexType.WILDCARD;

				Collection<Edge> nEdges = this.getEdges(this.g1, vn1, vn2);
				Collection<Edge> mEdges = this.getEdges(this.g2, vm1, vm2);

				if(!this.checkEdgeSetCompatibility(nEdges, mEdges, isWildcard)) {
					return false;
				}

				nEdges = this.getEdges(this.g1, vn2, vn1);
				mEdges = this.getEdges(this.g2, vm2, vm1);

				if(!this.checkEdgeSetCompatibility(nEdges, mEdges, isWildcard)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether the edges between two vertices of the plot graph and
	 * the edges between two vertices of the unit graph match.
	 * @param plotEdges The edges of the plot graph
	 * @param unitEdges The edges of the functional unit graph
	 * @param isWildcard whether one of the vertices adjacent to the edges in the functional unit graph was a wildcard vertex.
	 * @return true if the edges match, false if they don't.
	 */
	private boolean checkEdgeSetCompatibility(Collection<Edge> plotEdges, Collection<Edge> unitEdges, boolean isWildcard) {
		// If the unit vertex is a wildcard, simply check the amount of edges,
		// effectively making use of "wildcard edges"
		if(isWildcard) {
			int crossCharacterCount = 0;
			for(Edge e : unitEdges) {
				if(e.getType() == Edge.Type.CROSSCHARACTER) {
					crossCharacterCount++;
				}
			}
			for(Edge e : plotEdges) {
				if(e.getType() == Edge.Type.CROSSCHARACTER) {
					crossCharacterCount--;
				}
			}
			return crossCharacterCount == 0 && plotEdges.size() == unitEdges.size();
		}

		LinkedList<Edge.Type> edgeTypes = new LinkedList<>();

		for(Edge e : unitEdges) {
			edgeTypes.add(e.getType());
		}
		for(Edge e : plotEdges) {
			if(edgeTypes.contains(e.getType())) {
				edgeTypes.remove(e.getType());
			}
		}
		// If there were edges of some type in the unit graph
		// which are not available in the plot graph: incompatible
		return edgeTypes.isEmpty();
	}

	/**
	 * Computes the set of edges between two given vertices.
	 * Filters the edges using the {@link #isEdgeValid(Edge) isEdgeValid} method,
	 * by only including those for which that method returns true.
	 * @param g	Graph the vertices are in.
	 * @param v1 Vertex from which edges should originate
	 * @param v2 Vertex which the edges should lead to
	 * @return Collection with all valid edges between <i>v1</i> and <i>v2</i>.
	 */
	private Collection<Edge> getEdges(PlotDirectedSparseGraph g, Vertex v1, Vertex v2) {
		Collection<Edge> allEdges = g.findEdgeSet(v1, v2);
		Collection<Edge> filteredEdges = new HashSet<>();

		for(Edge e : allEdges) {
			if(this.isEdgeValid(e)) {
				filteredEdges.add(e);
			}
		}

		return filteredEdges;
	}

	/**
	 * Computes a set of indices of all predecessors of a vertex corresponding
	 * to a provided index <i>v</i>.
	 * @param g The graph the vertex is in.
	 * @param v The index of the vertex to look for predecessors of.
	 * @return HashSet containing indices of vertices which are predecessors.
	 */
	private Set<Integer> getPredecessors(PlotDirectedSparseGraph g, int v) {
		Vertex vert = g.getVertex(v);
		HashSet<Integer> predecessors = new HashSet<>();
		for(Vertex p : g.getPredecessors(vert)) {
			Collection<Edge> edges = g.findEdgeSet(p, vert);
			for(Edge e : edges) {
				if(this.isEdgeValid(e)) {
					predecessors.add(g.getVertexId(p));
					break;
				}
			}
		}
		return predecessors;
	}

	/**
	 * Computes a set of indices of all successors of a vertex corresponding
	 * to a provided index <i>v</i>.
	 * @param g The graph the vertex is in.
	 * @param v The index of the vertex to look for successors of.
	 * @return HashSet containing indices of vertices which are successors.
	 */
	private HashSet<Integer> getSuccessors(PlotDirectedSparseGraph g, int v) {
		Vertex vert = g.getVertex(v);
		HashSet<Integer> successors = new HashSet<>();
		for(Vertex p : g.getSuccessors(vert)) {
			Collection<Edge> edges = g.findEdgeSet(vert, p);
			for(Edge e : edges) {
				if(this.isEdgeValid(e)) {
					successors.add(g.getVertexId(p));
					break;
				}
			}
		}
		return successors;
	}

	/**
	 * Defines whether an edge should be considered in the isomorphism.
	 * @param e Edge
	 * @return whether <i>e</i> will be considered in the isomorphism search.
	 */
	private boolean isEdgeValid(Edge e) {
		return e.getType() != Edge.Type.TEMPORAL && e.getType() != Edge.Type.ROOT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.agentNodeCounts == null ? 0 : this.agentNodeCounts.hashCode());
		result = prime * result + Arrays.hashCode(this.core1);
		result = prime * result + Arrays.hashCode(this.core2);
		result = prime * result + this.depth;
		result = prime * result + (this.g1 == null ? 0 : this.g1.hashCode());
		result = prime * result + (this.g2 == null ? 0 : this.g2.hashCode());
		result = prime * result + Arrays.hashCode(this.in1);
		result = prime * result + Arrays.hashCode(this.in2);
		result = prime * result + this.candidateV1;
		result = prime * result + this.candidateV2;
		result = prime * result + this.n1;
		result = prime * result + this.n2;
		result = prime * result + Arrays.hashCode(this.out1);
		result = prime * result + Arrays.hashCode(this.out2);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		State other = (State) obj;
		if (this.agentNodeCounts == null) {
			if (other.agentNodeCounts != null) {
				return false;
			}
		} else if (!this.agentNodeCounts.equals(other.agentNodeCounts)) {
			return false;
		}
		if (!Arrays.equals(this.core1, other.core1)) {
			return false;
		}
		if (!Arrays.equals(this.core2, other.core2)) {
			return false;
		}
		if (this.depth != other.depth) {
			return false;
		}
		if (this.g1 == null) {
			if (other.g1 != null) {
				return false;
			}
		} else if (!this.g1.equals(other.g1)) {
			return false;
		}
		if (this.g2 == null) {
			if (other.g2 != null) {
				return false;
			}
		} else if (!this.g2.equals(other.g2)) {
			return false;
		}
		if (!Arrays.equals(this.in1, other.in1)) {
			return false;
		}
		if (!Arrays.equals(this.in2, other.in2)) {
			return false;
		}
		if (this.candidateV1 != other.candidateV1) {
			return false;
		}
		if (this.candidateV2 != other.candidateV2) {
			return false;
		}
		if (this.n1 != other.n1) {
			return false;
		}
		if (this.n2 != other.n2) {
			return false;
		}
		if (!Arrays.equals(this.out1, other.out1)) {
			return false;
		}
		if (!Arrays.equals(this.out2, other.out2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String repr =  this.getMapping().toString();

		if (this.isCandidate) {
			repr += " candidate: ";
			repr += "{" + this.g2.getVertex(this.candidateV2) + " = " + this.g1.getVertex(this.candidateV1) + "}";
		}

		return repr;
	}
}
