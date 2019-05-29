package inBloom.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.CounterfactualitySpaceSearchCycle;
import inBloom.ERcycle.PlotCycle.*;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.Tellability;
import inBloom.stories.little_red_hen.FarmEnvironment;
import inBloom.stories.little_red_hen.FarmModel;
import inBloom.stories.little_red_hen.RedHenCounterfactualityCycle;
import inBloom.stories.little_red_hen.RedHenPersonalityCycle;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;
import jason.util.Pair;

/**
 * Responsible for creating counterfactual plotlines of a given <i>original</i> graph
 * Can be used in order to get the best counterfactual plot
 * not responsible for graphical realisation, only calculation
 * can be extended in order to calculate counterfactuals not only for different personalities
 * but also for different happenings
 * @author Julia Kaltenborn
 */
public class CounterfactualityLauncher implements EnvironmentListener {
	
	protected static Logger logger = Logger.getLogger(CounterfactualityLauncher.class.getName());
	boolean isRunning = true;
	protected static long TIMEOUT = -1;
	private PlotDirectedSparseGraph original = null;
	//HashMap with List of Personalities linked to a pair of a graph and a double
	//concerning the value of pleasure (or something else later)
	//private HashMap<List<?>, Pair<?, ?>> allCounterfacts = new HashMap<List<?>, Pair<?, ?>>();
	
	//all counterfactualus: a list of personalities is mapped to a double expressing tellability/counterfactuality
	private HashMap<List<?>, Double> allCf = new HashMap<List<?>, Double>();
	private PlotDirectedSparseGraph counterfact = null;
	private double upper;
	private double lower;
	private double step;
	private String agentSrc;
	private PlotLauncher<?, ?> runner;
	private PlotModel<?> model;
	private String[] agentNames;
	
	/**
	 * Constructor
	 * sets default values for CounterfactualityLauncher
	 * @param original : we need the original graph in
	 * order to calculate the counterfactual scenarios
	 */
	public CounterfactualityLauncher(PlotDirectedSparseGraph original, String[] agents, String agentSrc, PlotLauncher<?, ?> runner, PlotModel<?> model) {
		this.original = original;
		this.lower = -1.0;
		this.upper = 1.0;
		this.step = 1;
		this.agentSrc = agentSrc;
		this.runner = runner;
		this.model = model;
		this.agentNames = agents;
		
		//add all the other parameters
		logger.info("Constructer CounterfactualityLauncher was called");
	}
	
	/**
	 * one can choose in which range the personalities should be choosen
	 * and in which steps, otherwise default values are used,
	 * -1.0 for lower, +1.0 for upper and 0.1 as step
	 * @param lower
	 * @param upper
	 * @param step
	 * TODO if lower or upper is not [-1.0 , +1.0] or step > 2 -> throw exception
	 */
	public void setPersonalityRange(double lower, double upper, double step) {
		this.lower = lower;
		this.upper = upper;
		this.step = step;
	}
	
	/**
	 * calculates first the best graph and
	 * @return the best counterfactual graph
	 */
	public PlotDirectedSparseGraph getCounterfact() {
		this.calculateCounterfact();
		//question: will the return wait until calculateCounterfact() has finished?
		//alternative: make a run method for calculating and a getCounterfact() method
		return counterfact;
	}
	
	/**
	 * first calculates all Possible Graphs
	 * and then chooses the best graph
	 */
	public void calculateCounterfact() {
		//calculate all possible personalities
		int personalityNum = agentNames.length;
		double[] personalityValues = this.calcAllPersonalityValues();
		logger.info(Arrays.toString(personalityValues));
		//the specific launcher will give you names, source, etc.
		Personality[] personalitySpace = createPersonalitySpace(personalityValues);
		logger.info("I have procuded a list with all personalities");
		List<Personality[]> personalityList = createPlotSpace(personalitySpace, personalityNum, false);
		logger.info("I have combined the different personalities and I have a list of all possible Agentcombinations");

		
		//produce a plot directed sparse graph for each personality
		//calculate the tellability (later just replace that with counterfactuality -> original graph as parameter)
		//and save only the personality and the tellability
		Double lastValue = 0.0;
		List<LauncherAgent> result;
		for (int i = 0; i < personalityList.size(); i++) {
			Personality[] p = personalityList.get(i);
			logger.info(Arrays.toString(p));
			//we get also the first personality
			List<LauncherAgent> lagents = createAgents(p);
			logger.info("Launcher Agent is created");
			Tellability t = simulate(lagents);
			logger.info("Simulation done");
			//simulation is making problems!
			Double currentValue = t.compute();
			logger.info("Tellability computed");
			if(currentValue > lastValue) {
				result = lagents;
			}
			logger.info("Comparison done");
		}
		
		runner.reset();
		
		//create a plot with the winning launcher agents
		
		//result is the plotdirectedgraph
		
		
		//for each story one must only create a new StoryCounterfactualityLauncher
		//where we only add needed information
		
	}
	
