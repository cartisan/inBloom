package plotmas.ERcycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import jason.asSemantics.Affect;
import jason.asSemantics.Personality;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.util.Pair;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.helper.TermParser;

public class AdaptPersonality implements ProblemFixCommand {
	protected static Function<List<?>, ?> SELECTION_STRATEGY =  x -> x.get(0);		// default strategy: get first element
	
    public static final Map<String, Double> VALUE_MAP;
    static {
    	VALUE_MAP = new HashMap<String, Double>(); 
    	VALUE_MAP.put("positive", 0.3);
    	VALUE_MAP.put("negative", -0.3);
    	VALUE_MAP.put("low",    -1.0);
    	VALUE_MAP.put("medium", 0.01);
    	VALUE_MAP.put("high",   1.0);
    }
    
    private static Map<String, List<Personality>> fixMap = new HashMap<>();
    
    public static AdaptPersonality getNextFixFor(String unresolvedHappening, String charName) {
    	String key = keyFor(unresolvedHappening, charName);
    	
    	if (fixMap.containsKey(key)) {
    		Personality persDiff = fixMap.get(key).remove(0);
    		return new AdaptPersonality(persDiff, charName);
    	}
    	
    	return new AdaptPersonality(unresolvedHappening, charName);
    }
    
	@SuppressWarnings("unchecked")
	private static void populateFixMap(List<List<Literal>> affectConditions, String persHapKey) {
		// affectConditions now contains potentially-working sets of affect conditions for all plans that could get triggered by this happening
		// ...each of these sets needs to be tested as AdaptPersonality fix, until one of them works!
		
		List<Personality> fixList = new LinkedList<>();
		for (List<Literal> selectedCondition : affectConditions) {
			// derive personality conditions from collected annotations in selectedCondition, aggregate them all in pDiff
			Personality pDiff = Personality.createDefaultPersonality();
			for (Literal annot : selectedCondition) {
				List<Pair<String, String>> viablePersonalities = TermParser.extractPersonalityAnnotation(annot.toString());
				Pair<String, String> selectedPersonality = (Pair<String, String>) SELECTION_STRATEGY.apply(viablePersonalities); 
				
				// compute new personality: traits with value 0 will not change oldPers, all other traits will be changed to new Pers
				pDiff.setTrait(selectedPersonality.getFirst(), VALUE_MAP.get(selectedPersonality.getSecond()));
			}
			fixList.add(pDiff);
		}

		// make sure that subsequent calls can execute the cached fixes
		AdaptPersonality.fixMap.put(persHapKey, fixList);
	}
	
    private static String keyFor(String unresolvedHappening, String charName) {
    	 return unresolvedHappening + "#" + charName;
    }
	
	private String charName;
	private Personality persDiff;
	private Personality oldPers;

	public AdaptPersonality(String unresolvedHappening, String charName) {
		this.charName = charName;
		String key = AdaptPersonality.keyFor(unresolvedHappening, charName);
		
		// Find which plans could get triggered by this happening and identify the preconditions of all involved steps
		PlanLibrary planLib = PlotLauncher.getPlanLibraryFor(charName);
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(unresolvedHappening));
		List<List<Literal>> affectConditions = collectAffectConditions(candidatePlans, planLib);

		// Create all possible personality fixes from the affect conditions, and cache them
		AdaptPersonality.populateFixMap(affectConditions, key);
		
		// set up this fix to use the personality fix made from the annotations of the first viablePlan
		Personality thisPersDiff = AdaptPersonality.fixMap.get(key).remove(0);
		this.persDiff = thisPersDiff;
	}
	
	public AdaptPersonality(Personality persDiff, String charName) {
		this.persDiff = persDiff;
		this.charName = charName;
	}
	
	@Override
	public void execute(EngageResult er) {
		for (LauncherAgent chara : er.getLastAgents()) {
			if (chara.name.equals(this.charName)) {
				this.oldPers = chara.personality.clone();		// save old personality so we can restore it if need be
				
				ListIterator<String> it = Personality.TRAITS.listIterator();
				while (it.hasNext()) {
					String trait = it.next();
					if (this.persDiff.getTrait(trait) != 0.0) {
						chara.personality.setTrait(trait, this.persDiff.getTrait(trait));
					}
				}
				
				return;
			}
		}
		throw new RuntimeException("No character: " + this.charName + " present in ER Cycle.");
	}

	@Override
	public void undo(EngageResult er) {
		for (LauncherAgent chara : er.getLastAgents()) {
			if (chara.name.equals(this.charName)) {
				chara.personality = this.oldPers;
				break;
			}
		}
	}
	
	@Override
	public String message() {
		return "Changing personality of character: " + this.charName + " using mask: " + this.persDiff.toString();
	}
	
	private List<List<Literal>> collectAffectConditions(List<Plan> candidatePlans, PlanLibrary planLib) {
		List<List<Literal>> affectConditions = new LinkedList<>(); 
		for (Plan p : candidatePlans) {
			LinkedList<Set<Literal>> conditionsList = new LinkedList<>();	// for each step in plan: stores a list with all potential conditions that could allow this step
			
			// iterate over all steps in plan body and collect affect annotations for each step that is a plan itself
			PlanBody planStep = p.getBody();
			while (planStep != null) {
				if ((planStep.getBodyType().equals(PlanBody.BodyType.achieve)) ||			// only look for preconditions on plans
						(planStep.getBodyType().equals(PlanBody.BodyType.achieveNF))) {
					String step = "+!" + planStep.getBodyTerm().toString();
					conditionsList.add(this.determineAllEnablingAffectiveConditions(step, planLib));
				}
				planStep = planStep.getBodyNext();
			}
			
			// conditionsList contains options for each plan-step. The cartesian product of conditionsList contains n-tuples (n = number of steps)
			// ...where each tuple is one potentially working set of affect conditions for the whole plan
			Collection<List<Literal>> conditionTuples = Sets.cartesianProduct(conditionsList);
			// TODO: Filter out contradicting tuples --> turn in CSP and find all solutions
			affectConditions.addAll(conditionTuples);
		}
		return affectConditions;
	}
	
	private Set<Literal> determineAllEnablingAffectiveConditions(String intention, PlanLibrary planLib) {
		// for each candidate plan, check affective preconditions (and context?) of its first step
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(intention));
		
		Set<Literal> candidateAnnotations = candidatePlans.stream()
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR) != null)  // filter out plans without affective preconditions, no need to change personalities there
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR).toString().contains(Personality.ANNOTATION_FUNCTOR))  // filter out plans without affective preconditions, no need to change personalities there
				.map(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR))
				.collect(Collectors.toSet());
		
		// this would give us the precondition for a coping plan, if we were to test that preconditions are met:
		//List<LogicalFormula> contexts = firstStepOptions.stream().map(x -> x.getContext()).collect(Collectors.toList());
		
		// TODO: each annotation should be transformed into all possible, true configurations: a & (b | c) -> [a & b; a & c]
		// ... which are added to the set of enabling affective conditions --> TermParser#extractPersonalityAnnotation
		return candidateAnnotations;
	}
}
