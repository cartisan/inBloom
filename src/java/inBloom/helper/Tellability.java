package inBloom.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

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
		
		// for each character in the story
		for (Vertex root : this.graph.getRoots()) 
		{
			List<String> emotionSequences = new ArrayList<String>();
			List<String> intentionSequences = new ArrayList<String>();
			List<String> beliefSequences = new ArrayList<String>();
			List<String> actionSequences = new ArrayList<String>();
			
			int charCounter = 0;
			// get its story graph
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
			logger.info("Emotions:");
			double emotionSym = sequenceAnalyser(emotionSequences);
			logger.info("Intentions:");
			double intentionSym = sequenceAnalyser(intentionSequences);
			logger.info("Beliefs:");
			double beliefSym = sequenceAnalyser(beliefSequences);
			logger.info("Actions:");
			double actionSym = sequenceAnalyser(actionSequences);
			characterSymmetries[charCounter] = (emotionSym + intentionSym + beliefSym + actionSym) / 4;
			logger.info("\n"+root.toString() + " Average Symmetry: "+ characterSymmetries[charCounter] +
					"\nWith:\n" + emotionSym + "(Emotions),\n" + 
					intentionSym + "(Intentions),\n" + 
					beliefSym + "(Beliefs),\n" + 
					actionSym + "(Actions)");
			charCounter++;
		}
		
		this.symmetry = ( Arrays.stream(characterSymmetries).sum() / this.graph.getRoots().size());
		logger.info("Overall symmetry: " + this.symmetry);
	}
	
	/**
	 * Calculates the story's overall symmetry based on the characters' beliefs, intentions, actions and emotions
	 * @param a graph containing the respective character's events (beliefs, intentions, actions, emotions) as a list of strings
	 * @return symmetry for the input graph 
	 */
	private double sequenceAnalyser(List<String> graphSequence)
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
					List<Integer> newSeq = sequenceMap.get(currentSeq);
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

		//TODO: decide for one measure
		/*
		* calculate symmetry values for all of the sequences
		* in the best case, punish overlapping sequences to avoid duplications:
		* ABCABCABC
		* ABC 
		* are two different entries but in theory the first contains the second 
		* three possible formulas for calculation:
		* 1) Neg: currentSequenceStart - length of prevSequence
		* 2) Dir: similar to (1), but only adds 1 if they do not overlap; else -1
		* 3) Normal: currentSequenceStart - prevSequenceStart
		*/
		
		List<Double> weightedNegs = new ArrayList<Double>();
		List<Double> weightedDirs = new ArrayList<Double>();
		List<Double> weightedNormals = new ArrayList<Double>();
		
		for (Map.Entry<List<String>, List<Integer>> entry : sequenceMap.entrySet()) 
		{
			// if a sequence appears more than once, weight them by their 
			// number of appearances and save the values in a new list
			if (entry.getValue().size() > 1)
			{
				// sum of each sequence
				int sumNeg = 0;
				int sumDir = 0;
				int sumNormal = 0;

				// sum the differences between the start indices of the sequences via the three formulas
				for (int prevIdx = 0; prevIdx < entry.getValue().size()-1; prevIdx ++) 
				{
					for (int currentIdx = prevIdx+1; currentIdx < entry.getValue().size()-1; currentIdx ++) 
					{
						sumNeg += entry.getValue().get(currentIdx) - (entry.getValue().get(prevIdx) + entry.getKey().size());
						sumDir += entry.getValue().get(currentIdx) - 
								(entry.getValue().get(prevIdx) + entry.getKey().size()) >= 0 ? 1 : -1; 
						sumNormal += entry.getValue().get(currentIdx) - entry.getValue().get(prevIdx);
					}
				}
				
				weightedNegs.add((double)(sumNeg * entry.getKey().size()));
				weightedDirs.add((double)(sumDir * entry.getKey().size()));
				weightedNormals.add((double)(sumNormal * entry.getKey().size()));
			}
		}
		
		// convert to intstream to make normalisation easier
		DoubleStream NegStream = weightedNegs.stream().mapToDouble(Double::doubleValue);
		DoubleStream DirStream = weightedDirs.stream().mapToDouble(Double::doubleValue);
		DoubleStream NormalStream = weightedNormals.stream().mapToDouble(Double::doubleValue);

		// streams are not re-usbale (can't call max() after already called min()): get the stats 
		DoubleSummaryStatistics statsNeg = NegStream.summaryStatistics();
		DoubleSummaryStatistics statsDir = DirStream.summaryStatistics();
		DoubleSummaryStatistics statsNormal = NormalStream.summaryStatistics();
		
		double minNeg = statsNeg.getMin();
		double maxNeg = statsNeg.getMax();
		double minDir = statsDir.getMin();
		double maxDir = statsDir.getMax();
		double minNormal = statsNormal.getMin(); // should be 0
		double maxNormal = statsNormal.getMax();;
			
		// TODO: do this for the other formulas
		// z-normalise each value
		for (int i = 0; i < weightedNormals.size(); i++)
		{
			double x = weightedNormals.get(i); 
			double normalised_x = x = ((x-minNeg)/(maxNeg-minNeg));
			weightedNormals.set(i, normalised_x);
		}
		
		double sumNormals = 0;
		for (int i = 0; i < weightedNormals.size(); i++)
		{
			sumNormals += weightedNormals.get(i);
		}

		// return the mean symmetry for the character's state (emotion/action/etc)
		//TODO: replace normalStream with Neg or DirStream if deciding against using this as
		// the final measure
		return (sumNormals/weightedNormals.size());
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
							 //(double) this.symmetry;
		
		logger.info("Overall tellability: " + tellability);
		return tellability;
	}
	
}
