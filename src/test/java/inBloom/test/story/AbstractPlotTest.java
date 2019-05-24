package inBloom.test.story;

import java.util.List;

import org.junit.Before;

import inBloom.LauncherAgent;
import inBloom.ERcycle.PlotCycle;
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
	protected static final boolean VISUALIZE = false;

	protected static TestLauncher runner; 
	protected static PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();
	protected static Tellability analysis;
	
	private static boolean hasSimulationFinished = false;
	private static Object simulationMonitor = new Object();
	
	public static void startSimulation(String agentFile, List<LauncherAgent> agents) throws Exception {
		runner = new TestLauncher();

        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        TestModel model = new TestModel(agents, hapDir);
        
        PlotCycle.Cycle simulation = new PlotCycle.Cycle(runner, model, new String[0], agents, agentFile);
		Thread t = new Thread(simulation);
		t.start();
		while(runner.getEnvironmentInfraTier() == null || runner.getUserEnvironment() == null) {
			Thread.sleep(100);
		}
		runner.getUserEnvironment().addListener(new EnvironmentListener() {

			@Override
			public void onPauseRepeat() {
				synchronized(simulationMonitor) {
					analysis = PlotGraphController.getPlotListener().analyze(analyzedGraph);
					if (VISUALIZE) {
						PlotGraphController graphView = PlotGraphController.fromGraph(analyzedGraph);
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
