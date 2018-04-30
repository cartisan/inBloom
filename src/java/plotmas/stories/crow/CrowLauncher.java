package plotmas.stories.crow;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.PlotLauncher;

public class CrowLauncher extends PlotLauncher {

	  public static void main(String[] args) throws JasonException {
		  logger.info("Starting up from Launcher!");
		    ENV_CLASS = CrowEnvironment.class;
		    runner = new CrowLauncher();

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		      runner.new LauncherAgent("crow",
		        new Personality(0, 1, 0, -1, 0)
		      ),
		      runner.new LauncherAgent("fox",
		    	Arrays.asList("hungry"),		//beliefs
		    	new LinkedList<String>(),		//goals
				new Personality(0, 1, 0, 1, 0)
				      )
		      
		    );

		    runner.run(args, agents, "crowAgent"); 
		  }
    
}
