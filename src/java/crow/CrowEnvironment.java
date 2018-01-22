package crow;

import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.little_red_hen.FarmEnvironment;
import plotmas.storyworld.StoryworldAgent;

public class CrowEnvironment extends PlotEnvironment<CrowModel> {

	static Logger logger = Logger.getLogger(CrowEnvironment.class.getName());
    @Override
    public void initialize(List<LauncherAgent> agents) {
      super.initialize(agents);
        CrowModel model = new CrowModel(agents, this);
        this.setModel(model);
      
      
    }
	
    @Override
    protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);
    	
    }
    
    
    @Override
    public boolean executeAction(String agentName, Structure action) {
      boolean result = super.executeAction(agentName, action);
      StoryworldAgent agent = getModel().getAgent(agentName);

     if (action.getFunctor().equals("walkAround")) {
    	 result = getModel().walkAround(agent);
     }
     
     if (action.getFunctor().equals("sitAround")) {
    	 result = getModel().sitAround(agent);
     }
     
     if (action.getFunctor().equals("flatter")) {
    	 Term receiverTerm = action.getTerm(0);
    	 
    	 StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
    	 result = getModel().flatter(agent, patient); 
     }
     
     if (action.getFunctor().equals("pickUpCheese")) {
    	 result = getModel().pickUpCheese(agent);
     }
     
     if (action.getFunctor().equals("sing")) {
    	 result = getModel().sing(agent);
     }
     pauseOnRepeat(agentName, action);
      return result;
    }
}
