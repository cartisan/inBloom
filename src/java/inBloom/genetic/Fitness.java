package inBloom.genetic;

import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.ReflectResult;
import inBloom.ERcycle.PlotCycle.Cycle;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.Tellability;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.environment.TimeSteppedEnvironment;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.runtime.MASConsoleGUI;

public class Fitness<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends PlotLauncher implements EnvironmentListener {
	
	public GeneticEnvironment<?, ?> GEN_ENV;
	//private String[] args = {};
	private String[] args = {PlotLauncher.DEAULT_FILE_NAME, "","20","-1"};

	protected boolean isRunning = true;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = -1;

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
		
		/*
		 * From PlotCycle.java
		 */
		double result = -1;
		
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
		
		// Get graph and compute tellability
		
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();	
		Tellability tellability = PlotGraphController.getPlotListener().analyze(graph);
		result = tellability.compute();

		runner.reset();
		
		return result;
	}
	
	@Override
	public void onPauseRepeat() {
		this.isRunning = false;
	}
	
	
	/**
	 * Runnable for a single simulation.
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
			}
		}
	}
}

// How to get this into Environment.class?

// number of times all agents need to repeat an action sequence before system is paused; -1 to switch off */
//public static final Integer MAX_REPEATE_NUM = 5;
// number of environment steps, before system automatically pauses; -1 to switch off */
//public static Integer MAX_STEP_NUM = -1;
// time in ms that {@link TimeSteppedEnvironment} affords agents to propose an action, before each step times out */
//static final String STEP_TIMEOUT = "300";
