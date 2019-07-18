package inBloom.stories.thesis;

import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.ModelState;

public class ThesisModel extends PlotModel<ThesisEnvironment> {

	@ModelState
	public boolean isDrunk = false;

	@ModelState
	public boolean hasFriend = false;


	public ThesisModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
	}

	public ActionReport getDrink(Character agent) {
		this.isDrunk = true;

		ActionReport res = new ActionReport(true);
		res.addPerception(agent.name, PerceptAnnotation.fromEmotion("joy")); // positive outcome for prim. unit success

		return res;
	}
}
