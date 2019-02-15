package plotmas.stories.oedipus;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.storyworld.ScheduledHappeningDirector;

public class OedipusLauncher extends PlotLauncher<OedipusEnvironment, OedipusModel> {
	
	public OedipusLauncher() {
		ENV_CLASS = OedipusEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
		logger.info("Starting up from Launcher!");

		PlotControlsLauncher.runner = new OedipusLauncher();

		ImmutableList<LauncherAgent> agents = ImmutableList.of(
				// new LauncherAgent("oedipus",
				//  new Personality(0, 0, 0, 0, 0)
				// ),
					new LauncherAgent("jocaste",
						new Personality(0, 0, 0, 0, 0)
					),
					new LauncherAgent("laios",
						new Personality(0, 0, 0, 0, 0)
					),
					new LauncherAgent("polybos_merope",
						new Personality(0, 1, 0, 0, 0)
					),
					new LauncherAgent("oracle",
						new Personality(0, 0, 0, 0, 0)
					),		  
					new LauncherAgent("shepherd",
						new Personality(0, -1, 0, 0, 0)
					) 
				);

        
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        OedipusModel model = new OedipusModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "agent_oedipus");
		runner.run();
	}
    
}
