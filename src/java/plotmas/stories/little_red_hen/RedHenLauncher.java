package plotmas.stories.little_red_hen;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.storyworld.Happening;
import plotmas.storyworld.StoryworldAgent;


/**
 * Responsible for setting up and starting the narrative system of "The Tale of The Little Red Hen".
 * Different personality settings of the agents can be used to explore a space of possible plots.
 * @author Leonid Berov
 */
public class RedHenLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {

	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        ENV_CLASS = FarmEnvironment.class;
        
        PlotControlsLauncher.runner = new RedHenLauncher();
        
        Happening<FarmModel> findCorn = new Happening<FarmModel>(
        		new Predicate<FarmModel>(){ 
                	public boolean test(FarmModel model) {
                		if((model.actionCount > 3) & (!model.wheatFound)) {
                			return true;
                		}
                		return false; 
                	}
            	},
        		new Consumer<FarmModel>() {
                	public void accept(FarmModel model) {
            			model.wheat = model.new Wheat();
            			StoryworldAgent agent = model.getAgent("hen");
            			
            			agent.addToInventory(model.wheat);
            			model.getEnvironment().addEventPerception(agent.name, "found(wheat)[emotion(joy)]");  
            			model.wheatFound = true;
            			model.getLogger().info(agent.name + " found wheat grains");               		
                	}
            	}
        );
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("hen",					  // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
									new Personality(0,  1, 0.7,  0.3, 0.15)    //punishment
//									new Personality(0,  1, 0.7,  0.3,  -1)    //low neurot --> no punishment
//									new Personality(0,  1, 0.7,  0.7,  -1)    //high aggrea --> sharing
									
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
        
        // Initialize MAS
        runner.initialize(args, agents, "agent");
        
        // Schedule happenings to be executed when their preconditions are met
        runner.getUserEnvironment().getModel().scheduleHappening(findCorn);
        
        // Execute MAS
		runner.run();
	}
}
