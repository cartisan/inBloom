package plotmas.test.story;

import jason.asSyntax.Structure;
import plotmas.PlotEnvironment;
import plotmas.storyworld.Character;

public class TestEnvironment extends PlotEnvironment<TestModel> {
	
	@Override
    public boolean doExecuteAction(String agentName, Structure action) {
		// let the PlotEnvironment update the plot graph, initializes result as false
		boolean result = false;
    	Character agent = getModel().getCharacter(agentName);
    	synchronized(getModel()) {
	    	if (action.getFunctor().equals("do_stuff")) {
	    		result = getModel().doStuff(agent);
	    	}
	    	
	    	if(action.getFunctor().equals("search")) {
	    		result = getModel().search(agent, action.getTerm(0).toString());
	    	}
    	}

    	return result;
    }
}
