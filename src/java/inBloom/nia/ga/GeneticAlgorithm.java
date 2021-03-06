package inBloom.nia.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomeLength;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.NIAlgorithm;
import inBloom.nia.NIEnvironment;

public class GeneticAlgorithm<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends NIAlgorithm<EnvType,ModType>{
	private static final boolean USE_FLOATING_PARAM = false;
	private static final double DEFAULT_CROSSOVER_PROB = 0.1;
	private static final double DEFAULT_MUTATION_PROB = 0.05;
	private static final double DEFAULT_DECAY_RATE = 0.05;
	private static final int DISCRETE_HAP_SPACING = 5;
	private static final int DISCRETE_LEN_SPACING = 5;

	// Parameters for static version
	public int selection_size;
	public double crossover_prob = DEFAULT_CROSSOVER_PROB;
	public double mutation_prob = DEFAULT_MUTATION_PROB;

	// Container for candidates
	private Individual[] offspring;
	private Individual[] mutated_offspring;

	// Information storage for floating parameter version
	private boolean floatingParameters;

	private double decay_rate = DEFAULT_DECAY_RATE;

	private double global_cross;
	private double global_mut;

	// parameters for Floating Param version
	private double[] cross_prob;
	private double[] mut_prob;
	private double[][] personality_cross;
	private double[][] personality_mut;
	private double[][] happenings_cross;
	private double[][] happenings_mut;
	private double length_mut;
	private double length_cross;

	// Performance measurement
	private List<Double> population_bestHalf = new ArrayList<>();
	private double population_bestAverage = 0;

	// Discrete values to choose from for personality initialization
	private static double[] discretePersValues = {-1,-0.9,-0.75,-0.5,-0.25,-0.1,0,0.1,0.25,0.5,0.75,0.9,1};
	private static int[] discreteHapValues = new int[DISCRETE_HAP_SPACING];
	private static int[] discreteLenValues = new int[DISCRETE_LEN_SPACING];

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
	 * Constructors for GA, version based on default parameters in static final members
	 */
	public GeneticAlgorithm (String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.floatingParameters = USE_FLOATING_PARAM;
		this.selection_size = number_selections;

		// set discreteHapValues such, that we have 5 evenly spaced entries starting from 1
		Arrays.setAll(discreteHapValues, i -> Math.round(this.estimated_max_steps / DISCRETE_HAP_SPACING) * i);			// for estimated_max_step 30 and spacing 5: {0, 6, 12, 18, 24}
		Arrays.setAll(discreteLenValues, i -> Math.round(MAX_SIM_LENGTH / DISCRETE_HAP_SPACING) * (i+1));	// for MAX_SIM_LENGTH 100 and spacing 5: {20, 40, 60, 80, 100}
	}

	/**
	 * Constructors for GA, static version
	 */
	public GeneticAlgorithm (String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections, double crossover_prob, double mutation_prob) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.floatingParameters = false;
		this.selection_size = number_selections;

		this.crossover_prob = crossover_prob;
		this.mutation_prob = mutation_prob;

