package plotmas.tutorial;

import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Model;

public class TutorialModel extends Model {

	public TutorialModel(List<LauncherAgent> agentList, PlotEnvironment<TutorialModel> env) {
		super(agentList, env);
	}
	
	public int add(int a, int b) {
		return a + b;
	}

}