package plotmas;

import java.util.List;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Mood;
import jason.asSemantics.Option;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.helper.MoodMapper;

/**
 * A type of affective agent that is responsible for maintaining the data that is relevant for plotmas. It decides which
 * agent events need to be logged in the console, displayed in the plot graph and maintains a table of agent's mood 
 * changes for later analysis, as e.g. by the mood graph.
 * @author Leonid Berov
 */
public class PlotAwareAg extends AffectiveAgent {
	
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	public static MoodMapper moodMapper = new MoodMapper();
	private String name;
	
    @Override
    public void initAg() {
        super.initAg();
        this.name = this.getTS().getUserAgArch().getAgName();
    }
    
    @Override
    public Option selectOption(List<Option> options) {
        Option o = super.selectOption(options);
        if(o != null) {
        	// Get the selected plan and apply unification
        	Plan unifiedPlan = o.getPlan().capply(o.getUnifier());
        	
        	// Find and add motivation
        	Event se = this.getTS().getC().getSelectedEvent();
        	Intention sourceIntention = se.getIntention();
        	String motivationString = "[motivation(%1s)]";
        	boolean planRecursive = false;
        	if(sourceIntention != null)
        		planRecursive = isPlanRecursive(sourceIntention.peek().getPlan(), new Unifier());
        	
        	if(sourceIntention != null && !planRecursive) {
        		motivationString = String.format(motivationString, sourceIntention.peek().getTrigger().getTerm(1).toString().split("\\[")[0]);
        	} else {
        		// If no intention was attached to the event, simply use the trigger of the event as the motivation
        		
        		if(!planRecursive) {
        			String[] parts = se.getTrigger().getTerm(1).toString().split("\\[");
        			String mot = "";
        			for(int i = 0; i < parts.length - 1; i++) {
        				mot += parts[i];
        			}
        			motivationString = String.format(motivationString, mot);
        		} else {
        			motivationString = "";
        		}
        	}
        	
        	// Convert plan to string
        	String planString = parsePlan(unifiedPlan);
        	
        	// Append motivation in "annotation style" to intention string. Triggers do not support addAnnot.
        	planString += motivationString;
        	
        	// Plot plan as intention in graph
        	if(planString.contains("!") && !isPlanRecursive(unifiedPlan, o.getUnifier().clone())) { // o.getUnifier() vs new Unifier()
        		PlotGraphController.getPlotListener().addEvent(this.name, planString, Vertex.Type.INTENTION);
        	}
        }
        return o;
    }

    /*
     * Checks whether a plan body contains a goal which unifies with the plan trigger.
     */
    private boolean isPlanRecursive(Plan plan, Unifier u) {
    	PlanBody pb = plan.getBody();
    	while(pb != null) {
    		switch(pb.getBodyType()) {
    			case achieve:
    			case achieveNF:
    				if(u.unifies(plan.getTrigger().getTerm(1), pb.getBodyTerm())) { // plan.getTrigger().getTerm(1) = trigger without +!
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
    
    private String parsePlan(Plan plan) {
    	return plan.getTrigger().toString().substring(1);
    }
        
	@Override
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        PlotGraphController.getPlotListener().addEvent(this.name, emotion.toLiteral().toString(), Vertex.Type.EMOTION);
        logger.info(this.name + " - appraised emotion: " + emotion.toLiteral().toString());
    }
	
	@Override
	public void updateMoodType(Mood oldMood, Mood newMood) throws JasonException {
		super.updateMoodType(oldMood, newMood);
		logger.info(this.name + "'s new mood: " + newMood.getType());
	}
	
	@Override
	public void updateMoodValue(Mood newMood) {
		this.mapMood(newMood);
	}
	
	public void initializeMoodMapper() {
		this.mapMood(this.getPersonality().defaultMood());
	}
	
	private void mapMood(Mood mood) {
		Long plotTime = PlotEnvironment.getPlotTimeNow();

		moodMapper.addMood(this.name, plotTime, mood);
		logger.fine("mapping " + this.name + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
	}
}
