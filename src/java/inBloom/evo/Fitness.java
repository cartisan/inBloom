package inBloom.evo;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	protected boolean isRunning = false;
	protected boolean set = false;
	protected boolean cleanup = true;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = -1;

	@SuppressWarnings("unchecked")
	public Fitness(EvolutionaryEnvironment<?, ?> environment){
		
		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(false);
		cleanup = true;
		
	}
	
	@SuppressWarnings("unchecked")
	public Fitness(EvolutionaryEnvironment<?, ?> environment, boolean showGui){
		
		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(showGui);
		cleanup = false;
		
	}
	
	public double evaluate_individual(Individual individual) throws JasonException {
		
		// Initialize Parameters
		double result = 0;
		isRunning = true;
		set = false;
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
			Thread t = new Thread(new Cycle(runner, model, agents, EVO_ENV.agentSrc));
			t.start();
		} catch (JasonException e) {
			//e.printStackTrace();
			onPauseRepeat();
		} catch (NullPointerException e) {
			//e.printStackTrace();
			onPauseRepeat();
		} catch (Exception e) {
			//e.printStackTrace();
			onPauseRepeat();
		}
		
		// Logging lvl
        Logger.getLogger("").setLevel(Level.FINE);
        
		MASConsoleGUI.get().setPause(false);
		boolean hasAddedListener = false;
		long startTime = System.currentTimeMillis();
		
		while(isRunning) {
			try {
				// This is needed in the loop, because the plot environment is null before starting
				if(!hasAddedListener) {
					if(runner.getEnvironmentInfraTier() != null) {
						if(runner.getEnvironmentInfraTier().getUserEnvironment() != null) {
					    	if(!set) {
					    		
					    		// Deactivate Max_Repeate feature
								PlotEnvironment.MAX_REPEATE_NUM = -1;
								
								// Upper Limit
								if(simulation_length< 100)
									PlotEnvironment.MAX_STEP_NUM = simulation_length;
								else
									PlotEnvironment.MAX_STEP_NUM = 100;
								
								runner.getUserEnvironment().addListener(this);
								hasAddedListener = true;
								set=true;
					    	}
						}
					}
				}
				// Handle the timeout if it was set
				if(TIMEOUT > -1 && (System.currentTimeMillis() - startTime) >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					//log("[PlotCycle] SEVERE: timeout for engagement step triggered, analyzing incomplete story and moving on");
					isRunning = false;
					pauseExecution();
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				onPauseRepeat();
			} catch (NullPointerException e) {
				//e.printStackTrace();
				onPauseRepeat();
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
		// Cleanup to avoid Fragments
		if(cleanup) {
			PlotGraphController.resetPlotListener();
			analyzer = null;
			analyzedGraph = null;
			super.reset();
		}
		
		individual.set_actualLength(PlotEnvironment.MAX_STEP_NUM);
		
		return result;
	}
	
	// pauseExecution() with added functionality for showGUI=true
	@Override
	public void pauseExecution() {

		if(!showGui) {
			MASConsoleGUI.get().setPause(true);
			runner.reset();
		}else {
			reset();
		}
	}
	
	@Override
	public void onPauseRepeat() {
		
		isRunning = false;
	}
	
	// Reset() without NullPointerException
	@Override
	public void reset() {
		
		if(env != null) {
			env.stop();
		}
		if (control != null) {
    		control.stop();
    	}
    	stopAgs();
    	ags.clear();
    }
	
	
	/**
	 * Runnable for a single simulation.
	 * From PlotCycle.java :
	 */
	public static class Cycle implements Runnable {
		
		private PlotLauncher<?, ?> runner;
		private PlotModel<?> model;
		private String[] args = new String[0];
		private List<LauncherAgent> agents;
		private String agSrc;
		
		public Cycle(PlotLauncher<?, ?> runner, PlotModel<?> model, List<LauncherAgent> agents, String agSrc) throws Exception {
			this.runner = runner;
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
