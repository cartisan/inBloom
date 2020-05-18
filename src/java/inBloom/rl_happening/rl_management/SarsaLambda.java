/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jfree.util.Log;

import com.google.common.collect.HashBasedTable;

import inBloom.PlotModel;
import inBloom.storyworld.Happening;

/**
 * A class implementing everything necessary for executing SARSA(lambda) on the Storyworld
 * 
 * @author Julia Wippermann
 * @version 19.1.20
 *
 */
public class SarsaLambda {
	
	/**
	 * UNDERLYING MODEL
	 */
	FeaturePlotModel<?> featurePlotModel;
	
	/**
	 * PARAMETER RESTRICTIONS
	 */
	private final int INITIAL_WEIGHT_MAX = 1;
	private final int INITIAL_WEIGHT_MIN = 0;
	
	/**
	 * FINE-TUNABLE VARIABLES
	 */
	private final double epsilon = 0; // epsilon-soft greedy actio nselection criteria
	private final double lambda = 0; // or int? // lambda-value
	private final double gamma = 0; // or int? // discount factor
	private final double alpha = 0; // or int? // learning rate
	
	/**
	 * DATA STRUCTURES FOR PERFORMING THE ALGORITHM
	 */
	private List<Integer> eligibilityTraces;
	private HashMap<String, Integer> weights;		/* String = Name of the Feature
													 * Integer = Weight */
//	private HashBasedTable<Integer, Happening<?>, Integer> qValues; /* Rows: States (Integer)
//	 															  	 * Columns: Actions (Happenings)
//	 															  	 * Values: Q-Values (State-Action-Utility)*/
	
	private HashMap<StateActionPair, Integer> qValues;
	
	private class StateActionPair {
		int state;
		Happening<?> action;

		public StateActionPair(int state, Happening<?> action) {
			this.state = state;
			this.action = action;
		}
		
		public int getState() {
			return this.state;
		}
		
		public Happening<?> getAction() {
			return this.action;
		}
	}
	
	
	/**
	 * BACKGROUND INFORMATION NEEDED
	 */
	private Happening<?>[] allHappenings;
	private LinkedList<String> allFeatures;
	private Happening<?> previousHappening;
	

	
	public SarsaLambda(FeaturePlotModel<?> model, ReinforcementLearningCycle daddy) {
		
		this.featurePlotModel = model;

		// TODO possibel problem: allhappenings are onyl initialized after HappeningManager.scheduleHappenings has been called
		// -> may change in the future since we won't really schedule Happenings anymore?
		// TODO change to LinkedList in general, not Array?
		
		// Initialize allHappenings
		LinkedList<Happening<?>> happeningList = HappeningManager.getAllHappenings();
		this.allHappenings = new Happening<?>[9];
		happeningList.toArray(this.allHappenings);
	
		
		// Initialize allFeatures
		// needed to create the initial weights and eligibility Traces for every feature
		this.allFeatures = this.featurePlotModel.getAllPossibleFeatures();
		
		
		// initialise qValues
		this.qValues = new HashMap<StateActionPair, Integer>();
		
		
		// Initialize previousHappening (null) -> TODO change to when first called later? But first, to make sure no NullPointer when starting or similar, we put it here
		this.previousHappening = null;
		
		daddy.log("Initialising Weights");
		this.initializeWeights();
		daddy.log("Initialising eligibility traces");
		this.initializeEligibilityTraces();
		
	}
	

	
	/**
	 * So far we just by 50% choose random Happening and by 50% choose the Happening with the highest q-Value so far
	 * TODO Overthink or research this maybe
	 * TODO we have to involve the frequencies in here at some point
	 * 
	 * @param currentState
	 * 			The state we are in right now from which we should choose the next Action
	 * @return The Happening that will be the next Action
	 */
	public Happening<?> chooseNewAction(int currentState) {
		// TODO
		
		for(Happening<?> action: this.allHappenings) {
			// features of action = get the features present in this state and action
			LinkedList<String> presentFeatures = this.getFeatures(action);
			
			
			// q-value of action = for all features of action get weight and sum it up
			//		idea: put in in a sorted way already?
			
			int qvalue = 0;
			
			for(String feature: presentFeatures) {
				qvalue += this.getWeightOfFeature(feature);
			}
			
			// TODO: This is the version where we also save the state.
			// get state to be saved in the q-value table
			int state = this.featurePlotModel.getStateValue();
			
			
//			this.qValues.put(state, action, qvalue);
			this.qValues.put(new StateActionPair(state, action), qvalue);
			
			
		}
		
		// find action with the maximum q-value
		// if random < epsilon:
		//		choose random action
		
		
		Happening<?> action;
		// with chance of epsilon: choose a random action
		// 		we chose echt kleiner, since 0.0 is included in Math.random(), but 1.0 is not, so we decided to put epsilon itself in the "right" category (going to 1.0)
		if(Math.random() < epsilon) {
			int numberOfHappenings = this.allHappenings.length;
			int randomHappeningIndex = new Random().nextInt(numberOfHappenings);
			action = allHappenings[randomHappeningIndex];
		}
		
		// greedily choose the best action
		else {
			// choose the action with the highest qvalue
			//qValues.
		}
		
		return null;
	}

	
	
	
	/**
	 * INITIALIZING STUFF
	 */
	
