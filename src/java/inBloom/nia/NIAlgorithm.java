package inBloom.nia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.utils.FileInterpreter;

public abstract class NIAlgorithm<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	public static final int MAX_SIM_LENGTH = 100;

	// Parameter for PlotLauncher
	public String[] args;
	public String filename = "results";
	public NIEnvironment<?, ?> EVO_ENV;

	// Standard parameters for an algorithm
	protected CandidateSolution[] population;
	public int number_agents;
	public int number_happenings;
	public int estimated_max_steps;
	public int individual_count;

	// management
	protected Level level = Level.OFF;
	protected Integer iterationNum;
	protected Random random;

	// Performance measurement
	protected List<Double> population_best = new ArrayList<>();
	protected List<Double> population_average = new ArrayList<>();

	// Print performance measurement over time
	protected boolean verbose = true;

	// Termination criteria
	protected static int no_improvement = 0;
	protected static int termination = 25;
	protected static long start_time;
	protected static long max_runtime = -1;

	// True -> exit system after completion
	protected boolean system_exit = true;

	public NIAlgorithm(String[] args, NIEnvironment<?, ?> EVO_ENV, int number_agents, int number_happenings,
			int max_steps, int individual_count) {

		this.args = args;
		this.EVO_ENV = EVO_ENV;
		this.number_agents = number_agents;
		this.number_happenings = number_happenings;
		this.estimated_max_steps = max_steps;
		this.individual_count = individual_count;
		this.iterationNum = 0;
		this.random = new Random();

	}

	/**
	 * Main loop of NIA. Initializes a population and runs iterations in a loop
	 * until termination criteria are met.
	 */
	public void run() {
		if (this.check_parameters()) {
			// Save current time
			start_time = System.currentTimeMillis();

			// Generate and evaluate initial particles
			this.initialize_population();
			this.evaluate_population();
			this.to_file(this.population[0], "");

			// Repeat until termination (no improvements found or time criterion
			// -if set- is met):
			while (this.keepRunning()) {
				// Print Statistics
				if (this.verbose) {
					this.generation_stats();
				}

				this.run_iteration();
				this.evaluate_population();
				this.iterationNum += 1;

				this.to_file(this.population[0], "");
			}

			// Print Statistics
			if (this.verbose) {
				this.final_stats();
			}

			if (this.system_exit) {
				System.exit(0);
			}
		}
	}

	protected boolean keepRunning() {
		return no_improvement < termination
				&& (max_runtime < 0 || start_time + max_runtime - System.currentTimeMillis() > 0);
	}

	// ***************** Utility Functions *****************
	public void setFileName(String name) {
		this.filename = name;
	}

	public void setExit(boolean exit) {
		this.system_exit = exit;
	}

	public void setTermination(int end) {
		if (end > 0) {
			termination = end;
		}
	}

	public void setMaxRuntime(long time) {
		if (time >= 0) {
			max_runtime = time * 1000;
		} else {
			max_runtime = -1;
		}
	}

	public void setVerbose(boolean bool) {
		this.verbose = bool;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	// ***************** Utility Functions *****************

	/**
	 * Rounds personality values in order to discretize the search space.
	 *
	 * @param personality
	 *            value
	 * @return rounded value
	 */
	public double round(double value) {
		double d = Math.round(value * 100 - 0.5);

		return d / 100;
	}

	/**
	 * Saves a log to file that contains the performance of the algorithm as
	 * well as the best found solution. Can be loaded and executed by
	 * {@linkplain FileInterpreter#readFile}.
	 *
	 * @param best
	 *            best solution found by this algoritm so far
	 */
	public void to_file(CandidateSolution best, String epilogue) {
		try {

			File file = new File(this.filename);

			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			writer.write("<Number of Generations>\n");
			writer.write(String.valueOf(this.iterationNum) + "\n");

			writer.write("<Best Found CandidateSolution So Far, Per Generation>\n");
			for (int i = 0; i < this.population_best.size(); i++) {
				writer.write(String.valueOf(this.population_best.get(i)));
				if (i < this.population_best.size() - 1) {
					writer.write(" ");
				}
			}
			writer.write("\n");

			writer.write("<Population Average Per Generation>\n");
			for (int i = 0; i < this.population_average.size(); i++) {
				writer.write(String.valueOf(this.population_average.get(i)));
				if (i < this.population_best.size() - 1) {
					writer.write(" ");
				}
			}
			writer.write("\n");

			writer.write("<Best CandidateSolution, Settings>\n");
			writer.write(best.to_String());

			writer.write(epilogue);

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generation_stats() {
		// Verbose
		int generation = this.population_best.size() - 1;

		System.out.println();
		System.out.println("Generation: " + generation);
		System.out.println("Best individual: " + this.population_best.get(generation));
		System.out.println("Generation Average: " + this.population_average.get(generation));
		System.out.println();

	}

	public void final_stats() {
		System.out.println();
		System.out.println("This is the End!");
		System.out.println();
		System.out.println("Generations: " + this.population_best.size());
		System.out.println("Best so far: " + this.population_best.get(this.population_best.size() - 1));
		System.out.println();

	}

	/**
	 * Sets the length of simulation of a chromosome according to it's
	 * Happenings. Determined value will be based on the step number of the last
	 * occuring happening plus an amount of additional steps between 0 and the
	 * square root of max step number increased by 1
	 *
	 * @param happenings:
	 *            Chromosome encoding steps at which happenings occur
	 * @return: total amount of simulation steps
	 */
	public Integer determineLength(ChromosomeHappenings happenings) {

		Integer length = 0;

		for (int i = 0; i < this.number_agents; i++) {
			for (int j = 0; j < this.number_happenings; j++) {
				if (happenings.values[i][j] >= length) {
					length = happenings.values[i][j];
				}
			}
		}
		// Determine extra length
		Integer buffer = (int) Math.round(Math.sqrt(length));

		// Let the simulation run for at least 1 more step than the last
		// happening
		return length + buffer + 1;
	}

	// ***************** Methods to be implemented by subclass *****************
	/**
	 * Checks whether NIA is configured in a valid way
	 *
	 * @return: boolean determining whether algorithm is runnable.
	 */
	public abstract boolean check_parameters();

	/**
	 * Compute information about population quality.
	 */
	protected abstract void evaluate_population();

	/**
	 * Initialize first population.
	 */
	protected abstract void initialize_population();

	/**
	 * Runs one iteration of the NIA, by creating new members of the populations
	 * according to its principles, evaluating their performance and updating
	 * the overall population.
	 */
	protected abstract void run_iteration();
}
