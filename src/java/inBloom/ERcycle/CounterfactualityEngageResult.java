package inBloom.ERcycle;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.helper.Counterfactuality;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;

public class CounterfactualityEngageResult extends EngageResult {
	private Counterfactuality counterfactuality;
	
	public CounterfactualityEngageResult(Counterfactuality counterfactuality, PlotDirectedSparseGraph plotGraph, Tellability tellability, List<LauncherAgent> lastAgents, PlotModel<?> lastModel, MoodMapper moodData) {
		super(plotGraph, tellability, lastAgents, lastModel, moodData);
		this.counterfactuality = counterfactuality;
	}
	
	public Counterfactuality getCounterfactuality() {
		return this.counterfactuality;
	}
}
