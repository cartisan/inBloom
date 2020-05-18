/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import inBloom.PlotModel;
import inBloom.rl_happening.happenings.ConditionalHappening;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.ScheduledHappeningDirector;

/**
 * @author Julia Wippermann
 * @version 3.12.19 
 *
 */
public class AutomatedHappeningDirector<T extends PlotModel<?>> implements HappeningDirector {

	// an Action can either be a Happening or null
	
	private List<Happening<?>> allHappenings;
	private SarsaLambda sarsa;
	
	private PlotModel<?> model;
	
	public AutomatedHappeningDirector(SarsaLambda rlApplication) {
		this.allHappenings = new LinkedList<Happening<?>>(); // TODO get your Happenings lol? :D
		this.allHappenings.addAll(HappeningManager.getAllHappenings());
		this.allHappenings.add(null); // TODO necessary?
		
		this.sarsa = rlApplication;
		
	}
	
	/**
	 * IMPLEMENTED METHODS FROM INTERFACE
	 */
	
	/**
	 * IN HERE SARSA IS CALLED
	 */
	@Override
	public List<Happening<?>> getTriggeredHappenings(int step) {
		
		LinkedList<Happening<?>> triggeredHappeningsInThisStep = new LinkedList<Happening<?>>();
		
		/*
		 * HERE SARSA IS CALLED WITH PERFORMSTEP
		 */
		Happening<?> currentAction = this.sarsa.performStep(step);
		
		/*
		 * Let the Happening happen
		 */
		if(currentAction != null) {
			triggeredHappeningsInThisStep.add(currentAction);
		}
		
		return triggeredHappeningsInThisStep;
	}

	@Override
	public List<Happening<?>> getAllHappenings() {
		return this.allHappenings; //allHappenings;
	}

	@Override
	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

	public AutomatedHappeningDirector<T> clone() {
		AutomatedHappeningDirector<T> clone = new AutomatedHappeningDirector<T>(this.sarsa);
		for(Happening<?> h: this.allHappenings) {
			clone.addHappening(h);
		}
		return clone;
	}
	
	
	
	
	/**
	 * NEW METHODS
	 */
	
	public void addHappening(Happening<?> happening) {
		//this.allActions.add((Action)happening);
		this.allHappenings.add(happening);
	}
	
	public void reset() {
		// TODO
		// reset all scheduled happenings to just generally all happenings
		// this.scheduledHappenings = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.allHappenings).clone();
	}
	
	
	
	
	/**
	 * EMPTY HAPPENING
	 */
	
	/**private static class EmptyHappening implements Action {
		
	}*/

}
