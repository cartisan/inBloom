package plotmas.stories.little_red_hen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jason.asSemantics.Personality;
import jason.util.Pair;
import plotmas.LauncherAgent;
import plotmas.PlotCycle;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.helper.PlotpatternAnalyzer;
import plotmas.helper.Tellability;
import plotmas.storyworld.ScheduledHappeningDirector;

public class RedHenHappeningCycle extends PlotCycle {

	public static final double THRESHOLD = 0.21;
	public static final int GIVE_UP = 1;
	
	public RedHenHappeningCycle(String[] agentNames, String agentSrc) {
		// Create PlotCycle with needed agents.
		super(agentNames, agentSrc);
	}
	
	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("Reflecting...");
		Tellability tellability = er.getTellability();
		
		log("tellability of last engagement result: " + tellability.compute());
		
		// Check if we found an appropriate story
		if ((tellability.compute() > THRESHOLD) || (PlotCycle.currentCycle > GIVE_UP)){
			// signal ER cycle to stop
			return new ReflectResult(null, null, null, false);
		}
		
		// Change constraints based on reflection results
		this.detectAndFixProblems(er);		// Changes params in er.agents and er.hapDir

		
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), er.getLastModel().happeningDirector);
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		return new ReflectResult(runner, model, er.getLastAgents());
	}

	/**
	 * Detects which problems a plot has, determines which plotting strategy can be applied to solve it, and changes
	 * simulation parameters according to one strategy. 
	 * @param tellability Statistics of last simulation, based on EngageResult
	 * @param agents Agent parameters that were used in last simulation, might be changed by this method
	 * @param hapDir Happening parameters that were used in last simulation, might be changed by this method
	 */
	private void detectAndFixProblems(EngageResult er) {
		// detect narrative equilibrium
		Vertex protagonist = detectPotentialProtagonist(er);
		List<String> events = new LinkedList<>();
		Map<Integer,Integer> positionStepMap = new HashMap<>();		// maps position of events in story-string to step when they occurred
		for (Vertex v : er.getPlotGraph().getCharSubgraph(protagonist)) {
			events.add(v.getLabel());
			int startPos = events.stream().collect(Collectors.joining(PlotpatternAnalyzer.EVENT_SEP)).length() - v.getLabel().length(); 
			positionStepMap.put(startPos, v.getStep());
		}
		
		HashMap<String, Integer> patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		for (Entry<String,Integer> patternCount : patternCounts.entrySet()) {
			if (patternCount.getValue() >= PlotEnvironment.MAX_REPEATE_NUM) {
				// found narrative equilibrium
				Pattern pattern = Pattern.compile("(" + Pattern.quote(patternCount.getKey() + PlotpatternAnalyzer.EVENT_SEP) + ")+");
				Pair<Integer,Integer> location = PlotpatternAnalyzer.patternLocation(events, pattern);
				
				// schedule happening to occur one step after start of previous equilibrium state
				Integer startStep = positionStepMap.get(location.getFirst()) + 1;
				
				// TODO: Select happening from catalog
				FindCornHappening findCorn = new FindCornHappening(
						// protagonist finds wheat to prevent equilibrium
						(FarmModel model) -> {
			            		if((model.getStep() >= startStep) & (!model.wheatFound)) {
			            			return true;
			            		}
			            		return false; 
			    		},
						protagonist.getLabel(),
						"");
				
				
				((ScheduledHappeningDirector) er.getLastModel().happeningDirector).scheduleHappening(findCorn);
				
				// TODO: adapt protagonist's personality
				break;		// enough to resolve first equilibrium
			}
		}
		
		// TODO detect lack of coupling 
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
			if (v.getLabel() == protagonist) {
				root = v;
				break;
			}
			throw new RuntimeException("Agent: " + protagonist + " not found in plot graph");
		}
		return root;
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		log("Creating start configuration");
		
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		
		// start with model that has no happenings
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), new ScheduledHappeningDirector());
		
		// start with neutral personalities
		List<LauncherAgent> startAgents = this.createAgs(new Personality[] {new Personality(0, 0, 0, 0, 0),
															  				new Personality(0, 0, 0, 0, 0),
															  				new Personality(0, 0, 0, 0, 0), 
															  				new Personality(0, 0, 0, 0, 0)});
		
		return new ReflectResult(runner, model, startAgents);
	}

	@Override
	protected void finish(EngageResult er) {
		log("Le fin");
		log("Displaying resulting story...");
		
		PlotGraphController graphViewer = new PlotGraphController();
		for (PlotDirectedSparseGraph graph : stories) {
			graphViewer.addGraph(graph);
			graphViewer.setSelectedGraph(graph);
		}
		
		// for last graph
		for (FunctionalUnit fu : er.getTellability().plotUnitTypes) {
			graphViewer.addDetectedPlotUnitType(fu);
		}
		graphViewer.addInformation("#Functional Units: " + er.getTellability().numFunctionalUnits);
		graphViewer.addInformation("Highlight Units:", graphViewer.getUnitComboBox());
		graphViewer.addInformation("#Polyvalent Vertices: " + er.getTellability().numPolyvalentVertices);
		graphViewer.addInformation("Suspense: " + er.getTellability().suspense);
		graphViewer.addInformation("Tellability: " + er.getTellability().compute());
		
		graphViewer.visualizeGraph();
	}
	
	public static void main(String[] args) {
		TIMEOUT = 1000;
		RedHenHappeningCycle cycle = new RedHenHappeningCycle(new String[] { "hen", "dog", "cow", "pig" },
															  "agent");
		cycle.run();

	}

}
