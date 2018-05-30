package plotmas.graph.isomorphism;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.util.Pair;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

public class UnitFinder {
	
	public UnitFinder() {}
	
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
						//unitList.add(nextState.getMapping());
					}
					nextState.backtrack();
				}
			}
		}
		return foundAny;
	}
}
