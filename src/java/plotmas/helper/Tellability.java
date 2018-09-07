package plotmas.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import plotmas.graph.CountingVisitor;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.graph.isomorphism.UnitFinder;

public class Tellability {
	protected static Logger logger = Logger.getLogger(Tellability.class.getName());
	
	// plot preconditions
	public CountingVisitor counter;
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
	 * Takes an analyzed graph and computes all necessary statistics of the plot to compute tellability.
	 * @param graph a graph that has been processed by both FullGraphPPVisitor and CompactGraphPPVisitor
	 */
	public Tellability(PlotDirectedSparseGraph graph) {
		counter = new CountingVisitor();
		
		// Find Functional Units and polyvalent Vertices
		detectPolyvalence(graph);

		// Perform quantitative analysis of plot
		computeSimpleStatistics(graph);
	}

	/**
	 * Computes all statistics that can be determined by counting in a single pass
	 * @param graph a graph that has been processed by both FullGraphPPVisitor and CompactGraphPPVisitor
	 */
	private void computeSimpleStatistics(PlotDirectedSparseGraph graph) {
		counter.apply(graph);
		this.productiveConflicts = counter.getProductiveConflictNumber();
		this.suspense = counter.getSuspense();
		this.plotLength = counter.getPlotLength();
		this.numAllVertices = counter.getVertexNum();
	}

	/**
	 * Identifies all instances of functional units in the plot graph and detects polyvalent vertices
	 * at their overlap.
	 * @param graph a graph that has been processed by both FullGraphPPVisitor and CompactGraphPPVisitor
	 */
	private void detectPolyvalence(PlotDirectedSparseGraph graph) {
		Map<Vertex, Integer> vertexUnitCount = new HashMap<>();
		
		UnitFinder finder = new UnitFinder();
		int polyvalentVertices = 0;
		int unitInstances = 0;
		Set<Vertex> polyvalentVertexSet = new HashSet<Vertex>();
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(graph, unit.getGraph());
			unitInstances += mappings.size();
			this.functionalUnitCount.put(unit, mappings.size());
			logger.log(Level.INFO, "Found '" + unit.getName() + "' " + mappings.size() + " times.");
			
			if (mappings.size() > 0 ) {
				PlotGraphController.getPlotListener().addDetectedPlotUnitType(unit);
			}
			
			for(Map<Vertex, Vertex> map : mappings) {
				for(Vertex v : map.keySet()) {
					graph.markVertexAsUnit(v, unit);
					if(!vertexUnitCount.containsKey(v)) {
						vertexUnitCount.put(v, 1);
					} else {
						int count = vertexUnitCount.get(v);
						count++;
						if(count == 2) {
							polyvalentVertices++;
							polyvalentVertexSet.add(v);
						}
						vertexUnitCount.put(v, count);
					}
				}
			}
		}
		
		this.numFunctionalUnits = unitInstances;
		this.numPolyvalentVertices = polyvalentVertices;
		
		// Mark polyvalent vertices with asterisk
		for(Vertex v : polyvalentVertexSet) {
			v.setPolyvalent();
		}
	}

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
		
		double tellability = (double) this.numPolyvalentVertices / this.numAllVertices + 
							 (double) this.suspense / this.plotLength;
		
		logger.info("Overall tellability: " + tellability);
		return tellability;
	}
}
