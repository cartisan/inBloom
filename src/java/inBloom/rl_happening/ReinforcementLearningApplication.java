/**
 * 
 */
package inBloom.rl_happening;

import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import jason.JasonException;

/**
 * @author Julia Wippermann
 * @version 3.12.19
 *
 */
public class ReinforcementLearningApplication {

	/*private PlotLauncher<?,?> launcher;
	private PlotModel<?> model;*/
	
	
	//private HashMap<State, Action> policy; // TODO how to model state
	// could the PlotModel implement a getCurrentState? That'd be nice
	
	/*public ReinforcementLearningApplication(PlotLauncher<?,?> launcher) {
		this.launcher = launcher;
		this.model = launcher.getUserModel();
	}*/
	
	public static void policy() {
		
	}
	
	private static String getCurrentModelState(PlotModel model, int step) {
		return "";
	}
	
	public static void main(String[] args) throws JasonException {
		IslandLauncherRL launcher = new IslandLauncherRL();
		System.out.println("Run startun.");
		launcher.executeRun(args);
		System.out.println("Run dun.");
		IslandEnvironment env = launcher.getUserEnvironment();
		System.out.println("Environment:" + env.toString());
		
		//IslandLauncherRL.executeRun(args);
		//IslandLauncherRL

	}
	
}
