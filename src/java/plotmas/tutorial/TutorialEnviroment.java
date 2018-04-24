package plotmas.tutorial;

import java.util.List;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;

public class TutorialEnviroment extends PlotEnvironment<TutorialModel> {

    @Override
    public void initialize(List<LauncherAgent> agents) {
      super.initialize(agents);
        TutorialModel model = new TutorialModel(agents, this);
        this.setModel(model);
    }
	
    @Override
    public boolean executeAction(String agentName, Structure action) {
		boolean result = super.executeAction(agentName, action);
		
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