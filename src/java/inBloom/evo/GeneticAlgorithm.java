package inBloom.evo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class GeneticAlgorithm<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends NIAlgorithm<EnvType,ModType>{
	private static final boolean USE_FLOATING_PARAM = false;
	
	// Parameters for static version
	public int selection_size;
	public double crossover_prob = 0.1;
	public double mutation_prob = 0.05;

	// Container for candidates
	private Candidate[] gen_pool;
	private Candidate[] offspring;
	private Candidate[] mutated_offspring;

	// Information storage for floating parameter version
	private boolean floatingParameters;

	private double decay_rate;

	private double global_cross;
	private double global_mut;

	// parameters for Floating Param version
	private double[] cross_prob;
	private double[] mut_prob;
	private double[][] personality_cross;
	private double[][] personality_mut;
	private double[][] happenings_cross;
	private double[][] happenings_mut;

	// Performance measurement
	private List<Double> population_bestHalf = new ArrayList<>();
	private double population_bestAverage = 0;

	// Discrete values to choose from for personality initialization
	private static double[] discretePersValues = {-1,-0.9,-0.75,-0.5,-0.25,-0.1,0,0.1,0.25,0.5,0.75,0.9,1};

	// Boolean arrays are used to manage genetic operators. True enables usage.

	// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
	private static boolean[] persInitBool = {true,true,true};
	// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
	private static boolean[] hapInitBool = {true,true,true};
	// randomSelector, rouletteWheelSelection
	private static boolean[] selectionBool = {true,true};
	// simpleCrossover,binomialCrossover,xPointCrossover,voteCrossover
	private static boolean[] crossoverBool = {true,true,true,true};
	// randomMutator,toggleMutator,orientedMutator,guidedMutator
	private static boolean[] mutationBool = {true,true,true,true};
	// true -> steadyNoDuplicatesReplacer, false -> partiallyRandomNoDuplicatesReplacer
	private static boolean steadyReplace = true;


	/**
	 * Constructors for GA, static version
	 */
	public GeneticAlgorithm (String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections, double crossover_prob, double mutation_prob) {

		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.selection_size = number_selections*2;
		this.floatingParameters = USE_FLOATING_PARAM;

		this.crossover_prob = crossover_prob;
		this.mutation_prob = mutation_prob;
	}

	/**
	 * Constructors for GA, floating param version
	 */
	public GeneticAlgorithm (String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections, double decay) {

		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.selection_size = number_selections*2;
		this.floatingParameters = USE_FLOATING_PARAM;

		this.decay_rate = decay;
	}


	/**
	 * Get & Set Methods
	 */

	public Candidate[] get_genPool() {

		return this.gen_pool;
	}

	// Discrete values for personality initialization
	public void setPersonalityValues(double[] discreteValues) {

		discretePersValues = discreteValues;
	}

	public double[] getPersonalityValues() {

		return discretePersValues;
	}


	// Chromosome personality initialization operators
	public boolean[] getPersInit() {

		return persInitBool;
	}

	public void setPersInit(int pos, boolean bool) {

		if(pos>=0 && pos < persInitBool.length) {
			persInitBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
	}

	public void setPersInit(boolean random, boolean steady, boolean uniqueSteady) {

		persInitBool[0] = random;
		persInitBool[1] = steady;
		persInitBool[2] = uniqueSteady;
	}


	// Chromosome happening initialization operators
	public boolean[] getHapInit() {

		return hapInitBool;
	}

	public void setHapInit(int pos, boolean bool) {

		if(pos>=0 && pos < hapInitBool.length) {
			hapInitBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
	}

	public void setHapInit(boolean random, boolean binomial, boolean steady) {

		hapInitBool[0] = random;
		hapInitBool[1] = binomial;
		hapInitBool[2] = steady;
	}


	// Selection operators
	public boolean[] getSelection() {

		return selectionBool;
	}

	public void setSelection(boolean random, boolean roulette) {

		selectionBool[0] = random;
		selectionBool[1] = roulette;
	}

	public void setSelection(int pos, boolean bool) {

		if(pos>=0 && pos < selectionBool.length) {
			selectionBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
	}


	// Crossover operators
	public boolean[] getCrossover() {

		return crossoverBool;
	}

	public void setCrossover(int pos, boolean bool) {

		if(pos>=0 && pos < crossoverBool.length) {
			crossoverBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
	}

	public void setCrossover(boolean random, boolean binomial, boolean xPoint, boolean vote) {

		crossoverBool[0] = random;
		crossoverBool[1] = binomial;
		crossoverBool[2] = xPoint;
		crossoverBool[3] = vote;
	}


	// Mutation operators
	public boolean[] getMutation() {

		return mutationBool;
	}

	public void setMutation(int pos, boolean bool) {

		if(pos>=0 && pos < mutationBool.length) {
			mutationBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
	}

	public void setMutation(boolean random, boolean toggle, boolean oriented, boolean guided) {

		mutationBool[0] = random;
		mutationBool[1] = toggle;
		mutationBool[2] = oriented;
		mutationBool[3] = guided;
	}


	// Replacer mode
	public boolean getReplaceMode() {

		return steadyReplace;
	}

	public void setReplaceMode(boolean mode) {

		steadyReplace = mode;
	}


	/*
	 * Checks the parameters of the genetic algorithm.
	 * @return: boolean determining whether algorithm is runnable.
	 */

	public boolean check_parameters() {

		// Ensure correctness of parameters

		// Minimum Population size
		if(this.individual_count<4) {
			System.out.println("Size of population defaulted to 4!");
			this.individual_count = 4;
		}

		// Selection size must be positive
		if(this.selection_size <= 2) {
			System.out.println("Selection_size defaulted to: " + this.selection_size);
			this.selection_size = 2;
		}

		// Selection size must be an even number smaller individual_count
		while(this.selection_size>=this.individual_count) {
			this.selection_size/=2;
			this.selection_size-=this.selection_size%2;
			System.out.println("Selection_size reduced to: " + this.selection_size);
		}

		// There need to be agents and happenings
		if(this.number_agents <= 0 || this.number_happenings <= 0) {
			System.out.println("Bad Configuration!");
			return false;
		}

		if(this.floatingParameters) {
			if(this.decay_rate<=0||this.decay_rate>=1) {
				this.decay_rate = 0.05;
				System.out.println("Decay_rate defaulted to 0.05!");
			}

		}else {

			if(this.mutation_prob <= 0||this.mutation_prob>1) {
				System.out.println("Mutation_prob: " + this.mutation_prob + " is suboptimal!");
				return false;
			}

			if(this.crossover_prob <= 0||this.crossover_prob>1) {
				System.out.println("Crossover_prob: " + this.crossover_prob + " is suboptimal!");
				return false;
			}
		}

		return true;
	}


	/**
	 * Instantiates new fitness object and hands it over to the Candidate to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Candidate
	 */
	public Candidate new_Candidate(ChromosomePersonality pers,ChromosomeHappenings hap) {

		return this.new_Candidate(pers,hap,this.determineLength(hap));
	}

	public Candidate new_Candidate(ChromosomePersonality pers,ChromosomeHappenings hap, Integer steps) {

		Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV, this.verbose, this.level);

		return new Candidate(pers, hap, steps, fit);

	}


	/*
	 * Saves information about population quality.
	 * The average fitness of the population as well as best candidate's fitness is saved.
	 * If there was no improvement compared to the last generation, termination criterion counter gets incremented.
	 * If there was an improvement, termination criterion counter is reset.
	 * Average will only take the best half of population into account to ensure termination.
	 */

	protected void evaluate_population() {

		Double best = this.gen_pool[0].get_tellability();
		Double average = 0.0;

		int relevant_size = this.individual_count/2;

		for(int i = 0; i < relevant_size; i++) {
			average += this.gen_pool[i].get_tellability();
		}

		double halfAverage = average/relevant_size;

		for(int i = relevant_size; i < this.individual_count; i++) {
			average += this.gen_pool[i].get_tellability();
		}

		average /= this.individual_count;


		// Determine if there was improvement
		if(this.population_best.size()>0) {
			if(this.population_best.get(this.population_best.size()-1)==best && this.population_bestHalf.get(this.population_bestHalf.size()-1)==halfAverage) {
				no_improvement++;
			} else {
				no_improvement=0;
			}
		}
		this.population_best.add(best);
		this.population_bestHalf.add(halfAverage);
		this.population_average.add(average);

		if(this.population_bestAverage > average) {
			this.population_bestAverage = average;
		}
	}


	/*
	 * Genetic algorithm main loop
	 * Initializes Population and runs the loop until termination criterion is met.
	 */

	public void run() {

		// Ensure correct Parameters
		if(this.check_parameters()) {

			// Initialize information containers for floating parameter version
			this.initialize_floatingParameters();

			if(this.floatingParameters) {
				this.determineGlobalParameters();
			}

			// Save current time
			start_time = System.currentTimeMillis();

			// Generate and evaluate initial population
			this.initialize_pop();
			this.evaluate_population();

			// Repeat until termination (no improvements found) or time criterion -if set- is met:
			while((no_improvement < 0 || no_improvement<termination) && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0) && (!this.floatingParameters || this.global_mut>0 || this.global_cross>0)) {

				// Print Statistics
				if(this.verbose) {
					this.generation_stats();
				}

				this.crossover(this.select());
				this.mutate();
				this.recombine();
				this.evaluate_population();

				if(this.floatingParameters) {
					this.determineGlobalParameters();
				}

				this.to_file(this.gen_pool[0]);
			}

			// Print Statistics
			if(this.verbose) {
				this.final_stats();
			}

			if(this.system_exit) {
				System.exit(0);
			}
		}
	}


	/*
	 * Initialization of information storage for floating parameter version of GA
	 */

	public void initialize_floatingParameters() {

		// Initialize global strategy probabilities

		this.global_cross = 0.5;
		this.cross_prob = new double[crossoverBool.length];

		for(int i = 0; i < crossoverBool.length; i++) {
			if(crossoverBool[i]) {
				this.cross_prob[i]=0.25;
			}
		}

		this.global_mut = 0.5;
		this.mut_prob = new double[mutationBool.length];

		for(int i = 0; i < mutationBool.length; i++) {
			if(mutationBool[i]) {
				this.mut_prob[i]=0.25;
			}
		}

		// Initialize local strategy probabilities
		this.personality_cross = new double[this.number_agents][5];
		this.personality_mut = new double[this.number_agents][5];
		this.happenings_cross = new double[this.number_agents][this.number_happenings];
		this.happenings_mut = new double[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(this.floatingParameters) {
					this.personality_cross[i][j] = 0.5;
					this.personality_mut[i][j] = 0.5;
				}else {
					this.personality_cross[i][j] = 1;
					this.personality_mut[i][j] = 1;
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(this.floatingParameters) {
					this.happenings_cross[i][j] = 0.5;
					this.happenings_mut[i][j] = 0.5;
				}else {
					this.happenings_cross[i][j] = 1;
					this.happenings_mut[i][j] = 1;
				}
			}
		}
	}


	/*
	 * Determine global strategy for floating parameters
	 */

	public void determineGlobalParameters() {

//		// Determine global crossover probability
//		global_cross = 0;
//		int count = 0;
//
//		for(int i = 0; i < cross_prob.length; i++) {
//			if(cross_prob[i]>=0.05) {
//				global_cross += cross_prob[i];
//				count+=1;
//			}
//		}
//
//		if(count > 0)
//			global_cross /= count;
//		else
//			global_cross = 1.0;
//
//		// Determine global mutation probability
//		global_mut = 0;
//		count = 0;
//
//		for(int i = 0; i < mut_prob.length; i++) {
//			if(mut_prob[i]>=0.05) {
//				global_mut += mut_prob[i];
//				count+=1;
//			}
//		}
//
//		if(count > 0)
//			global_mut /= count;
//		else
//			global_mut = 1.0;

		// Print state
		if(this.verbose) {

			System.out.println("Global Crossover Rating: " + this.global_cross);

			for(int i = 0; i < 4; i++) {
				System.out.print(this.cross_prob[i] + " ");
			}
			System.out.println();

			for(int i = 0; i < 5; i++) {
				System.out.print(this.personality_cross[0][i] + " ");
			}
			System.out.println();

			for(int i = 0; i < this.number_happenings; i++) {
				System.out.print(this.happenings_cross[0][i] + " ");
			}
			System.out.println();
			System.out.println();

			System.out.println("Global Mutation Rating: " + this.global_mut);

			for(int i = 0; i < 4; i++) {
				System.out.print(this.mut_prob[i] + " ");
			}
			System.out.println();

			for(int i = 0; i < 5; i++) {
				System.out.print(this.personality_mut[0][i] + " ");
			}
			System.out.println();

			for(int i = 0; i < this.number_happenings; i++) {
				System.out.print(this.happenings_mut[0][i] + " ");
			}
			System.out.println();
		}
	}


	/*
	 * Update local crossover probability
	 */

	public void updateLocalCrossoverProbabilites(boolean[][] pers, boolean[][] haps, boolean improvement) {

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(pers[i][j]) {
					this.personality_cross[i][j] = this.localUpdate(this.personality_cross[i][j],this.global_cross,improvement);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(haps[i][j]) {
					this.happenings_cross[i][j] = this.localUpdate(this.happenings_cross[i][j],this.global_cross,improvement);
				}
			}
		}
	}


	/*
	 * Update local mutation probability
	 */

	public void updateLocalMutationProbabilites(boolean[][] pers, boolean[][] haps, boolean improvement) {

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(pers[i][j]) {
					this.personality_mut[i][j] = this.localUpdate(this.personality_mut[i][j],this.global_mut,improvement);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(haps[i][j]) {
					this.happenings_mut[i][j] = this.localUpdate(this.happenings_mut[i][j],this.global_mut,improvement);
				}
			}
		}
	}

	// global Decay Update
	public double globalUpdate(double d, boolean improvement) {

		if(improvement) {
			return d+(1-d)*this.decay_rate*(1-d);
		}

		return d*(1-this.decay_rate*d);

	}

	// Local Decay Update Rule
	public double localUpdate(double d, double success_rating, boolean improvement) {

		if(improvement) {
			return d+(1-d)*this.decay_rate*(1-success_rating);
		}

		double result = d*(1-this.decay_rate*success_rating);

		// Deactivation threshold
		if(result < this.decay_rate) {
			return this.decay_rate;
		}

		return result;
	}

	// Global Decay Update Rule
	public double operatorUpdate(double d, double success_rating, boolean improvement) {

		double threshold = 0.5;

		if(improvement) {
			return d+(threshold-d)*this.decay_rate*(1-success_rating);
		}

		double result = d*(1-this.decay_rate*success_rating);

		// Deactivation threshold
		if(result < this.decay_rate) {
			return this.decay_rate;
		}

		return result;
	}



	/*
	 * Initialization of Population
	 */

	public void initialize_pop() {

		this.gen_pool = new Candidate[this.individual_count];
		this.offspring = new Candidate[this.selection_size];
		this.mutated_offspring = new Candidate[this.selection_size];

		int index = 0;

		// Set used initializers for personality
		List<Integer> persInitializer = new ArrayList<>();

		for(int i = 0; i < persInitBool.length; i++) {
			if(persInitBool[i]) {
				persInitializer.add(i);
			}
		}

		if(persInitializer.size()==0) {
			System.out.println("No initialization set for personality chromosome. Defaulting to random init!");
			persInitializer.add(0);
		}

		// Set used initializers for happenings
		List<Integer> hapInitializer = new ArrayList<>();

		for(int i = 0; i < hapInitBool.length; i++) {
			if(hapInitBool[i]) {
				hapInitializer.add(i);
			}
		}

		if(hapInitializer.size()==0) {
			System.out.println("No initialization set for happenings chromosome. Defaulting to random init!");
			hapInitializer.add(0);
		}

		// Initialize population
		while(index<this.individual_count) {

			// Create new personality chromosome
			ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);

			int persType = persInitializer.get((int)Math.round(Math.random()*persInitializer.size()-0.5));

			switch(persType) {

			case 2:

				if(discretePersValues.length>4) {
					personality = this.steadyDiscretePersonalityInitializer();
					break;
				}

			case 1:

				if(discretePersValues.length>0) {
					personality = this.discretePersonalityInitializer();
					break;
				}

			case 0:

				personality = this.randomPersonalityInitializer();
				break;

			default:

				System.out.println("Fatal Error @ Personality Initialization Selection!");
				break;
			}

			// Create new happening chromosome
			ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

			int hapType = hapInitializer.get((int)Math.round(Math.random()*hapInitializer.size()-0.5));

			switch(hapType) {

			case 0:

				happenings = this.randomHappeningsInitializer();
				break;

			case 1:

				happenings = this.probabilisticHappeningsInitializer();
				break;

			case 2:

				happenings = this.steadyHappeningsInitializer();
				break;

			default:

				System.out.println("Fatal Error @ Happening Initialization Selection!");
				break;
			}

			// Check for Duplicates
			boolean isDuplicate = false;

			for(int i = 0; i < index; i++) {
				if(this.gen_pool[i].equals(personality,happenings)) {

					isDuplicate = true;
				}
			}

			// First candidate cannot have duplicates
			if(index==0 || !isDuplicate) {

				this.gen_pool[index] = this.new_Candidate(personality,happenings);
				index++;
			}
		}

		// Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(this.gen_pool);
	}


	/*
	 * Random initializer for ChomosomePersonality
	 * Chooses 5 values from the Interval [-1;1] randomly for every agent
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomePersonality randomPersonalityInitializer() {

		ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < 5; j++) {

				personality.values[i][j] = this.round(Math.random()*2-1);
			}
		}
		return personality;
	}


	/*
	 * discrete initializer for ChomosomePersonality
	 * Chooses the personality values from a set of predetermined values
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomePersonality discretePersonalityInitializer() {

		ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				personality.values[i][j] = discretePersValues[(int)Math.round(Math.random()*discretePersValues.length-0.5)];
			}
		}
		return personality;
	}


	/*
	 * like discretePersonalityInitializer() but with unique values
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomePersonality steadyDiscretePersonalityInitializer() {

		ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);

		for(int i = 0; i < this.number_agents;i++) {

			ArrayList<Double> discreteList = new ArrayList<>();

			for (double discretePersValue : discretePersValues) {
				discreteList.add(discretePersValue);
			}

			for(int j = 0; j < 5; j++) {
				int position = (int)Math.round(Math.random()*discreteList.size()-0.5);
				personality.values[i][j] = discreteList.get(position);
				discreteList.remove(position);
			}
		}
		return personality;
	}


	/*
	 * Random initializer for ChromosomeHappening
	 * Inserts random numbers between 0 and max_steps into the chromosome.
	 * Numbers are discretized to be multiples of max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings randomHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				happenings.values[i][j] = (int)Math.round(Math.random()*(this.max_steps/this.number_happenings+1)-0.5)*this.number_happenings;
			}
		}
		return happenings;
	}


	/*
	 * Instantiates a happening with probability 1/number_agents
	 * Inserts random numbers between 0 and max_steps into the chromosome.
	 * Numbers are discretized to be multiples of max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings probabilisticHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				if(Math.random()<1/this.number_agents) {
					happenings.values[i][j] = (int)Math.round(Math.random()*(this.max_steps/this.number_happenings)+0.5)*this.number_happenings;
				}
			}
		}
		return happenings;
	}


	/*
	 * Instantiates every happening exactly once and assigns it to a random agent
	 * Inserts random numbers between 0 and max_steps into the chromosome.
	 * Numbers are discretized to be multiples of max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings steadyHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {

			int j = (int)Math.round(Math.random()*this.number_agents-0.5);

			happenings.values[i][j] = (int)Math.round(Math.random()*(this.max_steps/this.number_happenings)+0.5)*this.number_happenings;
		}
		return happenings;
	}


	/*
	 * Selection
	 *
	 * Picks Candidates for further application of genetic operators
	 * @Return: List with positions in gen_pool of chosen candidates
	 */

	public List<Integer> select() {

		// Set used selection operators
		List<Integer> selectionList = new ArrayList<>();

		for(int i = 0; i < selectionBool.length; i++) {
			if(selectionBool[i]) {
				selectionList.add(i);
			}
		}

		if(selectionList.size()==0) {
			System.out.println("No selection set. Defaulting to random selection!");
			selectionList.add(0);
		}

		int type = selectionList.get((int)Math.round(Math.random()*selectionList.size()-0.5));

		if(type == 1) {
			return this.rouletteWheelSelector();
		}

		return this.randomSelector();
	}


	/*
	 *  Random Selection
	 *
	 *  Chooses selection_size many Candidates from the gen_pool in a random manner
	 * @Return: List with positions of chosen individuals
	 */

	public List<Integer> randomSelector(){

		List<Integer> selectedindividuals = new ArrayList<>();

		while(selectedindividuals.size() < this.selection_size) {

			int position = (int)Math.round(Math.random()*this.individual_count-0.5);
			if(!selectedindividuals.contains(position)) {
				selectedindividuals.add(position);
			}
		}
		return selectedindividuals;
	}


	/*
	 * Fitness based Selection
	 *
	 * Uses roulette wheel approach to increase likelihood of choosing the best performing individuals
	 * @Return: List with positions of chosen individuals
	 */

	public List<Integer> rouletteWheelSelector() {

		// Construct roulette wheel
		double total_fitness = 0.0;
		double[] rouletteWheel = new double[this.individual_count];

		// Control parameters
		boolean control = true;
		int validIndividuals = 0;

		for(int i = 0; i < this.individual_count; i++) {

			total_fitness += this.gen_pool[i].get_tellability();
			rouletteWheel[i] = total_fitness;

			// Check if we have enough individuals with fitness
			if(control && this.gen_pool[i].get_tellability()==0) {

				validIndividuals = i;
				control = false;
			}
		}

		// Pick Candidates
		List<Integer> selectedIndividuals = new ArrayList<>();

		if(validIndividuals > this.selection_size) {

			//If there are enough valid candidates use Roulette-Wheel approach
			while(selectedIndividuals.size() < this.selection_size) {

				int position = 0;
				double value = Math.random()*total_fitness;

				while(value > rouletteWheel[position]) {
					position++;
				}
				if(!selectedIndividuals.contains(position)) {
					selectedIndividuals.add(position);
				}
			}

		}else {

			// Just fill selection with the first n individuals
			for(int i = 0; i < this.selection_size; i++) {
				selectedIndividuals.add(i);
			}

		}
		return selectedIndividuals;
	}


	/*
	 * Crossover
	 *
	 * Chooses two candidates randomly from the selected individuals list.
	 * Generated Candidates will be stored into this.offspring
	 * @param positions: List of candidate positions generated by Selection
	 */

	public void crossover(List<Integer> positions) {
		if(this.verbose) {
			System.out.println("Start Crossover");
		}

		// Set used Crossover operators
		List<Integer> crossoverList = new ArrayList<>();

		for(int i = 0; i < crossoverBool.length; i++) {
			if(crossoverBool[i]) {
				crossoverList.add(i);
			}
		}

		if(crossoverList.size()==0) {
			System.out.println("No selection set. Defaulting to binomial crossover!");
			crossoverList.add(1);
		}

		for(int i = 0; i < this.selection_size; i+=2) {

			int pos = (int)Math.round(Math.random()*positions.size()-0.5);
			int one = positions.get(pos);
			positions.remove(pos);
			pos = (int)Math.round(Math.random()*positions.size()-0.5);
			int two = positions.get(pos);
			positions.remove(pos);

			Integer mode = 0;

			if(this.floatingParameters) {

				double sum = 0.05;

				for (double element : this.cross_prob) {
					sum += element;
				}

				double roulette = Math.random()*sum-0.05;

				this.crossover_prob = this.global_cross;

				if(roulette < 0) {
					mode = -1;
				}else {
					while(roulette > this.cross_prob[mode]) {
						roulette -= this.cross_prob[mode];
						mode += 1;
					}
					//crossover_prob = (crossover_prob + cross_prob[mode])/2;
					this.crossover_prob = this.cross_prob[mode];
				}


			}else {

				mode = crossoverList.get((int)Math.round(Math.random()*crossoverList.size()-0.5));
			}

			switch(mode) {

			case 0:

				this.simpleCrossover(this.gen_pool[one],this.gen_pool[two],i);
				break;

			case 1:

				this.binomialCrossover(this.gen_pool[one],this.gen_pool[two],i);
				break;


			case 2:

				this.xPointCrossover(this.gen_pool[one],this.gen_pool[two],i);
				break;

			case 3:

				// Add 2 initial candidates to the List
				List<Candidate> candidates = new ArrayList<>();

				candidates.add(this.gen_pool[one]);
				candidates.add(this.gen_pool[two]);

				// Initialize list containing additional possible voters
				List<Integer> possibleVoters = new ArrayList<>();

				for(int v = 0; v < this.individual_count; v++) {
					if(v != one && v != two) {
						possibleVoters.add(v);
					}
				}

				// Add additional Votes
				int additionalVotes = (int)Math.round(Math.random()*(this.individual_count-2)-0.5);

				while(additionalVotes > 0) {

					int votePos = (int)Math.round(Math.random()*possibleVoters.size()-0.5);

					candidates.add(this.gen_pool[possibleVoters.get(votePos)]);
					additionalVotes--;
				}

				this.voteCrossover(candidates,i);
				break;

			default:

				this.offspring[i] = this.gen_pool[one];
				this.offspring[i+1] = this.gen_pool[two];
				System.out.println("No crossover operator selected!");
				break;
			}
		}
		// Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(this.offspring);

		if(this.verbose) {
			System.out.println("End Crossover\n");
		}
	}


	/*
	 * "Simply" exchanges ChromosomePersonality and ChromosomeHappenings
	 * Is equal to onePointCrossover with a fixed crossover Point
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void simpleCrossover(Candidate one, Candidate two, int index) {

		this.offspring[index] = this.new_Candidate(one.get_personality(), two.get_happenings());
		this.offspring[index+1] = this.new_Candidate(two.get_personality(), one.get_happenings());

		if(this.floatingParameters) {

			boolean improvement = false;

			if(this.offspring[index].get_tellability()>one.get_tellability() && this.offspring[index].get_tellability()>two.get_tellability()
			|| this.offspring[index+1].get_tellability()>one.get_tellability() && this.offspring[index+1].get_tellability()>two.get_tellability()) {
				improvement = true;
			}

			this.global_cross = this.globalUpdate(this.global_cross,improvement);
			this.cross_prob[0] = this.operatorUpdate(this.cross_prob[0], this.global_cross, improvement);

		}
	}



	/*
	 * Exchanges Allele with probability crossover_prob
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void binomialCrossover(Candidate one, Candidate two, int index) {

		ChromosomePersonality personalityOne = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(Math.random()<this.crossover_prob*this.personality_cross[i][j]) {
					personalityOne.values[i][j] = two.get_personality(i,j);
					personalityTwo.values[i][j] = one.get_personality(i,j);
					persChange[i][j] = true;
				}else {
					personalityOne.values[i][j] = one.get_personality(i,j);
					personalityTwo.values[i][j] = two.get_personality(i,j);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.crossover_prob*this.happenings_cross[i][j]) {
					happeningsOne.values[i][j] = two.get_happenings(i,j);
					happeningsTwo.values[i][j] = one.get_happenings(i,j);
					hapsChange[i][j] = true;
				}else {
					happeningsOne.values[i][j] = one.get_happenings(i,j);
					happeningsTwo.values[i][j] = two.get_happenings(i,j);
				}
			}
		}

		this.offspring[index] = this.new_Candidate(personalityOne,happeningsOne);
		this.offspring[index+1] = this.new_Candidate(personalityTwo,happeningsTwo);

		if(this.floatingParameters) {

			boolean local_improvement = false;
			boolean global_improvement = false;

			if(this.offspring[index].get_tellability()>one.get_tellability() && this.offspring[index].get_tellability()>two.get_tellability()
			|| this.offspring[index+1].get_tellability()>two.get_tellability() && this.offspring[index+1].get_tellability()>two.get_tellability()) {

				global_improvement = true;

				if(this.offspring[index].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability()
				|| this.offspring[index+1].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability()) {

					local_improvement = true;
				}
			}

			this.global_cross = this.globalUpdate(this.global_cross,global_improvement);
			this.updateLocalCrossoverProbabilites(persChange, hapsChange, local_improvement);
			this.cross_prob[1] = this.operatorUpdate(this.cross_prob[1], this.global_cross, global_improvement);
		}
	}


	/*
	 * Exchanges allele between crossover points.
	 * Crossover points are generated by the function setCrossoverPoints()
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void xPointCrossover(Candidate one, Candidate two, int index) {

		boolean[][] crossPersonality = new boolean[this.number_agents][5];
		boolean[][] personalityPoints = this.setCrossoverPoints(crossPersonality, this.personality_cross);

		boolean[][] crossHappenings = new boolean[this.number_agents][this.number_happenings];
		boolean[][] happeningPoints = this.setCrossoverPoints(crossHappenings, this.happenings_cross);

		ChromosomePersonality personalityOne = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(crossPersonality[i][j]) {
					personalityOne.values[i][j] = two.get_personality(i,j);
					personalityTwo.values[i][j] = one.get_personality(i,j);
				}else {
					personalityOne.values[i][j] = one.get_personality(i,j);
					personalityTwo.values[i][j] = two.get_personality(i,j);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(crossHappenings[i][j]) {
					happeningsOne.values[i][j] = two.get_happenings(i,j);
					happeningsTwo.values[i][j] = one.get_happenings(i,j);
				}else {
					happeningsOne.values[i][j] = one.get_happenings(i,j);
					happeningsTwo.values[i][j] = two.get_happenings(i,j);
				}
			}
		}

		this.offspring[index] = this.new_Candidate(personalityOne,happeningsOne);
		this.offspring[index+1] = this.new_Candidate(personalityTwo,happeningsTwo);


		if(this.floatingParameters) {

			boolean local_improvement = false;
			boolean global_improvement = false;

			if(this.offspring[index].get_tellability()>one.get_tellability() && this.offspring[index].get_tellability()>two.get_tellability()
			|| this.offspring[index+1].get_tellability()>two.get_tellability() && this.offspring[index+1].get_tellability()>two.get_tellability()) {

				global_improvement = true;

				if(this.offspring[index].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability()
				|| this.offspring[index+1].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability()) {

					local_improvement = true;
				}
			}

			this.global_cross = this.globalUpdate(this.global_cross,global_improvement);
			this.updateLocalCrossoverProbabilites(personalityPoints, happeningPoints, local_improvement);
			this.cross_prob[2] = this.operatorUpdate(this.cross_prob[2], this.global_cross, global_improvement);

		}
	}


	/*
	 * Generate crossover points
	 *
	 * @param x,y: dimensions of the array
	 * @return: Array containing truth values
	 */

	public boolean[][] setCrossoverPoints(boolean[][] result, double[][] chromosome_cross){

		boolean cross = false;

		int x = result.length;
		int y = result[0].length;

		boolean[][] crossover_points = new boolean[x][y];

		List<Integer> xlist = new ArrayList<>();

		for(Integer i = 0; i < x; i++) {
			xlist.add(i);
		}
		List<Integer> ylist = new ArrayList<>();

		for(Integer i = 0; i < y; i++) {
			ylist.add(i);
		}

		// mode is used to determine in which manner we iterate the array
		int mode = (int)Math.round(Math.random());

		if(mode == 0) {

			for(int i = 0; i < x; i++) {

				// Get a random x position
				int xPos = (int)Math.round(Math.random()*(x-i)-0.5);
				Integer xCoord = xlist.get(xPos);
				xlist.remove(xPos);

				for(int j = 0; j < y; j++){

					// Construct copy of ylist
					List<Integer> ycopy = new ArrayList<>();
					ycopy.addAll(ylist);

					// Get a random y position
					int yPos = (int)Math.round(Math.random()*(y-j)-0.5);
					Integer yCoord = ycopy.get(yPos);
					ycopy.remove(yPos);

					// FIXME: Here was error with java.lang.ArrayIndexOutOfBoundsException: 2
					if(Math.random() < this.crossover_prob * chromosome_cross[xPos][yPos]) {
						crossover_points[xPos][yPos] = true;
						cross = !cross;
					}

					result[xCoord][yCoord] = cross;
				}
			}

		} else {

			for(int j = 0; j < y; j++){

				// Get a random y position
				int yPos = (int)Math.round(Math.random()*(y-j)-0.5);
				Integer yCoord = ylist.get(yPos);
				ylist.remove(yPos);

				for(int i = 0; i < x; i++) {

					// Construct copy of xlist
					List<Integer> xcopy = new ArrayList<>();
					xcopy.addAll(xlist);

					// Get a random x position
					int xPos = (int)Math.round(Math.random()*(x-i)-0.5);
					Integer xCoord = xcopy.get(xPos);
					xcopy.remove(xPos);

					// FIXME: Here was error with java.lang.ArrayIndexOutOfBoundsException: 3
					if(Math.random() < this.crossover_prob * chromosome_cross[xPos][yPos]) { 
						crossover_points[xPos][yPos] = true;
						cross = !cross;
					}
					result[xCoord][yCoord] = cross;
				}
			}
		}
		return crossover_points;
	}


	/*
	 * Multi-sexual crossover
	 *
	 * Yields two candidates. The first one being the average of the vote values,
	 * the latter being constructed by choosing values randomly from the voters
	 *
	 * @param candidates: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void voteCrossover(List<Candidate> candidates, int index) {

		ChromosomePersonality personalityRandom = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityAverage = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsRandom = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsAverage = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				int sum = 0;
				int pos = (int)Math.round(Math.random()*candidates.size()-0.5);

				for(int k = 0; k < candidates.size(); k++) {

					sum += candidates.get(k).get_personality(i,j);
				}

				personalityRandom.values[i][j] = candidates.get(pos).get_personality(i,j);
				personalityAverage.values[i][j] = this.round(sum/candidates.size());
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				int sum = 0;
				int pos = (int)Math.round(Math.random()*candidates.size()-0.5);

				for(int k = 0; k < candidates.size(); k++) {

					sum += candidates.get(k).get_happenings(i,j);
				}

				happeningsRandom.values[i][j] = candidates.get(pos).get_happenings(i,j);
				happeningsAverage.values[i][j] = Math.round(sum/candidates.size());
			}
		}
		this.offspring[index] = this.new_Candidate(personalityAverage,happeningsAverage);
		this.offspring[index+1] = this.new_Candidate(personalityRandom,happeningsRandom);

		if(this.floatingParameters) {

			boolean improvement = false;

			if(this.offspring[index].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability()
			|| this.offspring[index+1].get_tellability()>this.gen_pool[this.individual_count/2-1].get_tellability() ) {
				improvement = true;
			}

			this.global_cross = this.globalUpdate(this.global_cross,improvement);
			this.cross_prob[3] = this.operatorUpdate(this.cross_prob[3], this.global_cross, improvement);
		}
	}


	/*
	 * Mutation
	 *
	 * Applies one mutation operator to every candidate in the offspring.
	 * Results will be stored into mutated_offspring
	 */

	public void mutate() {
		if(this.verbose) {
			System.out.println("Start Mutation");
		}

		// Set used Mutation operators
		List<Integer> mutationList = new ArrayList<>();

		for(int i = 0; i < mutationBool.length; i++) {
			if(mutationBool[i]) {
				mutationList.add(i);
			}
		}

		if(mutationList.size()==0) {
			System.out.println("No selection set. Defaulting to random mutation!");
			mutationList.add(0);
		}


		for(int i = 0; i < this.selection_size; i++) {

			int mode = 0;

			if(this.floatingParameters) {

				double sum = 0;

				for (double element : this.mut_prob) {
					sum += element;
				}

				double roulette = Math.random()*sum;

				while(roulette > this.mut_prob[mode]) {
					roulette -= this.mut_prob[mode];
					mode += 1;
				}

				this.mutation_prob = this.global_mut;

				if(this.mut_prob[mode]>0.05) {
					//mutation_prob = (mutation_prob + mut_prob[mode])/2;
					this.mutation_prob = this.mut_prob[mode];
				}

			}else {

				mode = mutationList.get((int)Math.round(Math.random()*mutationList.size()-0.5));
			}

			switch(mode) {

			case 0:

				this.mutated_offspring[i]=this.randomMutator(this.offspring[i]);
				break;

			case 1:

				this.mutated_offspring[i]=this.toggleMutator(this.offspring[i]);
				break;

			case 2:

				this.mutated_offspring[i]=this.orientedMutator(this.offspring[i]);
				break;

			case 3:

				int j = i;
				while(j==i) {
					j = (int)Math.round(Math.random()*this.selection_size-0.5);
				}
				this.mutated_offspring[i]=this.guidedMutator(this.offspring[i],this.offspring[j]);
				break;

			default:

				System.out.println("Fatal Error @ Mutation Operator Selection!");
				break;
			}
		}

		//Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(this.mutated_offspring);

		if(this.verbose) {
			System.out.println("End Mutation\n");
		}
	}


	/*
	 * Random Mutation
	 *
	 * Iterates the Chromosome and chooses a new random value for a position with probability mutation_prob
	 *
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */

	public Candidate randomMutator(Candidate recipient) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				if(Math.random()<this.mutation_prob*this.personality_mut[i][j]) {

					mutatedPersonality.values[i][j] = this.round(Math.random()*2-1);
					persChange[i][j] = true;
					change = true;

				}else {
					mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.mutation_prob*this.happenings_mut[i][j]) {

					mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*recipient.get_simLength()-0.5);
					hapsChange[i][j] = true;
					change = true;

				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}

		if(change) {

			Candidate result = this.new_Candidate(mutatedPersonality, mutatedHappenings);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellability()>recipient.get_tellability()) {

					global_improvement = true;

					if(result.get_tellability() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[0] = this.operatorUpdate(this.mut_prob[0], this.global_mut, global_improvement);
			}

			return result;
		}

		return this.randomMutator(recipient);
	}


	/*
	 * Toggle Mutation
	 *
	 * Iterates the Chromosome and makes changes based on the current values. Happenings get instantiated or turned off
	 * while personality parameters get multiplied by -1.
	 *
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */

	public Candidate toggleMutator(Candidate recipient) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				if(Math.random()<this.mutation_prob*this.personality_mut[i][j]) {

					mutatedPersonality.values[i][j] *= -1;
					persChange[i][j] = true;
					change = true;
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.mutation_prob*this.happenings_mut[i][j]) {

					if(recipient.get_happenings(i,j) > 0) {
						mutatedHappenings.values[i][j] = 0;
					} else {
						mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*(recipient.get_simLength()-1)+0.5);
					}

					hapsChange[i][j] = true;
					change = true;

				} else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}

		if(change) {

			Candidate result = this.new_Candidate(mutatedPersonality, mutatedHappenings);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellability()>recipient.get_tellability()) {

					global_improvement = true;

					if(result.get_tellability() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[1] = this.operatorUpdate(this.mut_prob[1], this.global_mut, global_improvement);
			}

			return result;
		}
		return this.toggleMutator(recipient);
	}


	/*
	 * Oriented Mutation
	 *
	 * Mutate value towards or away from another internal value in the same chromosome but at a different position
	 *
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */

	public Candidate orientedMutator(Candidate recipient) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);

				if(Math.random()<this.mutation_prob*this.personality_mut[i][j]) {

					persChange[i][j] = true;
					change = true;

					// Generate other position to look at
					int xPos = i;
					int yPos = j;

					while(i==xPos && j==yPos) {

						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*this.number_agents-0.5);
						}else {
							yPos = (int)Math.round(Math.random()*5-0.5);
						}
					}

					double ratio = Math.random()*2-1;
					double distance = recipient.get_personality(xPos,yPos) - recipient.get_personality(i,j);

					if(ratio > 0) {
						mutatedPersonality.values[i][j] += ratio * distance;
					}else {
						ratio*=-1;
						if(distance>0) {
							mutatedPersonality.values[i][j] += ratio * (-1-recipient.get_personality(i,j));
						} else {
							mutatedPersonality.values[i][j] += ratio * (1-recipient.get_personality(i,j));
						}
					}
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);

				if(Math.random()<this.mutation_prob*this.happenings_mut[i][j]) {

					hapsChange[i][j] = true;
					change = true;

					// Generate other position to look at
					int xPos = i;
					int yPos = j;

					while(i==xPos && j==yPos) {

						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*this.number_agents-0.5);
						}else {
							// FIXME: here was an error with 5 used instead of happ. num.
							yPos = (int)Math.round(Math.random()*this.number_happenings-0.5);
						}
					}

					double ratio = Math.random()*2-1;
					double distance = recipient.get_happenings(xPos,yPos) - recipient.get_happenings(i,j);

					if(ratio > 0) {
						mutatedHappenings.values[i][j] += ratio * distance;
					}else {
						ratio=-ratio;
						if(distance>0) {
							mutatedHappenings.values[i][j] -= ratio * recipient.get_happenings(i,j);
						} else {
							mutatedHappenings.values[i][j] += ratio * (recipient.get_simLength()-recipient.get_happenings(i,j));
						}
					}
				}
			}
		}

		if(change) {

			Candidate result = this.new_Candidate(mutatedPersonality, mutatedHappenings);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellability()>recipient.get_tellability()) {

					global_improvement = true;

					if(result.get_tellability() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[2] = this.operatorUpdate(this.mut_prob[2], this.global_mut, global_improvement);
			}

			return result;
		}
		return this.orientedMutator(recipient);
	}

	/*
	 * Guided Mutation
	 *
	 * Mutate a value towards or away from the corresponding value of another candidate with probability mutation_prob
	 *
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */

	public Candidate guidedMutator(Candidate recipient, Candidate mutator) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality().values[i][j];

				if(Math.random()<this.mutation_prob*this.personality_mut[i][j]) {

					persChange[i][j] = true;
					change = true;

					double ratio = Math.random()*2-1;
					double distance = mutator.get_personality(i,j) - recipient.get_personality(i,j);

					if(ratio > 0) {
						mutatedPersonality.values[i][j] += ratio * distance;
					}else {
						ratio*=-1;
						if(distance>0) {
							mutatedPersonality.values[i][j] += ratio * (-1-recipient.get_personality(i,j));
						} else {
							mutatedPersonality.values[i][j] += ratio * (1-recipient.get_personality(i,j));
						}
					}
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings().values[i][j];

				if(Math.random()<this.mutation_prob*this.happenings_mut[i][j]) {

					hapsChange[i][j] = true;
					change = true;

					double ratio = Math.random()*2-1;
					double distance = mutator.get_happenings(i,j) - recipient.get_happenings(i,j);

					if(ratio > 0) {
						mutatedHappenings.values[i][j] += ratio * distance;
					}else {
						ratio=-ratio;
						if(distance>0) {
							mutatedHappenings.values[i][j] -= ratio * recipient.get_happenings(i,j);
						} else {
							mutatedHappenings.values[i][j] += ratio * (recipient.get_simLength()-recipient.get_happenings(i,j));
						}
					}
				}
			}
		}

		if(change) {

			Candidate result = this.new_Candidate(mutatedPersonality, mutatedHappenings);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellability()>recipient.get_tellability()) {

					global_improvement = true;

					if(result.get_tellability() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[3] = this.operatorUpdate(this.mut_prob[3], this.global_mut, global_improvement);
			}

			return result;
		}
		return this.guidedMutator(recipient, mutator);
	}

	/*
	 * Setup next Generation
	 */

	public void recombine() {

		if(steadyReplace) {
			this.gen_pool = this.steadyNoDuplicatesReplacer();
		} else {
			this.gen_pool = this.partiallyRandomNoDuplicatesReplacer();
		}

		//Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(this.gen_pool);

		// remove artifacts
		this.offspring = new Candidate[this.selection_size];
		this.mutated_offspring = new Candidate[this.selection_size];
	}

	/**
	 * Keep best half of last generation
	 * Fill up other half with best performing individuals from offspring
	 *
	 * @return: Array containing candidates chosen to remain in the population
	 */
	public Candidate[] steadyNoDuplicatesReplacer() {

		Candidate[] next_gen = new Candidate[this.individual_count];

		int steady = this.individual_count/2;

		// Create List of all newly generated Candidates sorted by fitness descending (rest of old population is at end)
		List<Candidate> total_offspring = new ArrayList<>();
		int posPop = steady;
		int posOff = 0;
		int posMut = 0;

		for(int i = 0; i < this.individual_count-steady+this.selection_size*2; i++) {

			int best = 0;
			double bestTellability = -1;

			if(posOff < this.selection_size) {
				if(this.offspring[posOff].get_tellability()>bestTellability) {
					best = 1;
					bestTellability = this.offspring[posOff].get_tellability();
				}
			}

			if(posMut < this.selection_size) {
				if(this.mutated_offspring[posMut].get_tellability()>bestTellability) {
					best = 2;
					bestTellability = this.mutated_offspring[posMut].get_tellability();
				}
			}

			switch(best) {

			case 0:

				total_offspring.add(this.gen_pool[posPop]);
				posPop++;
				break;

			case 1:

				total_offspring.add(this.offspring[posOff]);
				posOff++;
				break;

			case 2:

				total_offspring.add(this.mutated_offspring[posMut]);
				posMut++;
				break;

			default:

				System.out.println("Missing Candidates @ partiallyRandomNoDuplicatesReplacer()");
				return null;
			}
		}

		// Keep best performing candidates of current Population
		for(int i = 0; i < steady; i++) {
			next_gen[i] = this.gen_pool[i];
		}

		// Add best candidates from total_offspring to next_gen avoiding duplicates
		for(int i = steady; i<this.individual_count; i++) {

			boolean done = false;

			if(total_offspring.isEmpty()) {
				done = true;
				next_gen[i] = this.gen_pool[i-steady];
			}

			while(!done) {
				if(!total_offspring.get(0).isContainedIn(next_gen)) {
					next_gen[i] = total_offspring.get(0);
					done = true;
				}
				total_offspring.remove(0);
			}
		}
		return next_gen;
	}

	/*
	 * Keep best n Candidates with n = individual_count/2
	 * Fill up other half random individuals from the population
	 *
	 * @return: Array containing candidates chosen to remain in the population
	 */

	public Candidate[] partiallyRandomNoDuplicatesReplacer() {

		Candidate[] next_gen = new Candidate[this.individual_count];

		// Initialize a List with all Candidates positions.get(state).get_lifespan()ed by fitness descending
		List<Candidate> allCandidates = new ArrayList<>();

		int posPop = 0;
		int posOff = 0;
		int posMut = 0;

		for(int i = 0; i < this.individual_count+this.selection_size*2; i++) {

			int best = 0;
			double bestTellability = -1;

			if(posPop < this.individual_count) {
				if(this.gen_pool[posPop].get_tellability()>bestTellability) {
					best = 1;
					bestTellability = this.gen_pool[posPop].get_tellability();
				}
			}

			if(posOff < this.selection_size) {
				if(this.offspring[posOff].get_tellability()>bestTellability) {
					best = 2;
					bestTellability = this.offspring[posOff].get_tellability();
				}
			}

			if(posMut < this.selection_size) {
				if(this.mutated_offspring[posMut].get_tellability()>bestTellability) {
					best = 3;
					bestTellability = this.mutated_offspring[posMut].get_tellability();
				}
			}

			switch(best) {

			case 1:

				allCandidates.add(this.gen_pool[posPop]);
				posPop++;
				break;

			case 2:

				allCandidates.add(this.offspring[posOff]);
				posOff++;
				break;

			case 3:

				allCandidates.add(this.mutated_offspring[posMut]);
				posMut++;
				break;

			default:

				System.out.println("Missing Candidates @ partiallyRandomNoDuplicatesReplacer()");
				return null;
			}
		}

		// Keep best performing candidates of current Population
		int steady = this.individual_count/2;

		for(int i = 0; i < steady; i++) {

			boolean done = false;

			while(!done) {
				if(!allCandidates.get(0).isContainedIn(next_gen)) {
					next_gen[i] = allCandidates.get(0);
					done = true;
				}
				allCandidates.remove(0);
			}
		}

		// Fill rest with random candidates but avoid adding duplicates
		for(int i = steady; i<this.individual_count; i++) {

			boolean done = false;

			while(!done) {
				int pos = (int)Math.round(Math.random()*allCandidates.size()-0.5);
				if(!allCandidates.isEmpty()) {
					if(!allCandidates.get(pos).isContainedIn(next_gen)) {
						next_gen[i] = allCandidates.get(pos);
						done = true;
					}
					allCandidates.remove(pos);
				}
			}
		}
		return next_gen;
	}
}
