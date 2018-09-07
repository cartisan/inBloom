package plotmas.test.story;

import org.junit.Before;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotCycle;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.helper.EnvironmentListener;
import plotmas.storyworld.ScheduledHappeningDirector;

/**
 * Abstract superclass for all plotmas unit tests that need to run a simulation. 
 * Enables unit tests to run a single simulation (and analyses the plotgraph) before they start
 * executing their @Test methods.
 * @author Leonid Berov
 */
public abstract class AbstractPlotTest {
	protected static final boolean VISUALIZE = false;

	protected static TestLauncher runner; 
	protected static PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();
	
	private static boolean hasSimulationFinished = false;
	private static Object simulationMonitor = new Object();
	
	@BeforeClass
	public static void startSimulation() throws Exception {
		runner = new TestLauncher();

        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
  
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        TestModel model = new TestModel(agents, hapDir);
        
        PlotCycle.Cycle simulation = new PlotCycle.Cycle(runner, model, new String[0], agents, "agent_primitive_unit");
		Thread t = new Thread(simulation);
		t.start();
		while(runner.getEnvironmentInfraTier() == null || runner.getUserEnvironment() == null) {
			Thread.sleep(100);
		}
		runner.getUserEnvironment().addListener(new EnvironmentListener() {

			@Override
			public void onPauseRepeat() {
				synchronized(simulationMonitor) {
					PlotGraphController.getPlotListener().analyze(analyzedGraph);
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
