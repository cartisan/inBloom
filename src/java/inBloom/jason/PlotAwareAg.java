package inBloom.jason;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Event;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Mood;
import jason.asSemantics.Option;
import jason.asSemantics.Unifier;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger.TEOperator;

import inBloom.PlotLauncher;
import inBloom.graph.Edge;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.helper.TermParser;

/**
 * A type of affective agent that is responsible for maintaining the data that is relevant for inBloom. It decides which
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

        	// ignore obligation(X) and wish(X) intentions in plotting
        	if (intention.getFunctor().contains("wish") | intention.getFunctor().contains("obligation")) {
        		return o;
        	}

        	// These will later be initialized with the motivation,
        	// once with free variables for recursion check
        	// and once with bound variables for plotting
        	Literal motivation = null;
        	String operator = "";
        	String type = "";
        	Literal motivationNoUnif = null;

        	// This variable will later be set to true,
        	// if this intention would trigger the plan
        	// it is the result of again.
        	boolean isRecursive = false;

        	// Find the motivation of this intention
        	Event event = this.getTS().getC().getSelectedEvent();
        	if(event != null) {
        		Intention filteredIntention = null;

        		if(event.getIntention() != null) {
        			filteredIntention = event.getIntention().clone();

        			// ignore obligation and wish layers in IntendedMeans stack when tracking motivation
        			while(filteredIntention.iterator().hasNext()) {
        				IntendedMeans imLayer = filteredIntention.iterator().next();

        				if(imLayer.getTrigger().toString().contains("obligation") || imLayer.getTrigger().toString().contains("wish")) {
        					// skip the obligation/wish layers: [!X, +!obligation(X), -!obligation(X), +obligation(X), real trigger...]
        					filteredIntention.pop();
        				} else {
        					break;
        				}
        			}
        		}

        		if(filteredIntention != null && filteredIntention.iterator().hasNext()) {
        			// Get the motivation with free variables in order to
        			// later check for recursion. This prevents intentions
        			// like default_activity and the recurring punished
        			// from being plotted.
        			Plan motivatingPlan = (Plan)filteredIntention.peek().getPlan().clone();
        			motivationNoUnif = (Literal)motivatingPlan.getTrigger().getLiteral().clone();

        			// We do not want a motivation, if the motivation was recursive (e.g. default_activity)
        			if(!this.isPlanRecursive(motivatingPlan, filteredIntention.peek().getUnif().clone())) {
        				type = filteredIntention.peek().getTrigger().getType().toString();				// "!" or ""
        				operator = filteredIntention.peek().getTrigger().getOperator().toString();		// "+" or "-"
        				motivation = (Literal)filteredIntention.peek().getTrigger().getLiteral().clone();
        			}
        		} else {
        			// If the selected event has no intention, then this
        			// intention is either an initial goal (default_activity)
        			// or the result of the listen. In the first case, return
        			// so we don't plot the intention. In the second case,
        			// use the event trigger as the motivation.
        			if(this.isPlanRecursive((Plan) o.getPlan().clone(), o.getUnifier().clone())) {
        				return o;
        			} else {
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
        	// if motivation was an intention, just use type "!", otherwise perception, use operator "+" or "-"
        	// results in sth like [motivation(+found(wheat)]
        	String motivationString = motivation == null ? "" : "[" + Edge.Type.MOTIVATION.toString() + "(" + (type == "" ? operator : type) + TermParser.removeAnnots(motivation.toString()) + ")]";

        	if(motivation != null) {
        		if(motivation.getFunctor().equals(Mood.ANNOTATION_FUNCTOR)) {

        			Collection<String> sources = this.getAffectiveTS().getAffectiveC().getS();
        			if(!sources.isEmpty()) {
        				motivationString = "["+ Edge.Type.MOTIVATION.toString() +"(";		// --> [motivation(... )
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
        		if (PlotLauncher.getRunner().getUserEnvironment().getStep() == 0) {
        			// Initialize step counting with first intention
        			PlotLauncher.getRunner().getUserEnvironment().setStep(1);

        			// set correct step number for the reasoning cycle num of the first intention that triggered env step 1
        			Integer cycNum = this.getAffectiveTS().getUserAgArch().getCycleNumber();
        			PlotLauncher.getRunner().getUserEnvironment().getModel().moodMapper.stepReasoningcycleNumMap.put(1, cycNum.longValue());
        		}

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
    	// if the trigger was an belief addition/deletion, plan can not be recursive
    	if (plan.getTrigger().getOperator() == TEOperator.add || plan.getTrigger().getOperator() == TEOperator.del) {
    		return false;
    	}

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
    public Intention selectIntention(Queue<Intention> intentions) {
        // make sure the selected Intention is removed from 'intentions'
        // and make sure no intention will "starve"!!!
    	
    	Iterator<Intention> ii = intentions.iterator();
    	
    	Intention bestIntention = ii.next();
    	
    	int best = returnPriority(bestIntention);
	    	
    	while(ii.hasNext()) {
    		
    		Intention i = ii.next();
    		
    		int current = returnPriority(i);
        	
    		if(returnPriority(i)>best) {
    			bestIntention = i;
    			best = current;
    		}    	
    	}
    	
    	intentions.remove(bestIntention);
    	return bestIntention;
    }
    
    
    /** returns true if the intention has a "idle" annotation */
    private int returnPriority(Intention i) {
    	// looks for an "idle" annotation in every
    	// intended means of the intention stack
    	
    	Iterator<IntendedMeans> ii = i.iterator();
    	
    	while(ii.hasNext()) {
    		
    		IntendedMeans im = ii.next();
    		
    		Pred label = im.getPlan().getLabel();
    		
    		Literal l = label.getAnnot("priority");
    		
    		if(l != null) {
    			String s = l.getTerm(0).toString();
    			
    			// Parse Negative Integer
    			if(s.charAt(0) == "(".charAt(0))
        			return Integer.parseInt(s.substring(1, s.length()-1));
    				
    			return Integer.parseInt(s);
    		}
    	}
    	return 0;
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
	public void updateMoodType(Mood oldMood) throws JasonException {
		super.updateMoodType(oldMood);
		logger.info(this.name + "'s new mood: " + this.getMood().getType());
	}

	@Override
	public void updateMoodValue(Mood newMood) {
		int reasoningCycleNum = this.getAffectiveTS().getUserAgArch().getCycleNumber();
		PlotLauncher.runner.getUserModel().mapMood(this.name, newMood, reasoningCycleNum);
	}

	public void initializeMoodMapper() {
		PlotLauncher.runner.getUserModel().mapMood(this.name, this.getPersonality().getDefaultMood(), 0);
	}
}
