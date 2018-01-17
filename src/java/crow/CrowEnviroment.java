package crow;

import java.util.List;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;

public class CrowEnviroment extends PlotEnvironment<CrowModel> {

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

      // check which action is executed by agent
  
      return result;
    }
}
