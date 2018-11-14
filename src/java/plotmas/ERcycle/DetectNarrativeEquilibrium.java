package plotmas.ERcycle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jason.util.Pair;
import plotmas.PlotEnvironment;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.graph.Vertex;
import plotmas.helper.PlotpatternAnalyzer;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

/**
 * Detects narrative equilibria, i.e. states where the same sequence of events repeats a {@link PlotEnvironment.MAX_REPEATE_NUM}
 * number of times in the subgraph of the plot's protagonist. Proposes to solve this problem by
 * inserting a happening right after the begin of the equilibrium. E.g. with MAX_REPEATE_NUM = 3 <br>
 * <i>plot:</i> <code> act1, act2, actA, actB, actA, actB, actA, actB </code> <i>solution</i>: insert happening after act2
 * @author Leonid Berov
 */
public class DetectNarrativeEquilibrium extends ProblemDetectionState {

	protected DetectNarrativeEquilibrium(RedHenHappeningCycle controller) {
		super(controller);
	}
	
	/**
	 * Identifies the character that is the most likely protagonist.
	 * At the moment this is the character with the most suspensfull, resolved conflict 
	 * @param er the EngagementResult instance of a successful ER-Cycle run
	 * @return the root vertex of the protagonist's subbgraph
	 */
	private Vertex detectPotentialProtagonist(EngageResult er) {
		String protagonist = er.getLastAgents().get(0).name;
		if (er.getTellability().suspense > 0)
			// the protagonist should be the character with the most suspensfull intention
			protagonist = er.getTellability().counter.mostSuspensefulIntention.getFirst();
		
		Vertex root = null;
		for (Vertex v : er.getPlotGraph().getRoots()) {
			if (v.getLabel().equals(protagonist)) {
				root = v;
				break;
			}
			throw new RuntimeException("Agent: " + protagonist + " not found in plot graph");
		}
		return root;
	}
	
	@Override
	public ProblemFixCommand performDetect(EngageResult er) {
		Vertex protagonist = detectPotentialProtagonist(er);
		
		// construct list of events for protagonist
		List<String> events = new LinkedList<>();					// list of all events of char
		Map<Integer,Integer> positionStepMap = new HashMap<>();		// maps position of events in story-string to step when they occurred
		for (Vertex v : er.getPlotGraph().getCharSubgraph(protagonist)) {
			// add event
			events.add(v.getLabel());
			// identify at which position in event string this event is found, that is which positions correspond to which plot step
			int startPos = events.stream().collect(Collectors.joining(PlotpatternAnalyzer.EVENT_SEP)).length() - v.getLabel().length(); 
			positionStepMap.put(startPos, v.getStep());
		}
		
		// detect all repeating event patterns , check if they repeat often enough to count as equilibrium state 
		HashMap<String, Integer> patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		for (Entry<String,Integer> patternCount : patternCounts.entrySet()) {
			if (patternCount.getValue() >= PlotEnvironment.MAX_REPEATE_NUM) {
				// found narrative equilibrium, identify in which step it starts. Use regex: (pattern ){rep_num}
				Pattern pattern = Pattern.compile(
						"(" + Pattern.quote(patternCount.getKey() + PlotpatternAnalyzer.EVENT_SEP) + "){" + patternCount.getValue() +"}"
						);
				Pair<Integer,Integer> location = PlotpatternAnalyzer.patternLocation(events, pattern);
				
				// schedule happening to occur one step after start of previous equilibrium state
				Integer startStep = positionStepMap.get(location.getFirst()) + 1;
				
				// we are scheduling happenings, so next problem detection step should always be to check if the
				// unresolved happenings are present. If no unresolved happenings are detected, defaultNextState
				// will be changed to its previous value by the responsible class
				this.nextReflectionState = this.getInstanceFor(DetectUnresolvedHappenings.class);
				return new ScheduleHappening(startStep, protagonist.getLabel());
			}
		}
		
		// no narrative equilibria identified
		return null;
	}
}
