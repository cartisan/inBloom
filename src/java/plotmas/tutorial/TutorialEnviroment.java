package plotmas.tutorial;

import java.util.List;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import plotmas.LauncherAgent;
import plotmas.PlotEnvironment;

public class TutorialEnviroment extends PlotEnvironment<TutorialModel> {

    @Override
    public void initialize(List<LauncherAgent> agents) {
      super.initialize(agents);
      this.model.position = 0;
    }
	
    @Override
    protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);
    	
    	int pos = model.position;
    	removePerceptsByUnif(agentName, Literal.parseLiteral("position(X)"));
    	addPercept(agentName, Literal.parseLiteral("position(" + String.valueOf(pos) + ")" ));
    	}
    	
    
    
    @Override
    public boolean executeAction(String agentName, Structure action) {
      boolean result = super.executeAction(agentName, action);

      // check which action is executed by agent
      
      
      if (action.getFunctor().equals("clean")) {
    	  this. model.suck();
    	  
    	  result = true;
      }
      
      if (action.getFunctor().equals("up")) {
    	this. model.up();
    	  
    	  result = true;
      }
      
      if (action.getFunctor().equals("down")) {
    	  this.model.down();
    	  
    	  result = true;
      }
      
      if (action.getFunctor().equals("right")) {
    	  this.model.right();
    	  
    	  result = true;
      }
      
      if (action.getFunctor().equals("left")) {
    	this. model.left();
    	  
    	  result = true;
      }

      return result;
    }
}
