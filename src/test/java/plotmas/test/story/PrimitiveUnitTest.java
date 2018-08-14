package plotmas.test.story;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;
import junit.framework.TestCase;
import plotmas.LauncherAgent;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.helper.EnvironmentListener;
import plotmas.helper.Tellability;
import plotmas.storyworld.ScheduledHappeningDirector;

public class PrimitiveUnitTest extends TestCase {

	private PlotDirectedSparseGraph analyzedGraph;
	
	@BeforeClass
	public void setUp() throws JasonException, InterruptedException {
		TestLauncher runner = new TestLauncher();

        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
  
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        TestModel model = new TestModel(agents, hapDir);

		// Execute MAS
		runner.initialize(new String[0], model, agents, "test_agent");
		while(runner.getUserEnvironment() == null) {
			Thread.sleep(150);
		}
		runner.getUserEnvironment().addListener(new EnvironmentListener() {

			@Override
			public void onPauseRepeat() {
				// Run tests and then
				// runner.finish();
			}
			
		});
		runner.run();
	}
	
	@AfterClass
	public void testAnalysisSuccess() {
		Tellability tellability = PlotGraphController.getPlotListener().analyze(analyzedGraph);
		assertNotNull(tellability);
	}
	
	public void testPause() {
		assertTrue(MASConsoleGUI.get().isPause());
	}
	@AfterClass
	public void testTest() throws Exception {
		assertEquals(1, 2);
	}
}
