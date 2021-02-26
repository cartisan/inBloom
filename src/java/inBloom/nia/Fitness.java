package inBloom.nia;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.runtime.MASConsoleGUI;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.GraphAnalyzer;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.Tellability;
import inBloom.nia.utils.FileInterpreter;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;

@SuppressWarnings("rawtypes")
public class Fitness<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends PlotLauncher {
	public NIEnvironment<?, ?> EVO_ENV;
	public Tellability tellability;

	protected boolean isRunning = false;
	protected boolean set = false;
	protected boolean cleanup = true;

	protected boolean verbose;
	protected Level level;

	public Thread t;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	public static long TIMEOUT = 10000;

	/**
	 * Used to start a simulation for the execution of a NIA.
	 *
	 * @param environment
	 * @param verbose
	 * @param level
	 * @param showGui
	 */
	@SuppressWarnings("unchecked")
	public Fitness(NIEnvironment<?, ?> environment, boolean verbose, Level level){

		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
		this.setShowGui(false);
		this.verbose = verbose;
		this.level = level;
		this.cleanup = true;

	}

	/**
	 * Used to start a simulation from {@linkplain FileInterpreter}.
	 *
	 * @param environment
	 * @param verbose
	 * @param level
	 * @param showGui
	 */
	@SuppressWarnings("unchecked")
	public Fitness(NIEnvironment<?, ?> environment, boolean verbose, Level level, boolean showGui){
		this.EVO_ENV = environment;
		this.ENV_CLASS = environment.ENV_CLASS;
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

		if(simulation_length < 100) {
			PlotEnvironment.MAX_STEP_NUM = simulation_length;
		} else {
			// Upper Limit
			PlotEnvironment.MAX_STEP_NUM = 100;
		}

		// Deactivate MAX_REPEAT if needed, by commenting back in:
//		PlotEnvironment.MAX_REPEATE_NUM = -1;

		if(this.verbose) {
			System.out.println("Starting new Simulation with length: " + simulation_length);
		}

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
			this.t = new Thread(new Cycle(this, model, agents, this.EVO_ENV.agentSrc));
			this.t.start();
		} catch (JasonException e) {
			e.printStackTrace();
			this.pauseExecution();
		} catch (NullPointerException e) {
			e.printStackTrace();
			this.pauseExecution();
		} catch (Exception e) {
			e.printStackTrace();
			this.pauseExecution();
		}

		// Logging level
        Logger.getLogger("").setLevel(this.level);

		MASConsoleGUI.get().setPause(false);
		long startTime = System.currentTimeMillis();
		while(this.isRunning) {
			try {
				// Handle the timeout if it was set
				if(TIMEOUT > -1 && System.currentTimeMillis() - startTime >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					this.pauseExecution();
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.pauseExecution();
			} catch (NullPointerException e) {
				e.printStackTrace();
				this.pauseExecution();
			}
		}

		/*
		 * Get the plot graph and compute corresponding tellability
		 */
		GraphAnalyzer analyzer = new GraphAnalyzer(PlotGraphController.getPlotListener().getGraph(), null);
		PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();			// analysis results will be cloned into this graph

		individual.set_actualLength(this.getUserEnvironment().getStep());

		try {
			this.tellability = analyzer.runSynchronously(analyzedGraph);
			result = this.tellability.compute();
		} catch(RuntimeException e) {
			e.printStackTrace();
		}

		// Cleanup to avoid Fragments
		if(this.cleanup) {
			PlotGraphController.resetPlotListener();
			this.reset();
			this.t.stop();
		}

		if(this.verbose) {
			System.out.println("Finished after " + individual.get_actualLength() + " steps with Tellability Score: " + result);
		}

		return result;
	}

	@Override
	public void pauseExecution() {
		this.isRunning = false;
		super.pauseExecution();
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
				PlotLauncher.runner = this.runner;
				BaseCentralisedMAS.runner = this.runner;
				this.runner.initialize(this.args, this.model, this.agents, this.agSrc);
				this.runner.run();
			} catch (JasonException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
