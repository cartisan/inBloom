package plotmas.stories.little_red_hen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.ERcycle.DetectInsufficientCoupling;
import plotmas.ERcycle.DetectNarrativeEquilibrium;
import plotmas.ERcycle.PlotCycle;
import plotmas.ERcycle.ProblemDetectionState;
import plotmas.ERcycle.ProblemFixCommand;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.helper.Tellability;
import plotmas.storyworld.ScheduledHappeningDirector;

public class RedHenHappeningCycle extends PlotCycle {

	public static final double THRESHOLD = 0.9;
	public static final int GIVE_UP = 9;
	
	/** current state of reasoning cycle responsible for detecting plot problems */
	protected ProblemDetectionState detectionState;
	
	/** ordered list of transformation commands identified during reflection and applied during engagement */
	protected List<ProblemFixCommand> transformations = new LinkedList<>();
	
	
	public RedHenHappeningCycle(String[] agentNames, String agentSrc) {
		// Create PlotCycle with needed agents.
		super(agentNames, agentSrc);
		
		ProblemDetectionState s1 = ProblemDetectionState.getInstance(DetectNarrativeEquilibrium.class, this);
		ProblemDetectionState s2 = ProblemDetectionState.getInstance(DetectInsufficientCoupling.class, this);
		
		s1.nextReflectionState = s2;
		s2.nextReflectionState = s1;
		
		this.detectionState = s1;
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
	
	public void setNextDetectionState(ProblemDetectionState detectionState) {
		this.detectionState = detectionState;
	}
	
	public List<ProblemFixCommand> getTransformations(){
		return this.transformations;
	}
	
	public static void main(String[] args) {
		TIMEOUT = 1000;
		RedHenHappeningCycle cycle = new RedHenHappeningCycle(new String[] { "hen", "dog", "cow", "pig" },
															  "agent");
		cycle.run();

	}

}
