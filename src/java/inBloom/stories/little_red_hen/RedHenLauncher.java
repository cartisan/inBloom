package inBloom.stories.little_red_hen;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;


/**
 * Responsible for setting up and starting the narrative system of "The Tale of The Little Red Hen".
 * Different personality settings of the agents can be used to explore a space of possible plots.
 * @author Leonid Berov
 */
public class RedHenLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {
	
	public RedHenLauncher() {
		//Der Logger killt Counterfactuality
		//logger.info("create RedHenLauncher");
		ENV_CLASS = FarmEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        
        PlotControlsLauncher.runner = new RedHenLauncher();
        
        LauncherAgent hen = new LauncherAgent("hen",					  	   // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
					Arrays.asList("hungry", "self(farm_animal)"),
					    new LinkedList<String>(),
					    new Personality(0,  1, 0.7,  0.3, 0.65)    //punishment
	//					new Personality(0,  1, 0.7,  0.3,  -1)     //low neurot --> no punishment
	//					new Personality(0,  1, 0.7,  0.7,  -1)     //high aggrea --> sharing
					
	//					new Personality(0, -1, 0.7,  0.3, 0.15)    //low consc --> no plot
					
	//					new Personality(0,  1, 0,    0.3, 0.15)    //low extra --> no help requests, no punishment, no sharing <-- not after update!
	//					new Personality(0,  1, 0,    0.7, 0.15)    //low extra, high aggrea --> no help requests, no punishment, sharing
        );
        LauncherAgent dog = new LauncherAgent("dog",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
						new Personality(0, -1, 0, -0.7, -0.8)
		//				new Personality(0, 1, 0, -0.7, -0.8)	// doggie helps hen v1
		);
        LauncherAgent cow = new LauncherAgent("cow",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
						new Personality(0, -1, 0, -0.7, -0.8)
		//				new Personality(0, 1, 0, -0.7, -0.8)	// cow helps hen v1
		);
        LauncherAgent pig = new LauncherAgent("pig",
					Arrays.asList("hungry", "self(farm_animal)"),
						new LinkedList<String>(),
						new Personality(0, -1, 0, -0.7, -0.8)
		);     
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(hen, dog, cow, pig);
        
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening(
				// hen finds wheat after 2 farm work actions
				(FarmModel model) -> {
	            		if(model.farm.farmingProgress > 1) {
	            			return true;
	            		}
	            		return false; 
	    		},
				"hen",
				"farmingProgress");
		hapDir.scheduleHappening(findCorn);
        
        FarmModel model = new FarmModel(agents, hapDir);

        hen.location = model.farm.name;
        dog.location = model.farm.name;
        cow.location = model.farm.name;
        pig.location = model.farm.name;

		// Execute MAS
		runner.initialize(args, model, agents, "agent");
		runner.run();
        
	}
}
