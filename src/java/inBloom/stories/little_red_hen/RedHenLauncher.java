package inBloom.stories.little_red_hen;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;


/**
 * Responsible for setting up and starting the narrative system of "The Tale of The Little Red Hen".
 * Different personality settings of the agents can be used to explore a space of possible plots.
 * @author Leonid Berov
 */
public class RedHenLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {


	public RedHenLauncher() {
		this.ENV_CLASS = FarmEnvironment.class;
		this.COUNTERFACT_CLASS = RedHenCounterfactualityCycle.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}

	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");

        PlotControlsLauncher.runner = new RedHenLauncher();

        LauncherAgent hen = new LauncherAgent("hen",					  	   // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
					Arrays.asList("hungry", "self(farm_animal)"),
					    new LinkedList<String>(),
					    new Personality(0,  1, 0.7, -0.3, -0.2)    //punishment
//						new Personality(0, -1, 0.7, -0.3, -0.2)    //low consc --> no plot
//						new Personality(0,  1, 0.7, -0.3,   -1)    //low neurot --> eat alone, no punishment
//						new Personality(0,  1, 0.7,  1,   -0.2)    //high aggrea --> sharing despite refusals
//						new Personality(0,  1, 0,   -0.3, -0.2)    //lower extra --> no help requests, no punishment, sharing
//						new Personality(0,  1, 0,   -0.3,  -1)     //lower extra, low neurot --> no help requests, no punishment, no sharing
////					    new Personality(0,  1, 0.7, 1, -1)   //low neurot, neg aggrea --> no punishment, no share
        );
        LauncherAgent dog = new LauncherAgent("dog",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
						new Personality(0, -1, -0.3, -0.7, -0.7)
//						new Personality(0, -1, -0.3, 0.7, -0.7)	// doggie helps hen v1
		);
        LauncherAgent cow = new LauncherAgent("cow",
					Arrays.asList("hungry", "self(farm_animal)"),
				    	new LinkedList<String>(),
						new Personality(0, -1, -0.3, -0.7, -0.7)
//						new Personality(0, -1, -0.3, 0.7, -0.7)	// cow helps hen v1
		);
        LauncherAgent pig = new LauncherAgent("pig",
					Arrays.asList("hungry", "self(farm_animal)"),
						new LinkedList<String>(),
						new Personality(0, -1, -0.3, -0.7, -0.7)
		);

        ImmutableList<LauncherAgent> agents = ImmutableList.of(hen, dog, cow, pig);

        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		FindCornHappening findCorn = new FindCornHappening();
		hapDir.scheduleHappening(findCorn);

        FarmModel model = new FarmModel(agents, hapDir);

        hen.location = model.farm.name;
        dog.location = model.farm.name;
        cow.location = model.farm.name;
        pig.location = model.farm.name;

		// Execute MAS
		runner.initialize(args, model, agents, "agent_folktale_animal");
		runner.run();

	}
}
