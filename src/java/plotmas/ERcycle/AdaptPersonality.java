package plotmas.ERcycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
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

	/** domain that personality traits can take, cast to integer for choco-solver CSP variables	 */
	public static int[] PERSONALITY_INT_DOMAIN = new int[]{-10, -3, 3, 10};

    /** map from happening#charName to a list of personality fixes that should be tried to resolve the happening  */
    private static Map<String, List<Personality>> fixMap = new HashMap<>();
    
    /**
     * Converts happening and char name to internal representation used in {@link #fixMap}.
     * @param unresolvedHappening
     * @param charName
     * @return
     */
    private static String keyFor(String unresolvedHappening, String charName) {
    	 return unresolvedHappening + "#" + charName;
    }
    
    /**
     * Returns the next personality fix that should be tried to resolve this happening for the respective character.
     * @param unresolvedHappening happening that was not resolved as in the respective vertex of plot graph
     * @param charName String representation as in root node of plot graph
     * @return
     */
    public static AdaptPersonality getNextFixFor(String unresolvedHappening, String charName) {
    	String key = keyFor(unresolvedHappening, charName);
    	
    	if (fixMap.containsKey(key)) {
    		List<Personality> cachedFixList = fixMap.get(key);
    		if (cachedFixList.isEmpty())
    			return null;
    		
    		Personality persDiff = cachedFixList.remove(0);
    		return new AdaptPersonality(persDiff, charName);
    	}
    	
    	return new AdaptPersonality(unresolvedHappening, charName);
    }
    
	/**
	 * Translate OCEANConditions into AdaptPersonality style Personality diffs, and cache them for future execution
	 * @param affectConditions OCEAN settings that can enable any one of the plans that could get triggered by this happening
	 * @param persHapKey key for happening and character in map that caches the fixes {@link #fixMap} 
	 */
	private static void populateFixMap(List<OCEANConstraints> affectConditions, String persHapKey) {
		List<Personality> fixList = new LinkedList<>();
		
		for (OCEANConstraints selectedCondition : affectConditions) {
			Personality pDiff = selectedCondition.toPersonalityDiff(); 
			fixList.add(pDiff);
		}

		// TODO: Decide how and if to sort potential fixes. We know: locality and sharp boundaries
//		fixList.sort(Personality.comparator());
		AdaptPersonality.fixMap.put(persHapKey, fixList);
	}
	
	private String charName;
	/** intended change of personality, in mask format. That is: 0 if trait will be unchanged, otherwise new value */
	private Personality persMask;
	/** Personality of character before AdaptPersonality fix was executed */
	private Personality oldPers;
	/** True when this id the first attempt to find the right personality -> no undo of previous attempts required */ 
	public boolean isFirstFix=false;

	/**
	 * Create a CSP that deduces from the plan library which personalities might allow an agent to react to the
	 * unresolved happening. Then solve the CSP and cache all solution-personalities so next time no computation
	 * will be necessary. Instantiate fix from first cached solution.
	 * @param unresolvedHappening
	 * @param charName
	 */
	public AdaptPersonality(String unresolvedHappening, String charName) {
		this.charName = charName;
		this.isFirstFix = true;
		String key = AdaptPersonality.keyFor(unresolvedHappening, charName);
		
		// Find which plans could get triggered by this happening, identify the preconditions of involved steps
		// and compute viable ocean settings that could potentially allow execution of one complete plan
		PlanLibrary planLib = PlotLauncher.getPlanLibraryFor(charName);
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(unresolvedHappening));
		List<OCEANConstraints> affectConditions = collectAffectConditions(candidatePlans, planLib);

		// translate conditions into personality fixes and cache them for future execution
		AdaptPersonality.populateFixMap(affectConditions, key);
		
		// set up this fix to use the first cached fix
		Personality thisPersMask = AdaptPersonality.fixMap.get(key).remove(0);
		this.persMask = thisPersMask;
	}
	
	/**
	 * Create fix for unrfesolved happening from cached personality without having to re-solve the CSP that is required
	 * to deduce a potential personality from the plan library. 
	 * @param persMask
	 * @param charName
	 */
	private AdaptPersonality(Personality persMask, String charName) {
		this.persMask = persMask;
		this.charName = charName;
	}
	
	@Override
	public void execute(EngageResult er) {
		LauncherAgent chara = er.getAgent(this.charName);
		this.oldPers = chara.personality.clone();		// save old personality so we can restore it if need be
		
		ListIterator<String> it = Personality.TRAITS.listIterator();
		while (it.hasNext()) {
			String trait = it.next();
			if (this.persMask.getTrait(trait) != 0.0) {
				chara.personality.setTrait(trait, this.persMask.getTrait(trait));
			}
		}
		
		return;
	}

	@Override
	public void undo(EngageResult er) {
		LauncherAgent chara = er.getAgent(this.charName);
		chara.personality = this.oldPers;
	}
	
	@Override
	public String message() {
		return "Changing personality of character: " + this.charName + " using mask: " + this.persMask.toString();
	}
	
	/**
	 * For a collection of candidate plans, i.e. plans that could get triggered by the happening, compute all
	 * OCEAN settings, that could allow the execution of at least one of them.
	 * @param candidatePlans
	 * @param planLib
	 * @return
	 */
	private List<OCEANConstraints> collectAffectConditions(List<Plan> candidatePlans, PlanLibrary planLib) {
		// will contain OCEAN settings that are promising in making a candidate plan execute
		List<OCEANConstraints> viablePlanSettings = new LinkedList<>();
		
		for (Plan p : candidatePlans) {
			// for each step in plan: stores a list with all potential conditions that could allow this step
			List<Set<OCEANConstraints>> viablePlanstepSettings = collectAllStepConditions(p, planLib);
			
			// stores all viable OCEAN constellations that might enable plan p
			Set<OCEANConstraints> oceanSolutions = computeAllOceanSolutions(p, viablePlanstepSettings);
			
			viablePlanSettings.addAll(oceanSolutions);
		}
		
		return viablePlanSettings;
	}

	/**
	 * For each step in plan, determines the preconditions for all possible plans to resolve that step. For each step, 
	 * returns a set of OCEAN constraints that could allow the execution of just this step. <br> 
	 * (Thus, the length of the returned list should be the same as the step-number in the plan.)
	 * @param plan
	 * @param planLib
	 * @return
	 */
	private List<Set<OCEANConstraints>> collectAllStepConditions(Plan plan, PlanLibrary planLib) {
		// contains OCEAN settings that are promising in making a candidate plan execute
		// TODO: Get to create(bread) from new found(wheat) based in obligations and wishes
		List<Set<OCEANConstraints>> viablePlanstepSettings = new LinkedList<>();
		
		// iterate over all steps in plan body and collect affect annotations for each step that is a plan itself
		PlanBody planStep = plan.getBody();
		while (planStep != null) {
			if ((planStep.getBodyType().equals(PlanBody.BodyType.achieve)) ||			// only look for preconditions on plans
					(planStep.getBodyType().equals(PlanBody.BodyType.achieveNF))) {
				String step = "+!" + planStep.getBodyTerm().toString();
				viablePlanstepSettings.add(this.determineAllEnablingOCEANsettings(step, planLib));
			}
			planStep = planStep.getBodyNext();
		}
		
		return viablePlanstepSettings;
	}
	
	/**
	 * For an intention, determines which plans could be executed to resolve this intention. Collects personality
	 * annotations from these plans and computes all OCEAN settings, that have the potential to allow the execution
	 * of such a plan, and thus, the resolution of intention.
	 * @param intention
	 * @param planLib
	 * @return
	 */
	private Set<OCEANConstraints> determineAllEnablingOCEANsettings(String intention, PlanLibrary planLib) {
		// for each intention, check affective preconditions (and context?) of its associated plans
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(intention));
		
		Set<Literal> candidatePlanAnnotations = candidatePlans.stream()
				.filter(x -> !Character.isUpperCase(x.getTrigger().getLiteral().getFunctor().charAt(0))) // ignore meta plans like +!X, they eventually have to call the concrete plan and this is what we are after
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR) != null)  // filter out plans without affective preconditions, no need to change personalities there
				.filter(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR).toString().contains(Personality.ANNOTATION_FUNCTOR))  // filter out plans without personality preconditions, no need to change personalities there
				.map(x -> x.getLabel().getAnnot(Affect.ANNOTATION_FUNCTOR))
				.collect(Collectors.toSet());
		
		// this would give us the precondition for a coping plan, if we were to test that preconditions are met:
		//List<LogicalFormula> contexts = firstStepOptions.stream().map(x -> x.getContext()).collect(Collectors.toList());
		
		// from each candidate plan annotation, derive all possible OCEAN settings that would allow this candidate to be executed
		Set<OCEANConstraints> enablingOCEANconditions = new HashSet<>();
		for(Literal annot: candidatePlanAnnotations) {
			enablingOCEANconditions.addAll(TermParser.solutionsForPersonalityAnnotation(annot));
		}
		
		return enablingOCEANconditions;
	}

	/**
	 * Computes OCEAN constraints to allow the execution of plan p, from the constraints of each of p's plan steps.
	 * To enable the execution of plan p, each plan-step has to be executable; i.e. we need to solve for the 
	 * conjunction of (disjunction of constraints of a step) for all steps.
	 * 
	 * @param p Plan for which ocean settings need to be found to allow its execution (just relevant for naming)
	 * @param viablePlanstepSettings consists of entries where the n-th entry contains all possible constraints for enabling the execution of the n-th plan-step of plan p
	 * @return
	 */
	private Set<OCEANConstraints> computeAllOceanSolutions(Plan p, List<Set<OCEANConstraints>> viablePlanstepSettings) {
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
		Set<OCEANConstraints> oceanSolutions = OCEANConstraints.toConstraintsSet(solutions, intVarCache.values());
		return oceanSolutions;
	}
	
	
	
	
	
	
	
	/**
	 * Represents one personality setting in the OCEAN space, which could enable the execution of a plan or plan step.
	 * For each trait that is relevant, {@link #constraints} holds a {@link Pair} where the first value represents the
	 * trait name and the second its value in integer representation: {-10, -3, 3, 10}. 
	 * @author Leonid Berov
	 */
	public static class OCEANConstraints {
		/** Set of (trait, int-value) pairs representing the relevant personality constraints */
		public Set<Pair<String, Integer>> constraints = new HashSet<>();
		
		/**
		 * Translates a list of choco-solutions into the set of corresponsing OCEANConstraints.
		 * @param solutions A list of solutions for a CSP based on ASL affect annotations 
		 * @param vars A collection of the IntVars that were used in the CSP, in order to retrive their value from each solution
		 * @return a set of OCEANConstraints that represents all solutions of the CSP
		 */
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

		/**
		 * Adds a trait and its integer-represented value to this constraint set.
		 * @param trait
		 * @param value
		 */
		public void addTrait(String trait, Integer value) {
			this.constraints.add(new Pair<>(trait, value));
		}
		
		/**
		 * Returns the value of a trait, if trait is constrained, or null if it is not relevant.
		 * @param trait
		 * @return
		 */
		public Integer getTrait(String trait) {
			Integer result = null;
			
			for(Pair<String, Integer> cst : this.constraints) {
				if (cst.getFirst().equals(trait)) {
					return cst.getSecond();
				}
			}
			
			return result;
		}
		
		/**
		 * Translates this OCEANConstraints into a single choco-solver type constraint used for CSP.
		 * @param varMap A map of trait-names to the IntVars that will be used by all constraints of the CSP, can be empty in which case it is intiaized lazy
		 * @param model The model that will be used to solve the CSP
		 * @return A constraint with at most 5 integer variables, one for each relevant trait
		 */
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
		
		
		/**
		 * Translates this OCEANConstraint into a personality diff as emplyed by {@code AdaptPersonality} fixes. For 
		 * each trait the diff contains either 0, if the trait remains unchanged, or the value that it should be
		 * changed to.
		 * @return
		 */
		public Personality toPersonalityDiff() {
			Personality pDiff = Personality.createDefaultPersonality();
			
			for (Pair<String, Integer> traitValuePair : this.constraints) {
				pDiff.setTrait(traitValuePair.getFirst(),traitValuePair.getSecond() / 10.0);
			}
			
			return pDiff;
		}
		
		@Override
		public String toString() {
			if (this.constraints.isEmpty()) {
				return "personality()";
			}
			
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
