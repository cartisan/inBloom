package inBloom.nia.pso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.NIAlgorithm;
import inBloom.nia.NIEnvironment;
import inBloom.nia.ga.Individual;

public class PSO <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> extends NIAlgorithm<EnvType,ModType> {
	private static final boolean USE_FLOATING_PARAM = true;
	
	// Particle container
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
	// false, false -> no velocity initialization. Works as long as population have different positions
	private static boolean[] velInitBool = {true,true};
	//
	private boolean deterministic = false;
	
	// This is used for combining the genetic algorithm with pso.
	// Data field for the gen_pool of the genetic algorithm.
	private Individual[] gen_pool;
	// Number of Individuals from the gen_pool.
	private static int geneticInit = 0;
	
	
	// determine manner of updating velocity
	// if floatingParameters == false: use this as update rate
	private double decay_rate = 0.1;
	// number of informants for a particle
	private int number_informants = 1; 
	// true -> Roulette Wheel Selection, false -> choose best
	private boolean fitnessBasedSelection = false;
	// false -> use static update rate, false -> update with force calculation
	private boolean floatingParameters = USE_FLOATING_PARAM;
	
	
	
	public PSO(String[] args, NIEnvironment <?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		
		super(args,EVO_ENV, number_agents, number_happenings, max_steps, individual_count);
		
	}

	
	/**
	 * Get & Set Methods
	 */
	
	// Get all population
	public Particle[] getParticles() {
		
		return (Particle[]) population;
	}
	
	// Get specified particle population
	public Particle getParticle(int position) {
		
		return (Particle) population[position];
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
	
	public void setGenInit(int count, Individual[] gen_pool) {
		
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
	
	public double getDecayRate() {
		return decay_rate;
	}
	
	public void setDecayRate(double rate) {
		decay_rate = rate;
	}
	
	public void setDeterministic(boolean manner) {
		deterministic = manner;
	}
	
	
	/**
	 * Difference Measurements
	 */
	
	
	/*
	 * Returns the normalized difference of two population tellability score
	 * 
	 * @param recipient: Particle that receives velocity update
	 * @param informant: Particle providing information to recipient
	 * @return: Double between [0,1] reflecting the difference rating
	 */
	
	public double fitness_rating(int recipient, int informant) {

		return (((Particle) population[informant]).best_tellability() - population[recipient].get_tellabilityValue())/((Particle) population[0]).best_tellability();
	}
	
	
	/*
	 * Regulates the update Rate of a particle based on it's tellability
	 */
	
	public double determine_spacetime(int recipient) {
		
//		return 1-(population[recipient].get_tellability()/population[0].best_tellability());
		return 1-Math.pow((population[recipient].get_tellabilityValue()/((Particle) population[0]).best_tellability()),2);
	}
	
	
	/*
	 * Rates the difference between two population in terms of distance.
	 * The difference is based on the normalized pythagorean distance.
	 * 
	 * @param recipient: Particle that receives velocity update
	 * @param informant: Particle providing information to recipient
	 * @return: Double between [0,1] reflecting the difference rating
	 */
	
	public double distance_rating(int recipient, int informant) {
		
		double distance = 0;
		int n = 0;
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				if(max_personality[i][j]-min_personality[i][j] != 0) {
					distance += Math.pow((((Particle) population[informant]).best_personality(i, j) - population[recipient].get_personality(i, j))/(max_personality[i][j]-min_personality[i][j]),2);
					n++;
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(max_happenings[i][j]-min_happenings[i][j] != 0) {
					distance += Math.pow((((Particle) population[informant]).best_happenings(i, j) - population[recipient].get_happenings(i, j))/(max_happenings[i][j]-min_happenings[i][j]),2);
					n++;
				}
			}
		}
		
		return 1-Math.sqrt(distance/n);
	}
	
	
	/*
	 * Determines the maximum and minimum values existent in the population chromosomes.
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
					
					if(population[index].get_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = population[index].get_personality(agents, personality);
					
					if(((Particle) population[index]).best_personality(agents, personality) < min_personality[agents][personality])
						min_personality[agents][personality] = ((Particle) population[index]).best_personality(agents, personality);
					
					if(population[index].get_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = population[index].get_personality(agents, personality);
					
					if(((Particle) population[index]).best_personality(agents, personality) > max_personality[agents][personality])
						max_personality[agents][personality] = ((Particle) population[index]).best_personality(agents, personality);
				}
				
				for(int happenings = 0; happenings < number_happenings; happenings++) {

					if(population[index].get_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = population[index].get_happenings(agents, happenings);

					if(((Particle) population[index]).best_happenings(agents, happenings) < min_happenings[agents][happenings])
						min_happenings[agents][happenings] = ((Particle) population[index]).best_happenings(agents, happenings);
					
					if(population[index].get_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = population[index].get_happenings(agents, happenings);
					
					if(((Particle) population[index]).best_happenings(agents, happenings) > max_happenings[agents][happenings])
						max_happenings[agents][happenings] = ((Particle) population[index]).best_happenings(agents, happenings);
				}
			}
		}
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
		
		double best = ((Particle) population[0]).best_tellability();
		double average = 0;

		for(int i = 0; i < individual_count; i++) {
			
			average += ((Particle) population[i]).best_tellability();
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
	public void run_iteration() {
		this.move_particles();
		this.update_movement();		
	}
	
	/*
	 * Instantiates new fitness object and hands it over to the Individual to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Individual
	 */
	
	public Particle new_particle(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap) {
		
		return new_particle(pers,velocity_pers,hap,velocity_hap,determineLength(hap));
	}
	
	public Particle new_particle(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap, Integer steps) {
		
		Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV,verbose,level);
		
		return new Particle(pers, velocity_pers, hap, velocity_hap, steps, fit);
	}

