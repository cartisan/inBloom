/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.HashBasedTable;

import inBloom.storyworld.Happening;

/**
 * A class implementing everything necessary for executing SARSA(lambda) on the Storyworld
 * 
 * @author Julia Wippermann
 * @version 19.1.20
 *
 */
public class SarsaLambda {
	
	//private List<Integer> weights;
	private List<Integer> eligibilityTraces;
	private HashMap<String, Integer> weights;
	/* Rows: States (Integer)
	 * Columns: Actions (Happenings)
	 * Values: Q-Values (State-Action-Utility)*/
	private HashBasedTable<Integer, Happening, Integer> qValues;
	
	private LinkedList<Happening> allHappenings;
	

	
	public SarsaLambda() {

		// TODO possibel problem: allhappenings are onyl initialized after HappeningManager.scheduleHappenings has been called
		// -> may change in the future since we won't really schedule Happenings anymore?
		this.allHappenings = HappeningManager.getAllHappenings();
		this.eligibilityTraces = new LinkedList<Integer>();
		
		this.initializeWeights(); // arbitrarily
		this.initiailizeEligibilityTraces(); // with 0
		
	}
	
	// TODO how do we find out if we are in an end state -> see RL_Cycle
	// TODO how do we get the Tellability in the end? Where is it saved? -> Tellability or similar?
	// TODO how do we get currentReward at all?
	public void sarsa_lambda_episode(int currentState, int currentReward) {
		
		// get initial state of the episode
		
		// Iterate over all possible actions
		for(Happening happening: allHappenings) {
			
		}
	}
	
	private boolean isEnd(int currentState) {
		return false;
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

	
	
	
	/**
	 * INITIALIZING STUFF
	 */
	
	private void initializeWeights() {
		// TODO initialize weights randomly
	}
	
	private void initiailizeEligibilityTraces() {
		// TODO initialize eligibilityTraces with 0
	}
	
	
}
