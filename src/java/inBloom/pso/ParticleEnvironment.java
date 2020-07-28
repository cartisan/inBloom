package inBloom.pso;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.storyworld.Happening;
import inBloom.storyworld.ScheduledHappeningDirector;

@SuppressWarnings("rawtypes")
public abstract class ParticleEnvironment <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	
	// Environment Class is needed for Plot_Launcher
	public Class<?> ENV_CLASS;
	public String agentSrc;
	
	// Parameters for GA
	public int number_agents;
	public int number_staticHappenings;
	public int number_dynamicHappenings;
	public int max_steps;
	public int particle_count;
	
	// Array to activate different genetic operators?
	// public boolean[] operators
	
	/*
	 * Constructor
	 * @param:
	 * 			ENV_CLASS: The .class file of the Environment
	 * 			number_agents: The count of agents 
	 * 			number_staticHappenings: The count of static happenings 
	 * 			number_dynamicHappenings: The count of dynamic happenings
	 * 			pop_size: The size of the population
	 * 			crossover_prob: The probability that a crossover operation on an allele will take place
	 * 			mutation_prob: The probability that a mutation operation on an allele will take place
	 * 
	 */
	
	public ParticleEnvironment(Class<?> ENV_CLASS, String agentSrc, int number_agents, int number_staticHappenings, int number_dynamicHappenings, int max_steps, int particle_count) {
		
		this.ENV_CLASS = ENV_CLASS;
		this.agentSrc = agentSrc;
		this.number_agents = number_agents;
		this.number_dynamicHappenings = number_dynamicHappenings;
		this.number_staticHappenings = number_staticHappenings;
		this.max_steps = max_steps;
		this.particle_count = particle_count;
		
	}

	
	/*
	 * Constructor with standard values for GA Parameters
	 */
	
	public ParticleEnvironment(Class<?> ENV_CLASS, String agentSrc, int number_agents, int number_staticHappenings, int number_dynamicHappenings, int max_steps) {
		
		this.ENV_CLASS = ENV_CLASS;
		this.agentSrc = agentSrc;
		this.number_agents = number_agents;
		this.number_dynamicHappenings = number_dynamicHappenings;
		this.number_staticHappenings = number_staticHappenings;
		this.max_steps = max_steps;
		this.particle_count = 20;	
		
	}

	
	/*
	 * Hands over informations to start the PSO algorithm
	 */
	
	public void run_PSO(String[] args, int time) {

		PSO<EnvType,ModType> pso = new PSO<EnvType,ModType> (args, this, this.number_agents, this.number_dynamicHappenings, this.max_steps, this.particle_count);
		pso.setMaxRuntime(time);
		pso.run();
		
	}
	
	
	/*
	 * Returns the PSO class in order to further customize the settings
	 */
	
	public PSO get_PSO(String[] args){
			
		return new PSO<EnvType,ModType> (args, this, this.number_agents, this.number_dynamicHappenings, this.max_steps, this.particle_count);
	}
	
	
	/* _____________________________________________________________________________________________
	 * 
	 * The following methods must be implemented for the PSO algorithm in order to work.
	 * They provide the domain specific knowledge, which is needed to start the PlotLauncher.runner.
	 * All methods get called by the Fitness Class  
	 * _____________________________________________________________________________________________
	 */
	
	
	/*
	 * Instantiate agents with Personality values from GA
	 * @param: personality values. 5 doubles for each person e [-1;1]
	 * @return: Immutable List containing instantiated agents
	 */
	
	public abstract ImmutableList<LauncherAgent> init_agents(double[][] personality);
	
	
	/*
	 * Instantiate happenings with timesteps provided by GA
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		happenings = Points in time where a happening occurs to a specific person
	 * @return: Immutable List with instantiated happenings which will be handed over to ScheduledHappeningDirector
	 */


	public ImmutableList<Happening> init_happenings(ImmutableList<LauncherAgent> agents, int[][] happenings){
		
		List<Happening> happeningList = new ArrayList<Happening>();
		
		// Get static Happenings
		if(this.number_staticHappenings > 0) {
			happeningList.addAll(init_staticHappenings(agents));
		}
		
		// Hand over Informations of the GA in order to instantiate the dynamic happenings
		for(int agentIndex=0; agentIndex<this.number_agents; agentIndex++) {
			
			for(int happeningIndex=0; happeningIndex<this.number_dynamicHappenings; happeningIndex++) {
				
				if(happenings[agentIndex][happeningIndex] > 0) {					
					
					happeningList.add(init_dynamicHappening(agents.get(agentIndex), happeningIndex, happenings[agentIndex][happeningIndex]));
				}			
			}
		}
				
		ImmutableList<Happening> immutable = ImmutableList.copyOf(happeningList);
		return immutable;
	}
	
	
	/*
	 * Initializes all static Happenings
	 * @param:	agents = Immutable List containing instantiated agents provided by init_agents
	 * @return: ImmutableList of all static Happenings
	 */
	
	public abstract ImmutableList<Happening> init_staticHappenings(ImmutableList<LauncherAgent> agents);
	
	
	/*
	 * Initializes all dynamic Happenings
	 * @param:	
	 * 			agent = LauncherAgent being the patient of a certain Happening
	 * 			hIndex = The type of Happening is determined by it's position in the chromosome
	 * 			step = time step at which the happening shall occur
	 * @return:
	 * 			Returns a instantiated Happening
	 */
	
	public abstract Happening init_dynamicHappening(LauncherAgent agent, int hIndex, int step);
	
	
	/*
	 * Instantiate the PlotModel
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		hapDir = ScheduledHappeningDirector. happenings will already be initialized
	 * @return: 
	 * 		returns the instantiated PlotModel
	 */
	
	public abstract PlotModel<EnvType> init_model(ImmutableList<LauncherAgent> agents, ScheduledHappeningDirector hapDir);
	
	
	/*
	 * Set default Locations of the agents according to the model
	 * @param:
	 * 		agents = Immutable List containing instantiated agents provided by init_agents
	 * 		model = Instantiated PlotModel provided by init_model
	 */
	
	public abstract void init_location(ImmutableList<LauncherAgent> agents, PlotModel model);
	
}
