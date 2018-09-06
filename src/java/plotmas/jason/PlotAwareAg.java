package plotmas.jason;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Event;
import jason.asSemantics.Mood;
import jason.asSemantics.Option;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import plotmas.PlotCircumstanceListener;
import plotmas.PlotLauncher;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;

/**
 * A type of affective agent that is responsible for maintaining the data that is relevant for plotmas. It decides which
 * agent events need to be logged in the console, displayed in the plot graph and maintains a table of agent's mood 
 * changes for later analysis, as e.g. by the mood graph.
 * @author Leonid Berov
 */
public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());

	private String name;
	
    @Override
    public void initAg() {
        super.initAg();
        this.name = this.getTS().getUserAgArch().getAgName();
        this.getTS().getC().addEventListener(new PlotCircumstanceListener(this, this.name));
    }

    /**
     * This method is responsible for selecting one of the applicable plans
     * and inserting it into the intention stack.
     * It is overridden to plot intentions as they are created by desires
     * from the agent.
     * @param options Options to select an intention from
     * @return Selected option
     */
    @Override
    public Option selectOption(List<Option> options) {
        Option o = super.selectOption(options);
        
        // If we are adding an intention
        if(o != null && o.getPlan().getTrigger().isAchvGoal()) {
        	
        	// Receive intention with bound variables
        	Literal intention = (Literal)o.getPlan().getTrigger().getLiteral().capply(o.getUnifier());
        	
        	// These will later be initialized with the motivation,
        	// once with free variables for recursion check
        	// and once with bound variables for plotting
        	Literal motivation = null;
        	Literal motivationNoUnif = null;
        	
        	// This variable will later be set to true,
        	// if this intention would trigger the plan
        	// it is the result of again.
        	boolean isRecursive = false;
        	
        	// Find the motivation of this intention
        	Event event = this.getTS().getC().getSelectedEvent();
        	if(event != null) {
        		if(event.getIntention() != null) {
        			
        			// Get the motivation with free variables in order to
        			// later check for recursion. This prevents intentions
        			// like default_activity and the recurring punished
        			// from being plotted.
        			Plan motivatingPlan = (Plan)event.getIntention().peek().getPlan().clone();
        			motivationNoUnif = (Literal)motivatingPlan.getTrigger().getLiteral().clone();
        			
        			// We do not want a motivation, if the motivation was recursive (e.g. default_activity)
        			if(!isPlanRecursive(motivatingPlan, event.getIntention().peek().getUnif().clone())) {
        				motivation = (Literal)event.getIntention().peek().getTrigger().getLiteral().clone();
        			}
        		} else {
        			// If the selected event has no intention, then this
        			// intention is either an initial goal (default_activity)
        			// or the result of the listen. In the first case, return
        			// so we don't plot the intention. In the second case,
        			// use the event trigger as the motivation.
        			if(isPlanRecursive((Plan) o.getPlan().clone(), o.getUnifier().clone())) {
        				return o;
        			} else {
//        				return o;
//        				motivation = (Literal)event.getTrigger().getLiteral().clone();
        				motivation = null;
        			}
        		}
        	}
        	
        	// Do the recursion check
        	Unifier u = new Unifier();
        	if(motivationNoUnif != null && u.unifiesNoUndo(intention, motivationNoUnif)) {
        		isRecursive = true;
        	}
        	
        	String intentionString = "!" + intention.toString();
        	String motivationString = motivation == null ? "" : "[motivation(" + motivation.toString() + ")]";
        	
        	if(motivation != null) {
        		if(motivation.getFunctor().equals("mood")) {
        			
        			Collection<String> sources = this.getAffectiveTS().getAffectiveC().getS();
        			if(!sources.isEmpty()) {
        				motivationString = "[motivation(";
            			for(String s : sources) {
            				motivationString += s;
            				motivationString += ";";
            			}
            			motivationString = motivationString.substring(0, motivationString.length() - 1);
            			motivationString += ")]";
        			}
        			logger.log(Level.FINE, "Resolving " + motivation.toString() + ": " + motivationString);
        		}
        	}
        	
        	
        	// Actually plot the intention with the motivation
        	if(!isRecursive) {
        		PlotGraphController.getPlotListener().addEvent(
        			this.name,
        			intentionString + motivationString,
        			Vertex.Type.INTENTION,
        			PlotLauncher.runner.getUserEnvironment().getStep()
        		);
        	}
        }
        return o;
    }
    
    /**
     * This method checks whether a plan is recursive.
     * This is the case if any of the body terms which
     * are of type "achieve" or "achieveNF" unify with
     * the plan trigger.
     * @param plan Plan to be checked for recursion
     * @param u Unifier to be used
     * @return true if the plan is recursive, false otherwise
     */
    private boolean isPlanRecursive(Plan plan, Unifier u) {
    	PlanBody pb = plan.getBody();
    	Literal trigger = plan.getTrigger().getLiteral();
    	while(pb != null) {
    		switch(pb.getBodyType()) {
    			case achieve:
    			case achieveNF:
    				if(u.unifies(trigger, pb.getBodyTerm())) {
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
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        int step = PlotLauncher.runner.getUserEnvironment().getStep();
        PlotGraphController.getPlotListener().addEvent(this.name, emotion.toString(), Vertex.Type.EMOTION, step);
        logger.info(this.name + " - appraised emotion: " + emotion.toString());
    }
	
	@Override
	public void updateMoodType(Mood oldMood, Mood newMood) throws JasonException {
		super.updateMoodType(oldMood, newMood);
		logger.info(this.name + "'s new mood: " + newMood.getType());
	}
	
	@Override
	public void updateMoodValue(Mood newMood) {
		PlotLauncher.runner.getUserModel().mapMood(this.name, newMood);
	}
	
	public void initializeMoodMapper() {
		PlotLauncher.runner.getUserModel().mapMood(this.name, this.getPersonality().defaultMood());
	}
}
