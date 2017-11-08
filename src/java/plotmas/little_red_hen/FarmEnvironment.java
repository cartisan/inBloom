package plotmas.little_red_hen;

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

public class FarmEnvironment extends PlotEnvironment {
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
    @Override
    public void initialize(List<LauncherAgent> agents) {
    	super.initialize(agents);
        FarmModel model = new FarmModel(agents, this);
        this.setModel(model);
    }

    @Override
    protected void updateStatePercepts(String agentName) {
    	super.updateStatePercepts(agentName);
    	
    	// update publicly known wheat state
    	if (!(getModel().wheat == null)) {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("wheat(X)"));
    		addPercept(agentName, Literal.parseLiteral(getModel().wheat.literal()));
    	}
    	else {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("wheat(X)"));
    	}
    }
    
	@Override
    public boolean executeAction(String agentName, Structure action) {
		// let the PlotEnvironment update the plot graph, initializes result as false
		boolean result = super.executeAction(agentName, action);
    	StoryworldAgent agent = getModel().getAgent(agentName);
    	
    	if (action.getFunctor().equals("farm_work")) {
    		result = getModel().farmWork(agent);
    	}
    	
    	if (action.getFunctor().equals("plant")) {
			result = getModel().plantWheat(agent);
    	}
    	
    	if (action.toString().equals("tend(wheat)")) {
    		result = getModel().tendWheat(agent);
    	}
    	
    	if (action.toString().equals("harvest(wheat)")) {
    		result = getModel().harvestWheat(agent);
    	}
    	
    	if (action.toString().equals("grind(wheat)")) {
    		result = getModel().grindWheat(agent);
    	}
    	
    	if (action.getFunctor().equals("bake")) {
    		result = getModel().bakeBread(agent);
    	}
    	
    	if (action.getFunctor().equals("eat")) {
    		String item = action.getTerm(0).toString();
    		result = agent.eat(item);
    	}

    	if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	if (action.getFunctor().equals("share")) {
    		String item = action.getTerm(0).toString();
    		Term receiverTerm = action.getTerm(1);
    		
    		if (receiverTerm.isList()) {
    			List<StoryworldAgent> receivers = new LinkedList<>();
    			for (Term rec: (ListTermImpl) receiverTerm) {
        			receivers.add(getModel().getAgent(rec.toString()));
    			}
    			result = agent.share(item, receivers);
    		} else {
    			StoryworldAgent patient = getModel().getAgent(receiverTerm.toString());
    			result = agent.share(item, patient);
    		}
    	}
    	
    	if (action.getFunctor().equals("relax")) {
			result = agent.relax();
    	}
    	
    	pauseOnRepeat(agentName, action);
    	return result;
    }
    
	public FarmModel getModel() {
		return (FarmModel) super.model;
	}

	public void setModel(FarmModel model) {
		this.model = model;
		updatePercepts();
	}
}
