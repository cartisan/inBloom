/**
 * 
 */
package inBloom.rl_happening.islandWorld;

import java.util.HashMap;
import java.util.logging.Logger;

import inBloom.ActionReport;
import inBloom.PlotEnvironment;
import inBloom.rl_happening.rl_management.RLEnvironment;
import inBloom.storyworld.Character;
import jason.asSyntax.Structure;

/**
 * @author Julia Wippermann
 * @version 7.8.20
 *
 * The Environment defin
 */
public class IslandEnvironment extends RLEnvironment<IslandModel> {
	
	static Logger logger = Logger.getLogger(IslandEnvironment.class.getName());	
	
	
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
		
		
		return result;
	}
	
	private void increaseHunger() {
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
	
	
	@Override
	protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
		super.stepFinished(step, elapsedTime, byTimeout);
		
		increaseHunger();
	}


	
}
