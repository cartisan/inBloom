package inBloom.evo;

import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.GraphAnalyzer;
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
	
	public EvolutionaryEnvironment<?, ?> EVO_ENV;
	private static String[] args = {};

	protected static boolean isRunning = false;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = -1;

	@SuppressWarnings("unchecked")
	public Fitness(EvolutionaryEnvironment<?, ?> environment){
		
		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		//this.args = args;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(false);
		
	}
	
	@SuppressWarnings("unchecked")
	public Fitness(EvolutionaryEnvironment<?, ?> environment, boolean showGui){
		
		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		//this.args = args;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(showGui);
		
	}
	
	public double evaluate_individual(Individual individual) throws JasonException {
		
		// Initialize Parameters
		double result = 0;
		isRunning = true;
		Integer simulation_length = individual.get_simLength();
		
		// Instantiate Objects with methods of GeneticEnvironment
		ImmutableList<LauncherAgent> agents = EVO_ENV.init_agents(individual.get_personality().values);
		ImmutableList<Happening> happenings = EVO_ENV.init_happenings(agents, individual.get_happenings().values);
		
		// Initialize MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
		
		for(Happening<?> happening : happenings) {
			
			hapDir.scheduleHappening(happening);
			
		}

		// Initialize PlotModel and set agent.location
		PlotModel model = EVO_ENV.init_model(agents, hapDir);
		
		EVO_ENV.init_location(agents, model);
		
		/*
		 * From PlotCycle.java :
		 */
		
		try {
			Thread t = new Thread(new Cycle(runner, model, args, agents, EVO_ENV.agentSrc));
			t.start();
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
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
				e.printStackTrace();
			} catch (NullPointerException e) {
			}
		}

		/*
		 * Get the plot graph and compute corresponding tellability 
		 */
		
		GraphAnalyzer analyzer = new GraphAnalyzer(PlotGraphController.getPlotListener().getGraph(), null);
		PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();			// analysis results will be cloned into this graph
		
		try {
			
			Tellability tel = analyzer.runSynchronously(analyzedGraph);
			result = tel.compute();
			
		}catch(RuntimeException e) {
			pauseExecution();
		}
		
		// Ensure Termination
		//reset();

		return result;
	}
	
	@Override
	public void pauseExecution() {
		
		if(!showGui) {
			MASConsoleGUI.get().setPause(true);
			runner.reset();
		}
	}
	
	@Override
	public void onPauseRepeat() {
		
		isRunning = false;
	}
	
	// Reset() without NullPointerException
	@Override
	public void reset() {
		
		if(!showGui) {
			
			if (control != null) {
    		control.stop();
	    	}
	    	if (env != null) {
	    		env.stop();
	    	}
	    	stopAgs();
		}
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
				//e.printStackTrace();
			} catch (NullPointerException e) {
				//e.printStackTrace();
			}
		}
	}
}
