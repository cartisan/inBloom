package inBloom.tutorial;

import inBloom.PlotEnvironment;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

public class TutorialEnviroment extends PlotEnvironment<TutorialModel> {

    @Override
    public boolean doExecuteAction(String agentName, Structure action) {
    	boolean result = false;
    	
		// check which action is executed by agent
		if (action.getFunctor().equals("add")) {
			// parse the term values passed by agent, pass them on to model
			int sum = this.model.add(Integer.valueOf( action.getTerm(0).toString() ),
									 Integer.valueOf( action.getTerm(1).toString() ));
			
			// create a perception of the result, which the agent receives
			addPercept(agentName, Literal.parseLiteral(String.format("sum(%d)", sum)));
			
			// indicate that action was successful
			result = true;
      }

    	return result;
    }
}
