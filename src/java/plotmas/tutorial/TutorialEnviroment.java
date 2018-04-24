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
    protected void updateStatePercepts(String agentName) {
    	}
    	
    
    
    @Override
    public boolean executeAction(String agentName, Structure action) {
      boolean result = super.executeAction(agentName, action);

      // check which action is executed by agent
  
      return result;
    }
}
