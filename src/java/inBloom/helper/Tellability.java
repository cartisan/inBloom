package inBloom.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import inBloom.framing.ConnectivityGraph;
import inBloom.graph.CountingVisitor;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;

public class Tellability {
	protected static Logger logger = Logger.getLogger(Tellability.class.getName());
	
	// plot preconditions
	public CountingVisitor counter;
	public int productiveConflicts = 0;
	
	// Functional Polyvalence
	public List<FunctionalUnit> plotUnitTypes;
	public int numFunctionalUnits;
	public int numPolyvalentVertices;
	public int numAllVertices;
	public Map<FunctionalUnit, Integer> functionalUnitCount = new HashMap<>();
	public ConnectivityGraph connectivityGraph;
	
	// Semantic Symmetry
	public double symmetry;
	
	
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
		this.plotUnitTypes = new LinkedList<FunctionalUnit>();
		
		// Find Functional Units and polyvalent Vertices
		detectPolyvalence(graph);

		
		emotionSequence(graph);
		
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
		
		connectivityGraph = new ConnectivityGraph(graph);
		
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(graph, unit.getGraph());
			unitInstances += mappings.size();
			this.functionalUnitCount.put(unit, mappings.size());
			logger.log(Level.INFO, "Found '" + unit.getName() + "' " + mappings.size() + " times.");
			
			if (mappings.size() > 0 ) {
				PlotGraphController.getPlotListener().addDetectedPlotUnitType(unit);
				this.plotUnitTypes.add(unit);
			}
			
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = unit.new Instance(graph, map.keySet(), unit.getName());
				instance.identifySubject(map);
				connectivityGraph.addVertex(instance);
				
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
		
		for(FunctionalUnit primitiveUnit : FunctionalUnits.PRIMITIVES) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(graph, primitiveUnit.getGraph());
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = primitiveUnit.new Instance(graph, map.keySet(), primitiveUnit.getName());
				connectivityGraph.addVertex(instance);
			}
		}
		
		this.numFunctionalUnits = unitInstances;
		this.numPolyvalentVertices = polyvalentVertices;
		
		// Mark polyvalent vertices with asterisk
		for(Vertex v : polyvalentVertexSet) {
			v.setPolyvalent();
		}
	}

	
	// Returns emotion sequence from graph
	private List<String> emotionSequence(PlotDirectedSparseGraph graph)
	{
		List<String> _sequence = new ArrayList<String>();
		
		for(Vertex v : graph.getVertices())
		{
			if (v.getEmotions().isEmpty()) continue;
			
			for (String emotion : v.getEmotions())
			{
				_sequence.add(emotion);
			}
		}
		
		for (String emo : _sequence)
		{
			logger.info("Emotion Sequence: " + emo );
		}
		return _sequence;
	}
	
	// Sequence generator
	private void sequenceCount(List<String> sequence, int maxSequenceLength)
	{		
		// x x x x x x
		// - - - - - -
		// - - - - -
		// - - - -
		// - - -
		// - -
		//   - - - - - 
		//   - - - -
		//   - - -
		//   - -
		//     - - - -
		//     - - - 
		//     - -
		//       - - -
		//       - -
		
		
		/*
		 * Data type/ structure ?
		 * 
		 * A) 	java 8 look at stream().match
		 * 
		 * B) 	Tree
		 * 
		 * C) 	List<List<List<String>>>
		 * 		SequenceLength<Sequences<Sequence<emotionString>>>
		 * 		 
		 * vlt erst mal die laenge der liste vergleichen => baum mit l�nge erstellen
		 * 
		 *  2			3 				4					5					
		 *  [str,str]   [str,str,str]	[str,str,str,str] 	[str,str,str,str,str]
		 *  [str,str]   [str,str,str]	[str,str,str,str] 	[str,str,str,str,str]
		 *  [str,str]   				[str,str,str,str] 	[str,str,str,str,str]
		 *  [str,str]
		 *
		 * 	 
		 * 
		 * */
		
		
		
		
		logger.info("input sequence length: " + sequence.stream().count());
		
	
		
		
		Map<List<String>, Integer> _emotionSequnces = new HashMap<>();
		logger.info("Sollte Null sein: " + _emotionSequnces.size());
		
		
		
		for (int start = 0; start < _emotionSequnces.size(); start++)
		{
			for(int end = _emotionSequnces.size() - 1; end > start + 1; end--)
			{
				// if not included
				//	  add
				// else
				//    counter + 1
			}
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
