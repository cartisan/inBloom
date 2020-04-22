package inBloom.test.story.helperClasses;

import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Item;
import inBloom.storyworld.ModelState;

public class TestModel extends PlotModel<TestEnvironment> {

	public int step = 0;

	public Wallet wallet = new Wallet();

	@ModelState
	public boolean isDrunk = false;

	@ModelState
	public boolean hasFriend = false;

	@ModelState
	public boolean brokenLeg = false;

	public TestModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
	}

	public ActionReport doStuff(Character agent) {
		logger.info("Doing stuff.");

		// problem and enablement
		if(this.step == 7) {
			this.environment.addEventPercept(agent.name,
					"step_on(poo)",
					PerceptAnnotation.fromEmotion("hate"));

			this.environment.addEventPercept(agent.name,
					"avoid_accident",
					PerceptAnnotation.fromEmotion("relief"));
		}

		if(this.step == 9) {
			this.environment.addEventPercept(agent.name,
											 "is_holiday(friday)",
											 PerceptAnnotation.fromEmotion("joy"));
		}

		if(this.step == 10) {
			this.environment.addEventPercept(agent.name, "bored", PerceptAnnotation.fromEmotion("distress"));  // initiates change of mind intention
		}

		this.step++;
		return new ActionReport(true);
	}

	public ActionReport search(Character agent, String item) {
		logger.info(agent.name + " is looking for their " + item + "!");
		if(this.step == 3) {
			agent.addToInventory(this.wallet);
			this.environment.addEventPercept(agent.name, "found(" + item + ")");
			logger.info(agent.name + " found their " + item + " !");
		}
		this.step++;
		return new ActionReport(true);
	}

	public ActionReport getDrink(Character agent) {
		this.isDrunk = true;

		ActionReport res = new ActionReport(true);
		res.addPerception(agent.name, PerceptAnnotation.fromEmotion("joy")); // positive outcome for prim. unit success

		return res;
	}

	public static class Wallet extends Item {

		@Override
		public String getItemName() {
			return "wallet";
		}
	}
}
