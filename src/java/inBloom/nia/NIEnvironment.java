package inBloom.nia;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.ga.GeneticAlgorithm;
import inBloom.nia.pso.PSO;
import inBloom.nia.qso.QSO;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;

public abstract class NIEnvironment<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {

	// Environment Class is needed for Plot_Launcher
	public Class<?> ENV_CLASS;
	public String agentSrc;


	// Parameters for GA
	public int number_agents;
	public int number_staticHappenings;
	public int number_dynamicHappenings;

	/**
	 * Constructor
	 * @param:
	 * 			ENV_CLASS: The .class file of the Environment
	 * 			number_agents: The count of agents
	 * 			number_staticHappenings: The count of static happenings
	 * 			number_dynamicHappenings: The count of dynamic happenings
	 * 			individual_count: The size of the population
	 *
	 */
	public NIEnvironment(Class<?> ENV_CLASS, String agentSrc, int number_agents, int number_staticHappenings, int number_dynamicHappenings) {

		this.ENV_CLASS = ENV_CLASS;
		this.agentSrc = agentSrc;
		this.number_agents = number_agents;
		this.number_staticHappenings = number_staticHappenings;
		this.number_dynamicHappenings = number_dynamicHappenings;

	}

	/**
	 * Returns an instance of the default version of the genetic algorithm 
	 */
	public GeneticAlgorithm<?,?> get_GA(String[] args, int init_stepnumber, int individual_count, int number_selections){
		return new GeneticAlgorithm<EnvType,ModType>(args, this, this.number_agents, this.number_dynamicHappenings, init_stepnumber, individual_count, number_selections);
	}
	
	/**
	 * Returns an instance of the static version of the genetic algorithm 
	 */
	public GeneticAlgorithm<?,?> get_GA_static(String[] args, int init_stepnumber, int individual_count, int number_selections, double crossover_prob, double mutation_prob){
		return new GeneticAlgorithm<EnvType,ModType>(args, this, this.number_agents, this.number_dynamicHappenings, init_stepnumber, individual_count, number_selections, crossover_prob, mutation_prob);
	}

	/**
	 * Returns an instance of the floating parameter version of the genetic algorithm 
	 */
	public GeneticAlgorithm<?,?> get_GA_float(String[] args, int init_stepnumber, int individual_count, int number_selections, double decay_rate){
		return new GeneticAlgorithm<EnvType,ModType>(args, this, this.number_agents, this.number_dynamicHappenings, init_stepnumber, individual_count, number_selections, decay_rate);
	}

	/**
	 * Returns the particle swarm optimization algorithm class in order to further customize the settings
	 */
	public PSO<?,?> get_PSO(String[] args, int init_stepnumber, int individual_count){

		return new PSO<EnvType,ModType> (args, this, this.number_agents, this.number_dynamicHappenings, init_stepnumber, individual_count);
	}

	/**
	 * Returns the quantum swarm optimization algorithm class in order to further customize the settings
	 */
	public QSO<?,?> get_QSO(String[] args, int init_stepnumber, int individual_count){

		return new QSO<EnvType,ModType> (args, this, this.number_agents, this.number_dynamicHappenings, init_stepnumber, individual_count);
	}

	/* _____________________________________________________________________________________________
	 *
	 * The following methods must be implemented for the Evolutionary Algorithms in order to work.
	 * They provide the domain specific knowledge, which is needed to start the PlotLauncher.runner.
	 * All methods get called by the Fitness Class
	 * _____________________________________________________________________________________________
	 */


	/**
	 * Instantiate agents with Personality values from GA
	 * @param: personality values. 5 doubles for each person e [-1;1]
	 * @return: Immutable List containing instantiated agents
	 */
	public abstract List<LauncherAgent> init_agents(double[][] personality);


	/**
	 * Instantiate happenings with timesteps provided by GA
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		happenings = Points in time where a happening occurs to a specific person
	 * @return: Immutable List with instantiated happenings which will be handed over to ScheduledHappeningDirector
	 */
	public List<Happening<?>> init_happenings(List<LauncherAgent> agents, int[][] happenings){

		List<Happening<?>> happeningList = new ArrayList<>();

		// Get static Happenings
		if(this.number_staticHappenings > 0) {
			happeningList.addAll(this.init_staticHappenings(agents));
		}

		// Hand over Informations of the GA in order to instantiate the dynamic happenings
		for(int agentIndex=0; agentIndex<this.number_agents; agentIndex++) {

			for(int happeningIndex=0; happeningIndex<this.number_dynamicHappenings; happeningIndex++) {

				if(happenings[agentIndex][happeningIndex] > 0) {

					happeningList.add(this.init_dynamicHappening(agents.get(agentIndex), happeningIndex, happenings[agentIndex][happeningIndex]));

				}
			}
		}

		ImmutableList<Happening<?>> immutable = ImmutableList.copyOf(happeningList);
		return immutable;
	}


	/**
	 * Initializes all static Happenings
	 * @param:	agents = Immutable List containing instantiated agents provided by init_agents
	 * @return: ImmutableList of all static Happenings
	 */
	public abstract List<Happening<?>> init_staticHappenings(List<LauncherAgent> agents);


	/**
	 * Initializes all dynamic Happenings
	 * @param:
	 * 			agent = LauncherAgent being the patient of a certain Happening
	 * 			hIndex = The type of Happening is determined by it's position in the chromosome
	 * 			step = time step at which the happening shall occur
	 * @return:
	 * 			Returns a instantiated Happening
	 */
	public abstract Happening<?> init_dynamicHappening(LauncherAgent agent, int hIndex, int step);


	/**
	 * Instantiate the PlotModel
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		hapDir = ScheduledHappeningDirector. happenings will already be initialized
	 * @return:
	 * 		returns the instantiated PlotModel
	 */
	public abstract PlotModel<EnvType> init_model(List<LauncherAgent> agents, ScheduledHappeningDirector hapDir);


	/**
	 * Set default Locations of the agents according to the model
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		model = Instantiated PlotModel provided by init_model
	 */
	public abstract void init_location(List<LauncherAgent> agents, PlotModel<?> model);
}
