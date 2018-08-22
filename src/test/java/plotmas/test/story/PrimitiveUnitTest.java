package plotmas.test.story;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotCycle;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.UnitFinder;
import plotmas.helper.EnvironmentListener;
import plotmas.storyworld.ScheduledHappeningDirector;

public class PrimitiveUnitTest {

	private static TestLauncher runner; 
	private static PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();
	
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
        
        PlotCycle.Cycle simulation = new PlotCycle.Cycle(runner, model, new String[0], agents, "test_agent");
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
					hasSimulationFinished = true;
					simulationMonitor.notifyAll();
				}
			}
			
		});
	}
	
	@Test
	public void testResolution() {
		waitForSimulation();
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.RESOLUTION.getGraph());
		assertEquals(1, mappings.size());
	}
	
	@Test
	public void testLoss() {
		waitForSimulation();
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.LOSS.getGraph());
		assertEquals(1, mappings.size());
	}
	
	@Test
	public void testPerseverance() {
		waitForSimulation();
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.PERSEVERANCE.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testPositiveTradeoff() {
		waitForSimulation();
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.POSITIVE_TRADEOFF.getGraph());
		assertEquals(2, mappings.size());
	}
	
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
