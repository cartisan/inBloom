package inBloom.stories.crow_fox;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.stories.little_red_hen.FarmEnvironment;
import inBloom.stories.little_red_hen.FarmModel;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

public class CrowFoxLauncher extends PlotLauncher<FarmEnvironment, FarmModel> {
	
	public CrowFoxLauncher() {
		ENV_CLASS = FarmEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        
        PlotControlsLauncher.runner = new CrowFoxLauncher();
        
        
        LauncherAgent crow = new LauncherAgent("crow", Arrays.asList(),
											   Collections.singletonList("approach(tree)"), 
											   new Personality(-0.7, -1, 0.3, 0, 0));
//        LauncherAgent crow = new LauncherAgent("crow", Arrays.asList(),
//				  								 Collections.singletonList("approach(tree)"), 
//				  								 new Personality(0, 0, 0.3, -1, 0));
        crow.inventory.add(new FarmModel.Bread());
        
        LauncherAgent fox = new LauncherAgent("fox", Collections.singletonList("hungry"), 
				 new LinkedList<>(), 
				 new Personality(0, -0.8, 0.3, 0, 0));
//        LauncherAgent fox = new LauncherAgent("fox", Collections.singletonList("hungry"),
//				 new LinkedList<>(), 
//				 new Personality(0, 0, 0, 1, 0));
//        LauncherAgent fox = new LauncherAgent("fox", Collections.singletonList("hungry"),
//				 new LinkedList<>(),
//				 new Personality(0, 0, 0, -1, 0));
        fox.location = FarmModel.TREE.name;
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(crow,fox);
       
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        
        FarmModel model = new FarmModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "agent");
		runner.run();
		
     }
}
