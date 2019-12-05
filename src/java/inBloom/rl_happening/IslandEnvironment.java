/**
 * 
 */
package inBloom.rl_happening;

import java.util.HashMap;
import java.util.logging.Logger;

import inBloom.ActionReport;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;
import jason.asSyntax.Structure;

/**
 * @author Julia Wippermann
 * @version 29.10.19
 *
 * The Environment defin
 */
public class IslandEnvironment extends PlotEnvironment<IslandModel> {
	
	static Logger logger = Logger.getLogger(IslandEnvironment.class.getName());
	private int currentStep = 0;
	
	private HashMap<Integer, Integer> hashCodes = new HashMap<Integer, Integer>();
	
	// updateStatePercepts? -> gibt es in FarmEnvironment nicht mehr
	// stattdessen initialize?

	protected ActionReport doExecuteAction(String agentName, Structure action) {
		
		ActionReport result = null;
		Character agent = getModel().getCharacter(agentName);
		
		if(action.getFunctor().equals("goOnCruise")) {
			result = getModel().goOnCruise(agent);
		}
		
		else if(action.getFunctor().equals("stayHome")) {
			result = getModel().stayHome(agent);
		}
		
		else if(action.getFunctor().equals("findFriend")) {
			result = getModel().findFriend(agent);
		}
		
		else if(action.getFunctor().equals("getFood")) {
			result = getModel().getFood(agent);
		}
		
		else if(action.getFunctor().equals("eat")) {
			result = getModel().eat(agent);
		}
		
		else if(action.getFunctor().equals("sleep")) {
			result = getModel().sleep(agent);
		}
		
		else if(action.getFunctor().equals("buildHut")) {
			result = getModel().buildHut(agent);
		}
		
		else if(action.getFunctor().equals("complain")) {
			result = getModel().complain(agent);
		}
		
		else if(action.getFunctor().equals("extinguishFire")) {
			result = getModel().extinguishFire(agent);
		}
		
		// TODO: idea: implement the functors as enums to iterate over
		// -> more control in default? -> looks nicer, though not that much less code
		
		// String function = action.getFunctor();
		
		return result;
	}
	
	@Override
	protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
		super.stepFinished(step, elapsedTime, byTimeout);
		
		// Only increase hunger if the simulation has started and a new time step has started
		if(getModel() != null && currentStep != this.step) {
			
			// to make sure we don't increase mutliple times in 1 time step
			currentStep = this.step;
			
			// for each agent hunger is increased
			for(Character agent: this.getModel().getCharacters()) {
				getModel().increaseHunger(agent);
			}
		}
		
		this.hashCodes.put(this.currentStep, this.model.getState());
		
		if(this.currentStep==35) {
			for(Integer i: this.hashCodes.keySet()) {
				System.out.println("Step " + i + ": " + this.hashCodes.get(i));
			}
		}
		
		System.out.println("\n--------------------------- STEP " + this.currentStep + " ---------------------------");
	}
	
}
