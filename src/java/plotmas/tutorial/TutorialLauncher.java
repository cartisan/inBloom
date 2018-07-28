package plotmas.tutorial;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;

public class TutorialLauncher extends PlotLauncher<TutorialEnviroment, TutorialModel> {

	  public static void main(String[] args) throws JasonException {
		    ENV_CLASS = TutorialEnviroment.class;
		    runner = new TutorialLauncher();

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		      new LauncherAgent("agent1",
		        new Personality(0, 0, 0, 0, 0)
		      )
		    );

		    runner.initialize(args, agents, "agentTutorial"); 
		    runner.run(); 
		  }
    
}
