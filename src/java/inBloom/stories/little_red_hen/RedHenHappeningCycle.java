package inBloom.stories.little_red_hen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.ERcycle.DetectLackingAdversity;
import inBloom.ERcycle.DetectNarrativeEquilibrium;
import inBloom.ERcycle.PlotCycle;
import inBloom.ERcycle.ProblemDetectionState;
import inBloom.ERcycle.ProblemFixCommand;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

public class RedHenHappeningCycle extends PlotCycle {

	public static final double THRESHOLD = 0.9;
	public static final int GIVE_UP = 7;
	
	/** current state of reasoning cycle responsible for detecting plot problems */
	protected ProblemDetectionState detectionState;
	
	/** ordered list of transformation commands identified during reflection and applied during engagement */
	protected List<ProblemFixCommand> transformations = new LinkedList<>();
	
	/** set of domain-specific happenings allowed to be scheduled by the ER Cycle */
	public HashSet<Class<?>> availableHappenings = new HashSet<>();
	
	/** current number of characters in simulations */
	public int charCount;
	
	
	public RedHenHappeningCycle(String agentSrc) {
		// Instantiate PlotCycle
		super(agentSrc);
		
		// Setup standard reasoning cycle
		ProblemDetectionState s1 = ProblemDetectionState.getInstance(DetectNarrativeEquilibrium.class, this);
		ProblemDetectionState s2 = ProblemDetectionState.getInstance(DetectLackingAdversity.class, this);
		s1.nextReflectionState = s2;
		s2.nextReflectionState = s1;
		
		this.detectionState = s1;
		
		// set up domain specific set of allowed happenings
		this.availableHappenings.add(FindCornHappening.class);
	}
	
	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("  Reflecting...");
		Tellability tellability = er.getTellability();
		
		log("    tellability of last engagement result: " + tellability.compute());
		
		// Check if we found an appropriate story
		if ((tellability.compute() > THRESHOLD) || (PlotCycle.currentCycle > GIVE_UP)){
			// signal ER cycle to stop
			return new ReflectResult(null, null, null, false);
		}
		
		// start state machine that detects plot problems, detections states return a fix and change the
		// the detectionState to the next state
		ProblemFixCommand problemFix = null;
		int counter = 0;
		while( (problemFix == null) & (counter < 6)) {
			counter++;
			log("    Testing for plot problems: " + detectionState.getClass().getSimpleName());
			problemFix = detectionState.detect(er);		// this has to always set the next detection state!
		}
		
		if (problemFix != null) {
			log("    Suggesting fix: " + problemFix.message());
			problemFix.execute(er);
			this.transformations.add(problemFix);
		} else {
			// signal ER cycle to stop cause no fixable problems were detected
			// TODO: possibly backtrack problem fixes, or perform a random change
			return new ReflectResult(null, null, null, false);
		}

		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), er.getLastModel().happeningDirector.clone());
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		return new ReflectResult(runner, model, er.getLastAgents());
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		log("Creating start configuration");
		
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		
		// start with model that has no happenings
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), new ScheduledHappeningDirector());
		
		// start with neutral personalities
		List<LauncherAgent> startAgents = this.createAgs(new String[]{"protagonist"}, 
														 new Personality[] {new Personality(0, 0, 0, 0, 0)});
		this.charCount = startAgents.size();
		
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
	
	public void setNextDetectionState(ProblemDetectionState detectionState) {
		this.detectionState = detectionState;
	}
	
	public List<ProblemFixCommand> getTransformations(){
		return this.transformations;
	}
	
	/**
	 * Changes the character count by adding 'number'. To decrease character count, number should be negative.
	 * @param number
	 */
	public void updateCharCount(int number) {
		this.charCount += number;
	}
	
	public void undoLastFix(EngageResult er) {
		ProblemFixCommand lastFix = this.transformations.get(this.transformations.size() - 1);
		lastFix.undo(er);
		this.transformations.remove(lastFix);
	}
	
	public static void main(String[] args) {
		RedHenHappeningCycle cycle = new RedHenHappeningCycle("agent");
		cycle.run();
	}

}
