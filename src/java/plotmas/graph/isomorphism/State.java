package plotmas.graph.isomorphism;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.util.Pair;
import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

public class State {
	
	private static final int NULL_NODE = -1;
	
	private PlotDirectedSparseGraph g1, g2;
	private int n1, n2;
	private int[] core1, core2, in1, in2, out1, out2;
	
	private int depth;
	
	private int lastAddedv1;
	
	private Map<String, Integer> agentNodeCounts;
	
	public State(PlotDirectedSparseGraph plotGraph, PlotDirectedSparseGraph unitGraph) {
		g1 = plotGraph;
		g2 = unitGraph;
		depth = 0;
		lastAddedv1 = NULL_NODE;
		n1 = g1.getVertexCount();
		n2 = g2.getVertexCount();
		assert n1 >= n2;
		core1 = new int[n1];
		core2 = new int[n2];
		in1 = new int[n1];
		in2 = new int[n2];
		out1 = new int[n1];
		out2 = new int[n2];
		
		agentNodeCounts = new HashMap<String, Integer>();
		
		Arrays.fill(core1, NULL_NODE);
		Arrays.fill(core2, NULL_NODE);
		Arrays.fill(in1, NULL_NODE);
		Arrays.fill(in2, NULL_NODE);
		Arrays.fill(out1, NULL_NODE);
		Arrays.fill(out2, NULL_NODE);
	}
	
	public State(State other) {
		g1 = other.g1;
		g2 = other.g2;
		depth = other.depth + 1;
		n1 = other.n1;
		n2 = other.n2;
		core1 = other.core1;
		core2 = other.core2;
		in1 = other.in1;
		in2 = other.in2;
		out1 = other.out1;
		out2 = other.out2;
		
		agentNodeCounts = other.agentNodeCounts;
	}
	
	/**
	 * Checks whether this state is a goal state,
	 * i.e. whether all vertices of the unit graph
	 * were successfully mapped.
	 * @return true if goal state, false otherwise
	 */
	public boolean isGoal() {
		return depth == n2;
	}
	
	public void addMapping(int v1, int v2) {
		assert core1[v1] == NULL_NODE;
		assert core2[v2] == NULL_NODE;
		
		// Save vertex ID for backtracking
		lastAddedv1 = v1;
		
		core1[v1] = v2;
		core2[v2] = v1;
		
		countNodeForAgent(g1.getAgent(g1.getVertex(v1)));
		
		Set<Integer> in1Set = getPredecessors(g1, v1);
		Set<Integer> out1Set = getSuccessors(g1, v1);
		Set<Integer> in2Set = getPredecessors(g2, v2);
		Set<Integer> out2Set = getSuccessors(g2, v2);
		
		for(int in1Node : in1Set) {
			in1[in1Node] = depth;
		}
		for(int out1Node : out1Set) {
			out1[out1Node] = depth;
		}
		for(int in2Node : in2Set) {
			in2[in2Node] = depth;
		}
		for(int out2Node : out2Set) {
			out2[out2Node] = depth;
		}
	}
	
	private void countNodeForAgent(String agent) {
		int current = 0;
		if(agentNodeCounts.containsKey(agent)) {
			current = agentNodeCounts.get(agent);
		}
		agentNodeCounts.put(agent, current + 1);
	}
	
	private void uncountNodeForAgent(String agent) {
		agentNodeCounts.put(agent, agentNodeCounts.get(agent) - 1);
	}
	
	public void backtrack() {
		assert lastAddedv1 != NULL_NODE;
		
		int lastAddedv2 = core1[lastAddedv1];
		core1[lastAddedv1] = NULL_NODE;
		core2[lastAddedv2] = NULL_NODE;
		
		uncountNodeForAgent(g1.getAgent(g1.getVertex(lastAddedv1)));
		
		Set<Integer> in1Set = getPredecessors(g1, lastAddedv1);
		Set<Integer> out1Set = getSuccessors(g1, lastAddedv1);
		Set<Integer> in2Set = getPredecessors(g2, lastAddedv2);
		Set<Integer> out2Set = getSuccessors(g2, lastAddedv2);
		
		for(int in1Node : in1Set) {
			in1[in1Node] = NULL_NODE;
		}
		for(int out1Node : out1Set) {
			out1[out1Node] = NULL_NODE;
		}
		for(int in2Node : in2Set) {
			in2[in2Node] = NULL_NODE;
		}
		for(int out2Node : out2Set) {
			out2[out2Node] = NULL_NODE;
		}
	}
	
