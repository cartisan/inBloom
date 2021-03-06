package inBloom.test.story.helperClasses;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;

import inBloom.LauncherAgent;
import inBloom.ERcycle.PlotCycle;
import inBloom.graph.GraphAnalyzer;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;

/**
 * Abstract superclass for all inBloom unit tests that need to run a simulation.
 * Enables unit tests to run a single simulation (and analyses the plotgraph) before they start
 * executing their @Test methods.
 * @author Leonid Berov
 */
public abstract class AbstractPlotTest {
	protected static boolean VISUALIZE = false;
	protected static boolean DEBUG = false;

	static protected Logger logger = Logger.getLogger(AbstractPlotTest.class.getName());

	protected static TestLauncher runner;
	protected static PlotDirectedSparseGraph fullGraph;
	protected static PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();
	protected static Tellability analysis;

	private static boolean hasSimulationFinished = false;
	private static Object simulationMonitor = new Object();

	public static void startSimulation(String agentFile, List<LauncherAgent> agents, ScheduledHappeningDirector hapDir) throws Exception {
		runner = new TestLauncher();
        TestModel model = new TestModel(agents, hapDir);

        String[] args = new String[0];
        if(DEBUG) {
        	args = new String[]{"-debug"};
        }

        PlotCycle.Cycle simulation = new PlotCycle.Cycle(runner, model, args, agents, agentFile);
		Thread t = new Thread(simulation);
		t.start();
		while(runner.getEnvironmentInfraTier() == null || runner.getUserEnvironment() == null) {
			Thread.sleep(100);
		}
		runner.getUserEnvironment().addListener(new EnvironmentListener() {

			@Override
			public void onPauseRepeat() {
				synchronized(simulationMonitor) {
					fullGraph = PlotGraphController.getPlotListener().getGraph();
					GraphAnalyzer analyzer = new GraphAnalyzer(fullGraph, null);
					analysis = analyzer.runSynchronously(analyzedGraph);

					if (VISUALIZE) {
						PlotGraphController graphView = PlotGraphController.fromGraph(analyzedGraph);
						graphView.addGraph(fullGraph);
						graphView.addGraph(TestUnits.ALL_UNITS_GRAPH);

						graphView.visualizeGraph();
					}
					else {
						hasSimulationFinished = true;
						simulationMonitor.notifyAll();
					}
				}
			}

		});
	}

	@Before
	public void waitForSimulation() {
		synchronized(simulationMonitor) {
			while(!hasSimulationFinished) {
				try {
					simulationMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
