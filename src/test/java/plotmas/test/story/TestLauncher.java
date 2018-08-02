package plotmas.test.story;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;

public class TestLauncher extends PlotLauncher {

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
							runner.new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
        
		runner.run(args, agents, "test_agent");
	}
}
