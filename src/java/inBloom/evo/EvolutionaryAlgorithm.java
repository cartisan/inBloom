package inBloom.evo;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public abstract class EvolutionaryAlgorithm <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	
	// Parameter for PlotLauncher
	public String[] args;
	public String filename = "results";
	public EvolutionaryEnvironment<?,?> EVO_ENV;
	
	// Standard parameters for a genetic algorithm
	public int number_agents;
	public int number_happenings;
	public int max_steps;
	public int individual_count;
	
	// Performance measurement
	protected List<Double> population_best = new ArrayList<Double>();
	protected List<Double> population_average = new ArrayList<Double>();
	
	// Termination criteria
	protected static int no_improvement=0;
	protected static int termination=25;
	protected static long start_time;
	protected static long max_runtime=-1;
	
	// True -> exit system after completion
	protected boolean system_exit=true;
	
	public EvolutionaryAlgorithm(String[] args, EvolutionaryEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		
		this.args = args;
		this.EVO_ENV = EVO_ENV;
		this.number_agents = number_agents;
		this.number_happenings = number_happenings;
		this.max_steps = max_steps;
		this.individual_count = individual_count;
		
	}

	/**
	 * Get & Set Methods
	 */
	
	public void setFileName(String name) {
		
		filename = name;
	}
	
	public void setExit(boolean exit) {
		
		system_exit = exit;
	}
	
	// Set termination criterion
	public void setTermination(int end) {
		
		if(end>0)
			termination = end;
	}
	
	// Set maximum runtime in seconds.
	public void setMaxRuntime(long time) {
		
		if(time>=0)
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
		
		double d = Math.round(value*100-0.5);
		
		return d/100;
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
		Integer buffer = (int)Math.round(Math.sqrt(length));
		
		// Let the simulation run for at least 1 more step than the last happening 
		return length+buffer+1;
	}
	
	
	// Methods to be implemented
	public abstract boolean check_parameters();
	
	public abstract void run();
	
	protected abstract void evaluate_population();
	
	public void to_file(Individual best) {
		
		try {
			
			File file = new File(filename);
			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			for(int i = 0; i < population_best.size(); i++) {
				writer.write(String.valueOf(population_best.get(i)));
				if(i<population_best.size()-1)
					writer.write(" ");
			}
			writer.write("\n");
			
			for(int i = 0; i < population_average.size(); i++) {
				writer.write(String.valueOf(population_average.get(i)));
				if(i<population_best.size()-1)
					writer.write(" ");
			}
			writer.write("\n");
			
			writer.write(best.to_String());
			
			writer.flush();
			writer.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
