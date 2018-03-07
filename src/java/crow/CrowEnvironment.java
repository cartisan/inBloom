package crow;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
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
		
		if (action.getFunctor().equals("askForCheese")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().askForCheese(agent, patient); 
		}

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
		
		if (action.getFunctor().equals("answerNegatively")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().answerNegatively(agent, patient); 
		}

		
		if (action.getFunctor().equals("pickUpCheese")) {
			result = getModel().pickUpCheese(agent);
		}

		if (action.getFunctor().equals("sing")) {
			result = getModel().sing(agent);
		}
		
		if (action.getFunctor().equals("share")) {
    		String item = action.getTerm(0).toString();
    		Term receiverTerm = action.getTerm(1);
    		
    		if (receiverTerm.isList()) {
    			List<StoryworldAgent> receivers = new LinkedList<>();
    			for (Term rec: (ListTermImpl) receiverTerm) {
        			receivers.add(getModel().getAgent(rec.toString()));
    			}
    			result = agent.share(item, receivers);
    		} else {
    			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
    			result = agent.share(item, patient);
    			
    		}
    	}
		
		pauseOnRepeat(agentName, action);
		return result;
		
		
	}
}
