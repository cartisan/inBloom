package plotmas.stories.crow;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.stories.little_red_hen.FarmModel;
import plotmas.storyworld.ScheduledHappeningDirector;

public class CrowLauncher extends PlotLauncher<CrowEnvironment, CrowModel> {

	  public static void main(String[] args) throws JasonException {
		  logger.info("Starting up from Launcher!");
		    runner = new CrowLauncher();
		    runner.ENV_CLASS = CrowEnvironment.class;

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		      new LauncherAgent("crow",
		        new Personality(0, 1, 0, -1, 0)
		      ),
		      new LauncherAgent("fox",
		    	Arrays.asList("hungry"),		//beliefs
		    	new LinkedList<String>(),		//goals
				new Personality(0, 1, 0, 1, 0)
				      )
		      
		    );

	        // Initialize MAS with a scheduled happening director
	        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
	        FarmModel model = new FarmModel(agents, hapDir);
	        
	        runner.initialize(args, model, agents, "crowAgent"); 
		    runner.run(); 
		  }
    
}
