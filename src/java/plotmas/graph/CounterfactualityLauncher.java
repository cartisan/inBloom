package plotmas.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import jason.asSemantics.Personality;
import jason.util.Pair;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.PlotModel;
//import plotmas.helper.Counterfactuality;
import plotmas.ERcycle.CounterfactualitySpaceSearchCycle;
import plotmas.stories.little_red_hen.RedHenCounterfactualityCycle;

/**
 * Responsible for creating counterfactual plotlines of a given <i>original</i> graph
 * Can be used in order to get the best counterfactual plot
 * not responsible for graphical realisation, only calculation
 * can be extended in order to calculate counterfactuals not only for different personalities
 * but also for different happenings
 * @author Julia Kaltenborn
 */
public class CounterfactualityLauncher {
	
	protected static Logger logger = Logger.getLogger(CounterfactualityLauncher.class.getName());
	private PlotDirectedSparseGraph original = null;
	//HashMap with List of Personalities linked to a pair of a graph and a double
	//concerning the value of pleasure (or something else later)
	private HashMap<List<?>, Pair<?, ?>> allCounterfacts = new HashMap<List<?>, Pair<?, ?>>();
	private PlotDirectedSparseGraph counterfact = null;
	private double upper;
	private double lower;
	private double step;
	private String agentSrc;
//	private PlotLauncher<?, ?> runner;
//	private PlotModel<?> model;
	private String[] agents;
	
	/**
	 * Constructor
	 * sets default values for CounterfactualityLauncher
	 * @param original : we need the original graph in
	 * order to calculate the counterfactual scenarios
	 */
	public CounterfactualityLauncher(PlotDirectedSparseGraph original, String[] agents, String agentSrc) {
		this.original = original;
		this.lower = -1.0;
		this.upper = 1.0;
		this.step = 1;
		this.agentSrc = agentSrc;
//		this.runner = runner;
//		this.model = model;
		this.agents = agents;
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
		return counterfact;
	}
	
	/**
	 * calculates first all Possible Graphs
	 * and than chooses the best graph
	 */
	public void calculateCounterfact() {
		//TODO do some fancy calculation stuff -> create all possible counterfactuals
		//TODO get the information from the original graph
		int personalityNum = agents.length;
		double[] personalityValues = this.calcAllPersonalityValues();
		logger.info(Arrays.toString(personalityValues));
		//if we use higher values than 2, then the algorithm needs years
		//CounterfactualitySpaceSearchCycle cfSearchSpace = new CounterfactualitySpaceSearchCycle(agents, agentSrc, 1, personalityValues);
		
		//example red hen, best tellability
		//ideal case: best tellability is produced here
		RedHenCounterfactualityCycle cfSearchSpace = new RedHenCounterfactualityCycle(personalityValues);
		
		//this is working
		List<Personality[]> allPersonalities = cfSearchSpace.getPersonalityList();
		logger.info("I have procuded a list with all personalities");
		
		//something in reflect is not working
		
		//later on I will change the reflect cycle and the engage part such
		//that the story with highest counterfactuality is choosen
		
		//at the moment we try it only with best tellability
		
		//if we get best personalities from that,
		//we produce the corresponding graph 
		//and do fancy graphic stuff with that
		
		//for each story one must only create a new StoryCounterfactualityLauncher
		//where we only add needed information
		chooseBestCounterfact(allCounterfacts);
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
	
	/**this method will be obsolete and we access Counterfactuality immediately **/
	//TODO how to use HashMaps best
	public void chooseBestCounterfact(HashMap<List<?>, Pair<?,?>> allCounterfacts) {
		//take all the double values about pleasure and
		//compare them to the pleasure value of the original graph
		//how is this comparison done?
		//we create the object counterfactuality and we give it always
		//two graphs (or a whole bunch of graphs and the original)
		//so we just have to create a new object here!
		
//		Counterfactuality cf = new Counterfactuality(allCounterfacts);
//		cf.chooseBestCounterfact();
		
		/** thats all for the class counterfactuality**/
		//TODO make a gatherer and choose the one farest away from original!
		//with the aid of a for loop
	}
	
		/** ATTENTION: new approach: we take the original graph and create a counterfactualitySpaceSearch
		 * The Rest is done over there (also choosing the best one)
		 * 
		 * What we do here:
		 * 1) calculating all possible personalityValues
		 * 2) getting the agent names and how much there are
		 * 3) getting the source of the agents
		 * 
		 * And actually really calculating which one we gonna choose!
		 * counterfactuality will have a lot of different methods and we can
		 * choose which one to use
		 * 
		 * We should think about implementing our own CounterfactualResult
		 * instead of EngageResult
		 */

}
