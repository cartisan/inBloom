package plotmas.stories.crow_fox;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.stories.little_red_hen.FarmEnvironment;
import plotmas.stories.little_red_hen.FarmModel;
import plotmas.storyworld.ScheduledHappeningDirector;

public class CrowFoxLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {
	
	public CrowFoxLauncher() {
		ENV_CLASS = FarmEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        
        PlotControlsLauncher.runner = new CrowFoxLauncher();
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
        		new LauncherAgent("crow", Collections.singletonList("hungry(false)"), Collections.singletonList("approachTree"), new Personality(0, 0, 0, 0, 0)),
				new LauncherAgent("fox", Collections.singletonList("hungry(true)"), Collections.singletonList("strolling"), new Personality(0, 0, 0, 0, 0))
				);
       
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        
        FarmModel model = new FarmModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "agent");
		runner.run();
		
     }
}
