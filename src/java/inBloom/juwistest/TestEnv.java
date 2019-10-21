/**
 * 
 */
package inBloom.juwistest;

import jason.asSyntax.*;
import jason.environment.*;
//import plotmas.storyworld.Character;	// -> would be necessary for perceiving the agent
										// who performed the action, because we need the type
										// Character for it, but we also need the class Model
										// for that -> so it only works in plotmas anyways

import java.util.logging.*;

/**
 * @author juwi
 *
 */
// Environment here is jason.environment.Environment
// Leonids Environment classes also extend the jason Environment we use here,
// he just has a few steps / classes in between:
// Leonid: FarmEnvironment -> PlotEnvironment -> TimeSteppedEnvironment -> Environment
// We:	   TestEnv														-> Environment
public class TestEnv extends Environment {

	// creates the link to the mas2j File? No.
	// Leonid didn't have the mas2j file name in here.
	// He only had FarmEnvironment.getClass() as an argument. So we somehow take the name of this class and wrap a logger around it.
	// Java API: This is just for logging info -> output?
	private Logger logger = Logger.getLogger("testenv.mas2j." + TestEnv.class.getName());
	
	/** Called before the MAS execution with the args informed in .mas2j */
	// Is this a main -> because of the args?
	// This does not exist in the FarmEnvironment
	@Override
	public void init(String[] args) {
		// Nothing happens in here
	}
	
	// Leonid doesn't use this method anymore in the FarmEnvironment.
	// In PlotEnvironment he implements both, this executeAction
	// and doExecuteAction, which is the one that he still uses in FarmEnvironment.
	// It is said in the documentation of doExecuteAction in PlotEnvironment,
	// that doExecuteAction needs to be overridden by the subclass of the implemented
	// project in order to communicate with the model.
	// PlotEnvironment's executeAction also calls its own doExecuteAction (that will
	// be overrided by us in our project) -> that's how it works together
	/**
	 * If an asl-agent attempts to execute an environment action, the method
	 * executeAction is called (in PlotEnvironment -> calls this method, so
	 * broadly equivalent for us right now).
	 * If Action burn is performed, the new perecept fire becomes available
	 * to ALL agents
	 */
	@Override
	public boolean executeAction(String agName, Structure action) {
		// we find out which action should be performed. This is where we
		// LINK TO THE AGENT!!!
		// -> long else if - statements, similar to switch case over the getFunctor
		if(action.getFunctor().equals("burn")) {
			// add a percept TO ALL AGENTS -> that is a method in jason.environment.Environment
			addPercept(Literal.parseLiteral("fire"));
			// yes queen, we performed the action!
			return true;
		} else {
			// unknown action
			// logger.info adds this to the Logger
			logger.info("executing: " + action + ", but not implemented!");
			return false;
		}
		
		// Leonid does not have the else-case in his doExecuteAction
		// but he also does the bodies of the if-elses different:
		// he calls the model with the perceived action and the
		// perceived agent.
		
		// Find character:
		// Character agent = getModel().getCharacter(agentName);
		
		// Perform action:
		// boolean result = getModel().actionName(agent);
	
		// getModel() is implemented in PlotEnvironment and returns th
		// protected variable model of the Type ModType (ModType extends PlotModel)
		// This Model can be added via a setModel() (only? -> not in init or initialise)
	}
	
	
	/** Called before the end of MAS execution */
	// This is implemented (also with super and one other command)
	// in TimeSteppedEnvironment. In all the subclasses it's not
	// explicitely overridden again.
	@Override
	public void stop() {
		super.stop();
	}
	

}
