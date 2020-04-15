/**
 * 
 */
package inBloom.rl_happening.archive;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.HashBasedTable;

import inBloom.rl_happening.rl_management.HappeningManager;
import inBloom.storyworld.Happening;

/**
 * A class implementing everything necessary for executing SARSA(lambda) on the Storyworld
 * 
 * @author Julia Wippermann
 * @version 19.1.20
 *
 */
public class DEPRECATEDRLAlgorithm {
	
	private HashMap<Integer, Happening> policy;
	private HashMap<Integer, Integer> utilityFunction;
	private HashBasedTable<Integer, Happening, Integer> transitionModel;
	
	
	private LinkedList<Happening<?>> allHappenings;
	
	/* Rows: States (Integer)
	 * Columns: Actions (Happenings)
	 * Values: Q-Values (State-Action-Utility)*/
	private HashBasedTable<Integer, Happening, Integer> qValues;
	/* Rows: States (Integer)
	 * Columns: Actions (Happenings)
	 * Values: Frequencies (How often this State-Action-Pair has occured so far)*/
	private HashBasedTable<Integer, Happening, Integer> frequencies;
	private int previousState;
	private Happening previousAction;
	private int previousReward;
	
	private final int MAX_NUMBER_OF_STEPS = 40;
	private final int NUMBER_OF_HAPPENINGS = 7;
	
	public DEPRECATEDRLAlgorithm() {
		
		this.policy = new HashMap<Integer, Happening>();
		this.utilityFunction = new HashMap<Integer, Integer>();
		this.transitionModel = HashBasedTable.create(); // TODO possible already define rows and columns
		this.transitionModel = HashBasedTable.create(MAX_NUMBER_OF_STEPS, NUMBER_OF_HAPPENINGS);
		
		this.setInitialPolicy();
		this.setInitialUtilityFunction();
		this.setInitialTransitionModel();
		
		
		// TODO possibel problem: allhappenings are onyl initialized after HappeningManager.scheduleHappenings has been called
		// -> may change in the future since we won't really schedule Happenings anymore?
		this.allHappenings = HappeningManager.getAllHappenings();
		
		this.qValues = HashBasedTable.create();
		this.frequencies = HashBasedTable.create();
				
		this.previousState = 0;
		this.previousAction = null;
		this.previousReward = 0;
		
	}
	
	// TODO how do we find out if we are in an end state -> see RL_Cycle
	// TODO how do we get the Tellability in the end? Where is it saved? -> Tellability or similar?
	// TODO how do we get currentReward at all?
	public void q_algorithm(int currentState, int currentReward) {
		
		this.initializeQValues(currentState);
		
		if(isEnd(currentState)) {
			this.qValues.put(currentState, null, currentReward);
		}
		
		if(currentState != 0) {
			increaseFrequency(this.previousState, this.previousAction);
			// update Q-Value
			this.previousState = currentState;
			this.previousAction = chooseNewAction(currentState); // TODO which action shall we choose now?
			this.previousReward = currentReward;
		}
	}
	
	private boolean isEnd(int currentState) {
		return false;
	}
	
	private void increaseFrequency(int state, Happening action) {
		if(this.frequencies.contains(state, action)) {
			int frequency = this.frequencies.get(state, action);
			this.frequencies.put(state, action, frequency+1);
		} else {
			this.frequencies.put(state, action, 1);
		}
	}
	
	private void updateQValue(int newValue) {
		return;
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
	private Happening chooseNewAction(int currentState) {
		// TODO WTF is f?!?
		// So far: Act like f chooses the action with the maximum state-action-utility -> TODO add probability
		// TODO check out lecture? MoAI?
		
		// f: 50% choose Happening with biggest utility
		//    50% choose Happening randomly
		
		boolean chooseRandomly = false;
		int randomHappeningIndex = 0;
		
		if(Math.random() >= 0.5) {
			chooseRandomly = true;
			int numberOfHappenings = this.allHappenings.size();
			randomHappeningIndex = new Random().nextInt(numberOfHappenings);
		}
		
		// perform a maximum value search for the Happening with the highest qValue
		Happening bestHappening = this.allHappenings.getFirst();
		int bestQValue = 0; // the value of the first Happening will be calculated and added to bestQValue during the for-loop
							// anyways, so calculating the actual value here would be wasted Runtime.
		int indexOfHappening = 0;
		
		// Go through all Happenings and find the one with the highest q-Value or the one we chose randomly
		for(Happening happening: this.allHappenings) {
			
			// find the one we chose randomly
			if(chooseRandomly) {
				if(indexOfHappening == randomHappeningIndex) {
					bestHappening = happening;
					// TODO increase indexOfHappening?
					break;
				}
			}

			
			// find the one with the highest q-value with maximum value search
			else {
				if(this.qValues.contains(currentState, happening)) {
					// get the qValue of this Happening
					int qValue = this.qValues.get(currentState, happening);
					if(qValue > bestQValue) {
						bestQValue = qValue;
						bestHappening = happening;
					}

				} else {
					throw new NullPointerException("No mapping from State to Happening when choosing new Action. "
							+ "Q-Value hasn't been initialized yet.");
				}
			}
		}
		
		return bestHappening;
	}
	
	
	
	
	
	private void initializeQValues(int currentState) {
		if(!this.qValues.containsRow(currentState)) {
			for(Happening happening: allHappenings) {
				this.qValues.put(currentState, happening, 0);
			}
		}
		// else do nothing, because it is already initialized
	}
	
	
	
	
	
	/**
	 * METHODS FOR THE POLICY
	 */
	public void setInitialPolicy() {
		for(int i = 0; i < MAX_NUMBER_OF_STEPS; i++) {
			// TODO what should happen here? WHAT SHOULD WE DO HOW DOES ALGRPITHM INITIATE OR DO OR CHANGE STUFF
			this.policy.put(i, null);
		}
	}
	
	public Happening executePolicy(int step) {
		return this.policy.get(step);
	}
	
	public void updatePolicy(HashMap<Integer, Happening> newPolicy) {
		this.policy = newPolicy;
	}
	
	/**
	 * Maps the given happening to the given step. If there already is a mapping for the step,
	 * it will be replaced by the new one. If not, a new mapping will be created.
	 * 
	 * @param step
	 * 			The step for which the Happening is set to happen
	 * @param happening
	 * 			The Happening that is set to happen at the given step
	 */
	public void updatePolicy(int step, Happening happening) {
		this.policy.put(step, happening);
	}
	
	public HashMap<Integer, Happening> getPolicy() {
		// TODO evtl. this.policy.clone()
		return this.policy;
	}

	
	
	/**
	 * METHODS FOR UTILITY FUNCTION
	 */
	
	public void setInitialUtilityFunction() {
		
	}
	
	
	
	/**
	 * METHODS FOR TRANSITION MODEL
	 */
	
	public void setInitialTransitionModel() {
		
	}
}
