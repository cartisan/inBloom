package oedipusnew;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.stories.little_red_hen.FarmModel;
import plotmas.storyworld.ScheduledHappeningDirector;

public class OedipusLauncherNew extends PlotLauncher<OedipusEnvironmentNew, OedipusModelNew> {

	  public static void main(String[] args) throws JasonException {
		  logger.info("Starting up from Launcher!");
		    ENV_CLASS = OedipusEnvironmentNew.class;
		    runner = new OedipusLauncherNew();

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		     // runner.new LauncherAgent("oedipus",
		      //  new Personality(0, 0, 0, 0, 0)
		     // ),
		      new LauncherAgent("jocaste",
				new Personality(0, 0, 0, 0, 0)
				      ),
		      new LauncherAgent("laios",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		      new LauncherAgent("polybos_merope",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		      new LauncherAgent("oracle",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		     // runner.new LauncherAgent("sphinx",
				//        new Personality(0, 0, 0, 0, 0)
				//      ),
		      new LauncherAgent("shepherd",
				       new Personality(0, 0, 0, 0, 0)
				     ) 
		    );

	        // Initialize MAS with a scheduled happening director
	        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
	        FarmModel model = new FarmModel(agents, hapDir);
	        
	        runner.initialize(args, model, agents, "agent_oedipusNew"); 
		    runner.run(); 
	  }
    
}

/** für Beliefs von Anfang an: mit Name und Personality gibt es keine initial Beliefs und Goals.
ODER bei Launcher Agent weitere Parameter dazugeben (im PlotLauncher), verschiedene Strings mit Beliefs etc mit Java linklist/arraylist. Leere Liste für Goals.
**/