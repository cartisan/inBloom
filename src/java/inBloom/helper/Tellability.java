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
import jason.util.Pair;

public class Tellability {
	
	public PlotDirectedSparseGraph graph;
	
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
		this.graph = graph; 

		// Find Functional Units and polyvalent Vertices
		detectPolyvalence();

		// TODO: calculate symmetry
		emotionSequence();
		
		// Perform quantitative analysis of plot
		computeSimpleStatistics();
	}

	/**
	 * Computes all statistics that can be determined by counting in a single pass
	 * @param graph a graph that has been processed by both FullGraphPPVisitor and CompactGraphPPVisitor
	 */
	private void computeSimpleStatistics() {
		counter.apply(this.graph);
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
	private void detectPolyvalence() {
		Map<Vertex, Integer> vertexUnitCount = new HashMap<>();
		
		UnitFinder finder = new UnitFinder();
		int polyvalentVertices = 0;
		int unitInstances = 0;
		Set<Vertex> polyvalentVertexSet = new HashSet<Vertex>();
		
		connectivityGraph = new ConnectivityGraph(this.graph);
		
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(this.graph, unit.getGraph());
			unitInstances += mappings.size();
			this.functionalUnitCount.put(unit, mappings.size());
			logger.log(Level.INFO, "Found '" + unit.getName() + "' " + mappings.size() + " times.");
			
			if (mappings.size() > 0 ) {
				PlotGraphController.getPlotListener().addDetectedPlotUnitType(unit);
				this.plotUnitTypes.add(unit);
			}
			
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = unit.new Instance(this.graph, map.keySet(), unit.getName());
				instance.identifySubject(map);
				connectivityGraph.addVertex(instance);
				
				for(Vertex v : map.keySet()) {
					
					this.graph.markVertexAsUnit(v, unit);
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
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(this.graph, primitiveUnit.getGraph());
			for(Map<Vertex, Vertex> map : mappings) {
				FunctionalUnit.Instance instance = primitiveUnit.new Instance(this.graph, map.keySet(), primitiveUnit.getName());
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
	private List<String> emotionSequence()
	{
		List<String> _sequence = new ArrayList<String>();
		
		for(Vertex v : this.graph.getVertices())
		{
			if (v.getEmotions().isEmpty()) continue;
			
			for (String emotion : v.getEmotions())
			{
				_sequence.add(emotion);
			}
		}
		logger.info("Length" + _sequence.size());
		sequenceCount(_sequence);
		
		return _sequence;
	}
	
	// Sequence generator
	private void sequenceCount(List<String> graphSequence)
	{		
		// saves a sequences as key with corresponding values [counter, List of Start Indices]
		Map<List<String>, Pair<Integer, List<Integer>>> sequenceMap = new HashMap<>();

		
		for (int start = 0; start < graphSequence.size(); start++)
		{
			for (int end = graphSequence.size() - 1; end > start + 1; end--)
			{
				List<String> currentSeq = graphSequence.subList(start, end);
				
				// if sequences already in list, increase the counter
				if (sequenceMap.containsKey(currentSeq))
				{
					List<Integer> newSeq = sequenceMap.get(currentSeq).getSecond();
					newSeq.add(start);
					
					sequenceMap.put(
							currentSeq, 
							new Pair<> (	
									sequenceMap.get(currentSeq).getFirst() + 1,
									newSeq)
							);
				}
				else
				{					
					List<Integer> newSeq = new ArrayList<Integer>();
					newSeq.add(start);
					
					sequenceMap.put(
							currentSeq, 
							new Pair<>(1, newSeq));
				}
			}
		}
		
		logger.info("Map: "+ sequenceMap.toString());
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
