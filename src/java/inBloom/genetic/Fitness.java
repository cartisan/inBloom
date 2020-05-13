package inBloom.genetic;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.ReflectResult;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.Tellability;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.infra.centralised.BaseCentralisedMAS;

public class Fitness<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends PlotLauncher {
	
	public GeneticEnvironment<?, ?> GEN_ENV;
	private String[] args;
	
	public Fitness(String[] args, GeneticEnvironment<?, ?> environment){
		
		this.GEN_ENV = environment;
		this.args = args;
		this.ENV_CLASS = environment.ENV_CLASS;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.showGui = false;
		
	}
	
	public double evaluate_Candidate(Candidate candidate) throws JasonException {
		
		// Instantiate Objects with methods of GeneticEnvironment
		ImmutableList<LauncherAgent> agents = GEN_ENV.init_agents(candidate.get_personality().values);
		ImmutableList<Happening> happenings = GEN_ENV.init_happenings(agents, candidate.get_happenings().values);
		
		// Initialize MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		
		for(Happening happening : happenings) {
			
			hapDir.scheduleHappening(happening);
			
		}
		
		//Initialize PlotModel and set agent.location
		PlotModel model = GEN_ENV.init_model(agents, hapDir);
		
		GEN_ENV.init_location(agents, model);
		
		// run simulation with characters and plot
		try {
			runner.initialize(args, model, agents, GEN_ENV.agentSrc);
			runner.run();
		} catch (JasonException e) {
			e.printStackTrace();
		}
		/*
		 * Does this already work?
		 */
		
		PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();	
		Tellability tellability = PlotGraphController.getPlotListener().analyze(analyzedGraph);
		double result = tellability.compute();
		
		return result;
	}
}
