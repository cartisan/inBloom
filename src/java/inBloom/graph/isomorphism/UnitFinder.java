package inBloom.graph.isomorphism;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;

/**
 * Class implementing the VF2 algorithm for inexact subgraph isomorphism matching.
 * For further information, see: </br>
 *  Cordella, L. P., Foggia, P., Sansone, C., & Vento, M. (1998). Subgraph transformations for the inexact matching of attributed relational graphs.</br>
 *  Cordella, L. P., Foggia, P., Sansone, C., & Vento, M. (2004). A (Sub)Graph Isomorphism Algorithm for Matching Large Graphs.
 *
 * @author Sven Wilke and Leonid Berov
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
		TreeSet<State> allMappings = new TreeSet<>(
				// two states are the same, iff they map the same positions in g1 and g2
				// their s#getMapping() might not be the same, though, because vertices at these positions might
				// differ--new once might be created in FU by cloning during FUTransformation
				new Comparator<State>() {
					@Override
					public int compare(State s1, State s2) {
						if (s1.getCore1().equals(s2.getCore1()) && s1.getCore2().equals(s2.getCore2())) {
							return 0;
						}
						return -1;
					}
				}
			);

		Stopwatch timer = Stopwatch.createStarted();
		this.match(new State(plotGraph, unitGraph), allMappings, tolerance);

		// remove those states in allMappings, that basically are a smaller state we found + an additional vertex from transformation
		Iterator<State> it = allMappings.iterator();
		while(it.hasNext()) {
			State state = it.next();
			for(State other : allMappings) {
				if(state.subsumes(other)) {
					it.remove();
					break;
				}
			}
		}

		logger.fine("     time taken: " + timer.stop());
		return allMappings.stream().map(s -> s.getMapping()).collect(Collectors.toSet());
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

	private boolean match(State s, Set<State> unitList, int tolerance) {
		if(s.isGoal()) {
			unitList.add(s);
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
				}
			}
		}
		return foundAny;
	}
}