/**
 *
 */
package inBloom.rl_happening.islandWorld;

import java.util.logging.Logger;

import jason.asSyntax.Structure;

import inBloom.ActionReport;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 * @version 29.10.19
 *
 * The Environment defin
 */
public class IslandEnvironment extends PlotEnvironment<IslandModel> {

	static Logger logger = Logger.getLogger(IslandEnvironment.class.getName());

	protected ActionReport doExecuteAction(String agentName, Structure action) {

		ActionReport result = null;
		Character agent = this.getModel().getCharacter(agentName);

		if(action.getFunctor().equals("goOnCruise")) {
			result = this.getModel().goOnCruise(agent);
		}

		else if(action.getFunctor().equals("stayHome")) {
			result = this.getModel().stayHome(agent);
		}

		else if(action.getFunctor().equals("swimToIsland")) {
			result = this.getModel().swimToIsland(agent);
		}

		else if(action.getFunctor().equals("happyEnd")) {
			result = this.getModel().happyEnd(agent);
		}

		else if(action.getFunctor().equals("goOnShip")) {
			result = this.getModel().goOnShip(agent);
		}

		else if(action.getFunctor().equals("findFriend")) {
			result = this.getModel().findFriend(agent);
		}

		else if(action.getFunctor().equals("complain")) {
			result = this.getModel().complain(agent);
		}

		else if(action.getFunctor().equals("getFood")) {
			result = this.getModel().getFood(agent);
		}

		else if(action.getFunctor().equals("eat")) {
			result = this.getModel().eat(agent);
		}

		else if(action.getFunctor().equals("findHealingPlants")) {
			result = this.getModel().findHealingPlants(agent);
		}

		else if(action.getFunctor().equals("useHealingPlants")) {
			result = this.getModel().useHealingPlants(agent);
		}

		else if(action.getFunctor().equals("sleep")) {
			result = this.getModel().sleep(agent);
		}

		else if(action.getFunctor().equals("buildHut")) {
			result = this.getModel().buildHut(agent);
		}

		else if(action.getFunctor().equals("extinguishFire")) {
			result = this.getModel().extinguishFire(agent);
		}

		// TODO: idea: implement the functors as enums to iterate over
		// -> more control in default? -> looks nicer, though not that much less code

		// String function = action.getFunctor();

		return result;
	}

	private void timedActivities() {
		// Only increase hunger if the simulation has started and a new time step has started
		if(this.getModel() != null && this.currentStep != this.step) {

			// to make sure we don't increase mutliple times in 1 time step
			this.currentStep = this.step;

			// Agent specific timed activities
			for(Character agent: this.getModel().getCharacters()) {
				this.getModel().increaseHunger(agent);
				this.getModel().increaseFatigue(agent);
				if(agent.isSick) {
					this.getModel().increasePoison(agent);
				}
			}

			// Island specific timed activities
			if(!this.getModel().island.hasHealingPlants()) {
				this.getModel().island.growPlants();
			}
			if(this.getModel().island.isRaining()) {
				this.getModel().island.continueRain();
			}
		}
	}

	/*protected synchronized void stepStarted(int step) {
		super.stepStarted(step);
		addStateValue();
	}*/

	@Override
	protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
		super.stepFinished(step, elapsedTime, byTimeout);

		this.timedActivities();
	}

//	private void addStateValue() {
//
//		if(this.stateValues == null) {
//			System.out.println("hashCodes is null");
//			return;
//		}
//
//		if(model == null) {
//			System.out.println("model is null");
//			return;
//		}
//
//		// Add the current state value as calculated in PlotModel.getStateValue()
//		this.stateValues.put(this.currentStep, this.model.getStateValue());
//
//		// If we reached the end of the story, we will stop and print our state values
//		// TODO not hard code the end
//		// CHECK outsource the printing
//		// TODO outsource these things in PlotEnvironment?
//		if(this.determineIfStoryHasEnded() && this.printAllStateValuesAtEnd) {
//			printAllStateValues();
//		}
//
//	}

	public boolean shouldPrintAllResults() {
		return this.currentStep==34;
	}

//	private void printAllStateValues() {
//		for(Integer i: this.stateValues.keySet()) {
//			System.out.println("Step " + i + ": ");
//		}
//
//		for(Integer i: this.stateValues.keySet()) {
//			//System.out.println("Step " + i + ": " + this.hashCodes.get(i));
//			if(this.stateValues.get(i) == null) {
//				System.out.println("0");
//			} else {
//				System.out.println(this.stateValues.get(i));
//			}
//		}
//	}

	/*private void printDetailedStateValue() {
		if(this.detailedHashCodes == null || model == null) {
			return;
		}

		this.detailedHashCodes.put(this.currentStep, this.model.getDetailedState());

		if(this.currentStep==34) {
			for(Integer i: this.detailedHashCodes.keySet()) {

				//System.out.println("Step " + i);

				HashMap<String, Object> currentValues = this.detailedHashCodes.get(i);

				for(String feature: currentValues.keySet()) {
					// System.out.println("Step " + i + ": " + feature + ": " + this.detailedHashCodes.get(i));
					//if(feature.equals("Mood")) {
					System.out.println("Step " + i + ": " + feature + ": " + currentValues.get(feature));
					//}
				}

			}
		}
	}*/

}
