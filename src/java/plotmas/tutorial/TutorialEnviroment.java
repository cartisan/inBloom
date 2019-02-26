package plotmas.tutorial;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import plotmas.PlotEnvironment;

public class TutorialEnviroment extends PlotEnvironment<TutorialModel> {

    @Override
	protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);
    	
    	// update publicly known wheat state
		removePerceptsByUnif(Literal.parseLiteral("dirty"));
		removePerceptsByUnif(Literal.parseLiteral("clean"));
		removePerceptsByUnif(Literal.parseLiteral("pos(X)"));
		addPercept(Literal.parseLiteral("pos("+ getModel().position + ")"));
		if (getModel().dirty[getModel().position] == true) {
			addPercept(Literal.parseLiteral("dirty"));
		}
		else addPercept(Literal.parseLiteral("clean"));
    }
	
    @Override
	public boolean doExecuteAction(String agentName, Structure action) {
    	boolean result = false;
    
		// check which action is executed by agent       
		if (action.getFunctor().equals("suck")){
			result = getModel().suck();
			// create a perception of the result, which the agent receives
//			addPercept(Literal.parseLiteral(String.format("clean")));
			// indicate that action was successful
			result = true;		
      }
		
		if (action.getFunctor().equals("left")) {	
			result = getModel().left();
			// create a perception of the result, which the agent receives
//			addPercept( Literal.parseLiteral(String.format("pos("+ getModel().position + ")")));
			// indicate that action was successful
			result = true;		
      }
		
		if (action.getFunctor().equals("right")) {
			result = getModel().right();
			// create a perception of the result, which the agent receives
//			addPercept( Literal.parseLiteral(String.format("pos("+ getModel().position + ")")));
			// indicate that action was successful
			result = true;		
      }
		
		if (action.getFunctor().equals("up")) {
			result = getModel().up();
			// create a perception of the result, which the agent receives
//			addPercept( Literal.parseLiteral(String.format("pos("+ getModel().position + ")")));
			// indicate that action was successful
			result = true;	
      }
		
		if (action.getFunctor().equals("down")) {
			result = getModel().down();
			// create a perception of the result, which the agent receives
//			addPercept( Literal.parseLiteral(String.format("pos("+ getModel().position + ")")));
			// indicate that action was successful
			result = true;	
			
      }
    	return result;
    }

}
