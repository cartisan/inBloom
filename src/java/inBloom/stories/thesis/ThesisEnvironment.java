package inBloom.stories.thesis;

import jason.asSyntax.Structure;

import inBloom.ActionReport;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;

public class ThesisEnvironment extends PlotEnvironment<ThesisModel> {

	@Override
    public ActionReport doExecuteAction(String agentName, Structure action) {
		// let the PlotEnvironment update the plot graph, initializes result as false
		ActionReport result = null;
    	Character agent = this.getModel().getCharacter(agentName);

    	synchronized(this.getModel()) {
	    	if(action.getFunctor().equals("get")) {
	    		result = this.getModel().getDrink(agent);
	    	}

	    	if(action.getFunctor().equals("wipe")) {
	    		result =  new ActionReport(true);
	    	}
    	}

    	return result;
    }
}
