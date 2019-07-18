package inBloom.stories.thesis;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;

public class ThesisLauncher extends PlotLauncher<ThesisEnvironment, ThesisModel> {

	public ThesisLauncher() {
		this.ENV_CLASS = ThesisEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}


	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!");

        PlotControlsLauncher.runner = new ThesisLauncher();

		// initialize agents
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									Arrays.asList("wish(drown_sorrow)"),
								    new LinkedList<String>(),
									new Personality(0,  0,  0,  0, 0)
							),
							new LauncherAgent("barbara",
									Arrays.asList("obligation(work(bar))"),
								    new LinkedList<String>(),
									new Personality(0,  0,  0,  0, 0)
							)
						);

        // Initialize MAS with a scheduled happening director
        // Initialize happenings
        Happening<ThesisModel> findFriendHap = new Happening<>(
        		new Predicate<ThesisModel>() {
    				@Override
    				public boolean test(ThesisModel model) {
    					if (model.isDrunk) {
    						return true;
    					}
    					return false;
    				}

        		},
        		new Consumer<ThesisModel>() {
    				@Override
    				public void accept(ThesisModel model) {
    					model.hasFriend = true;
    				}
        		},
        		"jeremy",
        		"isDrunk",
        		"friend(barbara)"
        	);

        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        hapDir.scheduleHappening(findFriendHap);
        ThesisModel model = new ThesisModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "thesisAgent");
		runner.run();

	}
}
