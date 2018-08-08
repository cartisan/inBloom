package oedipusnew;

import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.LauncherAgent;
import plotmas.PlotEnvironment;
import plotmas.storyworld.Character;

public class OedipusEnvironmentNew extends PlotEnvironment<OedipusModelNew> {

	static Logger logger = Logger.getLogger(OedipusEnvironmentNew.class.getName());
	@Override
	public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);
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
		Character agent = getModel().getCharacter(agentName);
		
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
		
		if (action.getFunctor().equals("answer_question")) {
			Term receiverTerm = action.getTerm(0);
		
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().answer_question(agent, patient);
		}
		
		//if (action.getFunctor().equals("getChild")) {
			//Term receiverTerm = action.getTerm(0);

			//StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			//result = getModel().getChild(agent, patient); 
		//}
		
		if (action.getFunctor().equals("ask")) {
			Term receiverTerm = action.getTerm(0);
			
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().ask(agent, patient);
		}
		
		/**if (action.getFunctor().equals("giveAway")) {
			Term receiverTerm = action.getTerm(0);
			
			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
			result = getModel().giveAway(agent, patient);
		}**/


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