	/**
	 * Initializes random initial weights for every possible feature of the underlying FeaturePlotModel
	 */
	private void initializeWeights() {
		
		// initialize the HashMap that maps from Feature to Weight
		this.weights = new HashMap<String, Integer>();
		
		// go through all features to assign random initial weight to them
		for(String feature: this.allFeatures) {
			
			// generate a random value for the initial weight
			int initialWeightValue = new Random().nextInt(INITIAL_WEIGHT_MAX - INITIAL_WEIGHT_MIN + 1) + INITIAL_WEIGHT_MIN;
			
			// set the initial weight for this feature to the generated value
			this.weights.put(feature, initialWeightValue);
		}
	}
	
	/**
	 * Initializes all inital eligibility values as 0 for every possible feature of the underlying FeaturePlotModel
	 */
	private void initializeEligibilityTraces() {
		
		// initialize the HashMap that maps from Feature to Eligibility Value
		this.eligibilityTraces = new LinkedList<Integer>();

		// go through all features to assign random initial eligibility values to them
		for(String feature: this.allFeatures) {

			// set the initial eligibility value for this feature to 0
			this.weights.put(feature, 0);
		}
	}


	public Happening<?> performStep(int step) {
		// observe reward:	none, bc only Tellability in the end.
		// alternative:		some punishment or similar for having taken an artificial action
		// QUESTION: what is the reward function dependent on? -> not specified in pseudo-code
		//			 idea: on the last action (Happening vs. no Happening). Unless we have Tellability
		
		// delta = delta + gamma * Q(a) where a ist the last performed action
		
		
		// TODO call action selection?
		// TODO maybe this doesn't make sense, I'm just inserting it for testing reasons
		this.chooseNewAction(this.getCurrentStateOfModel());
		
		
		return null;
	}
	
	
	
	
	/**
	 * METHODS THAT RETURN INTERNALLY NEEDED VALUES
	 */
	
	/**
	 * Returns the Features present in the current State of the PlotModel and the selected action
	 * 
	 * @param action
	 * 			The currently selected Action on which the Feature-space is dependent on.
	 * 			The feature space returned is also dependent on the current state of the PlotModel
	 * 			but that one is not given as a parameter since it can implicitaly be read from the
	 * 			known instance variable FeaturedPlotModel (since it is on its own in its current state)
	 * @return
	 */
	private LinkedList<String> getFeatures(Happening<?> action) {
		
		LinkedList<String> presentFeatures = new LinkedList<String>();
		
		// DEBUG this returns an empty list
		presentFeatures = this.featurePlotModel.getPresentFeatures();
		
		// TODO include the features of the action or the combination of state and action
		
		return presentFeatures;
	}
	
	/**
	 * Returns the current weight of the given feature
	 * 
	 * @param feature
	 * 			A string representing the feature that we want to know the weight of
	 * @return
	 * 			The weight (int) of the given feature
	 */
	private int getWeightOfFeature(String feature) {
		return this.weights.get(feature);
	}
	
	
	private int getCurrentStateOfModel() {
		return this.featurePlotModel.getStateValue();
	}
	
}
