package inBloom.graph;

import inBloom.helper.Tellability;

public interface AnalysisResultListener {

	/**
	 * Callback that is executed by {@link GraphAnalyzer} when it finishes processing and analyzing a graph
	 * @param analysisResult tellabillity instance that contains analysis results for the graph
	 * @param analyzedGraph the analyzed plot graph (i.e. graph after being post-processed by visitors)
	 */
	public void reiceiveAnalysisResult(Tellability analysisResult, PlotDirectedSparseGraph analyzedGraph);

}