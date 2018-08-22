package plotmas.stories.crow;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.storyworld.Character;

public class CrowEnvironment extends PlotEnvironment<CrowModel> {

	static Logger logger = Logger.getLogger(CrowEnvironment.class.getName());

	@Override
	protected void updateStatePercepts(String agentName) {
		super.updateStatePercepts(agentName);

	}


	@Override
	public boolean executeAction(String agentName, Structure action) {
		boolean result = super.executeAction(agentName, action);
		Character agent = getModel().getCharacter(agentName);
		
		if (action.getFunctor().equals("askForCheese")) {
			Term receiverTerm = action.getTerm(0);

			Character patient = getModel().getCharacter(receiverTerm.toString());
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

			Character patient = getModel().getCharacter(receiverTerm.toString());
			result = getModel().flatter(agent, patient); 
		}
		
		if (action.getFunctor().equals("answerNegatively")) {
			logger.info("in env, asnwerNegativeley");
			Term receiverTerm = action.getTerm(0);

			Character patient = getModel().getCharacter(receiverTerm.toString());
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
    			List<Character> receivers = new LinkedList<>();
    			for (Term rec: (ListTermImpl) receiverTerm) {
        			receivers.add(getModel().getCharacter(rec.toString()));
    			}
    			result = agent.share(item, receivers);
    		} else {
    			Character patient = getModel().getCharacter(receiverTerm.toString());
    			result = agent.share(item, patient);
    			
    		}
    	}
		

		if (action.getFunctor().equals("eat")) {
    		String item = action.getTerm(0).toString();
    		result = agent.eat(item);
		}
		
		return result;
		
		
	}
}
