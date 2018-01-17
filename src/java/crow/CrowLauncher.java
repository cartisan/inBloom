package crow;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.PlotLauncher;

public class CrowLauncher extends PlotLauncher {

	  public static void main(String[] args) throws JasonException {
		    ENV_CLASS = CrowEnviroment.class;
		    runner = new CrowLauncher();

		    ImmutableList<LauncherAgent> agents = ImmutableList.of(
		      runner.new LauncherAgent("crow",
		        new Personality(0, 0, 0, 0, 0)
		      ),
		      runner.new LauncherAgent("fox",
				        new Personality(0, 0, 0, 0, 0)
				      )
		      
		    );

		    runner.run(args, agents, "agentCrow");
		  }
    
}
