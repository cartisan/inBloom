package plotmas.stories.little_red_hen;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.storyworld.ScheduledHappeningDirector;


/**
 * Responsible for setting up and starting the narrative system of "The Tale of The Little Red Hen".
 * Different personality settings of the agents can be used to explore a space of possible plots.
 * @author Leonid Berov
 */
public class RedHenLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {
	
	public RedHenLauncher() {
		ENV_CLASS = FarmEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        
        PlotControlsLauncher.runner = new RedHenLauncher();
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("hen",					  	   // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
									new Personality(0,  1, 0.7,  0.3, 0.15)    //punishment
//									new Personality(0,  1, 0.7,  0.3,  -1)     //low neurot --> no punishment
//									new Personality(0,  1, 0.7,  0.7,  -1)     //high aggrea --> sharing
									
//									new Personality(0, -1, 0.7,  0.3, 0.15)    //low consc --> no plot
									
//									new Personality(0,  1, 0,    0.3, 0.15)    //low extra --> no help requests, no punishment, no sharing <-- not after update!
//									new Personality(0,  1, 0,    0.7, 0.15)    //low extra, high aggrea --> no help requests, no punishment, sharing
							),
							new LauncherAgent("dog",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// doggie helps hen v1
							),
							new LauncherAgent("cow",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// cow helps hen v1
							),
							new LauncherAgent("pig",
									new Personality(0, -1, 0, -0.7, -0.8)
							)
						);
        
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 4 farm work actions
				(FarmModel model) -> {
	            		if((model.actionCount > 2) & (!model.wheatFound)) {
	            			return true;
	            		}
	            		return false; 
	    		},
				"hen",
				"actionCount");
		hapDir.scheduleHappening(findCorn);        
        
        FarmModel model = new FarmModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "agent");
		runner.run();
        
	}
}
