package plotmas.stories.oedipus;

import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.StoryworldAgent;

public class OedipusEnvironment extends PlotEnvironment<OedipusModel> {

	static Logger logger = Logger.getLogger(OedipusEnvironment.class.getName());
	@Override
	public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);
		OedipusModel model = new OedipusModel(agents, this);
		this.setModel(model);
		this.model.location = null; 


	}

		
	@Override
	protected void updateStatePercepts(String agentName) {
	   super.updateStatePercepts(agentName);
	    	
	   String pos = model.location;
	   removePerceptsByUnif(agentName, Literal.parseLiteral("position(X)"));
	   addPercept(agentName, Literal.parseLiteral("position(" + String.valueOf(pos) + ")" ));
	   }
	
	public boolean executeAction(String agentName, Structure action) {
		boolean result = super.executeAction(agentName, action);
		StoryworldAgent agent = getModel().getAgent(agentName);
		
		if (action.getFunctor().equals("chilling")) {
			result = getModel().chilling(agent);
		}
		
		if (action.getFunctor().equals("working")) {
			result = getModel().working(agent);
		}
				
		if (action.getFunctor().equals("answer_question")) {
			Term receiverTerm = action.getTerm(0);
		
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().answer_question(agent, patient);
		}
			
		if (action.getFunctor().equals("ask")) {
			Term receiverTerm = action.getTerm(0);
			
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().ask(agent, patient);
		}
		
		if (action.getFunctor().equals("getChild")) {
			result = getModel().getChild(agent);
		}
		
		if (action.getFunctor().equals("giveChildTo")) {
			Term receiverTerm = action.getTerm(0);
			
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().giveChildTo(agent, patient);
			
		}
		
		if (action.getFunctor().equals("adopt")) {
			Term receiverTerm = action.getTerm(0);
			
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().adopt(agent, patient);
			
		}
		
		
		pauseOnRepeat(agentName, action); 
		return result;
		
		
	}
}
