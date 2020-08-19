package inBloom.evo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class PSO <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends EvolutionaryAlgorithm<EnvType,ModType> {

	// Particle container
	private Particle[] particles;
	private double[][] max_happenings;
	private double[][] min_happenings;
	private double[][] max_personality;
	private double[][] min_personality;
	
	
	// Discrete values to choose from for personality initialization
	private static double[] discretePersValues = {-1,-0.9,-0.75,-0.5,-0.25,-0.1,0,0.1,0.25,0.5,0.75,0.9,1};
	private static double[] discretePersVelocity = {-0.1,-0.05,0,0.05,0.1};
	private static int[] discreteHapVelocity = {-2,-1,0,1,2};

	
	// Boolean arrays are used to manage genetic operators. True enables usage.

	// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
	private static boolean[] persInitBool = {true,true,true};
	// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
	private static boolean[] hapInitBool = {true,true,true}; 
	// randomVelocityInit, discreteVelocityInit
	// false, false -> no velocity initialization. Works as long as particles have different positions
	private static boolean[] velInitBool = {true,true};
	
	// This is used for combining the genetic algorithm with pso.
	// Data field for the gen_pool of the genetic algorithm.
	private Candidate[] gen_pool;
	// Number of Individuals from the gen_pool.
	private static int geneticInit = 0;
	
	
	// determine manner of updating velocity
	// number of informants for a particle
	private static int number_informants = 1; 
	// true -> Roulette Wheel Selection, false -> choose best
	private static boolean fitnessBasedSelection = false;
	// false -> use static update rate, false -> update with force calculation
	private static boolean floatingParameters = false;
	// if floatingParameters == false: use this as update rate
	private static double update_rate = 0.1;
	// determine if particles shall move according to spacetime
	private static boolean spacetime = false;
	// exploration is used in the spacetime formula 
	private static double exploration = 1;
	
	
	public PSO(String[] args, EvolutionaryEnvironment <?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		
		super(args,EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		max_happenings = new double[number_agents][number_happenings];
		
	}

	
	/**
	 * Get & Set Methods
	 */
	
	// Get all particles
	public Particle[] getParticles() {
		
		return particles;
	}
	
	// Get specified particle particles
	public Particle getParticle(int position) {
		
		return particles[position];
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
	
	public int getGenInit() {
		
		return geneticInit;
	}
	
	public void setGenInit(int count, Candidate[] gen_pool) {
		
		if(count>0) {
			
			while(count > gen_pool.length) {
				count /= 2;
				System.out.println("Count was fixed to: " + count + " !");
			}
			
			geneticInit = count;
			this.gen_pool = gen_pool;
			
		}else {
			System.out.println("count must be > 0!");
		}
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
	
	public double getUpdateRate() {
		return update_rate;
	}
	
	public void setUpdateRate(double rate) {
		update_rate = rate;
	}

	public boolean getSpacetime() {
		return spacetime;
	}
	
	public void setSpacetime(boolean time) {
		spacetime = time;
	}
	
	
	/**
	 * Difference Measurements
	 */
	
	
	/*
	 * Rates the difference between two particles in terms of fitness.
	 * The difference is based on pythagorean distance.
	 * 
	 * @param recipient: Particle that receives velocity update
	 * @param informant: Particle providing information to recipient
	 * @return: Double between [0,1] reflecting the difference rating
	 */
	
	public double fitness_rating(int recipient, int informant) {
		
		// If both particles have a best tellability of 0 they shall get a high rating
		// Therefore they will get propelled away from another and ensure exploration.
		if(particles[informant].best_tellability()==0 && particles[recipient].best_tellability()==0)
			return -1;
		
		return Math.sqrt(Math.pow(particles[informant].best_tellability(), 2) - Math.pow(particles[recipient].get_tellability(), 2));
		
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
					distance += Math.pow(1-(Math.abs(particles[informant].best_personality(i, j) - particles[recipient].get_personality(i, j))/(max_personality[i][j]-min_personality[i][j])),2)/n;
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(max_happenings[i][j]-min_happenings[i][j]!=0)
					distance += Math.pow(1-(Math.abs(particles[informant].best_happenings(i, j) - particles[recipient].get_happenings(i, j))/(max_happenings[i][j]-min_happenings[i][j])),2)/n;
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
					
					if(particles[index].get_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = particles[index].get_personality(agents, personality);
					
					if(particles[index].best_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = particles[index].best_personality(agents, personality);
					
					if(particles[index].get_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = particles[index].get_personality(agents, personality);
					
					if(particles[index].best_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = particles[index].best_personality(agents, personality);
				}
				
				for(int happenings = 0; happenings < number_happenings; happenings++) {

					if(particles[index].get_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = particles[index].get_happenings(agents, happenings);

					if(particles[index].best_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = particles[index].best_happenings(agents, happenings);
					
					if(particles[index].get_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = particles[index].get_happenings(agents, happenings);
					
					if(particles[index].best_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = particles[index].best_happenings(agents, happenings);
				}
			}
		}
	}
	
	
	/*
	 * Determines how fast a particles moves through space and updates its movement.
	 * 
	 * @param index: Current particle id
	 * @return: Double in the interval [0,1]
	 */
	
	public double determine_spacetime(int index) {
		
		if(spacetime)
			return exploration*(1-Math.pow(particles[index].get_tellability(), 2)) + (1-exploration)*(Math.pow(particles[0].best_tellability(), 2)-Math.pow(particles[index].get_tellability(), 2));
		
		return 1;
	}
	
	
	/*
	 * Checks whether parameters are correctly set.
	 * Corrects parameters if possible.
	 * 
	 * @return: False if parameters were put wrong, True otherwise
	 */
	
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
	

	/*
	 * Saves information about particle quality.
	 * The average fitness of the particle as well as best particle's fitness is saved.
	 * If there was no improvement compared to the last iteration, termination criterion counter gets incremented.
	 * If there was an improvement, termination criterion counter is reset.
	 */
	
	protected void evaluate_population() {
		
		double best = particles[0].best_tellability();
		double average = 0;

		for(int i = 0; i < individual_count; i++) {
			
			average += particles[i].best_tellability();
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
		
		if(spacetime) {
			exploration = 1-(no_improvement/termination);
			if(max_runtime>0) {
				exploration *= System.currentTimeMillis()/(start_time+max_runtime);
				exploration = Math.sqrt(exploration);
			}
		}
	}
	
	
	public void run() {
		
		if(check_parameters()) {
			// Save current time
			start_time = System.currentTimeMillis();
			
			// Generate and evaluate initial particles
			initialize_particles();
			evaluate_population();
			
			// Repeat until termination (no improvements found or time criterion -if set- is met):
			while(no_improvement<termination && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0)) {
				
				// Verbose
				int generation = population_best.size()-1;
				
				System.out.println();
				System.out.println("Iteration: " + generation);
				System.out.println();
				System.out.println("Best Particle: " + population_best.get(generation));
				System.out.println("Iteration Average: " + population_average.get(generation));
				System.out.println();
				
				if(no_improvement>0) {
					System.out.println("No improvement found for " + no_improvement + " generations!");
					System.out.println();
				}
	
				move_particles();
				update_movement();
				evaluate_population();
				
			}
			
			Arrays.sort(particles);
			
			System.out.println();
			System.out.println("This is the End!");
			System.out.println();
			System.out.println("Iterations: " + population_best.size());
			System.out.println("Best particle found: " + particles[0].best_tellability() + " , with simulation length: " + particles[0].best_simLength());
			
			to_file(particles[0]);
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
	
	public Particle new_particle(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap) {
		
		return new_particle(pers,velocity_pers,hap,velocity_hap,determineLength(hap));
	}
	
	public Particle new_particle(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap, Integer steps) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV);
		
		return new Particle(pers, velocity_pers, hap, velocity_hap, steps, fit);
	}


	private void initialize_particles() {

		particles = new Particle[individual_count];
		
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
			
			// Initialize particles based on findings of the genetic algorithm
			if(index < geneticInit) {
				
				particles[index] = new Particle(gen_pool[index],pers_velocity,hap_velocity);
				
			// Otherwise use classic initialization
			}else {
				
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
				
				particles[index] = new_particle(personality,pers_velocity,happenings,hap_velocity,max_steps);
			}
		}
		
		Arrays.sort(particles);
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
				
				pers_velocity.values[i][j] = round(Math.random()*2-1);
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
	
	
	public void move_particles() {
		
		for(int i = 0; i < individual_count; i++) {
			
			particles[i].move();
			particles[i].update_tellability(new Fitness<EnvType,ModType>(EVO_ENV));
			
			if(spacetime) {
				particles[i].set_spacetime(determine_spacetime(i));
			}
		}
		
		Arrays.sort(particles);
		update_Distances();
	}

	
	public void update_movement() {

		for(int index = 0; index < individual_count; index++) {
			
			if(spacetime)
				particles[index].set_spacetime(determine_spacetime(index));
			
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
			total_fitness += particles[i].get_tellability();
			rouletteWheel[i] = total_fitness;
			
			// Check if we have enough individuals with fitness
			if(control && particles[i].get_tellability()==0) {
				
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
				
				while(value > rouletteWheel[position]) {
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
		
		double[][] update_personality = new double[number_agents][5];
		double[][] update_happenings = new double[number_agents][number_happenings];
		
		for(int index = 0; index < informants.size(); index++) {

			double force = update_rate;
			
			// determine if force is pulling towards or pushing away
			if(particles[informants.get(index)].best_tellability() <= particles[recipient].get_tellability())
				force*=-1;
			
			// copy information
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					update_personality[i][j] += force*(particles[informants.get(index)].best_personality(i, j) - particles[recipient].get_personality(i, j));
				}
				
				for(int j = 0; j < number_happenings; j++) {
					
					update_happenings[i][j] += force*(particles[informants.get(index)].best_happenings(i, j) - particles[recipient].get_happenings(i, j));
				}
			}
		}
		
		// Average out the velocity influence and update the chromosomes of current particle 
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				particles[recipient].update_persVelocity(i, j, update_personality[i][j] / informants.size(),update_rate);
			}
			
			for(int j = 0; j < number_happenings; j++) {

				particles[recipient].update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j] / informants.size()),update_rate);
			}
		}
	}
	
	
	public void floating_Updater(int recipient, List<Integer> informants) {
		
		double[][] update_personality = new double[number_agents][5];
		double[][] update_happenings = new double[number_agents][number_happenings];
		
		for(int index = 0; index < informants.size(); index++) {
			
			// determine strength of interaction
			double fitnessRating = fitness_rating(recipient, informants.get(index));
			double distanceRating = distance_rating(recipient, informants.get(index));
			double force = Math.sqrt(Math.abs(fitnessRating*distanceRating));
			
			// determine if force is pulling towards or pushing away
			if(fitnessRating < 0)
				force*=-1;
			
			// copy information
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					update_personality[i][j] += force*(particles[informants.get(index)].best_personality(i, j) - particles[recipient].get_personality(i, j));
				}
				
				for(int j = 0; j < number_happenings; j++) {
					
					update_happenings[i][j] += force*(particles[informants.get(index)].best_happenings(i, j) - particles[recipient].get_happenings(i, j));
				}
			}
		}
		
		// Average out the velocity influence and update the chromosomes of current particle 
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				particles[recipient].update_persVelocity(i, j, update_personality[i][j] / informants.size());
			}
			
			for(int j = 0; j < number_happenings; j++) {

				particles[recipient].update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j] / informants.size()));
			}
		}
	}
}