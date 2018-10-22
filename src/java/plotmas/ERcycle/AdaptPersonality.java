package plotmas.ERcycle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jason.asSemantics.Personality;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
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
	
	private PlanLibrary planLib;
	private String charName;
	private Personality persDiff;
	private Personality oldPers;
	
	@SuppressWarnings("unchecked")
	public AdaptPersonality(String unresolvedHappening, String charName) {
		this.planLib = PlotLauncher.getPlanLibraryFor(charName);
		this.charName = charName;
		
		// Find which plans could get triggered by this happening and identify their pre-conditions
		List<Plan> candidatePlans = planLib.getCandidatePlans(Trigger.parseTrigger(unresolvedHappening));
		List<Literal> affectConditions = new LinkedList<>(); 
		for (Plan p : candidatePlans) {
			String firstStep = p.getBody().getBodyTerm().toString();
			// for each candidate plan, check affective preconditions (and context?) of its first step
			List<Plan> firstStepOptions = planLib.getCandidatePlans(Trigger.parseTrigger("+!" + firstStep));
			
			
			firstStepOptions = firstStepOptions.stream().filter(x -> x.getTrigger().getLiteral().isGround())		// filter out generic plans like +X! <-- they are not ground
					.filter(x -> x.getLabel().getAnnot("affect") != null)		// filter out plans without affective preconditions, no need to change personalities there
					.collect(Collectors.toList());
			
			// this would give us the precondition for a coping plan, if we were to test that preconditions are met:
			//List<LogicalFormula> contexts = firstStepOptions.stream().map(x -> x.getContext()).collect(Collectors.toList());
			
			// choose one option to pursue; if none available, note that
			if (!firstStepOptions.isEmpty()) {
				Plan selectedPlan = (Plan) SELECTION_STRATEGY.apply(firstStepOptions);
				affectConditions.add(selectedPlan.getLabel().getAnnot("affect"));
			} else {
				affectConditions.add(null);
			}
		}
		
		// for each candidatePlan, affectConditions now contains either affect-precondition of first step, or null if candidatePlan is no viable
		assert(candidatePlans.size() == affectConditions.size());
		
		List<Literal> viablePlansPreConditions = affectConditions.stream().filter(x -> x != null).collect(Collectors.toList());
		Literal selectedCondition = (Literal) SELECTION_STRATEGY.apply(viablePlansPreConditions);
		
		// derive personality from condition
		List<Pair<String, String>> viablePersonalities = TermParser.extractPersonalityAnnotation(selectedCondition.toString());
		Pair<String, String> selectedPersonality = (Pair<String, String>) SELECTION_STRATEGY.apply(viablePersonalities); 
		
		// compute new personality: traits with value 0 will not change oldPers, all other traits will be changed to new Pers
		this.persDiff = Personality.createDefaultPersonality();
		this.persDiff.setTrait(selectedPersonality.getFirst(),
							  VALUE_MAP.get(selectedPersonality.getSecond()));
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
}
