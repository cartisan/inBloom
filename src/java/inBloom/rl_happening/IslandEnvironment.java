/**
 * 
 */
package inBloom.rl_happening;

import java.util.logging.Logger;

import inBloom.ActionReport;
import inBloom.PlotEnvironment;
import inBloom.juwistest.HogwartsEnvironment;
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
		
		return result;
	}
	
	private int currentStep = 0;
	
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
	}
	
}
