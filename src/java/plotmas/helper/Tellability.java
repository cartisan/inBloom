package plotmas.helper;

import java.util.HashMap;
import java.util.Map;

import plotmas.graph.isomorphism.FunctionalUnit;

public class Tellability {
	// plot preconditions
	public int productiveConflicts = 0;
	
	// Functional Polyvalence
	public int numFunctionalUnits;
	public int numPolyvalentVertices;
	public int numAllVertices;
	public Map<FunctionalUnit, Integer> functionalUnitCount = new HashMap<>();

	
	// Semantic Symmetry
	
	// Semantic Opposition
	
	// Suspense
	public int suspense;
	public int plotLength;
	
	// Dynamic Points
	
	/**
	 * Computes the overall tellability score, by normalizing all features into a range of (0,1) and adding them,
	 * which amounts to assigning each feature equal weight.
	 * @return
	 */
	public double compute() {
		if (productiveConflicts < 1) {
			// need at least one conflict and one attempt at resolution, for this to be a plot
			return 0;
		}
		
		return (double) this.numPolyvalentVertices / this.numAllVertices + 
			   (double) this.suspense / this.plotLength;
	}
}
