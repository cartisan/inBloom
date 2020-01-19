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
	
	//private LinkedList<Action> allActions; // TODO empty Happening
	private LinkedList<Happening<?>> allHappenings;
	private HashMap<Integer, Happening<T>> policy; // only 1 Action per TimeStep possible!
	
	private PlotModel<?> model;
	
	public AutomatedHappeningDirector() {
		//this.allActions = new LinkedList<Action>();
		this.allHappenings = new LinkedList<Happening<?>>();
		this.allHappenings.add(null); // TODO necessary
		
		this.policy = new HashMap<Integer,Happening<T>>();
		
		//this.allActions.add(new EmptyHappening());
	}
	
	/**
	 * IMPLEMENTED METHODS FROM INTERFACE
	 */
	
	@Override
	public List<Happening<?>> getTriggeredHappenings(int step) {
		
		LinkedList<Happening<?>> triggeredHappeningsInThisStep = new LinkedList<Happening<?>>();
		
		Happening<T> currentAction = policy(step);
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

	@SuppressWarnings("unchecked")
	@Override
	public AutomatedHappeningDirector<T> clone() {
		AutomatedHappeningDirector<T> clone = new AutomatedHappeningDirector<T>();
		//clone.model = this.model;
		//clone.allActions = (LinkedList<Action>) ((LinkedList<Action>)this.allActions).clone();

		clone.reset();
		return clone;
	}
	
	/**
	 * NEW METHODS
	 */
	
	public void addHappening(Happening<T> happening) {
		//this.allActions.add((Action)happening);
		this.allHappenings.add(happening);
	}
	
	public void reset() {
		// TODO
		// reset all scheduled happenings to just generally all happenings
		//this.scheduledHappenings = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.allHappenings).clone();
	}
	
	private Happening<T> policy(int step) {
		 Happening<T> currentAction = this.policy.get(step);
		 return currentAction;
	}
	
	
	
	/**
	 * EMPTY HAPPENING
	 */
	
	/**private static class EmptyHappening implements Action {
		
	}*/

}
