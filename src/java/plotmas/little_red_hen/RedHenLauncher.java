package plotmas.little_red_hen;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import plotmas.PlotLauncher;


public class RedHenLauncher extends PlotLauncher {

	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");
        ENV_CLASS = FarmEnvironment.class;
        
        runner = new RedHenLauncher();
        
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							runner.new LauncherAgent("hen",							  // works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
									new Personality(0,  1, 0.7,  0.3, 0.0)    //punishment
//									new Personality(0,  1, 0.7,  0.3,  -1)    //low neurot --> no punishment
//									new Personality(0,  1, 0.7,  0.7,  -1)    //high aggrea --> sharing
									
//									new Personality(0, -1, 0.7,  0.3, 0.0)    //low consc --> no plot (graph making brakes?!)
									
//									new Personality(0,  1, 0,    0.7, 0.0)    //low extra --> no help requests, no punishment
							),
							runner.new LauncherAgent("dog",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// doggie helps hen v1
							),
							runner.new LauncherAgent("cow",
									new Personality(0, -1, 0, -0.7, -0.8)
//									new Personality(0, 1, 0, -0.7, -0.8)	// cow helps hen v1
							),
							runner.new LauncherAgent("pig",
									new Personality(0, -1, 0, -0.7, -0.8)
							)
						);
        
		runner.run(args, agents);
	}
}
