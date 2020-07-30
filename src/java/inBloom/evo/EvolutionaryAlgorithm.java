package inBloom.evo;

import inBloom.pso.ChromosomeHappenings;

public abstract class EvolutionaryAlgorithm {
	
	// Parameter for PlotLauncher
	public String[] args;
	public EvolutionaryEnvironment<?,?> EVO_ENV;
	
	// Standard parameters for a genetic algorithm
	public int number_agents;
	public int number_happenings;
	public int max_steps;
	public int individual_count;
	
	// Termination criteria
	private static int no_improvement=0;
	private static int termination=50;
	private static long start_time;
	private static long max_runtime=-1;
	
	public EvolutionaryAlgorithm(String[] args, EvolutionaryEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		
		this.args = args;
		this.EVO_ENV = EVO_ENV;
		this.number_agents = number_agents;
		this.max_steps = max_steps;
		this.number_happenings = number_happenings;
		this.individual_count = individual_count;
		
	}

	/**
	 * Get & Set Methods
	 */
	
	// Set termination criterion
	public void setTermination(int end) {
		
		if(end>0)
			termination = end;
	}
	
	// Set maximum runtime in seconds.
	public void setMaxRuntime(long time) {
		
		if(time>0)
			max_runtime = time*1000;
		else
			max_runtime = -1;
	}
	
	/**
	 * Utility Functions
	 */
	
	/*
	 * Rounds personality values in order to discretize the search space.
	 * @param personality value
	 * @return rounded value
	 */
	
	public double round(double value) {
		
		return Math.round(value*100-0.5)/100;
	}
	
	
	/*
	 * Sets the length of simulation of a chromosome according to it's Happenings.
	 * Determined value will be based on the step number of the last occuring happening plus
	 * an amount of additional steps between 0 and the square root of max step number increased by 1
	 * @param happenings: Chromosome encoding steps at which happenings occur
	 * @return: total amount of simulation steps
	 */

	public Integer determineLength(ChromosomeHappenings happenings) {
		
		Integer length = 0;
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				if(happenings.values[i][j] >= length) {
					length=happenings.values[i][j];
				}
			}
		}
		// Determine extra length
		Integer buffer = (int)Math.round(Math.random()*Math.sqrt(length));
		
		// Let the simulation run for at least 1 more step than the last happening 
		return length+buffer+1;
	}
	
	
	// Methods to be implemented
	public abstract boolean check_parameters();
	
	public abstract void run();

}
