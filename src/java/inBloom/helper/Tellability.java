package inBloom.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
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
import inBloom.graph.Vertex.Type;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;

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
	
	
	// for testing purposes
	public Tellability() {}
	
	
	/**
	 * Takes an analyzed graph and computes all necessary statistics of the plot to compute tellability.
	 * @param graph a graph that has been processed by both FullGraphPPVisitor and CompactGraphPPVisitor
	 */
	public Tellability(PlotDirectedSparseGraph graph) {
		counter = new CountingVisitor();
		this.plotUnitTypes = new LinkedList<FunctionalUnit>();
		this.graph = graph; 

		// Find Functional Units and polyvalent Vertices
		//detectPolyvalence();

		// TODO: calculate symmetry
		detectSymmetry();
		
		// Perform quantitative analysis of plot
		//computeSimpleStatistics();
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
	private void detectPolyvalence() 
	{
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
			for(Map<Vertex, Vertex> map : mappings) 
			{
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

	
	/**
	 * Calculates the story's overall symmetry based on the characters' beliefs, intentions, actions and emotions
	 */
	private void detectSymmetry()
	{
		// get the actions, emotions, intentions and beliefs of a character
		double[] characterSymmetries = new double[this.graph.getRoots().size()];
		double minNormal = 0;

		// for each character in the story
		for (Vertex root : this.graph.getRoots()) 
		{
			List<String> emotionSequences = new ArrayList<String>();
			List<String> intentionSequences = new ArrayList<String>();
			List<String> beliefSequences = new ArrayList<String>();
			List<String> actionSequences = new ArrayList<String>();
			
			int charCounter = 0;
			
			// get the character's story graph
			for(Vertex v : this.graph.getCharSubgraph(root))
			{
				// get the emotions of the character
				if (!v.getEmotions().isEmpty())
				{
					for (String emotion : v.getEmotions())
					{
						emotionSequences.add(emotion);
					}
				}
				
				// get the intentions of character
				if (!v.getIntention().isEmpty())
				{
					intentionSequences.add(v.getIntention());
				}
				
				// get the intentions of character
				if (v.getType() == Type.PERCEPT)
				{
					beliefSequences.add(v.getFunctor());
				}
				
				// get the actions of the character
				if(v.getType() == Type.ACTION)
				{
					actionSequences.add(v.getFunctor());
				}
			}
			
			logger.info("\n\nCharacter: " + root.toString() + ": ");
			
			logger.info("Intentions:\n" + intentionSequences.toString());
			
			// run the analysis on the different states
			double emotionSym = sequenceAnalyser(emotionSequences);
			double intentionSym = sequenceAnalyser(intentionSequences);
			double beliefSym = sequenceAnalyser(beliefSequences);
			double actionSym = sequenceAnalyser(actionSequences);
			
			
			// NORMALISATION
			// for normalisaton: create maximal possible sequence and get its symmetry value
			List<String> maxSeqEmotion = new ArrayList<>();
			List<String> maxSeqIntention = new ArrayList<>();
			List<String> maxSeqBelief = new ArrayList<>();
			List<String> maxSeqAction = new ArrayList<>();
			
			for (int i = 0 ; i < emotionSequences.size(); i++) 
			{
				maxSeqEmotion.add("a");
			}
			
			for (int i = 0 ; i < intentionSequences.size(); i++) 
			{
				maxSeqIntention.add("a");
			}
			
			for (int i = 0 ; i < beliefSequences.size(); i++) 
			{
				maxSeqBelief.add("a");
			}
			
			for (int i = 0 ; i < actionSequences.size(); i++) 
			{
				maxSeqAction.add("a");
			}			
			
			
			// z-normalisation: (x - min) / (max - min)
			double normSymEmotion = (emotionSym - minNormal) / (sequenceAnalyser(maxSeqEmotion) - minNormal);
			double normSymIntention = (intentionSym - minNormal) / (sequenceAnalyser(maxSeqIntention) - minNormal);
			double normSymBelief = (beliefSym - minNormal) / (sequenceAnalyser(maxSeqBelief) - minNormal);
			double normSymAction = (actionSym - minNormal) / (sequenceAnalyser(maxSeqAction) - minNormal);
			
			
			// take overall mean over the different states of the character
			characterSymmetries[charCounter] = (normSymEmotion + normSymIntention + normSymBelief + normSymAction) / 4;
			
			logger.info("\n"+root.toString() + " Average Symmetry: "+ characterSymmetries[charCounter] +
					"\nWith:\n" + normSymEmotion + "(Emotions),\n" + 
					normSymIntention + "(Intentions),\n" + 
					normSymBelief + "(Beliefs),\n" + 
					normSymAction + "(Actions)");
			charCounter++;
		}
		
		// overall symmetry (normalisation to number of characters happens in the compute method)
		this.symmetry = Arrays.stream(characterSymmetries).sum();
	}
	
	/**
	 * Calculates the story's overall symmetry based on the characters' beliefs, intentions, actions and emotions
	 * It uses the normal distance measurement. For testing all three: run TestSymmetry.java
	 * @param a graph containing the respective character's events (beliefs, intentions, actions, emotions) as a list of strings
	 * @return symmetry for the input graph 
	 */
	public double sequenceAnalyser(List<String> graphSequence)
	{		
		// saves a sequences as key with corresponding values [counter, List of Start Indices]
		Map<List<String>, List<Integer>> sequenceMap = new HashMap<>();

		// loop over the graph and create the (sub)sequences
		for (int start = 0; start < graphSequence.size(); start++)
		{
			for (int end = graphSequence.size(); end > start + 1; end--)
			{
				List<String> currentSeq = new ArrayList<>(graphSequence.subList(start, end));
				
				// if the sequence already in the list, only increase the counter
				if (sequenceMap.containsKey(currentSeq))
				{
					List<Integer> newSeq = new ArrayList<>(sequenceMap.get(currentSeq));
					newSeq.add(start);
					
					sequenceMap.put(currentSeq, newSeq);
				}
				else
				{					
					List<Integer> newSeq = new ArrayList<Integer>();
					newSeq.add(start);
					
					sequenceMap.put(currentSeq, newSeq);
				}
			}
		}

		double sumWeightedNormals = 0;
		int sequenceValueNormal = 0;

		for (Map.Entry<List<String>, List<Integer>> entry : sequenceMap.entrySet()) 
		{
			// if a sequence appears more than once, weight them by their 
			// number of appearances and save the values in a new list
			if (entry.getValue().size() > 1)
			{
				// reset sequence value
				sequenceValueNormal = 0;
				// sum the distances between the start indices within a sequence
				for (int prevIdx = 0; prevIdx < entry.getValue().size(); prevIdx ++) 
				{
					for (int currentIdx = prevIdx+1; currentIdx < entry.getValue().size(); currentIdx ++) 
					{
						// sum distances between start indices 
						sequenceValueNormal += entry.getValue().get(currentIdx) - entry.getValue().get(prevIdx);
					}
				}
				// weight the distances by their length
				sumWeightedNormals += (double)(sequenceValueNormal * entry.getKey().size());
			}
		}
		return sumWeightedNormals; 
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
		
		// divide symmetry by the number of agents in the story
		double tellability = (double) this.numPolyvalentVertices / this.numAllVertices + 
							 (double) this.suspense / this.plotLength +
							 (double) this.symmetry / this.graph.getRoots().size();
;

		logger.info("Overall tellability: " + tellability);
		return tellability;
	}
	
}
