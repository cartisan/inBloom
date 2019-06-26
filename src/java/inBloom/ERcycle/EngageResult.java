package inBloom.ERcycle;

import java.util.List;
import java.util.stream.Collectors;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.helper.Counterfactuality;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;

/**
 * Result of the engage method. Contains plot graph of
 * the last simulation and the tellability score.
 * Can be extended to allow further return values.
 */
public class EngageResult {
	private PlotDirectedSparseGraph plotGraph;
	private MoodMapper moodData; 
	private PlotDirectedSparseGraph auxiliaryGraph;
	private Tellability tellability;
	private PlotModel<?> lastModel;
	private List<LauncherAgent> lastAgents;
	private Counterfactuality counterfactuality;
	
	// TODO add counterfactuality here!!!! (and change everything accordingly)
	public EngageResult(PlotDirectedSparseGraph plotGraph, Tellability tellability, List<LauncherAgent> lastAgents, PlotModel<?> lastModel, MoodMapper moodData) {
		this.plotGraph = plotGraph;
		this.tellability = tellability;
		this.lastAgents = lastAgents;
		this.lastModel = lastModel; 
		this.moodData = moodData;
	}
	
	public EngageResult(Counterfactuality counterfactuality, PlotDirectedSparseGraph plotGraph, Tellability tellability, List<LauncherAgent> lastAgents, PlotModel<?> lastModel, MoodMapper moodData) {
		this(plotGraph, tellability, lastAgents, lastModel, moodData);
		this.counterfactuality = counterfactuality;
	}
	
	public Counterfactuality getCounterfactuality() {
		return this.counterfactuality;
	}
	
	public LauncherAgent getAgent(String name) {
		List<LauncherAgent> agList = this.lastAgents.stream().filter(ag -> ag.name.compareTo(name) == 0)
	    													 .collect(Collectors.toList());
		
		if (agList.size() == 0) {
			throw new RuntimeException("No character: " + name + " present in ER Cycle.");
		} else if (agList.size() > 1) {
			throw new RuntimeException("Too many characters with name: " + name + " present in ER Cycle.");
		}
			
		return agList.get(0);
	}
	
	public PlotModel<?> getLastModel() {
		return lastModel;
	}

	public List<LauncherAgent> getLastAgents() {
		return lastAgents;
	}

	public PlotDirectedSparseGraph getPlotGraph() {
		return this.plotGraph;
	}
	
	public Tellability getTellability() {
		return this.tellability;
	}
	
	
	public PlotDirectedSparseGraph getAuxiliaryGraph() {
		return this.auxiliaryGraph;
	}

	public void setAuxiliaryGraph(PlotDirectedSparseGraph g) {
		this.auxiliaryGraph = g;
	}

	public MoodMapper getMoodData() {
		return moodData;
	}

	public void setMoodData(MoodMapper moodData) {
		this.moodData = moodData;
	}
}