	/**
	 * Returns the mapping of vertices from the plot
	 * graph to vertices of the unit graph represented
	 * by the current state
	 * @return HashMap containing current vertex mappings
	 */
	public Map<Vertex, Vertex> getMapping() {
		HashMap<Vertex, Vertex> mapping = new HashMap<Vertex, Vertex>();
		for(int i = 0; i < n1; i++) {
			if(core1[i] != NULL_NODE) {
				mapping.put(g1.getVertex(i), g2.getVertex(core1[i]));
			}
		}
		return mapping;
	}
	
	public Set<Pair<Integer>> getCandidates() {
		
		// TODO: Optimize by saving how many entries are in the sets
		//		 out1, out2, in1 and in2 and abort early

		// Consider successor pairs
		HashSet<Pair<Integer>> tOut = new HashSet<Pair<Integer>>();
		for(int currentOut1 = 0; currentOut1 < n1; currentOut1++) {
			if(out1[currentOut1] != NULL_NODE && core1[currentOut1] == NULL_NODE) {
				for(int currentOut2 = 0; currentOut2 < n2; currentOut2++) {
					if(out2[currentOut2] != NULL_NODE && core2[currentOut2] == NULL_NODE) {
						tOut.add(new Pair<Integer>(currentOut1, currentOut2));
					}
				}
			}
		}
		if(!tOut.isEmpty()) {
			return tOut;
		}
		
		// Consider predecessor pairs
		HashSet<Pair<Integer>> tIn = new HashSet<Pair<Integer>>();
		for(int currentIn1 = 0; currentIn1 < n1; currentIn1++) {
			if(in1[currentIn1] != NULL_NODE && core1[currentIn1] == NULL_NODE) {
				for(int currentIn2 = 0; currentIn2 < n2; currentIn2++) {
					if(in2[currentIn2] != NULL_NODE && core2[currentIn2] == NULL_NODE) {
						tIn.add(new Pair<Integer>(currentIn1, currentIn2));
					}
				}
			}
		}
		if(!tIn.isEmpty()) {
			return tIn;
		}
		
		HashSet<Pair<Integer>> tAll = new HashSet<Pair<Integer>>();
		for(int current1 = 0; current1 < n1; current1++) {
			if(core1[current1] == NULL_NODE) {
				for(int current2 = 0; current2 < n2; current2++) {
					if(core2[current2] == NULL_NODE) {
						tAll.add(new Pair<Integer>(current1, current2));
					}
				}
			}
		}
		
		return tAll;
	}
	
	public boolean isFeasible(int v1, int v2) {
		assert v1 < n1;
		assert v2 < n2;
		assert core1[v1] == NULL_NODE;
		assert core2[v2] == NULL_NODE;

		return isSynFeasible(v1, v2) && isSemFeasible(v1, v2);
	}
	
