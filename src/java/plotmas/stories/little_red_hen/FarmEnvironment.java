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
import plotmas.storyworld.Location;

/**
 * Custom controller for the "Tale of the Little Red Hen", managed by {@link RedHenLauncher}.
 * @author Leonid Berov
 */
public class FarmEnvironment extends PlotEnvironment<FarmModel> {
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
    @Override
    protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);

    	// update publicly known wheat state
    	if (!(FarmModel.FARM.produce == null)) {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("existant(wheat[X])"));
    		addPercept(agentName, Literal.parseLiteral("existant(" + FarmModel.FARM.produce.literal() + ")"));
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
    	
    	else if (action.getFunctor().equals("goTo")) {
    		Location target = getModel().getLocation(action.getTerm(0).toString());
    		result = getModel().goTo(agent, target );
    	}
    	
    	else if (action.getFunctor().equals("sing")) {
			result = agent.sing();
    	}
    	
    	else if (action.getFunctor().equals("collect")) {
    		String item = action.getTerm(0).toString();
    		result = agent.collect(item ); 
    	}
    	
    	else if (action.getFunctor().equals("handOver")) {
    		Character receiver = getModel().getCharacter(action.getTerm(0).toString());
    		String item = action.getTerm(1).toString();
			result = agent.handOver(receiver, item, false);
    	}
    	
    	else if (action.getFunctor().equals("refuseHandOver")) {
    		// TODO: rather in speech act?
    		Character target = getModel().getCharacter(action.getTerm(0).toString());
    		String item = action.getTerm(1).toString();
    		result = agent.handOver(target, item, true);
    	}
    	
    	else if (action.getFunctor().equals("relax")) {
			result = agent.relax();
    	}
    	
    	return result;
    }
}
