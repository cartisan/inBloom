package inBloom.evo;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class GeneticAlgorithm<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends EvolutionaryAlgorithm<EnvType,ModType>{
	
	public int selection_size;
	public double crossover_prob;
	public double mutation_prob;
	
	// Container for candidates
	private Candidate[] gen_pool;
	private Candidate[] offspring;
	private Candidate[] mutated_offspring;
	
	// Performance measurement
	private List<Double> population_bestHalf = new ArrayList<Double>();
	
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
	
	
	/*
	 * Constructors for GA
	 */
	
	public GeneticAlgorithm (String[] args, EvolutionaryEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count, int number_selections, double crossover_prob, double mutation_prob) {
		
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		this.selection_size = number_selections*2;
		this.crossover_prob = crossover_prob;
		this.mutation_prob = mutation_prob;
		
	}
	
	
	/**
	 * Get & Set Methods
	 */
	
	public Candidate[] get_genPool() {
		
		return gen_pool;
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
		
		if(pos>=0 && pos < persInitBool.length)
			persInitBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
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
		
		if(pos>=0 && pos < hapInitBool.length)
			hapInitBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
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
	
	public void setSelection(int pos, boolean bool) {
		
		if(pos>=0 && pos < selectionBool.length)
			selectionBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
	}
	
	public void setSelection(boolean random, boolean rWheel) {
		
		selectionBool[0] = random;
		selectionBool[1] = rWheel;		
	}
	
	
	// Crossover operators
	public boolean[] getCrossover() {
		
		return selectionBool;
	}
	
	public void setCrossover(int pos, boolean bool) {
		
		if(pos>=0 && pos < crossoverBool.length)
			crossoverBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
	}
	
	public void setCrossover(boolean random, boolean binomial, boolean xPoint, boolean vote) {
		
		crossoverBool[0] = random;
		crossoverBool[1] = binomial;	
		crossoverBool[2] = xPoint;	
		crossoverBool[3] = vote;		
	}
	
	
	// Mutation operators
	public boolean[] getMutation() {
		
		return selectionBool;
	}
	
	public void setMutation(int pos, boolean bool) {
		
		if(pos>=0 && pos < mutationBool.length)
			mutationBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
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
		if(individual_count<4) {
			System.out.println("Size of population defaulted to 4!");
			this.individual_count = 4;
		}
		
		// Selection size must be positive
		if(selection_size < 0) {
			System.out.println("Selection_size defaulted to: " + selection_size);
			selection_size = 2;
		}
		
		// Selection size must be an even number smaller individual_count
		while(selection_size>=individual_count) {
			selection_size/=2;
			selection_size%=2;
			System.out.println("Selection_size reduced to: " + selection_size);
		}
		
		// There need to be agents and happenings
		if(number_agents <= 0 || number_happenings <= 0) {
			System.out.println("Bad Configuration!");
			return false;
		}

		if(mutation_prob<0||mutation_prob>1) {
			System.out.println("Mutation_prob: " + mutation_prob + " is suboptimal!");
			return false;
		}
		
		if(crossover_prob<0||crossover_prob>1) {
			System.out.println("Crossover_prob: " + crossover_prob + " is suboptimal!");
			return false;
		}
		
		return true;
	}
	
	
	/*
	 * Instantiates new fitness object and hands it over to the Candidate to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Candidate
	 */
	
	public Candidate new_Candidate(ChromosomePersonality pers,ChromosomeHappenings hap) {
		
		return new_Candidate(pers,hap,determineLength(hap));
	}
	
	public Candidate new_Candidate(ChromosomePersonality pers,ChromosomeHappenings hap, Integer steps) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV);
		
		System.out.println("Starting new Simulation: " + steps);
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
		
		Double best = gen_pool[0].get_tellability();
		Double average = 0.0;
		
		int relevant_size = individual_count/2;
		
		for(int i = 0; i < relevant_size; i++)
			
			average += gen_pool[i].get_tellability();
		
		double halfAverage = average/relevant_size;
		
		for(int i = relevant_size; i < individual_count; i++)
			
			average += gen_pool[i].get_tellability();
		
		average /= individual_count;
		
		
		// Determine if there was improvement
		if(population_best.size()>0) {
			if(population_best.get(population_best.size()-1)==best && population_bestHalf.get(population_bestHalf.size()-1)==halfAverage)
				no_improvement++;
			else
				no_improvement=0;
		}
		population_best.add(best);
		population_bestHalf.add(halfAverage);
		population_average.add(average);
	}
	
	
	/*
	 * Genetic algorithm main loop
	 * Initializes Population and runs the loop until termination criterion is met.
	 */
	
	public void run() {
		
		// Ensure correct Parameters
		if(check_parameters()) {
			
			// Save current time
			start_time = System.currentTimeMillis();
			
			// Generate and evaluate initial population 
			initialize_pop();
			evaluate_population();
			
			// Repeat until termination (no improvements found or time criterion -if set- is met):
			while(no_improvement<termination && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0)) {
				
				// Verbose
				int generation = population_best.size()-1;
				
				System.out.println();
				System.out.println("Generation: " + generation);
				System.out.println();
				System.out.println("Best individual: " + population_best.get(generation));
				System.out.println("Generation Average: " + population_average.get(generation));
				System.out.println();
				
				if(no_improvement>0) {
					System.out.println("No improvement found for " + no_improvement + " generations!");
					System.out.println();
				}
	
				crossover(select());
				mutate();
				recombine();
				evaluate_population();
			}
			
			System.out.println();
			System.out.println("This is the End!");
			System.out.println();
			System.out.println("Generations: " + population_best.size());
			System.out.println("Best so far: " + gen_pool[0].get_tellability() + " , with simulation length: " + gen_pool[0].get_simLength());
			
			to_file(gen_pool[0]);
			
			if(system_exit)
				System.exit(0);
		}
	}
	
	
	/*
	 * Initialization
	 * Generates Initial population
	 */
	
	public void initialize_pop() {
		
		gen_pool = new Candidate[individual_count];
		offspring = new Candidate[selection_size];
		mutated_offspring = new Candidate[selection_size];
		
		int index = 0;

		// Set used initializers for personality
		List<Integer> persInitializer = new ArrayList<Integer>();
		
		for(int i = 0; i < persInitBool.length; i++) {
			if(persInitBool[i])
				persInitializer.add(i);
		}
		
		if(persInitializer.size()==0) {
			System.out.println("No initialization set for personality chromosome. Defaulting to random init!");
			persInitializer.add(0);
		}

		// Set used initializers for happenings
		List<Integer> hapInitializer = new ArrayList<Integer>();
		
		for(int i = 0; i < hapInitBool.length; i++) {
			if(hapInitBool[i])
				hapInitializer.add(i);
		}
		
		if(hapInitializer.size()==0) {
			System.out.println("No initialization set for happenings chromosome. Defaulting to random init!");
			hapInitializer.add(0);
		}
		
		// Initialize population
		while(index<individual_count) {
			
			// Create new personality chromosome
			ChromosomePersonality personality = new ChromosomePersonality(number_agents);

			int persType = persInitializer.get((int)Math.round(Math.random()*persInitializer.size()-0.5));
			
			switch(persType) {	

			case(2):
				
				if(discretePersValues.length>4) {
					personality = steadyDiscretePersonalityInitializer();
					break;
				}

			case(1):
				
				if(discretePersValues.length>0) {
					personality = discretePersonalityInitializer();
					break;
				}
					
			case(0):

				personality = randomPersonalityInitializer();
				break;
				
			default:
				
				System.out.println("Fatal Error @ Personality Initialization Selection!");
				break;
			}
			
			// Create new happening chromosome
			ChromosomeHappenings happenings = new ChromosomeHappenings(number_agents,number_happenings);
			
			int hapType = hapInitializer.get((int)Math.round(Math.random()*hapInitializer.size()-0.5));

			switch(hapType) {	

			case(0):	
				
				happenings = randomHappeningsInitializer();
				break;

			case(1):
	
				happenings = probabilisticHappeningsInitializer();
				break;
				
			case(2):
				
				happenings = steadyHappeningsInitializer();
				break;
				
			default:
				
				System.out.println("Fatal Error @ Happening Initialization Selection!");
				break;
			}
			
			// Check for Duplicates
			boolean isDuplicate = false;

			for(int i = 0; i < index; i++) {
				if(gen_pool[i].equals(personality,happenings)) {

					isDuplicate = true;
				}
			}
			
			// First candidate cannot have duplicates
			if(index==0 || !isDuplicate) {

				gen_pool[index] = new_Candidate(personality,happenings);
				index++;
			}
		}
		
		// Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(gen_pool);
	}
	
	
	/*
	 * Random initializer for ChomosomePersonality
	 * Chooses 5 values from the Interval [-1;1] randomly for every agent
	 * @Return: Instantiated Chromosome
	 */
	
	public ChromosomePersonality randomPersonalityInitializer() {
		
		ChromosomePersonality personality = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				
				personality.values[i][j] = round(Math.random()*2-1);
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
		
		ChromosomePersonality personality = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
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
		
		ChromosomePersonality personality = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {

			ArrayList<Double> discreteList = new ArrayList<Double>();
			
			for(int j = 0; j < discretePersValues.length; j++) {
				discreteList.add(discretePersValues[j]);
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
		
		ChromosomeHappenings happenings = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				happenings.values[i][j] = (int)Math.round(Math.random()*(max_steps/number_happenings+1)-0.5)*number_happenings;
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
		
		ChromosomeHappenings happenings = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				if(Math.random()<1/number_agents) {
					happenings.values[i][j] = (int)Math.round(Math.random()*(max_steps/number_happenings)+0.5)*number_happenings;
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
		
		ChromosomeHappenings happenings = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			
			int j = (int)Math.round(Math.random()*number_agents-0.5);

			happenings.values[i][j] = (int)Math.round(Math.random()*(max_steps/number_happenings)+0.5)*number_happenings;
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
		List<Integer> selectList = new ArrayList<Integer>();
				
		for(int i = 0; i < selectionBool.length; i++) {
			if(selectionBool[i])
				selectList.add(i);
		}
		
		if(selectList.size()==0) {
			System.out.println("No selection set. Defaulting to random selection!");
			selectList.add(0);
		}
		
		List<Integer> result = new ArrayList<Integer>();
		
		Integer type = selectList.get((int)Math.round(Math.random()*selectList.size()-0.5));
		
		switch(type) {
		
		case(0):

			result = randomSelector();
			break;
			
		case(1):
			
			result = rouletteWheelSelector();
			break;			
			
		default:
			
			System.out.println("Fatal Error @ Selection Operator Selection!");
			break;
		}	
		return result;
	}
	
	
	/*
	 *  Random Selection
	 *  
	 *  Chooses selection_size many Candidates from the gen_pool in a random manner
	 * @Return: List with positions of chosen individuals 
	 */
	
	public List<Integer> randomSelector(){
		
		List<Integer> selectedindividuals = new ArrayList<Integer>();
		
		while(selectedindividuals.size() < selection_size) {
			
			int position = (int)Math.round(Math.random()*individual_count-0.5);
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
		double[] rouletteWheel = new double[individual_count];
		
		// Control parameters
		boolean control = true;
		int validIndividuals = 0;
		
		for(int i = 0; i < individual_count; i++) {
			
			total_fitness += gen_pool[i].get_tellability();
			rouletteWheel[i] = total_fitness;
			
			// Check if we have enough individuals with fitness
			if(control && gen_pool[i].get_tellability()==0) {
				
				validIndividuals = i;
				control = false;
			}
		}
		
		// Pick Candidates
		List<Integer> selectedIndividuals = new ArrayList<Integer>();
		
		if(validIndividuals > selection_size) {
			
			//If there are enough valid candidates use Roulette-Wheel approach
			while(selectedIndividuals.size() < selection_size) {
				
				int position = 0;
				int repetition = 0;
				double value = Math.random()*total_fitness;
				
				while(value > rouletteWheel[position]) {
					position++;
				}
				if(!selectedIndividuals.contains(position))
					selectedIndividuals.add(position);
			}
			
		}else {
			
			// Just fill selection with the first n individuals
			for(int i = 0; i < selection_size; i++)
				selectedIndividuals.add(i);
			
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
		
		// Set used Crossover operators
		List<Integer> crossoverList = new ArrayList<Integer>();
				
		for(int i = 0; i < crossoverBool.length; i++) {
			if(crossoverBool[i])
				crossoverList.add(i);
		}
		
		if(crossoverList.size()==0) {
			System.out.println("No selection set. Defaulting to simple crossover!");
			crossoverList.add(0);
		}
		
		for(int i = 0; i < selection_size; i+=2) {
			
			int pos = (int)Math.round(Math.random()*positions.size()-0.5);
			int one = positions.get(pos);
			positions.remove(pos);
			pos = (int)Math.round(Math.random()*positions.size()-0.5);
			int two = positions.get(pos);
			positions.remove(pos);
			
			Integer type = crossoverList.get((int)Math.round(Math.random()*crossoverList.size()-0.5));
			
			switch(type) {

			case(0):
			
				simpleCrossover(gen_pool[one],gen_pool[two],i);
				break;
			
			case(1):
			
				binomialCrossover(gen_pool[one],gen_pool[two],i);
				break;
				
			
			case(2):
				
				xPointCrossover(gen_pool[one],gen_pool[two],i);
				break;
			
			case(3):
				
				// Add 2 initial candidates to the List
				List<Candidate> candidates = new ArrayList<Candidate>();
			
				candidates.add(gen_pool[one]);
				candidates.add(gen_pool[two]);
				
				// Initialize list containing additional possible voters
				List<Integer> possibleVoters = new ArrayList<Integer>();
				
				for(int v = 0; v < individual_count; v++) {
					if(v != one && v != two) 
						possibleVoters.add(v);
				}
				
				// Add additional Votes
				int additionalVotes = (int)Math.round(Math.random()*(individual_count-2)-0.5);
				
				while(additionalVotes > 0) {
					
					int votePos = (int)Math.round(Math.random()*possibleVoters.size()-0.5);
					
					candidates.add(gen_pool[possibleVoters.get(votePos)]);
					additionalVotes--;
				}
				
				voteCrossover(candidates,i);
				break;
				
			default:
				
				System.out.println("Fatal Error @ Crossover Operator Selection!");
				break;
			}
		}
		// Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(offspring);
	}
	
	
	/*
	 * "Simply" exchanges ChromosomePersonality and ChromosomeHappenings
	 * Is equal to onePointCrossover with a fixed crossover Point
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void simpleCrossover(Candidate one, Candidate two, int index) {
		
		offspring[index] = new_Candidate(one.get_personality(), two.get_happenings());
		offspring[index+1] = new_Candidate(two.get_personality(), one.get_happenings());
	}
	
	
	/*
	 * Exchanges Allele with probability crossover_prob
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	
	public void binomialCrossover(Candidate one, Candidate two, int index) {
		
		ChromosomePersonality personalityOne = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(Math.random()>crossover_prob) {
					personalityOne.values[i][j] = one.get_personality(i,j);
					personalityTwo.values[i][j] = two.get_personality(i,j);
				}else {
					personalityOne.values[i][j] = two.get_personality(i,j);
					personalityTwo.values[i][j] = one.get_personality(i,j);
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(Math.random()>crossover_prob) {
					happeningsOne.values[i][j] = one.get_happenings(i,j);
					happeningsTwo.values[i][j] = two.get_happenings(i,j);
				}else {
					happeningsOne.values[i][j] = two.get_happenings(i,j);
					happeningsTwo.values[i][j] = one.get_happenings(i,j);
				}
			}
		}
		
		offspring[index] = new_Candidate(personalityOne,happeningsOne);
		offspring[index+1] = new_Candidate(personalityTwo,happeningsTwo);	
	}
	
	
	/*
	 * Exchanges allele between crossover points.
	 * Crossover points are generated by the function setCrossoverPoints()
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	
	public void xPointCrossover(Candidate one, Candidate two, int index) {

		boolean[][] crossPersonality = setCrossoverPoints(number_agents,5);
		boolean[][] crossHappenings = setCrossoverPoints(number_agents,number_happenings);
		
		ChromosomePersonality personalityOne = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(crossPersonality[i][j]) {
					personalityOne.values[i][j] = one.get_personality(i,j);
					personalityTwo.values[i][j] = two.get_personality(i,j);
				}else {
					personalityOne.values[i][j] = two.get_personality(i,j);
					personalityTwo.values[i][j] = one.get_personality(i,j);
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(crossHappenings[i][j]) {
					happeningsOne.values[i][j] = one.get_happenings(i,j);
					happeningsTwo.values[i][j] = two.get_happenings(i,j);
				}else {
					happeningsOne.values[i][j] = two.get_happenings(i,j);
					happeningsTwo.values[i][j] = one.get_happenings(i,j);
				}
			}
		}	
		offspring[index] = new_Candidate(personalityOne,happeningsOne);
		offspring[index+1] = new_Candidate(personalityTwo,happeningsTwo);	
	}
	
	
	/*
	 * Generate crossover points
	 * 
	 * @param x,y: dimensions of the array
	 * @return: Array containing truth values
	 */
	
	public boolean[][] setCrossoverPoints(int x, int y){
		
		return setCrossoverPoints(x,y,false);
	}
	
	public boolean[][] setCrossoverPoints(int x, int y, boolean initial){

		boolean cross = initial;
		
		boolean[][] result = new boolean[x][y];
		
		List<Integer> xlist = new ArrayList<Integer>();
		
		for(Integer i = 0; i < x; i++) {
			xlist.add(i);
		}
		List<Integer> ylist = new ArrayList<Integer>();
		
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
					List<Integer> ycopy = new ArrayList<Integer>();
					ycopy.addAll(ylist);
					
					// Get a random y position
					int yPos = (int)Math.round(Math.random()*(y-j)-0.5);
					Integer yCoord = ycopy.get(yPos);
					ycopy.remove(yPos);
					
					// Determine whether current position will be a crossover point
					if(Math.random()<crossover_prob) {
						cross = !cross;
					}
					
					result[xCoord][yCoord] = cross;
				}
			}
		}else {		
			
			for(int j = 0; j < y; j++){

				// Get a random y position
				int yPos = (int)Math.round(Math.random()*(y-j)-0.5);
				Integer yCoord = ylist.get(yPos);
				ylist.remove(yPos);
				
				for(int i = 0; i < x; i++) {

					// Construct copy of xlist
					List<Integer> xcopy = new ArrayList<Integer>();
					xcopy.addAll(xlist);
					
					// Get a random x position
					int xPos = (int)Math.round(Math.random()*(x-i)-0.5);
					Integer xCoord = xcopy.get(xPos);
					xcopy.remove(xPos);
					
					// Determine whether current position will be a crossover point
					if(Math.random()<crossover_prob) {
						cross = !cross;
					}
					
					result[xCoord][yCoord] = cross;		
				}
			}
		}
		return result;
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
		
		ChromosomePersonality personalityRandom = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityAverage = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsRandom = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsAverage = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {
				
				int sum = 0;
				int pos = (int)Math.round(Math.random()*candidates.size()-0.5);
				
				for(int k = 0; k < candidates.size(); k++) {
					
					sum += candidates.get(k).get_personality(i,j);
				}
				
				personalityRandom.values[i][j] = candidates.get(pos).get_personality(i,j);
				personalityAverage.values[i][j] = round(sum/candidates.size());
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {
				
				int sum = 0;
				int pos = (int)Math.round(Math.random()*candidates.size()-0.5);
				
				for(int k = 0; k < candidates.size(); k++) {
					
					sum += candidates.get(k).get_happenings(i,j);
				}
									
				happeningsRandom.values[i][j] = candidates.get(pos).get_happenings(i,j);
				happeningsAverage.values[i][j] = (int)Math.round(sum/candidates.size());
			}
		}
		offspring[index] = new_Candidate(personalityAverage,happeningsAverage);
		offspring[index+1] = new_Candidate(personalityRandom,happeningsRandom);	
	}
	
	
	/*
	 * Mutation
	 * 
	 * Applies one mutation operator to every candidate in the offspring.
	 * Results will be stored into mutated_offspring
	 */
	
	public void mutate() {
		
		// Set used Mutation operators
		List<Integer> mutationList = new ArrayList<Integer>();
				
		for(int i = 0; i < mutationBool.length; i++) {
			if(mutationBool[i])
				mutationList.add(i);
		}
		
		if(mutationList.size()==0) {
			System.out.println("No selection set. Defaulting to random mutation!");
			mutationList.add(0);
		}
				
		
		for(int i = 0; i < selection_size; i++) {
			
			int mode = mutationList.get((int)Math.round(Math.random()*mutationList.size()-0.5));
			
			switch(mode) {

			case(0):
				
				mutated_offspring[i]=randomMutator(offspring[i]);
				break;
			
			case(1):
				
				mutated_offspring[i]=toggleMutator(offspring[i]);
				break;
				
			case(2):
				
				mutated_offspring[i]=orientedMutator(offspring[i]);
				break;
				
			case(3):
				
				int j = i;
				while(j==i) {
					j = (int)Math.round(Math.random()*selection_size-0.5);
				}
				mutated_offspring[i]=guidedMutator(offspring[i],offspring[j]);
				break;
				
			default:
				
				System.out.println("Fatal Error @ Crossover Operator Selection!");
				break;
			}
		}
		//Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(mutated_offspring);
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
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {
				
				if(Math.random()<mutation_prob) {
					mutatedPersonality.values[i][j] = round(Math.random()*2-1);
				}else {
					mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				}
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {

				if(Math.random()<mutation_prob) {
					mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*recipient.get_simLength()-0.5);
				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}	
		return new_Candidate(mutatedPersonality, mutatedHappenings);
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
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				if(Math.random()<mutation_prob)
					mutatedPersonality.values[i][j] *= -1;
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {

				if(Math.random()<mutation_prob) {
					
					if(recipient.get_happenings(i,j) > 0)
						mutatedHappenings.values[i][j] = 0;
					else
						mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*(recipient.get_simLength()-1)+0.5);
					
				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}	
		return new_Candidate(mutatedPersonality,mutatedHappenings);
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
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				
				if(Math.random()<mutation_prob) {
					
					// Generate other position to look at
					int xPos = i;
					int yPos = j;
					
					while(i==xPos && j==yPos) {
						
						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*number_agents-0.5);
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
						if(distance>0)
							mutatedPersonality.values[i][j] += ratio * (-1-recipient.get_personality(i,j));
						else
							mutatedPersonality.values[i][j] += ratio * (1-recipient.get_personality(i,j));
					}
				}
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				
				if(Math.random()<mutation_prob) {

					// Generate other position to look at
					int xPos = i;
					int yPos = j;
					
					while(i==xPos && j==yPos) {
						
						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*number_agents-0.5);
						}else {
							yPos = (int)Math.round(Math.random()*5-0.5);
						}
					}
					
					double ratio = Math.random()*2-1;
					double distance = recipient.get_happenings(xPos,yPos) - recipient.get_happenings(i,j);
					
					if(ratio > 0) {
						mutatedHappenings.values[i][j] += ratio * distance;
					}else {
						ratio*=-1;
						if(distance>0)
							mutatedHappenings.values[i][j] -= ratio * (recipient.get_happenings(i,j));
						else
							mutatedHappenings.values[i][j] += ratio * (recipient.get_simLength()-recipient.get_happenings(i,j));
					}
				}
			}
		}
		return new_Candidate(mutatedPersonality, mutatedHappenings);
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
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality().values[i][j];
				
				if(Math.random()<mutation_prob) {
					
					double ratio = Math.random()*2-1;
					double distance = mutator.get_personality(i,j) - recipient.get_personality(i,j);
					
					if(ratio > 0) {
						mutatedPersonality.values[i][j] += ratio * distance;
					}else {
						ratio*=-1;
						if(distance>0)
							mutatedPersonality.values[i][j] += ratio * (-1-recipient.get_personality(i,j));
						else
							mutatedPersonality.values[i][j] += ratio * (1-recipient.get_personality(i,j));
					}
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings().values[i][j];
				
				if(Math.random()<mutation_prob) {
					
					double ratio = Math.random()*2-1;
					double distance = mutator.get_happenings(i,j) - recipient.get_happenings(i,j);
					
					if(ratio > 0) {
						mutatedHappenings.values[i][j] += ratio * distance;
					}else {
						ratio*=-1;
						if(distance>0)
							mutatedHappenings.values[i][j] -= ratio * (recipient.get_happenings(i,j));
						else
							mutatedHappenings.values[i][j] += ratio * (recipient.get_simLength()-recipient.get_happenings(i,j));
					}
				}
			}
		}
		return new_Candidate(mutatedPersonality, mutatedHappenings);
	}
	
	/*
	 * Setup next Generation
	 */
	
	public void recombine() {
		
		if(steadyReplace)
			gen_pool = steadyNoDuplicatesReplacer();
		else
			gen_pool = partiallyRandomNoDuplicatesReplacer();

		//Sort Candidates by performance. Best Candidate will be at position zero descending
		Arrays.sort(gen_pool);
		
		// remove artifacts
		offspring = new Candidate[selection_size];
		mutated_offspring = new Candidate[selection_size];
	}
	
	/*
	 * Keep best half of last generation
	 * Fill up other half with best performing individuals from offspring
	 * 
	 * @return: Array containing candidates chosen to remain in the population
	 */
	
	public Candidate[] steadyNoDuplicatesReplacer() {
		
		Candidate[] next_gen = new Candidate[individual_count];
		
		int steady = individual_count/2;
		
		// Create List of all newly generated Candidates sorted by fitness descending (rest of old population is at end)
		List<Candidate> total_offspring = new ArrayList<Candidate>();
		int posPop = steady;
		int posOff = 0;
		int posMut = 0;
		
		for(int i = 0; i < (individual_count-steady)+selection_size*2; i++) {
			
			int best = 0;
			double bestTellability = -1;
			
			if(posOff < selection_size) {
				if(offspring[posOff].get_tellability()>bestTellability) {
					best = 1;
					bestTellability = offspring[posOff].get_tellability();
				}
			}
			
			if(posMut < selection_size) {
				if(mutated_offspring[posMut].get_tellability()>bestTellability) {
					best = 2;
					bestTellability = mutated_offspring[posMut].get_tellability();
				}
			}
			
			switch(best) {
			
			case(0):
				
				total_offspring.add(gen_pool[posPop]);
				posPop++;
				break;

			case(1):
				
				total_offspring.add(offspring[posOff]);
				posOff++;
				break;
				
			case(2):
				
				total_offspring.add(mutated_offspring[posMut]);
				posMut++;
				break;
				
			default:
			
				System.out.println("Missing Candidates @ partiallyRandomNoDuplicatesReplacer()");
				return null;
			}
		}
		
		// Keep best performing candidates of current Population
		for(int i = 0; i < steady; i++) {
			next_gen[i] = gen_pool[i];
		}
		
		// Add best candidates from total_offspring to next_gen avoiding duplicates
		for(int i = steady; i<individual_count; i++) {
			
			boolean done = false;
			
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
		
		Candidate[] next_gen = new Candidate[individual_count];
		
		// Initialize a List with all Candidates sorted by fitness descending
		List<Candidate> allCandidates = new ArrayList<Candidate>();
		
		int posPop = 0;
		int posOff = 0;
		int posMut = 0;
		
		for(int i = 0; i < individual_count+selection_size*2; i++) {
			
			int best = 0;
			double bestTellability = -1;
			
			if(posPop < individual_count) {
				if(gen_pool[posPop].get_tellability()>bestTellability) {
					best = 1;
					bestTellability = gen_pool[posPop].get_tellability();
				}
			}
			
			if(posOff < selection_size) {
				if(offspring[posOff].get_tellability()>bestTellability) {
					best = 2;
					bestTellability = offspring[posOff].get_tellability();
				}
			}
			
			if(posMut < selection_size) {
				if(mutated_offspring[posMut].get_tellability()>bestTellability) {
					best = 3;
					bestTellability = mutated_offspring[posMut].get_tellability();
				}
			}
			
			switch(best) {
			
			case(1):
				
				allCandidates.add(gen_pool[posPop]);
				posPop++;
				break;

			case(2):
				
				allCandidates.add(offspring[posOff]);
				posOff++;
				break;
				
			case(3):
				
				allCandidates.add(mutated_offspring[posMut]);
				posMut++;
				break;
				
			default:
			
				System.out.println("Missing Candidates @ partiallyRandomNoDuplicatesReplacer()");
				return null;
			}
		}
		
		// Keep best performing candidates of current Population
		int steady = individual_count/2;
		
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
		for(int i = steady; i<individual_count; i++) {
			
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
