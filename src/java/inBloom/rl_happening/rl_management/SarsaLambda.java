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
 * @author Julia Wippermann
 * @version 22.7.20
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
	private final double epsilon = 0; // epsilon-soft greedy actio nselection criteria
	private final double lambda = 0; // or int? // lambda-value
	private final double gamma = 0; // or int? // discount factor
	private final double alpha = 0; // or int? // learning rate
	
	/**
	 * DATA STRUCTURES FOR PERFORMING THE ALGORITHM
	 */
	protected HashMap<String, HashMap<Happening<?>, Double>> eligibilityTraces;
	protected HashMap<String, HashMap<Happening<?>, Double>> weights;		/* String = Name of the Feature
																			 * Happening = Action (incl. empty Happening)
	 																		 * Integer = Weight */

	private HashMap<Happening<?>, Double> qValues;
	
	
	/**
	 * BACKGROUND INFORMATION NEEDED
	 */
	// TODO include the empty Happening in this representation
	private Happening<?>[] allHappenings;
	private LinkedList<String> allFeatures;
	
	// necessary for updating eligibility traces at the beginning of 1 step with regards to the
	// features that had been present during the last action selection
	private LinkedList<String> currentlyPresentFeatures;
	
	
	// necessary for updating all weights of all features at the end of one episode with regard to the selected Action
	// since we only receive the reward at the end of one episode (no intermediate rewards)
	private LinkedList<StepwiseInformation> stepwiseMemory;
	private Happening<?> previousAction;
	private Double previousQValue;
	
	// only for log purposes in testing
	private ReinforcementLearningCycle daddy;
	
	
	
	

	
	public SarsaLambda(FeaturePlotModel<?> model, ReinforcementLearningCycle daddy) {
		
		this.featurePlotModel = model;
		this.daddy = daddy;
		
//		if(this.featurePlotModel != null) {
//			this.initializeSarsa();
//		}
		
	}
	
	public void initializeSarsa() {
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


		// Initialize allFeaturesOfTheEpisode
		this.stepwiseMemory = new LinkedList<StepwiseInformation>();
		this.previousAction = null;
		this.previousQValue = 0.0;


		// initialise qValues
		this.qValues = new HashMap<Happening<?>, Double>();


		// Initialize previousHappening (null) -> TODO change to when first called later? But first, to make sure no NullPointer when starting or similar, we put it here
		this.previousAction = null;

		
		
		
		daddy.log("Initialising Weights");
		this.initializeWeights();
		
		daddy.log("Weights:");
		this.printFeatureActionValues(this.weights);
		
		
		
		
		daddy.log("Initialising eligibility traces");
		this.initializeEligibilityTraces();
		
		daddy.log("Eligibility Traces:");
		this.printFeatureActionValues(this.eligibilityTraces);
		
		
		
		
		this.daddy.log(toString());
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
		
		// es sind in jedem State alle Actions erlaubt
		for(Happening<?> action: this.allHappenings) {
			
			// features of action = get the features present in this state and action
			// we make this only state-dependent, actions will have their effect when we look into the weights
			// because those are different depending on which action is chosen
			LinkedList<String> presentFeatures = this.getFeatures(action);
			this.currentlyPresentFeatures = presentFeatures;
			// TODO wir koennten jetzt weiterhin auf der Instanzvariable arbeiten statt auf der lokalen
			// Variable, ist aber nicht notwendig
			
			
			// q-value of action = for all features of action get weight and sum it up
			//		idea: put in in a sorted way already?
			
			double qvalue = 0.0;
			
			daddy.log("Present features: " + presentFeatures);
			
			// get the weight of this specific state-action-dependent feature (where states are represented as features?)
			for(String presentFeature: presentFeatures) {
				qvalue += this.getFeatureActionDependentValue(weights, presentFeature, action);
			}
			

			//int state = this.featurePlotModel.getStateValue();
			
			
			this.qValues.put(action, qvalue);
//			System.out.println("Q-Value added. Action: " + action.toString() + ". Q-Value: " + qvalue);
			
			daddy.log("QValue was added: " + action + ": " + qvalue);
			
			
		}
		
		daddy.log("\n");
		
		/* find action with the maximum q-value
		 * if random < epsilon:
		 *		choose random action
		 */
		
		
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
		this.stepwiseMemory.add(new StepwiseInformation(this.currentlyPresentFeatures, action, previousAction, max_qValue, this.previousQValue));
		
		//update previous Action AFTER stepwise Information is saved
		this.previousAction = action;
		this.previousQValue = qValue;
		
		return action;
	}
	
	
	
	public Happening<?> performStep(int step) {
		// observe reward:	none, bc only Tellability in the end.
		// alternative:		some punishment or similar for having taken an artificial action
		// QUESTION: what is the reward function dependent on? -> not specified in pseudo-code
		//			 idea: on the last action (Happening vs. no Happening). Unless we have Tellability
		
		// delta = delta + gamma * Q(a) where a ist the last performed action
		
		
		// Update eligibility Traces
		//this.updateEligibilityTraces();
		
		// Action selection
		Happening<?> chosenAction = this.chooseNewAction();
		
		
		return chosenAction;
	}
	
	
	private void updateEligibilityTraces() {

		// update ALL eligibility traces with regards to gamma and lambda
		for(String feature: this.eligibilityTraces.keySet()) {

			for(Happening<?> happening: this.eligibilityTraces.get(feature).keySet()) {
				
				double eligibilityValue = this.getFeatureActionDependentValue(eligibilityTraces, feature, happening);
				double newEligibilityValue = eligibilityValue * this.gamma * this.lambda;
				
				this.eligibilityTraces.get(feature).put(happening, newEligibilityValue);
				
			}

		}
		
		
		for(String previouslyPresentFeature: this.currentlyPresentFeatures) {

			for(Happening<?> happening: this.eligibilityTraces.get(previouslyPresentFeature).keySet()) {
				
				double eligibilityValue = this.getFeatureActionDependentValue(eligibilityTraces, previouslyPresentFeature, happening);
				double newEligibilityValue = eligibilityValue + 1;
				
				
				this.eligibilityTraces.get(previouslyPresentFeature).put(happening, newEligibilityValue);
				
			}

		}
		

	}
	

	public void updateWeightsAtEndOfEpisode(double reward) {
		
		for(StepwiseInformation step: this.stepwiseMemory) {
			
			// calculate delta before action selection
			double delta = reward - step.previousQValue;
			
			// calculate lambda after action selection
			delta += this.lambda * step.selectedQValue;
			
			// TODO really all weights updatet?
			
			// for each feature
			for(String feature: weights.keySet()) {
				HashMap<Happening<?>, Double> happeningToWeight = weights.get(feature);
				for(Happening<?> action: happeningToWeight.keySet()) {
					
					// calculate new weight
					double weight = happeningToWeight.get(action);
					double e = this.getFeatureActionDependentValue(eligibilityTraces, feature, action);
					weight += alpha * delta * e;
					
					// set new weight
					happeningToWeight.put(action, weight);
				}
			}
			
		}
		
		
		// empty the episode specific information
		this.stepwiseMemory = new LinkedList<StepwiseInformation>();
		
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
	private double getFeatureActionDependentValue(HashMap<String, HashMap<Happening<?>, Double>> featureActionValues, String feature, Happening<?> action) {
		return featureActionValues.get(feature).get(action);
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
		this.initializeSarsa();
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
