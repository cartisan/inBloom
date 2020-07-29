package inBloom.rl_happening.rl_management;

import java.util.LinkedList;
import java.util.List;

import inBloom.PlotModel;
import inBloom.storyworld.Happening;
import inBloom.storyworld.HappeningDirector;

/**
 * A class to link the selection of a Happening in a given time step to a Reinforcement Learning algorithm.
 * 
 * The class has a local variable to save an instance of an RL algorithm (Sarsa in this implementation)
 * that will be performed in order to decide which Happenings will be be scheduled.
 * 
 * The method getTriggeredHappening calls SarsaLambda.performStep, which furthermore performs the parts
 * of the algorithm that are needed in each step in order to select a new action.
 * 
 * @author	Julia Wippermann
 * @version 9.6.20
 */
public class AutomatedHappeningDirector <T extends PlotModel<?>> implements HappeningDirector {
	
	@SuppressWarnings("unused")
	private PlotModel<?> model;
	
	private LinkedList<Happening<?>> allHappenings;
	private SarsaLambda sarsa;
	
	
	
	/**
	 * The constructor creates a list of all Happenings, given by the class HappeningManager, in which
	 * there is a static list of all Happenings. If a possible Happening should be added to the plot,
	 * it should be added in the class HappeningManager. The list of Happenings given the HappeningManager
	 * usually includes the "EmptyHappening", a Happening that has no effects, but is necessary to implement.
	 * 
	 * The constructor also saves the given instance of a reinforcement learning algorithm. So far this
	 * is hardcoded to the algorithm of Sarsa(lambda), but could easily be generalized by introducing a
	 * super class for all reinforcement algorithms.
	 * 
	 * @param rlApplication
	 * 				The reinforcement learning algorithm that decides which Happening is to be scheduled
	 * 				in each step. Right now this is hard coded to Sarsa(lambda).
	 */
	public AutomatedHappeningDirector(SarsaLambda rlApplication) {
		
		// Create a list of all possible Happenings that can be scheduled in this world
		this.allHappenings = new LinkedList<Happening<?>>();
		this.allHappenings.addAll(HappeningManager.getAllHappenings());
		
		// Save the given instance of the reinforcement learning algorithm
		this.sarsa = rlApplication;
		
	}


	
	
	
	/**
	 * Calls the previously given reinforcement learning algorithm (currently hardcoded to Sarsa(lambda))
	 * to decide which Happening will be scheduled in the current time step. The method returns a list of
	 * all scheduledHappenings which will then be performed by the PlotModel. The list contains exactly
	 * one Happening.
	 * 
	 * @param step
	 * 			The current step of the plot (PlotModel), for which a Happening should be scheduled
	 * 
	 * @return	A list of all Happenings that are scheduled to happen in this step.
	 * 
	 * 			Note:
	 * 			For reasons of simplicity and runtime, SarsaLambda only ever returns one single Happening
	 * 			for one step. Even when no Happening should be performed in the current time step, for
	 * 			Sarsa(lambda) this equals to scheduling the so called "EmptyHappening". This means that
	 * 			the returned list will always contain EXACTLY ONE object. Since this is an implemented
	 * 			method from a more generale interface however, it is still returned in a List.
	 */
	@Override
	public List<Happening<?>> getTriggeredHappenings(int step) {
						
		LinkedList<Happening<?>> triggeredHappeningsInThisStep = new LinkedList<Happening<?>>();
		
		// call Sarsa(lambda) to decide which Happening will be performed in this step
		Happening<?> chosenAction = this.sarsa.performStep(step);
		
		// schedule the chosen Happening for this step
		if(chosenAction != null) {
			triggeredHappeningsInThisStep.add(chosenAction);
		}
		
		return triggeredHappeningsInThisStep;
	}

	/**
	 * Returns a list of all possible Happenings that could be scheduled at any given time point (step)
	 * 
	 * @return A list of all possible Happenings to be scheduled
	 */
	@Override
	public List<Happening<?>> getAllHappenings() {
		return this.allHappenings;
	}

	
	
	
	
	
	/**
	 * Saves the PlotModel on which the chosen Happenings will be performed
	 * 
	 * @param model
	 * 			The PlotModel on which the chosen Happenings will be performed
	 */
	@Override
	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

	/**
	 * Clones this instance and returns the clone. This is needed to fulfill the requirements of the
	 * interface HappeningDirector
	 * 
	 * @return A clone of this instance of the AutomatedHappeningDirector
	 */
	@Override
	public AutomatedHappeningDirector<T> clone() {
		AutomatedHappeningDirector<T> clone = new AutomatedHappeningDirector<T>(this.sarsa);
		for(Happening<?> h: this.allHappenings) {
			clone.addHappening(h);
		}
		return clone;
	}
	
	/**
	 * Adds a single, given Happening to the list of all (possible) Happenings. This is needed to
	 * implement the required method clone
	 * 
	 * @param happening
	 * 				The Happening that should be added to the list of all possible Happenings
	 */
	public void addHappening(Happening<?> happening) {
		this.allHappenings.add(happening);
	}

	
	
	public SarsaLambda getSarsa() {
		return this.sarsa;
	}
	
}
