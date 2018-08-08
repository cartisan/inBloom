package plotmas.test.story;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.stories.little_red_hen.FarmModel;
import plotmas.storyworld.ScheduledHappeningDirector;

public class TestLauncher extends PlotLauncher<TestEnvironment, TestModel> {

	public TestLauncher() {
		ENV_CLASS = TestEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting TestLauncher!");
        ENV_CLASS = TestEnvironment.class;
        
        runner = new TestLauncher();

        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
  
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        FarmModel model = new FarmModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "test_agent");
		runner.run();
	}
}
