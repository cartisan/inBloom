package inBloom.graph;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

import jason.util.Pair;

import inBloom.graph.visitor.EdgeGenerationPPVisitor;
import inBloom.graph.visitor.EdgeLayoutVisitor;
import inBloom.graph.visitor.VertexMergingPPVisitor;
import inBloom.graph.visitor.VisualizationFilterPPVisitor;
import inBloom.helper.Tellability;

public class GraphAnalyzer extends Thread {
	protected static Logger logger = Logger.getLogger(GraphAnalyzer.class.getName());

	private PlotDirectedSparseGraph graph;
	private AnalysisResultListener listener;

	public GraphAnalyzer(PlotDirectedSparseGraph graphToAnalyze, AnalysisResultListener listener) {
		this.graph = graphToAnalyze;
		this.listener = listener;
	}

	/**
	 * Executes the analysis <b>a</b>synchronously in an own thread and returns results via callback on listener.
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		Pair<Tellability, PlotDirectedSparseGraph> results = this.analyze();

		this.listener.reiceiveAnalysisResult(results.getFirst(), results.getSecond());
    }

	/**
	 * Executes the analysis synchronously while execution on caller is interrupted, and returns Tellability-instance
	 * as return value, while the analyzed graph is cloned into the provided parameter.<br>
	 * This is effectively a legacy mode for methods that relied on Tellability#analyze(PlotDirectedSparseGraph analyzedGraphContainer)
	 * @param analyzedGraphContainer empty graph, into which the analyzed graph is cloned
	 * @return
	 */
	public Tellability runSynchronously(PlotDirectedSparseGraph analyzedGraphContainer){
		Pair<Tellability, PlotDirectedSparseGraph> results = this.analyze();

		if(analyzedGraphContainer != null) {
			results.getSecond().cloneInto(analyzedGraphContainer);
		}
		return results.getFirst();
    }

	/**
	 * Analyzes the plot graph, computes the plots tellability, and returns it.
	 * <ul>
	 *  <li> Analyzing a plot graph includes merging related vertices and specifying the edge types from mere temporal to
	 * ones with more appropriate semantics so all primitive plot units can be represented.</li>
	 *  <li> Computing the tellability atm includes just computing functional polyvalence and suspense
	 *  in the info panel. </li>
	 *  </ul>
	 * @return a pair containing the result analysis stored in a tellability instance, and the post-processes graph
	 */
	private Pair<Tellability, PlotDirectedSparseGraph> analyze() {
		logger.info("Starting Graph Analysis");
		Instant start = Instant.now();
		PlotDirectedSparseGraph g1 = new VertexMergingPPVisitor().apply(this.graph);
		g1.setName("Merged Plot Graph");
		PlotGraphController.getPlotListener().addGraph(g1);

		PlotDirectedSparseGraph g2 = new EdgeGenerationPPVisitor().apply(g1);
		g2.setName("Analysed Graph");
		PlotGraphController.getPlotListener().addGraph(g2);

		PlotDirectedSparseGraph g3 = new VisualizationFilterPPVisitor().apply(g2);

		logger.info("Starting tellability computation");
		Tellability analysisResult = new Tellability(g3);
		logger.info( "Analyze time in ms:" + Duration.between(start, Instant.now()).toMillis());

		g3 = new EdgeLayoutVisitor(9).apply(g3);
		g3.setName("Filtered Plot Graph");
		PlotGraphController.getPlotListener().addGraph(g3);
		PlotGraphController.getPlotListener().setSelectedGraph(g3);

		return new Pair<>(analysisResult, g3);
	}
}