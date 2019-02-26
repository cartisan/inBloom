package plotmas.stories.little_red_hen;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import plotmas.PlotEnvironment;
import plotmas.storyworld.Character;

/**
 * Custom controller for the "Tale of the Little Red Hen", managed by {@link RedHenLauncher}.
 * @author Leonid Berov
 */
public class FarmEnvironment extends PlotEnvironment<FarmModel> {
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
    @Override
    protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);
    	
    	for (String location :getModel().locations.keySet()) {
    		List<Character> agents = getModel().locations.get(location);
    		if (!agents.isEmpty()) {
    			for (Character agent :agents) {
    				String agentname = agent.getName();
    				removePerceptsByUnif(agentname, Literal.parseLiteral("at(X)"));
    				addPercept(agentname, Literal.parseLiteral("at(" + location + ")"));
    			}
    		}
    	}
    	// update publicly known wheat state
    	if (!(getModel().wheat == null)) {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("existant(wheat[X])"));
    		addPercept(agentName, Literal.parseLiteral("existant(" + getModel().wheat.literal() + ")"));
    	}

    	else {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("existant(wheat[X])"));
    	}
    }
    
	@Override
    protected boolean doExecuteAction(String agentName, Structure action) {
		boolean result = false;
		Character agent = getModel().getCharacter(agentName);
    	
    	if (action.getFunctor().equals("farm_work")) {
    		result = getModel().farmWork(agent);
    	}
    	
    	else if (action.getFunctor().equals("plant")) {
			result = getModel().plantWheat(agent);
    	}
    	
    	else if (action.toString().equals("tend(wheat)")) {
    		result = getModel().tendWheat(agent);
    	}
    	
    	else if (action.toString().equals("harvest(wheat)")) {
    		result = getModel().harvestWheat(agent);
    	}
    	
    	else if (action.toString().equals("grind(wheat)")) {
    		result = getModel().grindWheat(agent);
    	}
    	
    	else if (action.getFunctor().equals("bake")) {
    		result = getModel().bakeBread(agent);
    	}
    	
    	else if (action.getFunctor().equals("eat")) {
    		String item = action.getTerm(0).toString();
    		result = agent.eat(item);
    	}

    	else if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	else if (action.getFunctor().equals("share")) {
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
    	
    	else if (action.getFunctor().equals("enterScene")) {
    		result = getModel().enterScene(agent);
    	}
    	
    	else if (action.getFunctor().equals("goToTree")) {
    		Character target = getModel().getCharacter("crow");
			result = getModel().goToTree(agent, target);
    	}
    	
    	else if (action.getFunctor().equals("flatter")) {
    		Character target = getModel().getCharacter("crow");
			result = getModel().flatter(agent, target);
    	}
    	
    	else if (action.getFunctor().equals("sing")) {
    		Character listener = getModel().getCharacter("fox");
			result = getModel().sing(agent, listener);
    	}
    	
    	else if (action.getFunctor().equals("relax")) {
			result = agent.relax();
    	}
    	
    	return result;
    }
}
