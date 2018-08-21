package plotmas.stories.little_red_hen;

import java.util.ArrayList;
import java.util.List;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotCycle;
import plotmas.PlotLauncher;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.helper.Tellability;
import plotmas.storyworld.ScheduledHappeningDirector;

public class RedHenHappeningCycle extends PlotCycle {

	public static final double THRESHOLD = -1;
	
	public RedHenHappeningCycle(String[] agentNames, String agentSrc) {
		// Create PlotCycle with needed agents.
		super(agentNames, agentSrc);
	}
	
	@Override
	protected ReflectResult reflect(EngageResult er) {
		log("Reflecting...");
		Tellability tellability = er.getTellability();
		PlotDirectedSparseGraph story = er.getPlotGraph();
		
		log("tellability of last engagement result: " + tellability.compute());
		
		// Check if we found an appropriate story
		if(tellability.compute() > THRESHOLD) {
			// signal ER cycle to stop
			return new ReflectResult(null, null, null, false);
		}
		
		// Change constraints based on reflection results
		// TODO: Schedule happenings
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		
		FarmModel model = new FarmModel(new ArrayList<LauncherAgent>(), hapDir);
		
		// TODO: fix agents
		List<LauncherAgent> agents = this.createAgs(new Personality[] {new Personality(0, 0, 0, 0, 0),
																	   new Personality(0, 0, 0, 0, 0),
															  		   new Personality(0, 0, 0, 0, 0), 
															  		   new Personality(0, 0, 0, 0, 0)});
		
		PlotLauncher<?, ?> runner = new RedHenLauncher();
		runner.setShowGui(false);
		return new ReflectResult(runner, model, agents);
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
		
		PlotGraphController graphView = PlotGraphController.fromGraph(er.getPlotGraph());
		graphView.visualizeGraph();
	}
	
	public static void main(String[] args) {
		TIMEOUT = 1000;
		RedHenHappeningCycle cycle = new RedHenHappeningCycle(new String[] { "hen", "dog", "cow", "pig" },
															  "agent");
		cycle.run();

	}

}
