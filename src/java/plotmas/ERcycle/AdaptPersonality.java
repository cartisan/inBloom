package plotmas.ERcycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

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

	/** domain that personality traits can take, cast to integer for choco-solver CSP variables	 */
	public static int[] PERSONALITY_INT_DOMAIN = new int[]{-10, -3, 3, 10};

    private static Map<String, List<Personality>> fixMap = new HashMap<>();
    
    public static AdaptPersonality getNextFixFor(String unresolvedHappening, String charName) {
    	String key = keyFor(unresolvedHappening, charName);
    	
    	if (fixMap.containsKey(key)) {
    		Personality persDiff = fixMap.get(key).remove(0);
    		return new AdaptPersonality(persDiff, charName);
    	}
    	
    	return new AdaptPersonality(unresolvedHappening, charName);
    }
    
	private static void populateFixMap(List<OCEANConstraints> affectConditions, String persHapKey) {
		// affectConditions contains OCEAN settings that can enable any one of the plans that could get triggered by this happening
		// ...each of these settings needs to be tested as AdaptPersonality fix, until one of them works!
		
		List<Personality> fixList = new LinkedList<>();
		for (OCEANConstraints selectedCondition : affectConditions) {
			// derive personality conditions from collected annotations in selectedCondition, aggregate them all in pDiff
			Personality pDiff = selectedCondition.toPersonalityDiff(); 
			fixList.add(pDiff);
		}

		// make sure that subsequent calls can execute the cached fixes
		// TODO: Decide how and if to sort potential fixes. We know: locality and sharp boundaries
//		fixList.sort(Personality.comparator());
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
		List<OCEANConstraints> affectConditions = collectAffectConditions(candidatePlans, planLib);

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
	
	private List<OCEANConstraints> collectAffectConditions(List<Plan> candidatePlans, PlanLibrary planLib) {
		// contains OCEAN settings that are promising in making a candidate plan execute
		List<OCEANConstraints> viablePlanSettings = new LinkedList<>();
		
		for (Plan p : candidatePlans) {
			// for each step in plan: stores a list with all potential conditions that could allow this step
			LinkedList<Set<OCEANConstraints>> viablePlanstepSettings = new LinkedList<>();
			
			// iterate over all steps in plan body and collect affect annotations for each step that is a plan itself
			PlanBody planStep = p.getBody();
			while (planStep != null) {
				if ((planStep.getBodyType().equals(PlanBody.BodyType.achieve)) ||			// only look for preconditions on plans
						(planStep.getBodyType().equals(PlanBody.BodyType.achieveNF))) {
					String step = "+!" + planStep.getBodyTerm().toString();
					viablePlanstepSettings.add(this.determineAllEnablingOCEANsettings(step, planLib));
				}
				planStep = planStep.getBodyNext();
			}
			
			// viablePlanstepSettings now consists of entries where the n-th entry contains all possible constraints for enabling the
			// ...execution of the n-th plan-step of plan p. 
			// To enable the execution of plan p, each plan-step has to be executable; i.e. we need to solve for the
			// ... conjunction of (disjunction of constraints of a step) for all steps
			Model model = new Model(p.toString());
			Map<String, IntVar> intVarCache = new HashMap<>();
			
			Constraint conjunctionConstr = model.trueConstraint();
			for(Set<OCEANConstraints> nthStepConstraints: viablePlanstepSettings) {
				Constraint disjunctionConstr = model.falseConstraint();
				for (OCEANConstraints stepConstraint: nthStepConstraints) {
					disjunctionConstr = model.or(disjunctionConstr, stepConstraint.toConstraint(intVarCache, model));
				}
				conjunctionConstr = model.and(disjunctionConstr, conjunctionConstr);
			}
			conjunctionConstr.post();
			Solver solver = model.getSolver();
			List<Solution> solutions = solver.findAllSolutions();

			// add all OCEAN settings that may allow this plan to be executed to list of viable OCEAN options
			viablePlanSettings.addAll(OCEANConstraints.toConstraintsSet(solutions, intVarCache.values()));
		}
		
		return viablePlanSettings;
	}
	
	private Set<OCEANConstraints> determineAllEnablingOCEANsettings(String intention, PlanLibrary planLib) {
		// for each intention, check affective preconditions (and context?) of its associated plans
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(intention));
		
		Set<Literal> candidatePlanAnnotations = candidatePlans.stream()
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR) != null)  // filter out plans without affective preconditions, no need to change personalities there
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR).toString().contains(Personality.ANNOTATION_FUNCTOR))  // filter out plans without affective preconditions, no need to change personalities there
				.map(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR))
				.collect(Collectors.toSet());
		
		// this would give us the precondition for a coping plan, if we were to test that preconditions are met:
		//List<LogicalFormula> contexts = firstStepOptions.stream().map(x -> x.getContext()).collect(Collectors.toList());
		
		// from each candidate plan annotation, derive all possible OCEAN settings that would allows this candidate to be executed
		Set<OCEANConstraints> enablingOCEANconditions = new HashSet<>();
		for(Literal annot: candidatePlanAnnotations) {
			enablingOCEANconditions.addAll(TermParser.solutionsForPersonalityAnnotation(annot));
		}
		
		return enablingOCEANconditions;
	}
	
	
	public static class OCEANConstraints {
		public Set<Pair<String, Integer>> constraints = new HashSet<>();
		public Map<String, IntVar> varMap = new HashMap<>();
		
		public static Set<OCEANConstraints> toConstraintsSet(List<Solution> solutions, Collection<IntVar> vars) {
	        Set<OCEANConstraints> results = new HashSet<>();
	       
	        for (Solution s:solutions) {
	        	
	        	OCEANConstraints personality = new OCEANConstraints();
	        	for(IntVar var: vars) {
	        		personality.addTrait(var.getName(), s.getIntVal(var));
	        	}
	        	
	        	results.add(personality);
	        }
	        
	        return results;
		}

		public void addTrait(String trait, Integer value) {
			this.constraints.add(new Pair<>(trait, value));
		}
		
		public Integer getTrait(String trait) {
			Integer result = null;
			
			for(Pair<String, Integer> cst : this.constraints) {
				if (cst.getFirst().equals(trait)) {
					return cst.getSecond();
				}
			}
			
			return result;
		}
		
		public Constraint toConstraint(Map<String, IntVar> varMap, Model model) {
			List<Constraint> cstrs = new LinkedList<>();
			for(Pair<String, Integer> constr : this.constraints) {
				if(!varMap.containsKey(constr.getFirst())) {
					varMap.put(constr.getFirst(), model.intVar(constr.getFirst(), AdaptPersonality.PERSONALITY_INT_DOMAIN));
				}
				IntVar traitVar = varMap.get(constr.getFirst());
				
				Constraint traitConst = model.arithm(traitVar,"=",constr.getSecond());
				cstrs.add(traitConst);
			}
			
			return Constraint.merge(this.toString(), cstrs.toArray(new Constraint[0]));
		}
		
		
		public Personality toPersonalityDiff() {
			Personality pDiff = Personality.createDefaultPersonality();
			
			for (Pair<String, Integer> traitValuePair : this.constraints) {
				pDiff.setTrait(traitValuePair.getFirst(),traitValuePair.getSecond() / 10.0);
			}
			
			return pDiff;
		}
		
		@Override
		public String toString() {
			LinkedList<Pair<String, Integer>> cstrs = new LinkedList<>(this.constraints);
			
			// sort list according to traits names
			cstrs.sort((Pair<String, Integer> p1, Pair<String, Integer> p2) -> p1.getFirst().compareTo(p2.getFirst()));
			
			return toString(cstrs);
		}
		
		private String toString(Pair<String, Integer> cstr) {
			return "personality(" + cstr.getFirst() + "," + cstr.getSecond() + ")";
		}
		
		private String toString(LinkedList<Pair<String, Integer>> cstrs) {
			if(cstrs.size() == 1) {
				return toString(cstrs.get(0));
			} else {
				Pair<String, Integer> head = cstrs.remove(0);
				return "and(" + toString(cstrs) + "," + toString(head) + ")";
			}
		}
		
		@Override
	    public int hashCode() {
	        return this.toString().hashCode();
	    }
		
	    @Override
	    public boolean equals(Object obj) {
	        if (obj == null) return false;
	        if (obj == this) return true;
	        if (obj instanceof OCEANConstraints) {
	        	OCEANConstraints other = (OCEANConstraints)obj;
	        	
	        	return this.toString().equals(other.toString());
	        }
	        return false;
	    }
	}
	
}
