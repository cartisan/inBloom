package inBloom.pso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class PSO <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {

	// Parameter for PlotLauncher
	public String[] args;
	public ParticleEnvironment<?,?> GEN_ENV;
	
	// Standard parameters for a genetic algorithm
	public int number_agents;
	public int number_happenings;
	public int max_steps;
	public int particle_count;
	
	// Particle container
	public Particle[] particles;
	private double[][] max_happenings;
	private double[][] min_happenings;
	private double[][] max_personality;
	private double[][] min_personality;
	
	// Performance measurement
	private static List<Double> particles_best = new ArrayList<Double>();
	private static List<Double> particles_average = new ArrayList<Double>();
	
	// Termination criteria
	private static int no_improvement=0;
	private static int termination=50;
	private static long start_time;
	private static long max_runtime=-1;
	
	// Discrete values to choose from for personality initialization
	private static double[] discretePersValues = {-1,-0.9,-0.75,-0.5,-0.25,-0.1,0,0.1,0.25,0.5,0.75,0.9,1};
	private static double[] discretePersVelocity = {-0.1,-0.05,0,0.05,0.1};
	private static int[] discreteHapVelocity = {-2,-1,0,1,2};

	// Boolean arrays are used to manage genetic operators. True enables usage.

	// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
	private static boolean[] persInitBool = {true,true,true};
	// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
	private static boolean[] hapInitBool = {true,true,true}; 
	// determine manner of velocity initialization
	private static boolean[] velInitBool = {true,true};
	
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
	
	public PSO(String[] args, ParticleEnvironment<?,?> GEN_ENV, int number_agents, int number_happenings, int max_steps, int particle_count) {
		
		this.args = args;
		this.GEN_ENV = GEN_ENV;
		this.number_agents = number_agents;
		this.max_steps = max_steps;
		this.number_happenings = number_happenings;
		this.particle_count = particle_count;
		max_happenings = new double[number_agents][number_happenings];
		
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
	 * an amount of additional steps between 0 and step number * ratio
	 * @param happenings: Chromosome encoding steps at which happenings occur
	 * @param ratio: maximum amount of extra steps after simulation ends
	 * @return: total amount of simulation steps
	 */
	
	public Integer determineLength(ChromosomeHappenings happenings) {
		return determineLength(happenings, 0.2);
	}

	public Integer determineLength(ChromosomeHappenings happenings, double ratio) {
		
		Integer length = 0;
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				if(happenings.values[i][j] >= length) {
					length=happenings.values[i][j];
				}
			}
		}
		// Determine extra length
		Integer buffer = (int)Math.round(Math.random()*ratio*length);
		
		// Let the simulation run for at least 1 more step than the last happening 
		return length+buffer+1;
	}
	
	
	public double fitness_rating(int recipient, int informant) {
		
		return Math.sqrt(Math.pow(particles[informant].best_tellability(), 2) - Math.pow(particles[recipient].get_tellability(), 2));
		
	}
	
	
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
	
	
	public void update_Distances() {

		// Reset memory (?)
		min_personality = max_personality;
		max_personality = new double[number_agents][5];
		
		min_happenings = max_happenings;
		max_happenings = new double[number_agents][number_happenings];
		
		// Find maximum values
		for(int index = 0; index < particle_count; index++) {
			for(int agents = 0; agents < number_agents; agents++) {
				
				for(int personality = 0; personality < 5; personality++) {
					


					if(particles[index].get_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = particles[index].get_personality(agents, personality);
					
					if(particles[index].get_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = particles[index].get_personality(agents, personality);
				}
				
				for(int happenings = 0; happenings < number_happenings; happenings++) {

					if(particles[index].get_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = particles[index].get_happenings(agents, happenings);
					
					if(particles[index].get_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = particles[index].get_happenings(agents, happenings);
				}
			}
		}
	}
	
	
	public double determine_spacetime(int index) {
		
		if(spacetime)
			return exploration*(1-Math.pow(particles[index].get_tellability(), 2)) + (1-exploration)*(Math.pow(particles[0].best_tellability(), 2)-Math.pow(particles[index].get_tellability(), 2));
		
		return 1;
	}
	
	
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
		while(number_informants>=particle_count) {
			number_informants/=2;
			System.out.println("number_informants reduced to: " + number_informants);
		}
			
		
		return true;
	}
	
	
	private void evaluate_particles() {
		
		Arrays.sort(particles);
		
		double average = 0;
		double best = 0;

		for(int i = 0; i < particle_count; i++) {
			
			average += particles[i].best_tellability();
			
			if(particles[i].best_tellability()>best)
				best = particles[i].best_tellability();
		}
		
		// Determine if there was improvement
		if(particles_best.size()>0) {
			if(particles_best.get(particles_best.size()-1)==best && particles_average.get(particles_average.size()-1)==average)
				no_improvement++;
			else
				no_improvement=0;
		}
		particles_best.add(best);
		particles_average.add(average);
	}
	
	
	public void run() {
		
		if(check_parameters()) {
			// Save current time
			start_time = System.currentTimeMillis();
			
			// Generate and evaluate initial particles
			initialize_particles();
			evaluate_particles();
			
			// Repeat until termination (no improvements found or time criterion -if set- is met):
			while(no_improvement<termination && (max_runtime<0 || start_time+max_runtime-System.currentTimeMillis()>0)) {
				
				// Verbose
				int generation = particles_best.size()-1;
				
				System.out.println();
				System.out.println("Iteration: " + generation);
				System.out.println();
				System.out.println("Best Particle: " + particles_best.get(generation));
				System.out.println("Iteration Average: " + particles_average.get(generation));
				System.out.println();
				
				if(no_improvement>0) {
					System.out.println("No improvement found for " + no_improvement + " generations!");
					System.out.println();
				}
	
				move_particles();
				update_movement();
				evaluate_particles();
				
			}
			
			System.out.println();
			System.out.println("This is the End!");
			System.out.println();
			System.out.println("Iterations: " + particles_best.size());
			System.out.println("Best so far: " + particles[0].get_tellability() + " , with simulation length: " + particles[0].get_simLength());
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
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(GEN_ENV);
		
		System.out.println("Starting new Simulation: " + steps);
		return new Particle(pers, velocity_pers, hap, velocity_hap, steps, fit);
	}


	private void initialize_particles() {

		particles = new Particle[particle_count];
		
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
		
		// Set used initializers for happenings
		List<Integer> velInitializer = new ArrayList<Integer>();
		
		for(int i = 0; i < velInitBool.length; i++) {
			if(velInitBool[i])
				velInitializer.add(i);
		}
		
		// Initialize population
		while(index<particle_count) {
			
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

			ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
			ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);
			
			if(!velInitializer.isEmpty()) {
				
				int velType = velInitializer.get((int)Math.round(Math.random()*velInitializer.size()-0.5));
				
				switch(velType) {
				
				case(0):	

					pers_velocity = randomPersonalityVelocityInitializer(personality);
					hap_velocity = randomHappeningsVelocityInitializer(happenings);
					break;

				case(1):
					
					pers_velocity = discretePersonalityVelocityInitializer(personality);
					hap_velocity = discreteHappeningsVelocityInitializer(happenings);
					break;
					
				default:
					
					System.out.println("Fatal Error @ Velocity Initialization Selection!");
					break;
				}
			}
			
			
			particles[index] = new_particle(personality,pers_velocity,happenings,hap_velocity,max_steps);
				
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

	
	public ChromosomePersonality randomPersonalityVelocityInitializer(ChromosomePersonality personality) {
		
		ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				
				pers_velocity.values[i][j] = round((Math.random()*2-1)*personality.values[i][j]);
			}
		}
		return pers_velocity;
	}
	
	
	public ChromosomeHappenings randomHappeningsVelocityInitializer(ChromosomeHappenings happenings) {
		
		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				
				hap_velocity.values[i][j] = (int)Math.round((Math.random()*2-1)*(number_happenings/max_steps)*hap_velocity.values[i][j]);
			}
		}
		return hap_velocity;	
	}
	
	
	public ChromosomePersonality discretePersonalityVelocityInitializer(ChromosomePersonality personality) {
		
		ChromosomePersonality pers_velocity = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				
				pers_velocity.values[i][j] = discretePersVelocity[(int)Math.round(Math.random()*discretePersVelocity.length-0.5)];
			}
		}
		return pers_velocity;
		
	}
	
	
	public ChromosomeHappenings discreteHappeningsVelocityInitializer(ChromosomeHappenings happenings) {
		
		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				hap_velocity.values[i][j] = discreteHapVelocity[(int)Math.round(Math.random()*discreteHapVelocity.length-0.5)];
			}
		}
		return hap_velocity;
	}
	
	
	public void move_particles() {
		
		for(int i = 0; i < particle_count; i++) {

			Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(GEN_ENV);
			
			particles[i].move();
			particles[i].update_tellability(fit);
		}
		
		Arrays.sort(particles);
		update_Distances();
	}

	
	public void update_movement() {

		for(int index = 0; index < particle_count; index++) {
			
			List<Integer> informants = select_particles(index);
				
			if(floatingParameters)
				floating_Updater(index, informants);
			else
				static_Updater(index, informants);
		}
	}
	
	
	public List<Integer> select_particles(int individuum) {
		
		if(fitnessBasedSelection)
			return rouletteWheel_selector(individuum);
		
		return best_selector(individuum);
	}
	
	
	public List<Integer> best_selector(int individuum) {
		
		List<Integer> selected_particles = new ArrayList<Integer>();
		
		// Every particle is an informant to himself
		selected_particles.add(individuum);
		
		// increment in order to avoid adding self to the selection
		int increment = 0;
		
		for(int i = 0; i < number_informants; i++) {
			
			if(i == individuum)
				increment = 1;
			
			selected_particles.add(i + increment);
		}
		
		return selected_particles;
	}
	
	
	public List<Integer> rouletteWheel_selector(int individuum) {
		
		// Construct roulette wheel
		double total_fitness = 0;
		double[] rouletteWheel = new double[particle_count];
		
		for(int i = 0; i < particle_count; i++) {
			total_fitness += particles[i].get_tellability();
			rouletteWheel[i] = total_fitness;
		}
		
		// Pick Particles
		List<Integer> selected_particles = new ArrayList<Integer>();
		
		// Every particle is an informant to himself
		selected_particles.add(individuum);
		
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
		
		return selected_particles;
	}
	
	
	public void static_Updater(int recipient, List<Integer> informants) {
		
		double[][] update_personality = new double[number_agents][5];
		double[][] update_happenings = new double[number_agents][number_happenings];
		
		for(int index = 0; index < informants.size(); index++) {

			double force = update_rate;
			
			// determine if force is pulling towards or pushing away
			if(particles[informants.get(index)].best_tellability() < particles[recipient].get_tellability())
				force*=-1;
			
			// copy information
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					update_personality[i][j] += force*particles[informants.get(index)].best_personality(i, j) - particles[recipient].get_personality(i, j);
				}
				
				for(int j = 0; j < number_happenings; j++) {
					
					update_happenings[i][j] += force*particles[informants.get(index)].best_happenings(i, j) - particles[recipient].get_happenings(i, j);
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
					
					update_personality[i][j] += force*particles[informants.get(index)].best_personality(i, j) - particles[recipient].get_personality(i, j);
				}
				
				for(int j = 0; j < number_happenings; j++) {
					
					update_happenings[i][j] += force*particles[informants.get(index)].best_happenings(i, j) - particles[recipient].get_happenings(i, j);
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
