package plotmas;

import java.util.logging.Logger;

import jason.asSemantics.Event;
import jason.asSemantics.GoalListener;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Trigger;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;

public class PlotGoalListener implements GoalListener {

	private String agentName;
	private Logger logger;
	
	public PlotGoalListener(String agName, Logger logger) {
		this.agentName = agName;
		this.logger = logger;
	}
	
	@Override
	public void goalStarted(Event goal) {
		if(goal.getIntention() == null) {
			return;
		}
		if(goal.getIntention().peek().getTrigger().equals(goal.getTrigger())) {
			return;
		}
		
		Literal intention = (Literal)goal.getTrigger().getLiteral().clone();
		intention.delSources();
		
		IntendedMeans motivation = (IntendedMeans)goal.getIntention().peek().clone();
		
		String intentionString = "!" + intention.toString();
		String motivationString = "";
		
		if(!isPlanRecursive(motivation.getPlan(), motivation.getUnif())) {
			if(motivation.getTrigger().equals(goal.getTrigger())) {
				return;
			}
			motivationString = String.format("[motivation(%s)]", motivation.getTrigger().getLiteral().toString());
		}
		
		PlotGraphController.getPlotListener().addEvent(
			this.agentName,
			intentionString + motivationString,
			Vertex.Type.INTENTION
		);
	}
	
	private boolean isPlanRecursive(Plan plan, Unifier u) {
    	PlanBody pb = plan.getBody();
    	while(pb != null) {
    		switch(pb.getBodyType()) {
    			case achieve:
    			case achieveNF:
    				if(u.unifies(plan.getTrigger().getLiteral(), pb.getBodyTerm())) {
    					return true;
    				}
    				break;
    			default:
    				break;
    		}
    		pb = pb.getBodyNext();
    	}
    	return false;
    }

	@Override
	public void goalFinished(Trigger goal, FinishStates result) {}

	@Override
	public void goalFailed(Trigger goal) {}

	@Override
	public void goalSuspended(Trigger goal, String reason) {}

	@Override
	public void goalResumed(Trigger goal) {}

}
