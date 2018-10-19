package plotmas.ERcycle;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jason.asSemantics.Personality;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.ERcycle.PlotCycle.EngageResult;

public class AdaptPersonality implements ProblemFixCommand {

	protected static Function<List<?>, ?> SELECTION_STRATEGY =  x -> x.get(0);		// default strategy: get first element
	
	private PlanLibrary planLib;
	private String charName;
	private Personality newPers;
	private Personality oldPers;
	
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
		List<Literal> viablePlansPreConditions = affectConditions.stream().filter(x -> x != null).collect(Collectors.toList());
		Literal selectedCondition = (Literal) SELECTION_STRATEGY.apply(viablePlansPreConditions);
		
		// TODO: derive Personality from Condition
		
		// decide which plan to pursue, extract personality from annotations
		System.out.println(selectedCondition);
		
	}
	
	@Override
	public void execute(EngageResult er) {
		for (LauncherAgent chara : er.getLastAgents()) {
			if (chara.name.equals(this.charName)) {
				this.oldPers = chara.personality;
				chara.personality = this.newPers;
				break;
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
		return "Changing personality of character " + this.charName + " to " + this.newPers.toString();
	}
}