	private boolean isSynFeasible(int v1, int v2) {
		
		Set<Integer> pred1 = getPredecessors(g1, v1);
		Set<Integer> pred2 = getPredecessors(g2, v2);
		Set<Integer> succ1 = getSuccessors(g1, v1);
		Set<Integer> succ2 = getSuccessors(g2, v2);
		
		// Calculate whether R_Pred and R_Succ hold.
		boolean rPred = true;
		boolean rSucc = true;
		
		for(int n : pred1) {
			if(core1[n] != NULL_NODE) {
				if(!pred2.contains(core1[n])) {
					rPred = false;
					break;
				}
			}
		}
		
		for(int n : succ1) {
			if(core1[n] != NULL_NODE) {
				if(!succ2.contains(core1[n])) {
					rSucc = false;
					break;
				}
			}
		}
		
		for(int n : pred2) {
			if(core2[n] != NULL_NODE) {
				if(!pred1.contains(core2[n])) {
					rPred = false;
					break;
				}
			}
		}
		
		for(int n : succ2) {
			if(core2[n] != NULL_NODE) {
				if(!succ1.contains(core2[n])) {
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
		
		for(int n = 0; n < n1; n++) {
			if(in1[n] != NULL_NODE) {
				if(succ1.contains(n)) {
					cardSuccIn1++;
				}
				if(pred1.contains(n)) {
					cardPredIn1++;
				}
			}
			if(out1[n] != NULL_NODE) {
				if(succ1.contains(n)) {
					cardSuccOut1++;
				}
				if(pred1.contains(n)) {
					cardPredOut1++;
				}
			}
		}
		for(int n = 0; n < n2; n++) {
			if(in2[n] != NULL_NODE) {
				if(succ2.contains(n)) {
					cardSuccIn2++;
				}
				if(pred2.contains(n)) {
					cardPredIn2++;
				}
			}
			if(out2[n] != NULL_NODE) {
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
		
		for(int n = 0; n < n1; n++) {
			if(   core1[n] != NULL_NODE
				&&  in1[n] != NULL_NODE
				&& out1[n] != NULL_NODE) {
				if(pred1.contains(n)) {
					cardN1Pred++;
				}
				if(succ1.contains(n)) {
					cardN1Succ++;
				}
			}
		}
		for(int n = 0; n < n2; n++) {
			if(   core2[n] != NULL_NODE
				&&  in2[n] != NULL_NODE
				&& out2[n] != NULL_NODE) {
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
	
	private boolean isSemFeasible(int v1, int v2) {
		return checkVertexCompatibility(v1, v2)
			&& checkEdgeCompatibility(v1, v2);
	}
	
	private boolean checkVertexCompatibility(int v1, int v2) {
		Vertex plotVertex = g1.getVertex(v1);
		Integer currentCount = agentNodeCounts.get(g1.getAgent(plotVertex));
		if(currentCount == null || currentCount == 0) {
			int involvedAgents = 0;
			for(int nodeCount : agentNodeCounts.values()) {
				if(nodeCount > 0) {
					involvedAgents++;
				}
			}
			if(involvedAgents >= 2) {
				return false;
			}
		}
		UnitVertexType t1 = UnitVertexType.typeOf(plotVertex);
        UnitVertexType t2 = UnitVertexType.typeOf(g2.getVertex(v2));
        if(t2 == UnitVertexType.WILDCARD) {
        	return t1 == UnitVertexType.POSITIVE || t1 == UnitVertexType.NEGATIVE;
        }
        return t1 != UnitVertexType.NONE && t1 == t2;
	}
	
	private boolean checkEdgeCompatibility(int v1, int v2) {
		for(int m = 0; m < n2; m++) {
			if(core2[m] != NULL_NODE) {
				Vertex vn1 = g1.getVertex(v1);
				Vertex vn2 = g1.getVertex(core2[m]);
				Vertex vm1 = g2.getVertex(v2);
				Vertex vm2 = g2.getVertex(m);
				
				Collection<Edge> nEdges = getEdges(g1, vn1, vn2);
				Collection<Edge> mEdges = getEdges(g2, vm1, vm2);
				
				if(!checkEdgeSetCompatibility(nEdges, mEdges)) {
					return false;
				}
				
				nEdges = getEdges(g1, vn2, vn1);
				mEdges = getEdges(g2, vm2, vm1);
				
				if(!checkEdgeSetCompatibility(nEdges, mEdges)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean checkEdgeSetCompatibility(Collection<Edge> plotEdges, Collection<Edge> unitEdges) {
		LinkedList<Edge.Type> edgeTypes = new LinkedList<Edge.Type>(); 
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
	
	private Collection<Edge> getEdges(PlotDirectedSparseGraph g, Vertex v1, Vertex v2) {
		Collection<Edge> allEdges = g.findEdgeSet(v1, v2);
		Collection<Edge> filteredEdges = new HashSet<Edge>();
		
		for(Edge e : allEdges) {
			if(isEdgeValid(e)) {
				filteredEdges.add(e);
			}
		}
		
		return filteredEdges;
	}
	
	private Set<Integer> getPredecessors(PlotDirectedSparseGraph g, int v) {
		Vertex vert = g.getVertex(v);
		HashSet<Integer> predecessors = new HashSet<Integer>();
		for(Vertex p : g.getPredecessors(vert)) {
			Collection<Edge> edges = g.findEdgeSet(p, vert);
			for(Edge e : edges) {
				if(isEdgeValid(e)) {
					predecessors.add(g.getVertexId(p));
					break;
				}
			}
		}
		return predecessors;
	}
	
	private HashSet<Integer> getSuccessors(PlotDirectedSparseGraph g, int v) {
		Vertex vert = g.getVertex(v);
		HashSet<Integer> successors = new HashSet<Integer>();
		for(Vertex p : g.getSuccessors(vert)) {
			Collection<Edge> edges = g.findEdgeSet(vert, p);
			for(Edge e : edges) {
				if(isEdgeValid(e)) {
					successors.add(g.getVertexId(p));
					break;
				}
			}
		}
		return successors;
	}
	
	private boolean isEdgeValid(Edge e) {
		return e.getType() != Edge.Type.TEMPORAL && e.getType() != Edge.Type.ROOT;
	}
}
