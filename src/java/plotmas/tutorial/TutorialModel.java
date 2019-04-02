package plotmas.tutorial;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.HappeningDirector;

public class TutorialModel extends PlotModel<TutorialEnviroment> {

	public TutorialModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);

	}
	
	public int add(int a, int b) {
		return a + b;
	}
}

