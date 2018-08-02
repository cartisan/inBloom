package plotmas.test.story;

import java.util.List;

import jason.asSyntax.Structure;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.StoryworldAgent;

public class TestEnvironment extends PlotEnvironment<TestModel> {
	
	@Override
    public void initialize(List<LauncherAgent> agents) {
    	super.initialize(agents);
        TestModel model = new TestModel(agents, this);
        this.setModel(model);
    }
	
	@Override
    public boolean executeAction(String agentName, Structure action) {
		// let the PlotEnvironment update the plot graph, initializes result as false
		boolean result = super.executeAction(agentName, action);
    	StoryworldAgent agent = getModel().getAgent(agentName);
    	synchronized(getModel()) {
	    	if (action.getFunctor().equals("do_stuff")) {
	    		result = getModel().doStuff(agent);
	    	}
	    	
	    	if(action.getFunctor().equals("search")) {
	    		result = getModel().search(agent, action.getTerm(0).toString());
	    	}
    	}
    	
    	pauseOnRepeat(agentName, action);
    	return result;
    }
}