	@Override
	protected void initialize_population() {
		population = new Particle[individual_count];
		
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
			
			// Initialize population based on findings of the genetic algorithm
			if(index < geneticInit) {

				Fitness<EnvType,ModType> fit = new Fitness<EnvType,ModType>(EVO_ENV,verbose,level);
				population[index] = new Particle(gen_pool[index],pers_velocity,hap_velocity,fit);
				
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
				
				population[index] = new_particle(personality,pers_velocity,happenings,hap_velocity,max_steps);
			}
		}
		
		Arrays.sort(population);
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
	
	
	public void move_particles() {
		
		for(int i = 0; i < individual_count; i++) {
			
			((Particle) population[i]).move();
			((Particle) population[i]).update_tellability(new Fitness<EnvType,ModType>(EVO_ENV,verbose,level));
			
		}
		
		Arrays.sort(population);
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
			return gravity_selector(individual);
		
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
	
	
	public List<Integer> gravity_selector(int individual) {
		
		// Pick Particles
		List<Integer> selected_particles = new ArrayList<Integer>();
		
		for(int i = 0; i < individual_count; i++) {

			// A Particles qualifies as an informant if it's fitness is greater than the particle to be updated
			if(((Particle) population[i]).best_tellability()>=population[individual].get_tellabilityValue()) 
				selected_particles.add(i);
			
		}
		return selected_particles;
	}
	
	
	public void static_Updater(int recipient, List<Integer> informants) {
		
		double[][] update_personality = new double[number_agents][5];
		double[][] update_happenings = new double[number_agents][number_happenings];
		
		double random_factor = 1;
		
		for(int index = 0; index < informants.size(); index++) {

			double force = decay_rate;
			
			// determine if force is pulling towards or pushing away
			if(((Particle) population[informants.get(index)]).best_tellability() <= population[recipient].get_tellabilityValue())
				force*=-1;
			
			// copy information
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					if(!deterministic)
						random_factor = Math.random();
					
					update_personality[i][j] += random_factor * force*(((Particle) population[informants.get(index)]).best_personality(i, j) - population[recipient].get_personality(i, j));
				}
				
				for(int j = 0; j < number_happenings; j++) {
					
					if(!deterministic)
						random_factor = Math.random();
					
					update_happenings[i][j] += random_factor * force*(((Particle) population[informants.get(index)]).best_happenings(i, j) - population[recipient].get_happenings(i, j));
				}
			}
		}
		
		// Average out the velocity influence and update the chromosomes of current particle 
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(deterministic)
					((Particle) population[recipient]).update_persVelocity(i, j, update_personality[i][j] / informants.size(),decay_rate);
				else
					((Particle) population[recipient]).update_persVelocity(i, j, update_personality[i][j] / informants.size(),decay_rate/2);
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(deterministic)
					((Particle) population[recipient]).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j] / informants.size()),decay_rate);
				else
					((Particle) population[recipient]).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j] / informants.size()),decay_rate/2);
			}
		}
	}
	
	
	public void floating_Updater(int recipient, List<Integer> informants) {

		double[][] update_personality = new double[number_agents][5];
		double[][] update_happenings = new double[number_agents][number_happenings];
		
		double total_force=0;
	
		double random_factor = 1;
		
		for(int index = 0; index < informants.size(); index++) {
			
			// determine strength of interaction
			double energy = fitness_rating(recipient, informants.get(index));
			double inertia = determine_spacetime(recipient);
			double distance = distance_rating(recipient, informants.get(index));
			
			double force = energy*inertia*Math.pow(distance,2);
			
//			if(this.verbose) {
			if(false) {
				System.out.println("Informant: " + index);
				System.out.println("  Energy: " + energy);
				System.out.println("  Inertia: " + inertia);
				System.out.println("  Distance: " + distance);
				System.out.println("  Force: " + force);
			}
			
//			if(force < 0){
//				force *= determine_spacetime(index);
//			}
			
			total_force+=force;
			
			// copy information
			for(int i = 0; i < number_agents; i++) {
				
				for(int j = 0; j < 5; j++) {
					
					if(!deterministic)
						random_factor = Math.random();
					
					update_personality[i][j] += random_factor*force*(((Particle) population[informants.get(index)]).best_personality(i, j) - population[recipient].get_personality(i, j));
				}
				
				for(int j = 0; j < number_happenings; j++) {

					if(!deterministic)
						random_factor = Math.random();
					
					update_happenings[i][j] += random_factor*force*(((Particle) population[informants.get(index)]).best_happenings(i, j) - population[recipient].get_happenings(i, j));
				}
			}
		}
		
		// Average out the velocity influence and update the chromosomes of current particle 
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(deterministic)
					((Particle) population[recipient]).update_persVelocity(i, j, update_personality[i][j]/informants.size(),total_force/informants.size());
				else
					((Particle) population[recipient]).update_persVelocity(i, j, update_personality[i][j]/informants.size(),total_force/(informants.size()*2));
					
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				if(deterministic)
					((Particle) population[recipient]).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),total_force/informants.size());
				else
					((Particle) population[recipient]).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),total_force/(informants.size()*2));
			}
		}
	}
}
