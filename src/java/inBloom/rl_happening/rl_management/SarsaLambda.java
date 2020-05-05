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
	private HashBasedTable<Integer, Happening<?>, Integer> qValues; /* Rows: States (Integer)
	 															  	 * Columns: Actions (Happenings)
	 															  	 * Values: Q-Values (State-Action-Utility)*/
	private Happening<?>[] allHappenings = new Happening<?>[0];
	
	private LinkedList<String> allFeatures;
	

	
	public SarsaLambda(FeaturePlotModel<?> model, ReinforcementLearningCycle daddy) {
		
		this.featurePlotModel = model;

		// TODO possibel problem: allhappenings are onyl initialized after HappeningManager.scheduleHappenings has been called
		// -> may change in the future since we won't really schedule Happenings anymore?
		// TODO change to LinkedList in general, not Array?
		LinkedList<Happening<?>> happeningList = HappeningManager.getAllHappenings();
		happeningList.toArray(this.allHappenings);
	
		
		
		// needed to create the initial weights and eligibility Traces for every feature
		this.allFeatures = this.featurePlotModel.getAllPossibleFeatures();
		
		daddy.log("Initialising Weights");
		this.initializeWeights();
		daddy.log("Initialising eligibility traces");
		this.initializeEligibilityTraces();
		
	}
	

	
	/**
	 * So far we just by 50% choose random Happening and by 50% choose the Happening with the highest q-Value so far
	 * TODO Overthink or research this maybe
	 * TODO we have to involve the frequencies in here at some point
	 * TODO find out WTF IS F AND WHERE IT IS EXPLAINED WTF DUDE
	 * 
	 * @param currentState
	 * 			The state we are in right now from which we should choose the next Action
	 * @return The Happening that will be the next Action
	 */
	public Happening<?> chooseNewAction(int currentState) {
		// TODO
		
		for(Happening<?> action: this.allHappenings) {
			// features of action = get the features present in this state and action
			// q-value of action = for all features of action get weight and sum it up
			//		idea: put in in a sorted way already?
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
		return null;
	}
	
	
	
}
