/**
 *
 */
package inBloom.juwistest;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 *
 */
public class HogwartsEnvironment extends PlotEnvironment<HogwartsModel>{

	static Logger logger = Logger.getLogger(HogwartsEnvironment.class.getName());

	@Override
    public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);

		// Add special initialization of agent percepts for custom environment here.
		// Left blank intentionally.
	}
	
	/**protected void updateStatePercepts(String agentName) {
		super.updateStatePercepts(agentName);

		// Here general percepts (for all agents) can be updated that are
		// based on the model?
		if(getModel().burning) {
			// if it burning: add that to the belief base
			removePerceptsByUnif(agentName, Literal.parseLiteral("is_okay"));
			addPercept(agentName, Literal.parseLiteral("burning"));
		} else {
			// if it is not burning: remove any possible burning-belief
			//Collection<Literal> currentPercepts = getPercepts(agentName);
			// if currentPercepts contains burning, then delete it and give me EMOTIONS
			// else nothing changes
			/*if(currentPercepts.contains(Literal.parseLiteral("burning"))) {
				System.out.println("I found the previously burning stuff!");
			} else {
				System.out.println("Didn't find anything burning :(");
			}*/
	

			/**if(getModel().world_was_saved) {
				removePerceptsByUnif(agentName, Literal.parseLiteral("burning"));
				//addEventPerception(agentName, "saved", new PerceptAnnotation("relief"));
				addPercept(agentName, Literal.parseLiteral("been_saved"));
			}
		}
	}*/

	protected ActionReport doExecuteAction(String agentName, Structure action) {

		ActionReport result = null;
		Character agent = getModel().getCharacter(agentName);

		if (action.getFunctor().equals("burn")) {
    		result = getModel().burn(agent);
    	}

		else if (action.getFunctor().equals("run")) {
			result = getModel().run(agent);
		}

		else if (action.getFunctor().equals("scream")) {
			result = getModel().scream(agent);
		}

		else if (action.getFunctor().equals("smile")) {
			result = getModel().smile(agent);
		}

		else if (action.getFunctor().equals("extinguishFire")) {
			result = getModel().extinguishFire(agent);
		}

		/**else {
			logger.info("Unknown action " + action + " performed.");
			return false;
		}*/
		
		// String functor = action.getFunctor();
		
		return result;
	}

}
