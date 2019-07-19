package inBloom.stories.little_red_hen;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;
import inBloom.storyworld.Location;

/**
 * Custom controller for the "Tale of the Little Red Hen", managed by {@link RedHenLauncher}.
 * @author Leonid Berov
 */
public class FarmEnvironment extends PlotEnvironment<FarmModel> {
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());

	@Override
    public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);

		// Add special initialization of agent percepts for custom environment here.
		// Left blank intentionally.
	}

	@Override
    protected ActionReport doExecuteAction(String agentName, Structure action) {
		ActionReport result = null;
		Character agent = this.getModel().getCharacter(agentName);

    	if (action.getFunctor().equals("farm_work")) {
    		result = this.getModel().farmWork(agent);
    	}

    	else if (action.getFunctor().equals("plant")) {
			result = this.getModel().plantWheat(agent);
    	}

    	else if (action.toString().equals("tend(wheat)")) {
    		result = this.getModel().tendWheat(agent);
    	}

    	else if (action.toString().equals("harvest(wheat)")) {
    		result = this.getModel().harvestWheat(agent);
    	}

    	else if (action.toString().equals("grind(wheat)")) {
    		result = this.getModel().grindWheat(agent);
    	}

    	else if (action.getFunctor().equals("bake")) {
    		result = this.getModel().bakeBread(agent);
    	}

    	else if (action.getFunctor().equals("eat")) {
    		String item = action.getTerm(0).toString();
    		result = agent.eat(item);
    	}

    	// TODO: invent a semantics for help (ideally one fitting the Honored Request FU)
    	else if (action.getFunctor().equals("help")) {
    		result = new ActionReport(true);
    	}

    	else if (action.getFunctor().equals("share")) {
    		String item = action.getTerm(0).toString();
    		Term receiverTerm = action.getTerm(1);

    		if (receiverTerm.isList()) {
    			List<Character> receivers = new LinkedList<>();
    			for (Term rec: (ListTermImpl) receiverTerm) {
        			receivers.add(this.getModel().getCharacter(rec.toString()));
    			}
    			result = agent.share(item, receivers);
    		} else {
    			Character patient = this.getModel().getCharacter(receiverTerm.toString());
    			result = agent.share(item, patient);

    		}
    	}

    	else if (action.getFunctor().equals("goTo")) {
    		Location target = this.getModel().getLocation(action.getTerm(0).toString());
    		result = agent.goTo(target);
    	}

    	else if (action.getFunctor().equals("sing")) {
			result = agent.sing();
    	}

    	else if (action.getFunctor().equals("collect")) {
    		String item = action.getTerm(0).toString();
    		result = agent.collect(item );
    	}

    	else if (action.getFunctor().equals("handOver")) {
    		Character receiver = this.getModel().getCharacter(action.getTerm(0).toString());
    		String item = action.getTerm(1).toString();
			result = agent.handOver(receiver, item);
    	}

    	else if (action.getFunctor().equals("refuseHandOver")) {
    		// TODO: rather in speech act?
    		Character target = this.getModel().getCharacter(action.getTerm(0).toString());
    		String item = action.getTerm(1).toString();
    		result = agent.refuseHandOver(target, item);
    	}

    	else if (action.getFunctor().equals("relax")) {
			result = agent.relax();
    	}

    	return result;
    }
}