	public List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters, boolean repeat) {
		//check out the real createPlotSpace.
		//we will only add the other personalities for the moment
		List<Personality[]> allPersonalityCombinations = new LinkedList<Personality[]>();
		
		if(!repeat) {
			Personality dog = new Personality(0, -1, 0, -0.7, -0.8);
			Personality cow = new Personality(0, -1, 0, -0.7, -0.8);
			Personality pig = new Personality(0, -1, 0, -0.7, -0.8);
			for(Personality pers : personalitySpace) {
				Personality[] persAdd = {pers, dog, cow, pig};
				allPersonalityCombinations.add(persAdd);
			}
			logger.info("Default Personalities created for PlotSpace");
			
		} else {
		
			logger.info("I start creating the plot space");
			List<int[]> values = allCombinations(characters, personalitySpace.length, repeat);
			logger.info("I have calculated all Combinations");
			logger.info("Now I start the for loop");
			for(int[] charPersonalities : values) {
				Personality[] personalityArray = new Personality[characters];
				for(int i = 0; i < characters; i++) {
					personalityArray[i] = personalitySpace[charPersonalities[i]];
				}
				allPersonalityCombinations.add(personalityArray);
			}
			logger.info("All personalities calculated for plot space");
		}
		return allPersonalityCombinations;
	}
	
	protected Personality[] createPersonalitySpace(double[] posVal){
		List<Personality> personalities = new LinkedList<Personality>();
		List<int[]> values = allCombinations(5, posVal.length, true);
		for(int[] ocean : values) {
			personalities.add(new Personality(
					posVal[ocean[0]],
					posVal[ocean[1]], 
					posVal[ocean[2]],
					posVal[ocean[3]],
					posVal[ocean[4]]));
		}
		Personality[] result = new Personality[personalities.size()];
		return personalities.toArray(result);
	}
	
	private List<int[]> allCombinations(int n, int k, boolean repeat) {
		List<int[]> res = new ArrayList<int[]>((int)Math.pow(k, n));
		allCombinations(new int[n], k, res, 0, 0, repeat);
		return res;
	}
	
	private void allCombinations(int[] v, int k, List<int[]> result, int index, int min, boolean repeat) {
		if(index == v.length) {
			result.add(v);
			return;
		}
		for(int i = min; i < k; i++) {
			v[index] = i;
			allCombinations(v.clone(), k, result, index + 1, repeat ? min : i, repeat);
		}
	}
	
	protected List<LauncherAgent> createAgents(Personality[] personalities){
		if(personalities.length != this.agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i], personalities[i]));
		}
		return agents;
	}
	
	protected Tellability simulate(List<LauncherAgent> lagents) {
		
		logger.info("We start to simulate");
		try {			
			Thread t = new Thread(new Cycle(runner, model, new String[0], lagents, agentSrc));
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		runner.deleteUserEnvironment();
		
		MASConsoleGUI.get().setPause(false);
		boolean hasAddedListener = false;
		long startTime = System.currentTimeMillis();
		//as long as the environment is running, we must sleep
		//only if it is paused, we can start doing our job
		while(isRunning) {
			try {
				//we add a listener to the environment such that we dont crash the whole process
				//when the environment is paused
				if(!hasAddedListener) {
					if(runner.getEnvironmentInfraTier() != null) {
						if(runner.getEnvironmentInfraTier().getUserEnvironment() != null) {
							runner.getUserEnvironment().addListener(this);
							runner.getUserEnvironment().showListeners();
							hasAddedListener = true;
						}
					}
				}
				//handle timeout
				if(TIMEOUT > -1 && (System.currentTimeMillis() - startTime) >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					isRunning = false;
					logger.info("TIMEOUT");
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				logger.info("InterruptedException");
			}
		}
		
		logger.info("Thread has started");
		PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();
		logger.info("we have got the graph!!!");
		//current bug: everything is empty!
		//funny thing: Sven has the same result: tellability is completely empty
		Tellability tel = PlotGraphController.getPlotListener().analyze(analyzedGraph);
		logger.info("We have got the tellability!!!");
		double showResult = tel.compute();
		logger.info(Double.toString(showResult));
		//runner.reset(); //not needed anymore, because done before
		isRunning = true;
		logger.info("Resetting the runner was successful!");
		return tel;
	}
	/**
	 * 
	 * @return all possible values one aspect of a personality can have
	 */
	public double[] calcAllPersonalityValues() {
		ArrayList<Double> allValues = new ArrayList<Double>();
		//TODO think about using BigDecimal for preventing rounding errors
		for(double i = this.lower; i <= this.upper; i += step) {
			allValues.add(i);
		}
		return allValues.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	
	public void chooseBestCounterfact(HashMap<List<?>, Double> allCounterfacts) {

	}
	
	//if simulation is paused -> then do your job
	@Override
	public void onPauseRepeat() {
		logger.info("Paused!");
		this.isRunning = false;
		
	}
	


}
