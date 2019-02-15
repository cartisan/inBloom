package plotmas.stories.oedipus;

import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.storyworld.Character;

public class OedipusEnvironment extends PlotEnvironment<OedipusModel> {

	static Logger logger = Logger.getLogger(OedipusEnvironment.class.getName());
		
	@Override
	protected void updateStatePercepts(String agentName) {
	   super.updateStatePercepts(agentName);
	    	
	   String pos = model.location;
	   removePerceptsByUnif(agentName, Literal.parseLiteral("position(X)"));
	   addPercept(agentName, Literal.parseLiteral("position(" + String.valueOf(pos) + ")" ));
	   }
	
	public boolean doExecuteAction(String agentName, Structure action) {
		boolean result = false;
		Character agent = getModel().getCharacter(agentName);
		
		if (action.getFunctor().equals("chilling")) {
			result = getModel().chilling(agent);
		}
		
		if (action.getFunctor().equals("working")) {
			result = getModel().working(agent);
		}
				
		if (action.getFunctor().equals("answer_question")) {
			Term receiverTerm = action.getTerm(0);
		
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().answer_question(agent, patient);
		}
			
		if (action.getFunctor().equals("ask")) {
			Term receiverTerm = action.getTerm(0);
			
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().ask(agent, patient);
		}
		
		if (action.getFunctor().equals("getChild")) {
			result = getModel().getChild(agent);
		}
		
		if (action.getFunctor().equals("giveChildTo")) {
			Term receiverTerm = action.getTerm(0);
			
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().giveChildTo(agent, patient);
			
		}
		
		if (action.getFunctor().equals("adopt")) {
			Term receiverTerm = action.getTerm(0);
			
			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().adopt(agent, patient);
			
		}
		
		return result;
		
		
	}
}
