package inBloom.nia;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.runtime.MASConsoleGUI;

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

@SuppressWarnings("rawtypes")
public class Fitness<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends PlotLauncher implements EnvironmentListener {

	public NIEnvironment<?, ?> EVO_ENV;

	protected boolean isRunning = false;
	protected boolean set = false;
	protected boolean cleanup = true;

	protected boolean verbose;
	protected Level level;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = 10000;

	@SuppressWarnings("unchecked")
	public Fitness(NIEnvironment<?, ?> environment, boolean verbose, Level level){

		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(false);
		this.verbose = verbose;
		this.level = level;
		this.cleanup = true;

	}

	@SuppressWarnings("unchecked")
	public Fitness(NIEnvironment<?, ?> environment, boolean verbose, Level level, boolean showGui){

		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
		this.setShowGui(showGui);
		this.verbose = verbose;
		this.level = level;
		this.cleanup = false;

	}

	public double evaluate_individual(CandidateSolution individual) throws JasonException {
		// Initialize Parameters
		double result = 0;
		this.isRunning = true;
		this.set = false;
		Integer simulation_length = individual.get_simLength();

		if(verbose)
			System.out.println("Starting new Simulation with length: " + simulation_length);
		
		// Instantiate Objects with methods of GeneticEnvironment
		List<LauncherAgent> agents = this.EVO_ENV.init_agents(individual.get_personality().values);
		List<Happening<?>> happenings = this.EVO_ENV.init_happenings(agents, individual.get_happenings().values);

		// Initialize MAS with a scheduled happening director
		ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();

		for(Happening<?> happening : happenings) {

			hapDir.scheduleHappening(happening);

		}

		// Initialize PlotModel and set agent.location
		PlotModel model = this.EVO_ENV.init_model(agents, hapDir);

		this.EVO_ENV.init_location(agents, model);

		/*
		 * From PlotCycle.java :
		 */

		try {
			Thread t = new Thread(new Cycle(runner, model, agents, this.EVO_ENV.agentSrc));
			t.start();
		} catch (JasonException e) {
			//e.printStackTrace();
			this.onPauseRepeat();
		} catch (NullPointerException e) {
			//e.printStackTrace();
			this.onPauseRepeat();
		} catch (Exception e) {
			//e.printStackTrace();
			this.onPauseRepeat();
		}

		// Logging level
        Logger.getLogger("").setLevel(level);

		MASConsoleGUI.get().setPause(false);
		boolean hasAddedListener = false;
		long startTime = System.currentTimeMillis();

		while(this.isRunning) {
			try {
				// This is needed in the loop, because the plot environment is null before starting
				if(!hasAddedListener) {
					if(runner.getEnvironmentInfraTier() != null) {
						if(runner.getEnvironmentInfraTier().getUserEnvironment() != null) {
					    	if(!this.set) {

					    		// Deactivate Max_Repeate feature
					    		// TODO: Activated again for TLRH
//								PlotEnvironment.MAX_REPEATE_NUM = -1;

								// Upper Limit
								if(simulation_length< 100) {
									PlotEnvironment.MAX_STEP_NUM = simulation_length;
								} else {
									PlotEnvironment.MAX_STEP_NUM = 100;
								}

								runner.getUserEnvironment().addListener(this);
								hasAddedListener = true;
								this.set=true;
					    	}
						}
					}
				}
				// Handle the timeout if it was set
				if(TIMEOUT > -1 && System.currentTimeMillis() - startTime >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					//log("[PlotCycle] SEVERE: timeout for engagement step triggered, analyzing incomplete story and moving on");
					this.isRunning = false;
					this.pauseExecution();
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				this.onPauseRepeat();
			} catch (NullPointerException e) {
				//e.printStackTrace();
				this.onPauseRepeat();
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
			this.pauseExecution();
		}
		// Cleanup to avoid Fragments
		if(this.cleanup) {
			PlotGraphController.resetPlotListener();
			analyzer = null;
			analyzedGraph = null;
			super.reset();
		}

		individual.set_actualLength(PlotEnvironment.MAX_STEP_NUM);

		if(verbose)
			System.out.println("Finished after " + PlotEnvironment.MAX_STEP_NUM +" steps with Tellability Score: " + result);
		
		return result;
	}

	// pauseExecution() with added functionality for showGUI=true
	@Override
	public void pauseExecution() {

		if(!this.showGui) {
			MASConsoleGUI.get().setPause(true);
			runner.reset();
		}else {
			this.reset();
		}
	}

	@Override
	public void onPauseRepeat() {

		this.isRunning = false;
	}

	// Reset() without NullPointerException
	@Override
	public void reset() {

		if(this.env != null) {
			this.env.stop();
		}
		if (this.control != null) {
    		this.control.stop();
    	}
    	this.stopAgs();
    	this.ags.clear();
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
				this.runner.initialize(this.args, this.model, this.agents, this.agSrc);
				this.runner.run();
			} catch (JasonException e) {
				//e.printStackTrace();
			} catch (NullPointerException e) {
				//e.printStackTrace();
			}
		}
	}
}
