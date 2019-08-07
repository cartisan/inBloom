package inBloom.graph.isomorphism;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;

/**
 * Class which handles finding subgraph isomorphisms.
 * For further information, see:
 * 		https://ieeexplore.ieee.org/abstract/document/1323804/
 * @author Sven Wilke
 */
public class UnitFinder {
	protected static Logger logger = Logger.getLogger(UnitFinder.class.getName());

	/**
	 * Finds all subgraphs of the form unitGraph in the given plotGraph.
	 * @param unitGraph The graph describing the subgraphs to look for
	 * @param plotGraph The graph to find subgraphs in
	 * @param tolerance The number of transformations allowed on the unitGraph in order to find fits
	 * @return Set which contains a map for each instance of unitGraph in plotGraph.
	 * 		   The map maps from vertices of the unitGraph to vertices of the plotGraph.
	 */
	public Set<Map<Vertex, Vertex>> findUnits(PlotDirectedSparseGraph unitGraph, PlotDirectedSparseGraph plotGraph, int tolerance) {
		HashSet<Map<Vertex, Vertex>> allMappings = new HashSet<>();
		this.match(new State(plotGraph, unitGraph), allMappings, tolerance);
		Class cls = PlotGraphController.class;

		return allMappings;
	}

	/**
	 * Finds all subgraphs of the form unitGraph in the given plotGraph, without trying to transform subgraphs.
	 * @param unitGraph The graph describing the subgraphs to look for
	 * @param plotGraph The graph to find subgraphs in
	 * @return Set which contains a map for each instance of unitGraph in plotGraph.
	 * 		   The map maps from vertices of the unitGraph to vertices of the plotGraph.
	 */
	public Set<Map<Vertex, Vertex>> findUnits(PlotDirectedSparseGraph unitGraph, PlotDirectedSparseGraph plotGraph) {
		return this.findUnits(unitGraph, plotGraph, 0);
	}

	private boolean match(State s, Set<Map<Vertex, Vertex>> unitList, int tolerance) {

		if(s.isGoal()) {
			unitList.add(s.getMapping());
			return true;
		}

		Set<State> candidateSet = s.getCandidates(tolerance);

		boolean foundAny = false;
		if(candidateSet.isEmpty()) {
			return false;
		} else {
			for(State nextState : candidateSet) {
				if(nextState.isFeasible()) {
					nextState.addCandidateMapping();
					boolean found = this.match(nextState, unitList, tolerance);
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