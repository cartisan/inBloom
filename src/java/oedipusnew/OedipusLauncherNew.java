package oedipusnew;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.PlotLauncher;

public class OedipusLauncherNew extends PlotLauncher {

	  public static void main(String[] args) throws JasonException {
		  logger.info("Starting up from Launcher!");
		    ENV_CLASS = OedipusEnvironmentNew.class;
		    runner = new OedipusLauncherNew();

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		     // runner.new LauncherAgent("oedipus",
		      //  new Personality(0, 0, 0, 0, 0)
		     // ),
		      runner.new LauncherAgent("jocaste",
				new Personality(0, 0, 0, 0, 0)
				      ),
		      runner.new LauncherAgent("laios",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		      runner.new LauncherAgent("polybos_merope",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		      runner.new LauncherAgent("oracle",
				        new Personality(0, 0, 0, 0, 0)
				      ),
		     // runner.new LauncherAgent("sphinx",
				//        new Personality(0, 0, 0, 0, 0)
				//      ),
		      runner.new LauncherAgent("shepherd",
				       new Personality(0, 0, 0, 0, 0)
				     ) 
		    );

		    runner.run(args, agents, "agent_oedipusNew"); 
		  }
    
}

/** für Beliefs von Anfang an: mit Name und Personality gibt es keine initial Beliefs und Goals.
ODER bei Launcher Agent weitere Parameter dazugeben (im PlotLauncher), verschiedene Strings mit Beliefs etc mit Java linklist/arraylist. Leere Liste für Goals.
**/