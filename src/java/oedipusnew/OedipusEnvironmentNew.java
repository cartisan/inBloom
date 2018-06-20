package oedipusnew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotAwareAg;
import plotmas.PlotAwareAgArch;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.StoryworldAgent;

public class OedipusEnvironmentNew extends PlotEnvironment<OedipusModelNew> {

	static Logger logger = Logger.getLogger(OedipusEnvironmentNew.class.getName());
	@Override
	public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);
		OedipusModelNew model = new OedipusModelNew(agents, this);
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
	
	public void createAgent() {
		 ArrayList<String> agArchs = new ArrayList<String>(Arrays.asList(PlotAwareAgArch.class.getName()));
	        try {
	this.getEnvironmentInfraTier().getRuntimeServices().createAgent("oedipus", "agent_oedipusNew.asl", PlotAwareAg.class.getName(), agArchs, null, null, null);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
	}


	@Override
	public boolean executeAction(String agentName, Structure action) {
		boolean result = super.executeAction(agentName, action);
		StoryworldAgent agent = getModel().getAgent(agentName);
		
		if (action.getFunctor().equals("chilling")) {
			result = getModel().chilling(agent);
		}
		
		if (action.getFunctor().equals("working")) {
			result = getModel().working(agent);
		}
		
		if (action.getFunctor().equals("goToPlace")) {
			Term receiverTerm = action.getTerm(0);

			String location = getModel().location.toString();
			result = getModel().goToPlace(agent, location); 
		}
		
		
		if (action.getFunctor().equals("getChild")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().getChild(agent, patient); 
		}
		
		if (action.getFunctor().equals("ask")) {
			Term receiverTerm = action.getTerm(0);
			
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().ask(agent, patient);
		}


		/**if (action.getFunctor().equals("giveChildTo")) {
			Term receiverTerm = action.getTerm(0);

			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().giveChildTo(agent, patient); 
		}**/
		
		
		
		
		pauseOnRepeat(agentName, action); 
		return result;
		
		
	}
}



// this.getEnvironmentInfraTier().getRuntimeServices().createAgent(agName, agSource, agClass, archClasses, bbsPars, stts, father)
// -> kann vom Model aus aufgerufen werden	z.B.					oedipus, "agent.asl", PlotAwareAgName.get_name(), plotAwareAgArchClass.get_name()

/** Liste der Agenten holen (bei update state percepts).
model.getAgent(agentName).partner gibt uns Partner des Agenten. Literal partner(X) löschen und neuen hinzufügen.
Hierbei wird perception mit Partner jedes Mal hinzugefügt, sonst ändern sodass es nur bei Änderungen neue perception gibt.**/