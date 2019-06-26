package inBloom.graph.isomorphism;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.util.Pair;
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
	 * @param plotGraph The graph to find subgraphs in
	 * @param unitGraph The graph describing the subgraphs to look for
	 * @return Set which contains a map for each instance of unitGraph in plotGraph.
	 * 		   The map maps from vertices of the plotGraph to vertices of the unitGraph.
	 */
	public Set<Map<Vertex, Vertex>> findUnits(PlotDirectedSparseGraph plotGraph, PlotDirectedSparseGraph unitGraph) {
		HashSet<Map<Vertex, Vertex>> allMappings = new HashSet<Map<Vertex, Vertex>>();
		match(new State(plotGraph, unitGraph), allMappings);
		return allMappings;
	}
	
	private boolean match(State s, Set<Map<Vertex, Vertex>> unitList) {
		
		if(s.isGoal()) {
			unitList.add(s.getMapping());
			return true;
		}
		
		Set<Pair<Integer>> candidateSet = s.getCandidates();
		boolean foundAny = false;
		if(candidateSet.isEmpty()) {
			return false;
		} else {
			for(Pair<Integer> pair : candidateSet) {
				if(s.isFeasible(pair.getFirst(), pair.getSecond())) {
					State nextState = new State(s);
					nextState.addMapping(pair.getFirst(), pair.getSecond());
					boolean found = match(nextState, unitList);
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