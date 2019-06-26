package inBloom.ERcycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import inBloom.PlotModel;
import inBloom.ERcycle.EngageResult;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSyntax.Trigger;

/**
 * Detects a fitting happening to resolve a narrative equilibrium state and schedules it for execution right at
 * the beginning of the equilibrium state.
 * @author Leonid Berov
 */
public class ScheduleHappening implements ProblemFixCommand {
	protected static Logger logger = Logger.getLogger(ScheduleHappening.class.getName());
	
	private Happening<?> happening;
	private int startStep;
	
	/**
	 * Identifies which happenings are available to be scheduled and returns a fix that schedules one of these happenings.
	 * At the moment, the only availability criterion is that happenings are unused.
	 * @param startStep step at which happening is to be scheduled
	 * @param character name of character at whom a happening is directed
	 * @param controler ERCycle that tracks executed and available happenings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T extends Happening<?>> ScheduleHappening scheduleRandomHappening(int startStep, String character, RedHenHappeningCycle controler) {
		// find all happenings that have been used so far
		Set<Class<T>> usedHappenings = controler.getTransformations().stream()
													.filter(x -> ScheduleHappening.class.isInstance(x))			       // find all scheduled happenings
													.map(x -> (Class<T>)((ScheduleHappening) x).happening.getClass())  // cast to happening class
													.collect(Collectors.toSet());
		
		// find all happenings available to the controller, and subtract the used ones
		// e.g. we adopt a 'never use happenings twice' policy
		HashSet<Class<T>> availableHappenings = (HashSet<Class<T>>) controler.availableHappenings.clone();
		availableHappenings.removeAll(usedHappenings);
		
		if (availableHappenings.size() > 0){
			// simply take the first happening in the set, TODO: perhaps, use a 'better' randomization strategy
			Class<T> selectedHappeningClass = new ArrayList<Class<T>>(availableHappenings).get(0);
			
			try {
				return new ScheduleHappening(selectedHappeningClass, startStep, character);
			} catch (Exception e) {
				// If we can't instantiate ScheduleHappening because meta-programming failed return null and get on with the cycle
				logger.severe("Couldn't instantiate ScheduleHappening for: " + selectedHappeningClass.getSimpleName());
				return null;
			}
		}
		
		return null;
	}
	
	public<T extends Happening<?>>  ScheduleHappening(Class<T> happeningClass, int startStep, String character) throws Exception {
		this.startStep = startStep;
		
		// the only trigger function relevant for ER cycles is to schedule after a certain plot step
		Predicate<PlotModel<?>> startFunc =  (PlotModel<?> model) -> model.getStep() >= startStep;
		
		// use meta-programming to create an instance of the happening class this needs to schedule
		this.happening = happeningClass.getDeclaredConstructor(Predicate.class, String.class).newInstance(startFunc, character);
	}
	
	@Override
	public void execute(EngageResult er) {
		((ScheduledHappeningDirector) er.getLastModel().happeningDirector).scheduleHappening(this.happening);
	}

	@Override
	public void undo(EngageResult er) {
		((ScheduledHappeningDirector) er.getLastModel().happeningDirector).removeHappening(this.happening);
	}
	
	@Override
	public String message() {
		return "Scheduling happening " + this.happening.getClass().getSimpleName() + " at step " + this.startStep;
	}
	
	/**
	 * Determines the label of the vertex that this happening will cause in a characters subgraph, if it is successfully
	 * perceived. Does not return annotations. Can be used to detect this happening in a plot graph using e.g.
	 * <code> scheduledHappening.getGraphRepresentation().equals(TermParser.removeAnnots(v.getLabel())); </code>
	 * @return Label with belief-added operator and without annotations
	 */
	public String getGraphRepresentation() {
		return Trigger.TEOperator.add.toString() + this.happening.getPercept();
	}

}
