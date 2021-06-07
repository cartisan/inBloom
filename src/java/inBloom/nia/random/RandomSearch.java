package inBloom.nia.random;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomeLength;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.NIAlgorithm;
import inBloom.nia.NIEnvironment;
import inBloom.nia.ga.Individual;

public class RandomSearch <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends NIAlgorithm<EnvType,ModType>{

	public Individual bestSolution;

	public RandomSearch(String[] args, NIEnvironment<?, ?> EVO_ENV, int number_agents, int number_happenings,
			int max_steps, int individual_count) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		no_improvement=0;
	}

	@Override
	public boolean check_parameters() {
		if(this.individual_count<4) {
			System.out.println("Size of population defaulted to 4!");
			this.individual_count = 4;
		}

		// There need to be agents and happenings
		if(this.number_agents <= 0 || this.number_happenings <= 0) {
			System.out.println("Bad Configuration!");
			return false;
		}

		return true;
	}

	@Override
	protected void evaluate_population() {
		Double generationAverage = 0.0;
		Double lenAve = 0.0;

		for(int i = 0; i < this.individual_count; i++) {
			if(this.bestSolution.get_tellabilityValue() < this.population[i].get_tellabilityValue()) {
				this.bestSolution = (Individual) this.population[i];
			}
			generationAverage += this.population[i].get_tellabilityValue();
			lenAve += this.population[i].get_actualLength();
		}

		generationAverage /= this.individual_count;
		lenAve /= this.individual_count;

		this.population_average.add(generationAverage);
		this.population_best.add(this.bestSolution.get_tellabilityValue());
		this.average_length.add(lenAve);

		// Determine if there was improvement
		if(this.population_best.size() > 1) {
			if(this.population_best.get(this.population_best.size()-1) == this.population_best.get(this.population_best.size() - 2)) {
				no_improvement++;
			} else {
				no_improvement=0;
			}
		}
	}

	@Override
	protected void initialize_population() {
		this.population = this.randomPopulation();

		// set a random best, will be changed later during evaluate_population()
		this.bestSolution = (Individual) this.population[0];
	}


	@Override
	public void run() {
		if (this.check_parameters()) {
			// Save current time
			start_time = System.currentTimeMillis();

			// Generate and evaluate initial particles
			this.initialize_population();
			this.evaluate_population();
			this.to_file(this.bestSolution, "");

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

				this.to_file(this.bestSolution, "");
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

	@Override
	protected void run_iteration() {
		this.population = this.randomPopulation();
	}

	private Individual[] randomPopulation() {
		Individual[]  candidates = new Individual[this.individual_count];
		for(int index=0; index < this.individual_count; ++index) {
			ChromosomeLength length = new ChromosomeLength(this.random.nextInt(MAX_SIM_LENGTH));

			ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);
			for(int i = 0; i < this.number_agents;i++) {
				for(int j = 0; j < 5; j++) {
					personality.values[i][j] = this.round(this.random.nextDouble()*2-1);
				}
			}

			ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);
			for(int i = 0; i < this.number_agents;i++) {
				for(int j = 0; j < this.number_happenings; j++) {
					happenings.values[i][j] = this.random.nextInt(Math.max(length.value, 1));
				}
			}

			Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV, this.verbose, this.level);
			candidates[index] = new Individual(personality, happenings, length.value, fit);
		}
		return candidates;
	}

	public void generation_stats() {
		// Verbose
		int generation = this.population_best.size() - 1;

		System.out.println();
		System.out.println("Generation: " + generation);
		System.out.println("Generation Best Individual: " + this.population_best.get(generation));
		System.out.println("Generation Average: " + this.population_average.get(generation));
		System.out.println();

	}

	public void final_stats() {
		System.out.println();
		System.out.println("This is the End!");
		System.out.println();
		System.out.println("Generations: " + this.population_best.size());
		System.out.println("Best so far: " + this.bestSolution.get_tellabilityValue());
		System.out.println();

	}

}
