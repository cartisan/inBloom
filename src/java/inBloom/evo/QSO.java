package inBloom.evo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class QSO <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>>  extends EvolutionaryAlgorithm<EnvType,ModType>{

	
	// Particle container
	private Quantum[] quantum_particles;
	
	private double[][] max_happenings;
	private double[][] min_happenings;
	private double[][] max_personality;
	private double[][] min_personality;
	
	// Discrete values to choose from for personality initialization
	private static double[] discretePersValues = {-1,-0.9,-0.75,-0.5,-0.25,-0.1,0,0.1,0.25,0.5,0.75,0.9,1};
	private static double[] discretePersVelocity = {-0.1,-0.05,0,0.05,0.1};
	private static int[] discreteHapVelocity = {-2,-1,0,1,2};

	
	// Boolean arrays are used to manage operators. True enables usage.

	// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
	private static boolean[] persInitBool = {true,true,true};
	// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
	private static boolean[] hapInitBool = {true,true,true}; 
	// randomVelocityInit, discreteVelocityInit
	// false, false -> no velocity initialization. Works as long as particles have different positions
	private static boolean[] velInitBool = {true,true};
	// binomialCrossover,xPointCrossover,voteCrossover
	private static boolean[] crossoverBool = {false,true,true,true};
	// randomMutator,toggleMutator,orientedMutator,guidedMutator
	private static boolean[] mutationBool = {true,true,true,true}; 
	
	private double crossover_prob;
	private double mutation_prob;
	
	// determine manner of updating velocity
	// number of informants for a particle
	private static int number_informants = 1; 
	// true -> Roulette Wheel Selection, false -> choose best
	private static boolean fitnessBasedSelection = false;
	// false -> use static update rate, false -> update with force calculation
	private static boolean floatingParameters = false;
	// if floatingParameters == false: use this as update rate
	private static double decay_rate = 0.05;
	
	
	public QSO(String[] args, EvolutionaryEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		crossover_prob = 5+number_happenings;
		mutation_prob = 5+number_happenings;
	}

	/**
	 * Get & Set Methods
	 */
	
	// Get all particles
	public Quantum[] getParticles() {
		
		return quantum_particles;
	}
	
	// Get specified particle particles
	public Quantum getParticle(int position) {
		
		return quantum_particles[position];
	}
	
	// Discrete values for personality initialization
	public void setPersonalityValues(double[] discreteValues) {
		
		discretePersValues = discreteValues;
	}
	
	public double[] getPersonalityValues() {
		
		return discretePersValues;
	}

	
	// Discrete values for personality initialization
	public void setPersonalityVelocity(double[] discreteVelocity) {
		
		discretePersVelocity = discreteVelocity;
	}
	
	public double[] getPersonalityVelocity() {
		
		return discretePersVelocity;
	}

	// Discrete values for personality initialization
	public void setHappeningsVelocity(int[] discreteVelocity) {
		
		discreteHapVelocity = discreteVelocity;
	}
	
	public int[] getHappeningsVelocity() {
		
		return discreteHapVelocity;
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
	
	
	// Velocity initialization operators
	public boolean[] getVelInit() {
		
		return velInitBool;
	}
	
	public void setVelInit(int pos, boolean bool) {
		
		if(pos>=0 && pos < velInitBool.length)
			velInitBool[pos] = bool;
		else
			System.out.println("Position is out of bounds");
	}
	
	public void setVelInit(boolean random, boolean discrete) {
		
		velInitBool[0] = random;
		velInitBool[1] = discrete;
	}
	
	
	// Crossover operators
	public boolean[] getCrossover() {
		
		return crossoverBool;
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
		
		return mutationBool;
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

	// velocity update
	public int getVelocityInformants() {
		return number_informants;
	}
	
	public void setVelocityInformants(int informants) {
		number_informants = informants;
	}
	
	public boolean getSelectionManner() {
		return fitnessBasedSelection;
	}
	
	public void setSelectionManner(boolean manner) {
		fitnessBasedSelection = manner;
	}
	
	public boolean getFloatingParameters() {
		return floatingParameters;
	}
	
	public void setFloatingParameters(boolean floating) {
		floatingParameters = floating;
	}
	
	public double getDecayRate() {
		return decay_rate;
	}
	
	public void setDecayRate(double rate) {
		decay_rate = rate;
	}
	
	
	/**
	 * Difference Measurements
	 */
	
	
	/*
	 * Returns the normalized difference of two particles tellability score
	 * 
	 * @param recipient: Particle that receives velocity update
	 * @param informant: Particle providing information to recipient
	 * @return: Double between [0,1] reflecting the difference rating
	 */
	
	public double fitness_rating(int recipient, int state, int informant) {
		
		// If both particles have a best tellability of 0 they shall get a high rating
		// Therefore they will get propelled away from another and ensure exploration.
		if(quantum_particles[informant].best_tellability()==0 && quantum_particles[recipient].best_tellability()==0)
			return -1;
		
		return (quantum_particles[informant].best_tellability() - quantum_particles[recipient].get_tellability(state))/quantum_particles[0].best_tellability();
	}
	
	
	/*
	 * Rates the difference between two particles in terms of distance.
	 * The difference is based on the normalized pythagorean distance.
	 * 
	 * @param recipient: Particle that receives velocity update
	 * @param informant: Particle providing information to recipient
	 * @return: Double between [0,1] reflecting the difference rating
	 */
	
	public double distance_rating(int recipient, int informant) {
		
		double distance = 0;
		int n = number_agents*5 + number_agents*number_happenings;
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				//distance += 1-(Math.abs(particles[informant].best_personality(i, j) - particles[recipient].get_personality(i, j))/2);
				if(max_personality[i][j]-min_personality[i][j] != 0)
					distance += Math.pow(1-(Math.abs(quantum_particles[informant].best_personality(i, j) - quantum_particles[recipient].get_personality(i, j))/(max_personality[i][j]-min_personality[i][j])),2)/n;
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(max_happenings[i][j]-min_happenings[i][j]!=0)
					distance += Math.pow(1-(Math.abs(quantum_particles[informant].best_happenings(i, j) - quantum_particles[recipient].get_happenings(i, j))/(max_happenings[i][j]-min_happenings[i][j])),2)/n;
			}
		}
		
		return Math.sqrt(distance);
	}
	
	
	/*
	 * Determines the maximum and minimum values existent in the particles chromosomes.
	 */
	
	public void update_Distances() {

		// Reset memory
		min_personality = max_personality;
		max_personality = new double[number_agents][5];
		
		min_happenings = max_happenings;
		max_happenings = new double[number_agents][number_happenings];
		
		// Find maximum values
		for(int index = 0; index < individual_count; index++) {
			for(int agents = 0; agents < number_agents; agents++) {
				
				for(int personality = 0; personality < 5; personality++) {
					
					if(quantum_particles[index].get_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = quantum_particles[index].get_personality(agents, personality);
					
					if(quantum_particles[index].best_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = quantum_particles[index].best_personality(agents, personality);
					
					if(quantum_particles[index].get_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = quantum_particles[index].get_personality(agents, personality);
					
					if(quantum_particles[index].best_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = quantum_particles[index].best_personality(agents, personality);
				}
				
				for(int happenings = 0; happenings < number_happenings; happenings++) {

					if(quantum_particles[index].get_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = quantum_particles[index].get_happenings(agents, happenings);

					if(quantum_particles[index].best_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = quantum_particles[index].best_happenings(agents, happenings);
					
					if(quantum_particles[index].get_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = quantum_particles[index].get_happenings(agents, happenings);
					
					if(quantum_particles[index].best_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = quantum_particles[index].best_happenings(agents, happenings);
				}
			}
		}
	}
	

	@Override
	public boolean check_parameters() {
		
		if(number_agents <= 0 || number_happenings <= 0) {
			System.out.println("Bad Configuration!");
			return false;
		}
		
		// number_informants should not be negative
		if(number_informants < 0) {
			number_informants = 1;
			System.out.println("number_informants defaulted to: " + number_informants);
		}
		
		// number_informants must be smaller than pop_size
		while(number_informants>=individual_count) {
			number_informants/=2;
			System.out.println("number_informants reduced to: " + number_informants);
		}
		
		return true;
	}

	@Override
	protected void evaluate_population() {
		
		double best = quantum_particles[0].best_tellability();
		double average = 0;

		for(int i = 0; i < individual_count; i++) {
			
			average += quantum_particles[i].best_tellability();
		}
		
		average /= individual_count;
		
		// Determine if there was improvement
		if(population_best.size()>0) {
			if(population_best.get(population_best.size()-1)==best && population_average.get(population_average.size()-1)==average)
				no_improvement++;
			else
				no_improvement=0;
		}
		
		population_best.add(best);
		population_average.add(average);
		
	}
	
	

	@Override
	public void run() {
		
		if(check_parameters()) {
			// Save current time
			start_time = System.currentTimeMillis();
			
			// Generate and evaluate initial particles
			initialize_particles();
			evaluate_population();
			
			// Repeat until termination (no improvements found or time criterion -if set- is met):
			while(no_improvement<termination && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0)) {
				
				// Print Statistics
				if(verbose)
					generation_stats();
				
				crossover();
				mutate();
				move_particles();
				update_movement();
				evaluate_population();
				
			}

			// Print Statistics
			if(verbose)
				final_stats();

			to_file(quantum_particles[0]);
			if(system_exit)
				System.exit(0);
		}
	}
	/*
	 * Instantiates new fitness object and hands it over to the Candidate to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Candidate
	 */
	
	public Quantum new_quantum(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap) {
		
		return new_quantum(pers,velocity_pers,hap,velocity_hap,determineLength(hap));
	}
	
	public Quantum new_quantum(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap, Integer steps) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV,verbose,level);
		
		return new Quantum(pers, velocity_pers, hap, velocity_hap, steps, fit);
	}
	
	public QuantumPosition new_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap) {
		
		return new_quantumPosition(quant,state,pers,hap,determineLength(hap));
	}
	
	public QuantumPosition new_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap, Integer steps) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV,verbose,level);

		return new QuantumPosition(pers, quant.get_position(state).get_persVelocity(), hap,quant.get_position(state).get_hapVelocity(), steps, 0, fit);
	}
	
	public void add_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV,verbose,level);

		quant.add_Position(state, pers, hap, determineLength(hap), fit);
	}


	private void initialize_particles() {

		quantum_particles = new Quantum[individual_count];
	
		// Initialize arrays for tracking minimum and maximum values
		min_personality = new double[number_agents][5];
		max_personality = new double[number_agents][5];
		
		min_happenings = new double[number_agents][number_happenings];
		max_happenings = new double[number_agents][number_happenings];

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
		
		// Set used initializers for happenings
		List<Integer> velInitializer = new ArrayList<Integer>();
		
		for(int i = 0; i < velInitBool.length; i++) {
			if(velInitBool[i])
				velInitializer.add(i);
		}
		
		
		
		// Initialize population
		for(int index=0; index<individual_count; index++) {
			
			// Create new chromosomes
			ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
			ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);

			if(!velInitializer.isEmpty()) {
				
				int velType = velInitializer.get((int)Math.round(Math.random()*velInitializer.size()-0.5));
				
				switch(velType) {
				
				case(0):	

					pers_velocity = randomPersonalityVelocityInitializer();
					hap_velocity = randomHappeningsVelocityInitializer();
					break;

				case(1):
					
					pers_velocity = discretePersonalityVelocityInitializer();
					hap_velocity = discreteHappeningsVelocityInitializer();
					break;
					
				default:
					
					System.out.println("Fatal Error @ Velocity Initialization Selection!");
					break;
				}
			}
			
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
			
			quantum_particles[index] = new_quantum(personality,pers_velocity,happenings,hap_velocity,max_steps);
		}
		
		Arrays.sort(quantum_particles);
		// Update max distances
		update_Distances();
		// Update min distances
		update_Distances();
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

	
	public ChromosomePersonality randomPersonalityVelocityInitializer() {
		
		ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				
				pers_velocity.values[i][j] = round(Math.random()*0.2-0.1);
			}
		}
		return pers_velocity;
	}
	
	
	public ChromosomeHappenings randomHappeningsVelocityInitializer() {
		
		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				
				hap_velocity.values[i][j] = (int)Math.round((Math.random()*2-1)*max_steps/number_happenings);
			}
		}
		return hap_velocity;	
	}
	
	
	public ChromosomePersonality discretePersonalityVelocityInitializer() {
		
		ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				
				pers_velocity.values[i][j] = discretePersVelocity[(int)Math.round(Math.random()*discretePersVelocity.length-0.5)];
			}
		}
		return pers_velocity;
		
	}
	
	
	public ChromosomeHappenings discreteHappeningsVelocityInitializer() {
		
		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				hap_velocity.values[i][j] = discreteHapVelocity[(int)Math.round(Math.random()*discreteHapVelocity.length-0.5)];
			}
		}
		return hap_velocity;
	}
	
	public List<Integer> select(int amount){
		
		List<Integer> unique_positions = new ArrayList<Integer>();
		
		double total_tellability = 0;
		
		for(int i = 0; i < individual_count; i++) {
			
			total_tellability += quantum_particles[i].best_tellability();
		}
		
		for(int i = 0; i < amount; i++) {
			
			double roulette = Math.random()*total_tellability;
			int pos = 0;
			
			while(roulette > quantum_particles[pos].best_tellability()) {
				
				roulette -= quantum_particles[pos].best_tellability();
			}
			
			unique_positions.add(pos);
		}
		
		return unique_positions;
	}
	
	public void crossover() {
		
		// Set used Crossover operators
		List<Integer> crossoverList = new ArrayList<Integer>();
				
		for(int i = 0; i < crossoverBool.length; i++) {
			if(crossoverBool[i])
				crossoverList.add(i);
		}
		
		if(crossoverList.size()==0) {
			System.out.println("No selection set. Defaulting to binomial crossover!");
			crossoverList.add(1);
		}
		
		int mode = crossoverList.get((int)Math.round(Math.random()*crossoverList.size()-0.5));
		
		int amount = 2;
		
		if(mode == 3)
			amount += Math.random()*(individual_count-2);
		
		List<Integer> positions = select(amount);
		
		int state = quantum_particles[positions.get(0)].choosePosition();
		
		
		
		switch(mode) {

		case(0):
			
			simpleCrossover(quantum_particles[positions.get(0)],quantum_particles[positions.get(1)],state);
			break;
		
		case(1):
		
			binomialCrossover(quantum_particles[positions.get(0)],quantum_particles[positions.get(1)],state);
			break;
			
		
		case(2):
			
			xPointCrossover(quantum_particles[positions.get(0)],quantum_particles[positions.get(1)],state);
			break;
		
		case(3):
			
			voteCrossover(positions,state);
			break;
		}
	}

	/*
	 * "Simply" exchanges ChromosomePersonality and ChromosomeHappenings
	 * Is equal to onePointCrossover with a fixed crossover Point
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void simpleCrossover(Quantum recipient, Quantum donor, int state) {
		
		QuantumPosition one = new_quantumPosition(recipient,state,recipient.get_personality(state),donor.best_happenings());
		QuantumPosition two = new_quantumPosition(recipient,state,donor.best_personality(),recipient.get_happenings(state));
		
		if(one.get_tellability() > two.get_tellability())
			recipient.add_Position(one, state);
		else
			recipient.add_Position(two, state);
			
	}
	
	
	
	/*
	 * Exchanges Allele with probability crossover_prob
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	
	public void binomialCrossover(Quantum recipient, Quantum donor, int state) {
		
		ChromosomePersonality personalityOne = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(number_agents,number_happenings);
		
		boolean change = false;
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(Math.random()<crossover_prob) {
					personalityOne.values[i][j] = donor.get_position(state).get_personality(i,j);
					personalityTwo.values[i][j] = recipient.get_position(state).get_personality(i,j);
					change = true;
				}else {
					personalityOne.values[i][j] = recipient.get_position(state).get_personality(i,j);
					personalityTwo.values[i][j] = donor.get_position(state).get_personality(i,j);
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(Math.random()<crossover_prob) {
					happeningsOne.values[i][j] = donor.get_position(state).get_happenings(i,j);
					happeningsTwo.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					change = true;
				}else {
					happeningsOne.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					happeningsTwo.values[i][j] = donor.get_position(state).get_happenings(i,j);
				}
			}
		}
		
		if(change) {
			
			QuantumPosition one = new_quantumPosition(recipient,state,personalityOne,happeningsOne);
			QuantumPosition two = new_quantumPosition(recipient,state,personalityTwo,happeningsTwo);
			
			if(one.get_tellability() > two.get_tellability())
				recipient.add_Position(one, state);
			else
				recipient.add_Position(two, state);
			
		}else {
			binomialCrossover(recipient, donor, state);
		}
	}
	
	
	/*
	 * Exchanges allele between crossover points.
	 * Crossover points are generated by the function setCrossoverPoints()
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */
	
	public void xPointCrossover(Quantum recipient, Quantum donor, int state) {

		boolean[][] crossPersonality = new boolean[number_agents][5];
		setCrossoverPoints(crossPersonality);
		
		boolean[][] crossHappenings = new boolean[number_agents][number_happenings];
		setCrossoverPoints(crossHappenings);
		
		ChromosomePersonality personalityOne = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(number_agents,number_happenings);
		
		boolean change = false;
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(crossPersonality[i][j]) {
					personalityOne.values[i][j] = donor.get_personality(i,j);
					personalityTwo.values[i][j] = recipient.get_personality(i,j);
					change=true;
				}else {
					personalityOne.values[i][j] = recipient.get_personality(i,j);
					personalityTwo.values[i][j] = donor.get_personality(i,j);
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(crossHappenings[i][j]) {
					happeningsOne.values[i][j] = donor.get_happenings(i,j);
					happeningsTwo.values[i][j] = recipient.get_happenings(i,j);
					change=true;
				}else {
					happeningsOne.values[i][j] = recipient.get_happenings(i,j);
					happeningsTwo.values[i][j] = donor.get_happenings(i,j);
				}
			}
		}

		if(change) {

			QuantumPosition one = new_quantumPosition(recipient,state,personalityOne,happeningsOne);
			QuantumPosition two = new_quantumPosition(recipient,state,personalityTwo,happeningsTwo);
			
			if(one.get_tellability() > two.get_tellability())
				recipient.add_Position(one, state);
			else
				recipient.add_Position(two, state);
			
		}else {
			xPointCrossover(recipient, donor, state);
		}
	}
	
	
	/*
	 * Generate crossover points
	 * 
	 * @param x,y: dimensions of the array
	 * @return: Array containing truth values
	 */
	
	public void setCrossoverPoints(boolean[][] result){
		
		boolean cross = false;
		
		int x = result.length;
		int y = result[0].length;
		
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
							
					if(Math.random()<crossover_prob)
						cross = !cross;
						
					result[xCoord][yCoord] = cross;		
				}
			}
		}
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
	
	public void voteCrossover(List<Integer> positions, int state) {
		
		ChromosomePersonality personalityRandom = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityAverage = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsRandom = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsAverage = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {
				
				int sum = 0;
				int pos = positions.get((int)Math.round(Math.random()*positions.size()-0.5));
				
				for(int k = 0; k < positions.size(); k++) {
					
					sum += quantum_particles[k].get_position(state).get_personality(i,j);
				}
				
				personalityRandom.values[i][j] = quantum_particles[pos].get_position(state).get_personality(i,j);
				personalityAverage.values[i][j] = round(sum/positions.size());
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {
				
				int sum = 0;
				int pos = positions.get((int)Math.round(Math.random()*positions.size()-0.5));
				
				for(int k = 0; k < positions.size(); k++) {
					
					sum += quantum_particles[k].get_position(state).get_happenings(i,j);
				}
									
				happeningsRandom.values[i][j] = quantum_particles[pos].get_position(state).get_happenings(i,j);
				happeningsAverage.values[i][j] = (int)Math.round(sum/positions.size());
			}
		}
			
		QuantumPosition one = new_quantumPosition(quantum_particles[positions.get(0)],state,personalityRandom,happeningsRandom);
		QuantumPosition two = new_quantumPosition(quantum_particles[positions.get(0)],state,personalityAverage,happeningsAverage);
		
		if(one.get_tellability() > two.get_tellability())
			quantum_particles[positions.get(0)].add_Position(one, state);
		else
			quantum_particles[positions.get(0)].add_Position(two, state);
		
	}
	
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
		
		int mode = mutationList.get((int)Math.round(Math.random()*mutationList.size()-0.5));
		
		int amount = 1;
		
		if(mode == 3)
			amount++;
		
		List<Integer> positions = select(amount);
		
		int state = quantum_particles[positions.get(0)].choosePosition();
		
		switch(mode) {

		case(0):
			
			randomMutator(quantum_particles[positions.get(0)],state);
			break;
		
		case(1):
			
			toggleMutator(quantum_particles[positions.get(0)],state);
			break;
			
		case(2):
			
			orientedMutator(quantum_particles[positions.get(0)],state);
			break;
			
		case(3):
			
			guidedMutator(quantum_particles[positions.get(0)],quantum_particles[positions.get(1)],state);
			break;
			
		default:
			
			System.out.println("Fatal Error @ Mutation Operator Selection!");
			break;
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
	
	public void randomMutator(Quantum recipient, int state) {
		
		boolean change = false;
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		boolean[][] persChange = new boolean[number_agents][5];
		boolean[][] hapsChange = new boolean[number_agents][number_happenings];
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {
				
				if(Math.random()<mutation_prob) {
					
					mutatedPersonality.values[i][j] = round(Math.random()*2-1);
					persChange[i][j] = true;
					change = true;
					
				}else {
					mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				}
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {

				if(Math.random()<mutation_prob) {
					
					mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*recipient.get_simLength()-0.5);
					hapsChange[i][j] = true;
					change = true;
					
				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}	
		
		if(change)
			add_quantumPosition(recipient, state, mutatedPersonality, mutatedHappenings);
		else 
			randomMutator(recipient,state);
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
	
	public void toggleMutator(Quantum recipient, int state) {
		
		boolean change = false;
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		boolean[][] persChange = new boolean[number_agents][5];
		boolean[][] hapsChange = new boolean[number_agents][number_happenings];
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				if(Math.random()<mutation_prob) {

					mutatedPersonality.values[i][j] *= -1;
					persChange[i][j] = true;
					change = true;
				}
			}
			
			// Happenings
			for(int j = 0; j < number_happenings; j++) {

				if(Math.random()<mutation_prob) {
					
					if(recipient.get_happenings(i,j) > 0)
						mutatedHappenings.values[i][j] = 0;
					else
						mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*(recipient.get_simLength()-1)+0.5);

					hapsChange[i][j] = true;
					change = true;
					
				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}	
		
		if(change) 
			add_quantumPosition(recipient, state, mutatedPersonality, mutatedHappenings);
		else
			toggleMutator(recipient,state);
	}
	
	
	/*
	 * Oriented Mutation
	 * 
	 * Mutate value towards or away from another internal value in the same chromosome but at a different position
	 * 
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */
	
	public void orientedMutator(Quantum recipient, int state) {
		
		boolean change = false;
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		boolean[][] persChange = new boolean[number_agents][5];
		boolean[][] hapsChange = new boolean[number_agents][number_happenings];
		
		for(int i = 0; i < number_agents; i++) {
			
			// Personality
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				
				if(Math.random()<mutation_prob) {
					
					persChange[i][j] = true;
					change = true;
					
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
					
					hapsChange[i][j] = true;
					change = true;

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
		
		if(change)
			add_quantumPosition(recipient, state, mutatedPersonality, mutatedHappenings);
		else
			orientedMutator(recipient,state);
	}
	
	/*
	 * Guided Mutation
	 * 
	 * Mutate a value towards or away from the corresponding value of another candidate with probability mutation_prob
	 * 
	 * @param recipient: Candidate to be mutated
	 * @return: mutated Candidate
	 */
	
	public void guidedMutator(Quantum recipient, Quantum mutator, int state) {
		
		boolean change = false;
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		boolean[][] persChange = new boolean[number_agents][5];
		boolean[][] hapsChange = new boolean[number_agents][number_happenings];
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality().values[i][j];
				
				if(Math.random()<mutation_prob) {

					persChange[i][j] = true;
					change = true;
					
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

					hapsChange[i][j] = true;
					change = true;
					
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
		
		if(change)
			add_quantumPosition(recipient, state, mutatedPersonality, mutatedHappenings);
		else 
			 guidedMutator(recipient, mutator, state);
	}

	
	
	public void move_particles() {
		
		for(int i = 0; i < individual_count; i++) {
			
			for(int state = 0; state < quantum_particles[i].amount_positions(); state++)
			
			quantum_particles[i].move(state,new Fitness<EnvType,ModType>(EVO_ENV,verbose,level));
			
		}
		
		Arrays.sort(quantum_particles);
		
		if(floatingParameters)
			update_Distances();
	}

	
	public void update_movement() {

		for(int index = 0; index < individual_count; index++) {
			
			List<Integer> informants = select_particles(index);
				
			if(floatingParameters)
				floating_Updater(index, informants);
			else
				static_Updater(index, informants);
		}
	}
	
	
	public List<Integer> select_particles(int individual) {
		
		if(fitnessBasedSelection)
			return rouletteWheel_selector(individual);
		
		return best_selector(individual);
	}
	
	
	public List<Integer> best_selector(int individual) {
		
		List<Integer> selected_particles = new ArrayList<Integer>();
		
		// Every particle is an informant to himself
		selected_particles.add(individual);
		
		// increment in order to avoid adding self to the selection
		int increment = 0;
		
		for(int i = 0; i < number_informants; i++) {
			
			if(i == individual)
				increment = 1;
			
			selected_particles.add(i + increment);
		}
		
		return selected_particles;
	}
	
	
	public List<Integer> rouletteWheel_selector(int individual) {
		
		// Construct roulette wheel
		double total_fitness = 0;
		double[] rouletteWheel = new double[individual_count];
		
		// Control parameters
		boolean control = true;
		int validParticles = 0;
		
		for(int i = 0; i < individual_count; i++) {
			total_fitness += quantum_particles[i].best_tellability();
			rouletteWheel[i] = total_fitness;
			
			// Check if we have enough individuals with fitness
			if(control && quantum_particles[i].best_tellability()==0) {
				
				validParticles = i;
				control = false;
				
				// Check if current particle is one of the particles with fitness
				if(individual < i)
					validParticles-=1;
			}
		}
		
		// Pick Particles
		List<Integer> selected_particles = new ArrayList<Integer>();
		
		// Every particle is an informant to himself
		selected_particles.add(individual);
		
		// If there are more than n particles with fitness (n=number_informants)
		if(validParticles > number_informants) {
				
			// Select other Informants
			while(selected_particles.size() < number_informants) {
				
				int position = 0;
				double value = Math.random()*total_fitness;
				
				while(value > rouletteWheel[position] && position < rouletteWheel.length) {
					position++;
				}
				if(!selected_particles.contains(position)) {
					selected_particles.add(position);
				}	
			}

		// If we don't have enough particles with fitness, selection with the first n individuals except self
		}else {

			int buffer = 0;
			
			for(int i = 0; i < number_informants; i++) {
				
				// exclude self
				if(i == individual)
					buffer +=1;
				
				selected_particles.add(i+buffer);
			}
		}
		return selected_particles;
	}
	
	
	public void static_Updater(int recipient, List<Integer> informants) {
		
		double[][][] update_personality = new double[quantum_particles[recipient].amount_positions()][number_agents][5];
		double[][][] update_happenings = new double[quantum_particles[recipient].amount_positions()][number_agents][number_happenings];
		
		for(int index = 0; index < informants.size(); index++) {
			
			for(int state = 0; state < quantum_particles[recipient].amount_positions(); state++) {
			
				double force = decay_rate;
				
				// determine if force is pulling towards or pushing away
				if(quantum_particles[informants.get(index)].best_tellability() <= quantum_particles[recipient].get_tellability(state))
					force*=-1;
				
				// copy information
				for(int i = 0; i < number_agents; i++) {
					
					for(int j = 0; j < 5; j++) {
						
						update_personality[state][i][j] += force*(quantum_particles[informants.get(index)].best_personality(i, j) - quantum_particles[recipient].get_personality(i, j));
					}
					
					for(int j = 0; j < number_happenings; j++) {
						
						update_happenings[state][i][j] += force*(quantum_particles[informants.get(index)].best_happenings(i, j) - quantum_particles[recipient].get_happenings(i, j));
					}
				}
			}
		}

		for(int state = 0; state < quantum_particles[recipient].amount_positions(); state++) {
		
			// Average out the velocity influence and update the chromosomes of current particle 
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					quantum_particles[recipient].get_position(state).update_persVelocity(i, j, update_personality[state][i][j] / informants.size(),decay_rate);
				}
				
				for(int j = 0; j < number_happenings; j++) {
	
					quantum_particles[recipient].get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[state][i][j] / informants.size()),decay_rate);
				}
			}
		}
	}
	
	
	public void floating_Updater(int recipient, List<Integer> informants) {
		
		double[][][] update_personality = new double[quantum_particles[recipient].amount_positions()][number_agents][5];
		double[][][] update_happenings = new double[quantum_particles[recipient].amount_positions()][number_agents][number_happenings];
		
		for(int index = 0; index < informants.size(); index++) {
			
			for(int state = 0; state < quantum_particles[recipient].amount_positions(); state++) {
			
				// determine strength of interaction
				double fitnessRating = fitness_rating(recipient, state, informants.get(index));
				double distanceRating = distance_rating(recipient, informants.get(index));
				double force = Math.sqrt(Math.abs(fitnessRating*Math.pow(distanceRating,2)));
				
				// determine if force is pulling towards or pushing away
				if(fitnessRating < 0)
					force*=-1;
				
				// copy information
				for(int i = 0; i < number_agents; i++) {
				
				
					for(int j = 0; j < 5; j++) {
						
						update_personality[state][i][j] += force*(quantum_particles[informants.get(index)].best_personality(i, j) - quantum_particles[recipient].get_personality(i, j));
					}
					
					for(int j = 0; j < number_happenings; j++) {
						
						update_happenings[state][i][j] += force*(quantum_particles[informants.get(index)].best_happenings(i, j) - quantum_particles[recipient].get_happenings(i, j));
					}
				}
			}
		}
		
		// Average out the velocity influence and update the chromosomes of current particle 

		for(int state = 0; state < quantum_particles[recipient].amount_positions(); state++) {
			
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					quantum_particles[recipient].get_position(state).update_persVelocity(i, j, update_personality[state][i][j] / informants.size());
				}
				
				for(int j = 0; j < number_happenings; j++) {
	
					quantum_particles[recipient].get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[state][i][j] / informants.size()));
				}
			}
		}
	}

}
