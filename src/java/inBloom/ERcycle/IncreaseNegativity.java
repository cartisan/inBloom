package inBloom.ERcycle;

import java.lang.reflect.InvocationTargetException;

import inBloom.ERcycle.PlotCycle.EngageResult;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;

public class IncreaseNegativity implements ProblemFixCommand {

	private ProblemFixCommand cause;
	RedHenHappeningCycle controller;
	
	public IncreaseNegativity(RedHenHappeningCycle controller) {
		this.controller = controller;
		// TODO: implement more sophisticated version of oldCaus filter
		ProblemFixCommand oldCause = this.controller.getTransformations().stream()
														 .filter(x -> x.getClass().equals(IntroduceAntagonist.class))
														 .findAny()
														 .get();
		
		try {
			this.cause = (ProblemFixCommand) oldCause.getClass().getConstructors()[0].newInstance(this.controller);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			// TODO Do it properly
			e.printStackTrace();
		}
	}
	
	@Override
	public void execute(EngageResult er) {
		this.cause.execute(er);

	}

	@Override
	public void undo(EngageResult er) {
		this.cause.undo(er);
	}

	@Override
	public String message() {
		return "Increasing negativity by re-executing '" + this.cause.message() + "'";
	}

}
