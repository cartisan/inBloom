package plotmas.tutorial;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.storyworld.ScheduledHappeningDirector;

public class TutorialLauncher extends PlotLauncher<TutorialEnviroment, TutorialModel> {


	public static void main(String[] args) throws JasonException {
		runner = new TutorialLauncher();
		runner.ENV_CLASS = TutorialEnviroment.class;

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		    	new LauncherAgent("agent1", new Personality(0, 0, 0, 0, 0))
		    );

	        
	        // Initialize MAS with a scheduled happening director
	        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
       
	        
	        TutorialModel model = new TutorialModel(agents, hapDir);

			// Execute MAS
			runner.initialize(args, model, agents, "agentTutorial");
			runner.run();
	        
		}
    
}
