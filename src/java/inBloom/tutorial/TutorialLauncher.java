package inBloom.tutorial;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.asSemantics.Personality;

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
	        
	        runner.initialize(args, model, agents, "agentTutorial"); 
		    runner.run(); 
		  }
    
}
