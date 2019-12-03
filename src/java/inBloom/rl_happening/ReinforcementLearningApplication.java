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

	private PlotLauncher<?,?> launcher;
	private PlotModel<?> model;
	//private HashMap<State, Action> policy; // TODO how to model state
	// could the PlotModel implement a getCurrentState? That'd be nice
	
	public ReinforcementLearningApplication(PlotLauncher<?,?> launcher) {
		this.launcher = launcher;
		this.model = launcher.getUserModel();
	}
	
	public void policy() {
		
	}
	
	private String getCurrentModelState(PlotModel model, int step) {
		return "";
	}
	
	private void tryRun(String[] args) {
		try {
			launcher.main(args);
		} catch (JasonException e) {
			e.printStackTrace();
		}
	}
	
}
