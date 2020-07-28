package inBloom.juwistest;

import java.util.List;
import java.util.logging.Logger;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Location;
import jason.asSyntax.Literal;
import inBloom.storyworld.Character;

/**
* @author Julia Wippermann
*
*/
public class HogwartsModel extends PlotModel<HogwartsEnvironment> {

	/**
	 * GLOBAL VARIABLES
	 */
	public boolean burning;
	public boolean world_was_saved;	// requires that the world was previously endangered
	
	public Hogwarts hogwarts = new Hogwarts();
	
	

	/**
	 * CONSTRUCTOR
	 */

	public HogwartsModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		
		this.addLocation(this.hogwarts);

		// here we could define our variables
		this.burning = false;
		this.world_was_saved = false;
	}



	/**
	 * ACTION METHODS
	 */

	public ActionReport burn(Character agent) {
		ActionReport result = new ActionReport();
		
		logger.info("Something is burning.");
		//this.environment.addEventPerception(agent.name, "burn()", new PerceptAnnotation("distress", "fear"));
		//this.environment.addEventPerception(agent.name, "burn", new PerceptAnnotation("pride"));
		// COMMENT: Durch Leonid's Änderung, jetzt keine environment.addEventPerception mehr möglich,
		// dafür aber (vermutlich) ActionReport.addPerception -> das aber ohne "EventName"
		//result.addPerception(agent.name, new PerceptAnnotation("pride"));
		
		this.environment.addEventPercept(agent.name, "burn", new PerceptAnnotation("pride"));
		
		//TODO HERE
		this.environment.removePerceptsByUnif(this.hogwarts, Literal.parseLiteral("is_okay"));
		//this.environment.addPercept(agentName, Literal.parseLiteral("burning"));
		this.environment.addPercept(this.hogwarts, Literal.parseLiteral("burning"));
		
		
		this.burning = true;
		result.success = true;
		return result;
	}

	public ActionReport run(Character agent) {
		logger.info(agent.name + " is running.");
		return new ActionReport(true);
	}

	public ActionReport scream(Character agent) {
		logger.info(agent.name + " is screaming.");
		return new ActionReport(true);
	}

	public ActionReport smile(Character agent) {
		logger.info(agent.name + " is smiling.");
		return new ActionReport(true);
	}

	public ActionReport extinguishFire(Character agent) {
		ActionReport result = new ActionReport();
		if(burning) {
			logger.info(agent.name + " extinguishes the fire.");
			
			
			this.environment.addEventPercept(agent.name, "extinguish_fire", new PerceptAnnotation("pride"));
			// COMMENT addEventPerception in ActionReport verlegt
			//result.addPerception(agent.name, new PerceptAnnotation("pride"));
			
			this.burning = false;
			this.world_was_saved = true;
			result.success = true;
			return result;
		} else {
			logger.info(agent.name + " fails. 404 Fire not found.");
			result.success = false;
			return result;
		}
	}
	
	
	public static class Hogwarts extends Location {
		
		public Hogwarts() {
			super("hogwarts");
		}
		
		private boolean enter(Character character, List<Character> loc) {
			if(null == character.location) {
				// no prev location, just set this one
				loc.add(character);
				this.characterLocationUpdate(character);
				return true;
			} else if(character.location.leave(character)) {
				// prev location was present, character left it successfully
				loc.add(character);
				this.characterLocationUpdate(character);
				return true;
			} else {
				// prev location was present, character couldn't leave
				return false;
			}
		}
		
	}

}