		// set discreteHapValues such, that we have 5 evenly spaced entries starting from 1
		Arrays.setAll(discreteHapValues, i -> Math.round(this.estimated_max_steps / DISCRETE_HAP_SPACING) * i);			// for estimated_max_steps 30 and spacing 5: {0, 6, 12, 18, 24}
		Arrays.setAll(discreteLenValues, i -> Math.round(MAX_SIM_LENGTH / DISCRETE_HAP_SPACING) * (i+1));	// for MAX_SIM_LENGTH 100 and spacing 5: {20, 40, 60, 80, 100}
	}

	/**
	 * Constructors for GA, floating param version
	 */
	public GeneticAlgorithm (String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections, double decay) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.floatingParameters = true;
		this.selection_size = number_selections;

		this.decay_rate = decay;

		// set discreteHapValues such, that we have 5 evenly spaced entries starting from 1
		Arrays.setAll(discreteHapValues, i -> Math.round(this.estimated_max_steps / DISCRETE_HAP_SPACING) * i);			// for estimated_max_steps 30 and spacing 5: {0, 6, 12, 18, 24}
		Arrays.setAll(discreteLenValues, i -> Math.round(MAX_SIM_LENGTH / DISCRETE_HAP_SPACING) * (i+1));	// for MAX_SIM_LENGTH 100 and spacing 5: {20, 40, 60, 80, 100}
	}


	/**
	 * Get & Set Methods
	 */
	public Individual[] get_genPool() {
		return (Individual[])this.population;
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

	@Override
	public boolean check_parameters() {

		// Ensure correctness of parameters

		// Minimum Population size
		if(this.individual_count<4) {
			System.out.println("Size of population defaulted to 4!");
			this.individual_count = 4;
		}

		// Selection size must be positive
		if(this.selection_size < 2) {
			this.selection_size = 2;
			System.out.println("Selection_size defaulted to: " + this.selection_size);
		}

		// Selection size must be at least half of population
		if(this.selection_size < this.individual_count / 2) {
			this.selection_size = (int) Math.ceil((double) this.individual_count / 2);
			System.out.println("Selection_size must be at least half of population size, setting to: " + this.selection_size);
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
	 * Instantiates new fitness object and hands it over to the Individual to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Individual
	 */
	public Individual new_Candidate(ChromosomePersonality pers, ChromosomeHappenings hap) {

		return this.new_Candidate(pers, hap, this.determineLength(hap));
	}

	public Individual new_Candidate(ChromosomePersonality pers, ChromosomeHappenings hap, Integer steps) {
		Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV, this.verbose, this.level);

		return new Individual(pers, hap, steps, fit);

	}

	/**
	 * The average fitness of the population as well as best candidate's fitness is computed.
	 * If there was no improvement compared to the last generation, termination criterion counter gets incremented.
	 * If there was an improvement, termination criterion counter is reset.
	 * Average will only take the best half of population into account to ensure termination.
	 */
	@Override
	protected void evaluate_population() {
		Double best = this.population[0].get_tellabilityValue();
		Double average = 0.0;
		Double lenAve = 0.0;

		int relevant_size = this.individual_count/2;

		for(int i = 0; i < relevant_size; i++) {
			average += this.population[i].get_tellabilityValue();
			lenAve += this.population[i].get_actualLength();
		}

		double halfAverage = average/relevant_size;

		for(int i = relevant_size; i < this.individual_count; i++) {
			average += this.population[i].get_tellabilityValue();
			lenAve += this.population[i].get_simLength().value;
		}

		average /= this.individual_count;
		lenAve /= this.individual_count;

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
		this.average_length.add(lenAve);

		if(this.population_bestAverage > average) {
			this.population_bestAverage = average;
		}

		if(this.floatingParameters) {
			this.determineGlobalParameters();
		}

		// Clean log containing detailed information about tellability computation of individuals that are not best
		// otherwise, we run out of heap memory
		for (int i=1; i<this.population.length; ++i) {
			this.population[i].cleanTellabilityLog();
		}
	}

	@Override
	public void run_iteration() {
		this.crossover(this.select());
		this.mutate();
		this.recombine();
	}

	@Override
	protected boolean keepRunning() {
		return (no_improvement < 0 || no_improvement<termination) && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0) && (!this.floatingParameters || this.global_mut>0 || this.global_cross>0);
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

		if(this.floatingParameters) {
			this.length_mut = 0.5;
			this.length_cross = 0.5;
		} else {
			this.length_mut = 1.0;
			this.length_cross = 1.0;
		}
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

			System.out.println(this.length_mut);
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
		this.length_mut =  this.localUpdate(this.length_mut, this.global_mut, improvement);

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

	@Override
	public void initialize_population() {
		// Initialize information containers for floating parameter version
		this.initialize_floatingParameters();

		this.population = new Individual[this.individual_count];
		this.offspring = new Individual[this.selection_size];
		this.mutated_offspring = new Individual[this.selection_size];

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
		while(index < this.individual_count) {
			// Create new length chromosome
			ChromosomeLength length = null;
			int lenType = this.random.nextInt(2);
			switch(lenType) {
			case 0:
				// random initialization of length
				length = new ChromosomeLength(this.random.nextInt(MAX_SIM_LENGTH));
				break;
			case 1:
				// discretized initialization of length
				length = new ChromosomeLength(discreteLenValues[this.random.nextInt(discreteLenValues.length)]);
				break;
			}

			// Create new personality chromosome
			ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);

			int persType = persInitializer.get((int)Math.round(Math.random()*persInitializer.size()-0.5));
			switch(persType) {
			case 0:
				personality = this.randomPersonalityInitializer();
				break;
			case 1:
				if(discretePersValues.length>0) {
					personality = this.discretePersonalityInitializer();
					break;
				}
			case 2:
				if(discretePersValues.length>4) {
					personality = this.steadyDiscretePersonalityInitializer();
					break;
				}
			default:
				System.out.println("Fatal Error @ Personality Initialization Selection!");
				break;
			}

			// Create new happening chromosome
			ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

			int hapType = hapInitializer.get((int)Math.round(Math.random()*hapInitializer.size()-0.5));
			switch(hapType) {
			case 0:
				happenings = this.randomHappeningsInitializer(length.value);
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
				if(((Individual) this.population[i]).equals(personality,happenings)) {

					isDuplicate = true;
				}
			}

			// First candidate cannot have duplicates
			if(index==0 || !isDuplicate) {

				this.population[index] = this.new_Candidate(personality, happenings, length.value);
				index++;
			}
		}

		// Sort Candidates by performance. Best Individual will be at position zero descending
		Arrays.sort(this.population);
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
				personality.values[i][j] = discretePersValues[this.random.nextInt(discretePersValues.length)];
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
				int position = this.random.nextInt(discreteList.size());
				personality.values[i][j] = discreteList.get(position);
				discreteList.remove(position);
			}
		}
		return personality;
	}


	/**
	 * Random initializer for ChromosomeHappening
	 * Inserts random numbers between 0 and simLengh into the chromosome.
	 * @Return: Instantiated Chromosome
	 */
	public ChromosomeHappenings randomHappeningsInitializer(int simLengh) {
		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				happenings.values[i][j] = this.random.nextInt(simLengh);
			}
		}
		return happenings;
	}


	/**
	 * Instantiates a random happening based on one of DISVRETE_HAPP_SPACING discrete values, in order to achieve more
	 * chance of synchronicity
	 * @Return: Instantiated Chromosome
	 */
	public ChromosomeHappenings probabilisticHappeningsInitializer() {
		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				happenings.values[i][j] = discreteHapValues[this.random.nextInt(discreteHapValues.length)];
			}
		}
		return happenings;
	}


	/**
	 * Instantiates every happening exactly once and assigns it to a random agent
	 * Inserts random numbers between 0 and estimated_max_steps into the chromosome.
	 * Numbers are discretized to be multiples of estimated_max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */
	public ChromosomeHappenings steadyHappeningsInitializer() {
		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int j = 0; j < this.number_happenings; j++) {
			int i = this.random.nextInt(this.number_agents);
			happenings.values[i][j] = discreteHapValues[this.random.nextInt(discreteHapValues.length)];
		}
		return happenings;
	}


	/*
	 * Selection
	 *
	 * Picks Candidates for further application of genetic operators
	 * @Return: List with positions in population of chosen candidates
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
	 *  Chooses selection_size many Candidates from the population in a random manner
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

			total_fitness += this.population[i].get_tellabilityValue();
			rouletteWheel[i] = total_fitness;

			// Check if we have enough individuals with fitness
			if(control && this.population[i].get_tellabilityValue()==0) {

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
				this.simpleCrossover((Individual) this.population[one], (Individual) this.population[two], i);
				break;
			case 1:
				this.binomialCrossover((Individual) this.population[one], (Individual) this.population[two], i);
				break;
			case 2:
				this.xPointCrossover((Individual) this.population[one], (Individual) this.population[two], i);
				break;
			case 3:
				// Add 2 initial candidates to the List
				List<Individual> candidates = new ArrayList<>();

				candidates.add((Individual) this.population[one]);
				candidates.add((Individual) this.population[two]);

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

					candidates.add((Individual) this.population[possibleVoters.get(votePos)]);
					additionalVotes--;
				}

				this.voteCrossover(candidates,i);
				break;
			default:
				this.offspring[i] = (Individual) this.population[one];
				this.offspring[i+1] = (Individual) this.population[two];
				System.out.println("No crossover operator selected!");
				break;
			}
		}

		if(this.verbose) {
			System.out.println("End Crossover\n");
		}
	}


	/**
	 * Exhanges one or two chromosomes between individuals one and two, on a random basis.
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	public void simpleCrossover(Individual one, Individual two, int index) {
		Integer lengthOne = one.get_simLength().value;
		Integer lengthTwo = two.get_simLength().value;

		ChromosomePersonality personalityOne = one.get_personality();
		ChromosomePersonality personalityTwo = two.get_personality();

		ChromosomeHappenings happeningsOne = one.get_happenings();
		ChromosomeHappenings happeningsTwo = two.get_happenings();


		// randomly decide whether one or two chromosomes are exchanged
		int changeNum = this.random.nextInt(2) + 1;

		// select which chromosomes to exchange: 0 - length, 1 - personality, 2 - happening
		List<Integer> chromosomeIndex = Lists.newArrayList(0, 1, 2);
		Collections.shuffle(chromosomeIndex);		// random shuffle places index of chromosomes to be swapped at beginning
		while (changeNum > 0) {
			int switchChrom = chromosomeIndex.remove(0);

			switch (switchChrom) {
			case 0:
				int tmpL = lengthOne;
				lengthOne = lengthTwo;
				lengthTwo = tmpL;
				break;
			case 1:
				ChromosomePersonality tmpP = personalityOne;
				personalityOne = personalityTwo;
				personalityTwo = tmpP;
				break;
			case 2:
				ChromosomeHappenings tmpH = happeningsOne;
				happeningsOne = happeningsTwo;
				happeningsTwo = tmpH;
				break;
			}

			changeNum -= 1;
		}

		this.offspring[index] = this.new_Candidate(personalityOne, happeningsOne, lengthOne);
		this.offspring[index+1] = this.new_Candidate(personalityTwo, happeningsTwo, lengthTwo);

		if(this.floatingParameters) {

			boolean improvement = false;

			if(this.offspring[index].get_tellabilityValue()>one.get_tellabilityValue() && this.offspring[index].get_tellabilityValue()>two.get_tellabilityValue()
			|| this.offspring[index+1].get_tellabilityValue()>one.get_tellabilityValue() && this.offspring[index+1].get_tellabilityValue()>two.get_tellabilityValue()) {
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

	public void binomialCrossover(Individual one, Individual two, int index) {
		Integer lengthOne = one.get_simLength().value;
		Integer lengthTwo = two.get_simLength().value;

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

		if(Math.random() < this.crossover_prob * this.length_cross) {
			Integer tmp = lengthOne;
			lengthOne = lengthTwo;
			lengthTwo = tmp;
		}

		this.offspring[index] = this.new_Candidate(personalityOne,happeningsOne, lengthOne);
		this.offspring[index+1] = this.new_Candidate(personalityTwo,happeningsTwo, lengthTwo);

		if(this.floatingParameters) {

			boolean local_improvement = false;
			boolean global_improvement = false;

			if(this.offspring[index].get_tellabilityValue()>one.get_tellabilityValue() && this.offspring[index].get_tellabilityValue()>two.get_tellabilityValue()
			|| this.offspring[index+1].get_tellabilityValue()>two.get_tellabilityValue() && this.offspring[index+1].get_tellabilityValue()>two.get_tellabilityValue()) {

				global_improvement = true;

				if(this.offspring[index].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue()
				|| this.offspring[index+1].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue()) {

					local_improvement = true;
				}
			}

			this.global_cross = this.globalUpdate(this.global_cross,global_improvement);
			this.updateLocalCrossoverProbabilites(persChange, hapsChange, local_improvement);
			this.cross_prob[1] = this.operatorUpdate(this.cross_prob[1], this.global_cross, global_improvement);
		}
	}


	/**
	 * Exchanges allele between crossover points.
	 * Crossover points are generated by the function setCrossoverPoints()
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	public void xPointCrossover(Individual one, Individual two, int index) {
		boolean[][] crossPersonality = new boolean[this.number_agents][5];
		boolean[][] personalityPoints = this.setCrossoverPoints(crossPersonality, this.personality_cross);

		// FIXME: Here was error where crossHappenings was combined with this.personality_cross
		boolean[][] crossHappenings = new boolean[this.number_agents][this.number_happenings];
		boolean[][] happeningPoints = this.setCrossoverPoints(crossHappenings, this.happenings_cross);

		Integer lengthOne = one.get_simLength().value;
		Integer lengthTwo = two.get_simLength().value;

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

		// since length is a single allele instead of an array, this is simply a single switch decision
		if(Math.random() < this.crossover_prob * this.length_cross) {
			Integer tmp = lengthOne;
			lengthOne = lengthTwo;
			lengthTwo = tmp;
		}

		this.offspring[index] = this.new_Candidate(personalityOne,happeningsOne);
		this.offspring[index+1] = this.new_Candidate(personalityTwo,happeningsTwo);

		if(this.floatingParameters) {
			boolean local_improvement = false;
			boolean global_improvement = false;

			if(this.offspring[index].get_tellabilityValue()>one.get_tellabilityValue() && this.offspring[index].get_tellabilityValue()>two.get_tellabilityValue()
			|| this.offspring[index+1].get_tellabilityValue()>two.get_tellabilityValue() && this.offspring[index+1].get_tellabilityValue()>two.get_tellabilityValue()) {

				global_improvement = true;

				if(this.offspring[index].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue()
				|| this.offspring[index+1].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue()) {

					local_improvement = true;
				}
			}

			this.global_cross = this.globalUpdate(this.global_cross,global_improvement);
			this.updateLocalCrossoverProbabilites(personalityPoints, happeningPoints, local_improvement);
			this.cross_prob[2] = this.operatorUpdate(this.cross_prob[2], this.global_cross, global_improvement);

		}
	}


	/**
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

	public void voteCrossover(List<Individual> candidates, int index) {

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

			if(this.offspring[index].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue()
			|| this.offspring[index+1].get_tellabilityValue()>this.population[this.individual_count/2-1].get_tellabilityValue() ) {
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

		if(this.verbose) {
			System.out.println("End Mutation\n");
		}
	}


	/**
	 * Random Mutation
	 *
	 * Iterates the Chromosome and chooses a new random value for a position with probability mutation_prob
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */
	public Individual randomMutator(Individual recipient) {
		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);
		Integer mutatedLength = recipient.get_simLength().value;

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];

		// Simulation Length
		if(Math.random() < this.mutation_prob * this.length_mut) {
			mutatedLength = this.random.nextInt(MAX_SIM_LENGTH);
			change = true;
		}

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
					mutatedHappenings.values[i][j] = (int)Math.round(Math.random() * mutatedLength - 0.5);
					hapsChange[i][j] = true;
					change = true;

				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}

		if(change) {
			Individual result = this.new_Candidate(mutatedPersonality, mutatedHappenings, mutatedLength);
			if(this.floatingParameters) {
				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellabilityValue()>recipient.get_tellabilityValue()) {

					global_improvement = true;

					if(result.get_tellabilityValue() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[0] = this.operatorUpdate(this.mut_prob[0], this.global_mut, global_improvement);
			}

			return result;
		}

		return recipient;
	}


	/*
	 * Toggle Mutation
	 *
	 * Iterates the Chromosome and makes changes based on the current values. Happenings get instantiated or turned off,
	 * personality parameters get multiplied by -1, length gets set to max or randomized
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public Individual toggleMutator(Individual recipient) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);
		Integer mutatedLength = recipient.get_simLength().value;

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];


		// Simulation Length
		if(Math.random() < this.mutation_prob * this.length_mut) {
			// if length is max, set to random
			if (recipient.get_simLength().value >= MAX_SIM_LENGTH) {
				mutatedLength = this.random.nextInt(MAX_SIM_LENGTH);
			}
			// if length random, set to max
			else {
				mutatedLength = MAX_SIM_LENGTH;
			}
			change = true;
		}

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
						mutatedHappenings.values[i][j] = (int)Math.round(Math.random() * mutatedLength + 0.5);
					}

					hapsChange[i][j] = true;
					change = true;

				} else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}

		if(change) {

			Individual result = this.new_Candidate(mutatedPersonality, mutatedHappenings, mutatedLength);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellabilityValue()>recipient.get_tellabilityValue()) {

					global_improvement = true;

					if(result.get_tellabilityValue() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[1] = this.operatorUpdate(this.mut_prob[1], this.global_mut, global_improvement);
			}

			return result;
		}
		return recipient;
	}


	/*
	 * Oriented Mutation
	 *
	 * Mutate value towards or away from another internal value in the same chromosome but at a different position. No
	 * changed for simLength.
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public Individual orientedMutator(Individual recipient) {

		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);
		Integer mutatedLength = recipient.get_simLength().value;

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
							mutatedHappenings.values[i][j] += ratio * (mutatedLength - recipient.get_happenings(i,j));
						}
					}
				}
			}
		}

		if(change) {
			Individual result = this.new_Candidate(mutatedPersonality, mutatedHappenings, mutatedLength);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellabilityValue()>recipient.get_tellabilityValue()) {

					global_improvement = true;

					if(result.get_tellabilityValue() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[2] = this.operatorUpdate(this.mut_prob[2], this.global_mut, global_improvement);
			}

			return result;
		}
		return recipient;
	}

	/*
	 * Guided Mutation
	 *
	 * Mutate a value towards or away from the corresponding value of another candidate with probability mutation_prob
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public Individual guidedMutator(Individual recipient, Individual mutator) {
		boolean change = false;

		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);
		Integer mutatedLength = recipient.get_simLength().value;

		boolean[][] persChange = new boolean[this.number_agents][5];
		boolean[][] hapsChange = new boolean[this.number_agents][this.number_happenings];
		double ratio;
		double distance;

		// Simulation Length
		if(Math.random() < this.mutation_prob * this.length_mut) {
			ratio = Math.random() * 2 - 1;		// value in [-1, 1)
			distance = mutator.get_simLength().value - recipient.get_simLength().value;
			mutatedLength += (int) Math.round(ratio * distance);
		}

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality().values[i][j];

				if(Math.random()<this.mutation_prob*this.personality_mut[i][j]) {

					persChange[i][j] = true;
					change = true;

					ratio = Math.random()*2-1;
					distance = mutator.get_personality(i,j) - recipient.get_personality(i,j);

					if(ratio > 0) {
						mutatedPersonality.values[i][j] += ratio * distance;
					} else {
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

					ratio = Math.random()*2-1;
					distance = mutator.get_happenings(i,j) - recipient.get_happenings(i,j);

					if(ratio > 0) {
						mutatedHappenings.values[i][j] += ratio * distance;
					}else {
						ratio=-ratio;
						if(distance>0) {
							mutatedHappenings.values[i][j] -= ratio * recipient.get_happenings(i,j);
						} else {
							mutatedHappenings.values[i][j] += ratio * (mutatedLength - recipient.get_happenings(i,j));
						}
					}
				}
			}
		}

		if(change) {
			Individual result = this.new_Candidate(mutatedPersonality, mutatedHappenings, mutatedLength);

			if(this.floatingParameters) {

				boolean global_improvement = false;
				boolean local_improvement = false;

				if(result.get_tellabilityValue()>recipient.get_tellabilityValue()) {

					global_improvement = true;

					if(result.get_tellabilityValue() > this.population_bestAverage) {
						local_improvement = true;
					}
				}

				this.global_mut = this.globalUpdate(this.global_mut,global_improvement);
				this.updateLocalMutationProbabilites(persChange, hapsChange, local_improvement);
				this.mut_prob[3] = this.operatorUpdate(this.mut_prob[3], this.global_mut, global_improvement);
			}

			return result;
		}
		return recipient;
	}

	/*
	 * Setup next Generation
	 */

	public void recombine() {
		System.out.println("Start evaluate mutated offspring");
		for (Individual i: this.mutated_offspring) {
			i.evaluate();
		}
		Arrays.sort(this.mutated_offspring);

		if(steadyReplace) {
			this.population = this.steadyNoDuplicatesReplacer();
		} else {
			this.population = this.partiallyRandomNoDuplicatesReplacer();
		}

		//Sort Candidates by performance. Best Individual will be at position zero descending
		Arrays.sort(this.population);

		// remove artifacts
		this.offspring = new Individual[this.selection_size];
		this.mutated_offspring = new Individual[this.selection_size];
	}

	/**
	 * Tries to find the index iTmp of the next individual in sourceCollection, at or after originalIndex, that is not yet
	 * contained in targetCollection, such that appending sourceCollection[iTmp] to targetCollection will not leed to
	 * duplication.
	 * Returns index iTmp, or if this is impossible, -1.
	 * @param originalIndex
	 * @param targetCollection
	 * @param sourceCollection
	 * @return
	 */
	private int indexOfNextNonDuplicate(int originalIndex, Individual[] targetCollection, CandidateSolution[] sourceCollection) {
		int iTmp = originalIndex;
		while(iTmp < sourceCollection.length) {
			// check if this individual is already part of population
			if(! ((Individual) sourceCollection[iTmp]).isContainedIn(targetCollection) ) {
				// if it isn't, we can use this index
				return iTmp;
			} else {
				// if it is, try with next one
				iTmp += 1;
			}
		}
		// we reached end of sourceCollection and did not find a non-duplicate, indicate this with -1 code
		return -1;
	}

	/**
	 * Keep best half of last generation
	 * Fill up other half with best performing individuals from offspring
	 *
	 * @return: Array containing candidates chosen to remain in the population
	 */
	public Individual[] steadyNoDuplicatesReplacer() {
		Individual[] next_gen = new Individual[this.individual_count];
		int middle = this.individual_count/2;

		// copy best half of population into next gen
		for (int i=0; i < middle; ++i) {
			int sourceIndex = this.indexOfNextNonDuplicate(i, next_gen, this.population);
			// if we found a non duplicate in population at or after index i, use this one
			if (sourceIndex != -1) {
				next_gen[i] = (Individual) this.population[sourceIndex];
			} else {
				//otherwise take i and accept that there will be duplication in next_gen
				next_gen[i] = (Individual) this.population[i];
			}
		}

		// copy best half of offspring into next gen
		for (int i=middle; i < this.individual_count; ++i) {
			int sourceIndex = this.indexOfNextNonDuplicate(i-middle, next_gen, this.mutated_offspring);
			// if we found a non duplicate in population at or after index i, use this one
			if (sourceIndex != -1) {
				next_gen[i] = this.mutated_offspring[sourceIndex];
			} else {
				//otherwise take i and accept that there will be duplication in next_gen
				next_gen[i] = this.mutated_offspring[i-middle];
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

	public Individual[] partiallyRandomNoDuplicatesReplacer() {

		Individual[] next_gen = new Individual[this.individual_count];

		// Initialize a List with all Candidates positions.get(state).get_lifespan()ed by fitness descending
		List<Individual> allCandidates = new ArrayList<>();

		int posPop = 0;
		int posOff = 0;
		int posMut = 0;

		for(int i = 0; i < this.individual_count+this.selection_size*2; i++) {

			int best = 0;
			double bestTellability = -1;

			if(posPop < this.individual_count) {
				if(this.population[posPop].get_tellabilityValue()>bestTellability) {
					best = 1;
					bestTellability = this.population[posPop].get_tellabilityValue();
				}
			}

			if(posOff < this.selection_size) {
				if(this.offspring[posOff].get_tellabilityValue()>bestTellability) {
					best = 2;
					bestTellability = this.offspring[posOff].get_tellabilityValue();
				}
			}

			if(posMut < this.selection_size) {
				if(this.mutated_offspring[posMut].get_tellabilityValue()>bestTellability) {
					best = 3;
				}
			}

			switch(best) {

			case 1:

				allCandidates.add((Individual) this.population[posPop]);
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
