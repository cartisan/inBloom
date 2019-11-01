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
	
	static Logger logger = Logger.getLogger(HogwartsEnvironment.class.getName());
	
	// updateStatePercepts? -> gibt es in FarmEnvironment nicht mehr
	// stattdessen initialize?

	protected ActionReport doExecuteAction(String agentName, Structure action) {
		
		ActionReport result = null;
		Character agent = getModel().getCharacter(agentName);
		
		if(action.getFunctor().equals("goOnCruise")) {
			result = getModel().goOnCruise(agent);
		}
		
		else if(action.getFunctor().equals("doNothing")) {
			result = getModel().doNothing(agent);
		}
		
		return result;
	}
}
