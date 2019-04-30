package inBloom.tutorial;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.storyworld.HappeningDirector;

public class TutorialModel extends PlotModel<TutorialEnviroment> {

	public TutorialModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);

	}
	
	public int add(int a, int b) {
		return a + b;
	}
}

