package inBloom.stories.oedipus;

import java.util.List;
import java.util.logging.Logger;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

public class OedipusEnvironment extends PlotEnvironment<OedipusModel> {

	static Logger logger = Logger.getLogger(OedipusEnvironment.class.getName());
		
	@Override
    public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);
		   	
		String pos = model.location;
		addPercept(Literal.parseLiteral("position(" + String.valueOf(pos) + ")" ));
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
