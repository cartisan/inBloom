package oedipus;

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
import plotmas.tutorial.TutorialModel;

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


	@Override
	public boolean executeAction(String agentName, Structure action) {
		boolean result = super.executeAction(agentName, action);
		StoryworldAgent agent = getModel().getAgent(agentName);
		
		if (action.getFunctor().equals("chilling")) {
			result = getModel().chilling(agent);
		}
		
		if (action.getFunctor().equals("reigning")) {
			result = getModel().reigning(agent);
		}
		
		if (action.getFunctor().equals("goToPlace")) {
			Term receiverTerm = action.getTerm(0);

			String location = getModel().location.toString();
			result = getModel().goToPlace(agent, location); 
		}
		
		if (action.getFunctor().equals("suicide")) {
			result = getModel().suicide(agent);
		}
		
		if (action.getFunctor().equals("blinding")) {
			result = getModel().blinding(agent);
		}
		
		
		
		
		
		
		
		if (action.getFunctor().equals("getChild")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().getChild(agent, patient); 
		}


		if (action.getFunctor().equals("giveChildTo")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().giveChildto(agent, patient); 
		}
		
		if (action.getFunctor().equals("askRiddle")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().askRiddle(agent, patient); 
		}

		if (action.getFunctor().equals("kill")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().kill(agent, patient); 
		}

		if (action.getFunctor().equals("marry")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().marry(agent, patient); 
		}
		
		
		
		pauseOnRepeat(agentName, action);
		return result;
		
		
	}
}
