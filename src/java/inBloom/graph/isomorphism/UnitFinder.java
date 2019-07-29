package inBloom.graph.isomorphism;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Class which handles finding subgraph isomorphisms.
 * For further information, see:
 * 		https://ieeexplore.ieee.org/abstract/document/1323804/
 * @author Sven Wilke
 */
public class UnitFinder {

	/**
	 * Finds all subgraphs of the form unitGraph in the given plotGraph.
	 * @param unitGraph The graph describing the subgraphs to look for
	 * @param plotGraph The graph to find subgraphs in
	 * @return Set which contains a map for each instance of unitGraph in plotGraph.
	 * 		   The map maps from vertices of the unitGraph to vertices of the plotGraph.
	 */
	public Set<Map<Vertex, Vertex>> findUnits(PlotDirectedSparseGraph unitGraph, PlotDirectedSparseGraph plotGraph) {
		HashSet<Map<Vertex, Vertex>> allMappings = new HashSet<>();
		this.match(new State(plotGraph, unitGraph), allMappings);
		return allMappings;
	}

	private boolean match(State s, Set<Map<Vertex, Vertex>> unitList) {

		if(s.isGoal()) {
			unitList.add(s.getMapping());
			return true;
		}

		Set<State> candidateSet = s.getCandidates();
		boolean foundAny = false;
		if(candidateSet.isEmpty()) {
			return false;
		} else {
			for(State nextState : candidateSet) {
				if(nextState.isFeasible()) {
					nextState.addCandidateMapping();
					boolean found = this.match(nextState, unitList);
					if(found) {
						foundAny = true;
					}
					nextState.backtrack();
				}
			}
		}
		return foundAny;
	}
}