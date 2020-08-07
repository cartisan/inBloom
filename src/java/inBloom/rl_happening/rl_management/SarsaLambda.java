/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.HashMap;
import java.util.Iterator;
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
 * Using a two-dimensional hashMap for the weights
 * 
 * @author Julia Wippermann die Coole
 * @version 7.8.20
 *
 */
@SuppressWarnings("unused")
public class SarsaLambda {
	
	/**
	 * UNDERLYING MODEL
	 */
	FeaturePlotModel<?> featurePlotModel;
	
	/**
	 * FINE-TUNABLE VARIABLES
	 */
	private final double epsilon = 0.3; // epsilon-soft greedy action selection criteria
	private final double lambda = 0.8;	// balance btw MC/TD; 1 = MC (high backtracing of reward); 0 = TD (low backtracing of reward)
	private final double gamma = 1;	// discount factor - 0 = ignoring future rewards, 1 = 100% additive rewards
	private final double alpha = 0.001;	// learning rate   - 
	
	/**
	 * DATA STRUCTURES FOR PERFORMING THE ALGORITHM
	 */
	protected HashMap<String, HashMap<Happening<?>, Double>> eligibilityTraces;
	protected HashMap<String, HashMap<Happening<?>, Double>> weights;		/* String = Name of the Feature
																			 * Happening = Action (incl. empty Happening)
	 																		 * Integer = Weight */
	private HashMap<Happening<?>, Double> qValues;
	
	
	/**
	 * BOOLEANS FOR OUTPUTS
	 */
	private final boolean PRINT_QVALUES = false;
	private final boolean PRINT_FEATURES_PER_STEP = true;
	private final boolean SHOW_ELIGIBILITY_TRACES_PER_STEP = false;
	private final boolean PRINT_WEIGHTS_AFTER_UPDATE = false;
	public final boolean PRINT_WEIGHTS_AFTER_EPISODE = true;
	private final boolean PRINT_CHOSEN_ACTION_PER_STEP = true;
	private final boolean PRINT_UPDATE_CALCULATIONS = false;
	private final boolean PRINT_ELIGIBILITY_UPDATES = false;
	
	
	/**
	 * BACKGROUND INFORMATION NEEDED
	 */
	private Happening<?>[] allHappenings;
	private LinkedList<String> allFeatures;
	
	// necessary for updating eligibility traces at the beginning of 1 step with regards to the
	// features that had been present during the last action selection
	private LinkedList<String> currentlyPresentFeatures;
	private int stepOfCurrentFeatures;
	
	
	// necessary for updating all weights of all features at the end of one episode with regard to the selected Action
	// since we only receive the reward at the end of one episode (no intermediate rewards)
//	private LinkedList<StepwiseInformation> stepwiseMemory;
	protected int step;
	private Happening<?> previousAction;
	private int stepOfPreviousAction;
	private Double previousQValue;
	
	private Happening<?> currentAction;
	private int stepOfCurrentAction;
	private Double currentQValue;
	
	// only for log purposes in testing
	public ReinforcementLearningCycle rlCycle;
	
	
	
	

	
	public SarsaLambda(FeaturePlotModel<?> model, ReinforcementLearningCycle rl) {
		this.featurePlotModel = model;
		this.rlCycle = rl;
	}
	
