package inBloom.genetic;

import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.Tellability;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.runtime.MASConsoleGUI;

@SuppressWarnings("rawtypes")
public class Fitness<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends PlotLauncher implements EnvironmentListener {
	
	public GeneticEnvironment<?, ?> GEN_ENV;
	private static String[] args = {};

	protected static boolean isRunning = false;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = -1;

	@SuppressWarnings("unchecked")
	public Fitness(GeneticEnvironment<?, ?> environment){
	//public Fitness(String[] args, GeneticEnvironment<?, ?> environment){
		
		this.GEN_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		//this.args = args;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(false);
		
	}
	
	public double evaluate_Candidate(Candidate candidate) throws JasonException {
		
		// Initialize Parameters
		double result = -1;
		isRunning = true;
		Integer simulation_length = candidate.get_simLength();
		
		// Instantiate Objects with methods of GeneticEnvironment
		ImmutableList<LauncherAgent> agents = GEN_ENV.init_agents(candidate.get_personality().values);
		ImmutableList<Happening> happenings = GEN_ENV.init_happenings(agents, candidate.get_happenings().values);
		
		// Initialize MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		
		for(Happening<?> happening : happenings) {
			
			hapDir.scheduleHappening(happening);
			
		}

		// Initialize PlotModel and set agent.location
		PlotModel model = GEN_ENV.init_model(agents, hapDir);
		
		GEN_ENV.init_location(agents, model);
		
		/*
		 * From PlotCycle.java :
		 */
		
		try {
			Thread t = new Thread(new Cycle(runner, model, args, agents, GEN_ENV.agentSrc));
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		MASConsoleGUI.get().setPause(false);
		boolean hasAddedListener = false;
		long startTime = System.currentTimeMillis();
		
		while(isRunning) {
			try {
				// This is needed in the loop, because the plot environment is null before starting
				if(!hasAddedListener) {
					if(runner.getEnvironmentInfraTier() != null) {
						if(runner.getEnvironmentInfraTier().getUserEnvironment() != null) {
					    	
							PlotEnvironment.MAX_REPEATE_NUM = -1;
							PlotEnvironment.MAX_STEP_NUM = simulation_length;
							
							runner.getUserEnvironment().addListener(this);
							hasAddedListener = true;
						}
					}
				}
				// Handle the timeout if it was set
				if(TIMEOUT > -1 && (System.currentTimeMillis() - startTime) >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					//log("[PlotCycle] SEVERE: timeout for engagement step triggered, analyzing incomplete story and moving on");
					isRunning = false;
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
			}
		}

		/*
		 * Get the plot graph and compute corresponding tellability 
		 */
		
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();	
		Tellability tellability = PlotGraphController.getPlotListener().analyze(graph);
		result = tellability.compute();
		
		/*
		 * End simulation and reset runner. 
		 */
		
		MASConsoleGUI.get().setPause(true);
		
		runner.reset();
		
		return result;
	}
	
	@Override
	public void onPauseRepeat() {
		isRunning = false;
	}
	
	// Reset() without NullPointerException
	@Override
	public void reset() {
    	if (control != null) {
    		control.stop();
    		control = null;
    	}
    	if (env != null) {
    		env.stop();
    		env = null;
    	}
    	stopAgs();
    }
	
	
	/**
	 * Runnable for a single simulation.
	 * From PlotCycle.java :
	 */
	public static class Cycle implements Runnable {
		
		private PlotLauncher<?, ?> runner;
		private PlotModel<?> model;
		private String[] args;
		private List<LauncherAgent> agents;
		private String agSrc;
		
		public Cycle(PlotLauncher<?, ?> runner, PlotModel<?> model, String[] args, List<LauncherAgent> agents, String agSrc) throws Exception {
			this.runner = runner;
			this.args = args;
			this.agents = agents;
			this.agSrc = agSrc;
			this.model = (PlotModel<?>) model.getClass().getConstructors()[0].newInstance(agents, model.happeningDirector);
			
			for(LauncherAgent ag : agents) {
				model.addCharacter(ag);
			}
		}
	
		@Override
		public void run() {
			try {
				runner.initialize(args, model, agents, agSrc);
				runner.run();
			} catch (JasonException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