	public void initializeSarsa(FeaturePlotModel model) {

		// Initialize allHappenings
		LinkedList<Happening<?>> happeningList = HappeningManager.getAllHappenings();
		this.allHappenings = new Happening<?>[9];
		happeningList.toArray(this.allHappenings);

		this.featurePlotModel = model;

		// Initialize allFeatures
		// needed to create the initial weights and eligibility Traces for every feature
		this.allFeatures = model.getAllPossibleFeatures();


		// Initialize allFeaturesOfTheEpisode
//		this.stepwiseMemory = new LinkedList<StepwiseInformation>();
		this.previousAction = null;
		this.previousQValue = 0.0;
		this.currentAction = null;
		this.currentQValue = 0.0;
		this.step = 0;


		// initialise qValues
		this.qValues = new HashMap<Happening<?>, Double>();


		// Initialize previousHappening (null)
		this.previousAction = null;
		
		this.currentlyPresentFeatures = null;

		
		
		
		rlCycle.log("Initialising Weights");
		this.initializeWeights();
		
		rlCycle.log("Initialising eligibility traces");
		this.initializeEligibilityTraces();
		
		
		this.rlCycle.log(toString());
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
	public Happening<?> chooseNewAction() {		
		
		this.previousAction = this.currentAction;
		this.previousQValue = this.currentQValue;
		this.stepOfPreviousAction = this.stepOfCurrentAction;
		
		// features = get the features present in this state
		// we make this only state-dependent, actions will have their effect when we look into the weights
		// because those are different depending on which action is chosen
		LinkedList<String> presentFeatures = this.featurePlotModel.getCopyOfPresentFeatures();
		this.currentlyPresentFeatures = presentFeatures;
		this.stepOfCurrentFeatures = this.step;
		
		if(PRINT_FEATURES_PER_STEP) {
			rlCycle.log("Present features: " + presentFeatures);
		}
		
		
		// es sind in jedem State alle Actions erlaubt
		for(Happening<?> action: this.allHappenings) {
			
			// q-value of action = for all features of action get weight and sum it up
			
			double qvalue = 0.0;
			
			
			// get the weight of this specific state-action-dependent feature (where states are represented using the present features)
			for(String presentFeature: presentFeatures) {
				qvalue += this.getFeatureActionDependentValue(weights, presentFeature, action);
			}
			
			
			this.qValues.put(action, qvalue);
			
			if(PRINT_QVALUES) {
				rlCycle.log("QValue was added: " + action + ": " + qvalue);
			}
			
		}
		
		if(PRINT_QVALUES) {
			rlCycle.log("\n");
		}
		
		
		
		
		Happening<?> action;
		
		// choose a random action (that will stay with chance of epsilon)

		int numberOfHappenings = this.allHappenings.length;
		int randomHappeningIndex = new Random().nextInt(numberOfHappenings);
		action = allHappenings[randomHappeningIndex];


		// with chance of 1 - epsilon: choose a greedy action
		// 		we chose (echt kleiner epsilon) for p(random action)
		//		therefore (größer gleich epsilon) for p(greedy action)
		// , since 0.0 is included in Math.random(), but 1.0 is not, so we decided to put epsilon itself in the "right" category (going to 1.0)
		double max_qValue = 0.0;
		if(Math.random() >= epsilon) {
			
			// choose the action with the highest qvalue

			max_qValue = 0.0;
			
			for(Happening<?> happening: qValues.keySet()) {

				double current_qValue = qValues.get(happening);

				if(current_qValue > max_qValue) {
					max_qValue = current_qValue;
					action = happening;
				}
			}
			
		}
		
		
		System.out.println("Chosen Action: " + action);
		
		// save all information necessary for later update of weights at the end of this episode
		double qValue = this.qValues.get(action);
//		this.stepwiseMemory.add(new StepwiseInformation(this.currentlyPresentFeatures, action, previousAction, max_qValue, this.previousQValue));
		
		//update previous Action AFTER stepwise Information is saved
		this.currentAction = action;
		this.currentQValue = qValue;
		this.stepOfCurrentAction = this.step;
		
		return action;
	}
	
	
	
	public Happening<?> performStep(int step) {
		// observe reward:	none, bc only Tellability in the end.
		// alternative:		some punishment or similar for having taken an artificial action
		// QUESTION: what is the reward function dependent on? -> not specified in pseudo-code
		//			 idea: on the last action (Happening vs. no Happening). Unless we have Tellability
		
		// delta = delta + gamma * Q(a) where a ist the last performed action
		
		// Update eligibility Traces
		this.step = step;
				
		if(this.currentlyPresentFeatures!=null && this.currentAction!=null) {
			
			this.updateEligibilityTraces();
						
			if(SHOW_ELIGIBILITY_TRACES_PER_STEP) {
				rlCycle.log(printFeatureActionValues(eligibilityTraces));
			}
		}
		
		
		// Action selection
		Happening<?> chosenAction = this.chooseNewAction();
		
		if(PRINT_CHOSEN_ACTION_PER_STEP) {
			rlCycle.log("Performed Action: " + chosenAction);
		}

		
		return chosenAction;
	}
	
	
	private void updateEligibilityTraces() {

		// decrease ALL (non-null) eligibility traces with regards to gamma and lambda
		for(String feature: this.eligibilityTraces.keySet()) {

			for(Happening<?> happening: this.eligibilityTraces.get(feature).keySet()) {
				
				double eligibilityValue = this.getFeatureActionDependentValue(eligibilityTraces, feature, happening);
				
				if(eligibilityValue!=0.0) {
					
					if(PRINT_ELIGIBILITY_UPDATES) {
						rlCycle.log("Decreasing e for feature [" + feature + "], action [" + happening + "]");
					}
					
					double newEligibilityValue = eligibilityValue * this.gamma * this.lambda;
					this.eligibilityTraces.get(feature).put(happening, newEligibilityValue);
				}
				
			}

		}
		
		
		for(String previouslyPresentFeature: this.currentlyPresentFeatures) {

			// TODO nur für das Happening, das auch gewählt worden war
			//for(Happening<?> happening: this.eligibilityTraces.get(previouslyPresentFeature).keySet()) {
			
			if(PRINT_ELIGIBILITY_UPDATES) {
				rlCycle.log("++Increasing e for feature [" + previouslyPresentFeature + "[" + this.stepOfCurrentFeatures + "]], action [" + currentAction + "[" + this.stepOfCurrentAction + "]]");
			}
				
				double eligibilityValue = this.getFeatureActionDependentValue(eligibilityTraces, previouslyPresentFeature, this.currentAction);
				double newEligibilityValue = eligibilityValue + 1;
				
				
				this.eligibilityTraces.get(previouslyPresentFeature).put(this.previousAction, newEligibilityValue);
				
//			}

		}
		

	}
	

	/**
	 * Updates the weights of each feature after each step of an episode with respect to the following formula:
	 * 
	 * for each weight and the corresponding eligibiloty trace:
	 * weight = weight + alpha * (reward - Qa + gamma * Qa') * e
	 * 
	 * 
	 * This is implemented in the following steps, following the pseudocode by Sutton and Barto ():
	 * 
	 * delta = reward - Qa
	 * [a' is selected]
	 * delta = delta + gamma * Qa'
	 * 
	 * for each weight:
	 * weight = weight + alpha * delta * corresponding eligibility
	 * 
	 * @param reward
	 * 			The reward received at the current step (this is 0.0 unless the episode is finished)
	 */
	public void updateWeights(double reward) {
		
		if(PRINT_UPDATE_CALCULATIONS) {
			rlCycle.log("\nWEIGHT UPDATE");
		}

		// calculate delta before action selection
		// d = r - Qa
		double delta = reward - previousQValue;

		if(PRINT_UPDATE_CALCULATIONS) {
			rlCycle.log("d = r - Qa: " + reward + " - " + previousQValue + " = " + delta);
			rlCycle.log("d + (g * Qa'): " + delta + " + " + "(" + gamma + " * " + currentQValue  + ")");
		}

		// update delta after action selection
		// d = d + g + Qa'
		delta += (this.gamma * this.currentQValue);

		if(PRINT_UPDATE_CALCULATIONS) {
			rlCycle.log("new error: " + delta);
		}


		// update all features with regards to the elgibility traces
		for(String feature: weights.keySet()) {

			HashMap<Happening<?>, Double> happeningToWeight = weights.get(feature);

			for(Happening<?> action: happeningToWeight.keySet()) {

				double weight = happeningToWeight.get(action);
				double e = this.getFeatureActionDependentValue(eligibilityTraces, feature, action);

				if(e != 0.0) {

					// calculate new weight
					// w = w + a * d * e
					
					if(PRINT_UPDATE_CALCULATIONS) {
						rlCycle.log("\nFeature: " + feature);
						rlCycle.log("Action:  " + action);
						rlCycle.log("w = w + (a * d * e): " + weight + " + " + "(" + alpha + " * " + delta + " * " + e + ")");
					}
					weight += (alpha * delta * e);

					if(PRINT_UPDATE_CALCULATIONS) {
						rlCycle.log("new weight: " + weight);
					}

					// set new weight
					happeningToWeight.put(action, weight);
				}
			}
		}
		
		if(PRINT_WEIGHTS_AFTER_UPDATE) {
			rlCycle.log(printFeatureActionValues(weights));
		}

	}
	
	
	
	
	/**
	 * INITIALIZING STUFF
	 */
	
	/**
	 * Initializes random initial weights for every possible feature of the underlying FeaturePlotModel
	 */
	private void initializeWeights() {
		
		// initialize the HashMap that maps from Feature to Weight
		this.weights = new HashMap<String, HashMap<Happening<?>,Double>>();
		
		
		/* 
		 * for each feature
		 * 		for each Happening
		 * 			create a random weight
		 * 			assign this weight to this Happening
		 * 		assign this HashMap to this feature
		 */
		
		
		// go through all features to assign random initial weight to each of their actions
		for(String feature: this.allFeatures) {
			
			HashMap<Happening<?>,Double> actionDependentWeights = new HashMap<Happening<?>,Double>();
			
			for(Happening<?> action: this.allHappenings) {
				
				// generate a random value for the initial weight
				// Math.random() method returns a double value with a positive sign, greater than or equal to 0.0 and less than 1.0.
				// (from https://www.java2novice.com/java-fundamentals/math/random/)
				double initialWeightValue = Math.random();
				
				actionDependentWeights.put(action, initialWeightValue);
				
			}
			
			// set the initial weight for this feature to the generated value
			this.weights.put(feature, actionDependentWeights);
			
		}
		
	}
	
	
	/**
	 * Initializes all inital eligibility values as 0 for every possible feature of the underlying FeaturePlotModel
	 */
	private void initializeEligibilityTraces() {
		
		// initialize the HashMap that maps from Feature to Eligibility Value
		this.eligibilityTraces = new HashMap<String, HashMap<Happening<?>, Double>>();

		// go through all features to assign random initial eligibility values to them
		for(String feature: this.allFeatures) {

			

			HashMap<Happening<?>,Double> actionDependentEligibilityTrace = new HashMap<Happening<?>,Double>();

			for(Happening<?> action: this.allHappenings) {

				// set the initial eligibility value for this feature to 0
				actionDependentEligibilityTrace.put(action, 0.0);

			}

			// set the initial weight for this feature to the generated value
			this.eligibilityTraces.put(feature, actionDependentEligibilityTrace);

		}
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
//	private LinkedList<String> getFeatures() {
//		
//		LinkedList<String> presentFeatures = new LinkedList<String>();
//		
//		presentFeatures = this.featurePlotModel.getPresentFeatures();
//				
//		return presentFeatures;
//	}
	
	
	/**
	 * Returns the current weight of the given feature
	 * 
	 * @param feature
	 * 			A string representing the feature that we want to know the weight of
	 * @return
	 * 			The weight (int) of the given feature
	 */
	private double getFeatureActionDependentValue(HashMap<String, HashMap<Happening<?>, Double>> featureActionValues, String feature, Happening<?> action) {
		try {
			return featureActionValues.get(feature).get(action);
		} catch(NullPointerException e) {
			System.out.println(featureActionValues);
			System.out.println(feature);
			System.out.println(action);
			throw new NullPointerException();
		}
	}
	
	
	
	
	
	// TODO delete?
	private int getCurrentStateOfModel() {
		return this.featurePlotModel.getStateValue();
	}
	
	
	
	
	
	
	
	/**
	 * PRINTING
	 */
	
	
	public String printFeatureActionValues(HashMap<String, HashMap<Happening<?>, Double>> featureActionValues) {
		String formatString = "%25s";
		String formatNumber = "%25f";
		String featureFormat = "%21s";
		
		String result = "";
				
		result += String.format(featureFormat, "."); // empty space for feature
		
		//daddy.log(featureActionValues.toString());
		//daddy.log(allFeatures.toString());
		
		for(Happening<?> happening: featureActionValues.get(allFeatures.getFirst()).keySet()) {
			result += String.format(formatString, happening);
		}
		
		result += "\n";
		
		for(String feature: this.weights.keySet()) {
			result += String.format(featureFormat, feature) + ":";
			
			for(Happening<?> happening: featureActionValues.get(feature).keySet()) {
				
				double featureActionValue = this.getFeatureActionDependentValue(featureActionValues, feature, happening);
				double featureActionValueRounded = Math.round(featureActionValue*1000000);
				featureActionValueRounded = featureActionValueRounded /1000000;
				
//				gerundet = Math.round(deineZahl * 10) / 10;
				result += String.format(formatNumber, featureActionValueRounded);
			}
			result += "\n";
		}
		
		return result;
	}
	
	public String printEligibilityTraces() {
		// protected HashMap<String, Integer> eligibilityTraces;
		// map from feature -> double
		
		String featureFormat = "%21s";
		
		String result = "";
		for(String feature: this.eligibilityTraces.keySet()) {
			result += String.format(featureFormat, feature) + ":" + String.format("%5d", this.eligibilityTraces.get(feature)) + "\n";
		}
		
		return result;
	}
	
	public String toString() {
		String result = "";
		
		result += "Happenings:\n";
		List<Happening<?>> allHappenings = this.featurePlotModel.happeningDirector.getAllHappenings();
		for(Happening<?> happening: allHappenings) {
			result += "               " + happening + "\n";
		}
		
		result += "Weights:\n";
		//result += this.printWeights();
		result += printFeatureActionValues(this.weights);
		
		result += "\nEligibility Traces:\n";
		
		//result += this.printEligibilityTraces();
		result += printFeatureActionValues(this.eligibilityTraces);
			
		return result;
	}
	
	
	
	public void setPlotModel(FeaturePlotModel<?> model) {
		this.featurePlotModel = model;
	}
	
	
	private class StepwiseInformation {
		
		private LinkedList<String> presentFeatures;
		private Happening<?> selectedAction;
		private Happening<?> previousAction;
		private double selectedQValue;
		private double previousQValue;
		
		public StepwiseInformation(LinkedList<String> presentFeatures,
								   Happening<?> selectedAction,
								   Happening<?> previousAction,
								   double selectedQValue,
								   double previousQValue) {
			this.presentFeatures = presentFeatures;
			this.selectedAction = selectedAction;
			this.previousAction = previousAction;
			this.selectedQValue = selectedQValue;
			this.previousQValue = previousQValue;
		}
		
	}
	
}
