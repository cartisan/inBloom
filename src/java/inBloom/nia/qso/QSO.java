package inBloom.nia.qso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.NIAlgorithm;
import inBloom.nia.NIEnvironment;

public class QSO <EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>>  extends NIAlgorithm<EnvType,ModType>{
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
	// if floatingParameters == false: use this as update rate
	private double decay_rate = 0.05;
	// number of informants for a particle
	private int number_informants = 1;
	// true -> Roulette Wheel Selection, false -> choose best
	private boolean fitnessBasedSelection = false;
	// false -> use static update rate, false -> update with force calculation
	private boolean floatingParameters = false;
	//
	private boolean deterministic = false;

	// counts the amount of neighbors that have been looked at
	private int analyzed_neighbors=0;
	private int found_best=0;


	public QSO(String[] args, NIEnvironment<?,?> EVO_ENV, int number_agents, int number_happenings, int max_steps, int individual_count) {
		super(args, EVO_ENV, number_agents, number_happenings, max_steps, individual_count);

		int parameter_count = (5+number_happenings)*number_agents;
		this.crossover_prob = Math.sqrt(parameter_count)/parameter_count;
		this.mutation_prob = Math.sqrt(parameter_count)/parameter_count;
	}

	/**
	 * Get & Set Methods
	 */

	// Get all particles
	public Quantum[] getParticles() {
		return (Quantum[]) this.population;
	}

	// Get specified particle particles
	public Quantum getParticle(int position) {

		return (Quantum) this.population[position];
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


	// Velocity initialization operators
	public boolean[] getVelInit() {

		return velInitBool;
	}

	public void setVelInit(int pos, boolean bool) {

		if(pos>=0 && pos < velInitBool.length) {
			velInitBool[pos] = bool;
		} else {
			System.out.println("Position is out of bounds");
		}
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

	// velocity update
	public int getVelocityInformants() {
		return this.number_informants;
	}

	public void setVelocityInformants(int informants) {
		this.number_informants = informants;
	}

	public boolean getSelectionManner() {
		return this.fitnessBasedSelection;
	}

	public void setSelectionManner(boolean manner) {
		this.fitnessBasedSelection = manner;
	}

	public boolean getFloatingParameters() {
		return this.floatingParameters;
	}

	public void setFloatingParameters(boolean floating) {
		this.floatingParameters = floating;
	}

	public double getDecayRate() {
		return this.decay_rate;
	}

	public void setDecayRate(double rate) {
		this.decay_rate = rate;
	}

	public void setDeterministic(boolean manner) {
		this.deterministic = manner;
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

		return (((Quantum) this.population[informant]).best_tellability() - ((Quantum) this.population[recipient]).get_tellability(state)) / ((Quantum) this.population[0]).best_tellability();
	}


	/*
	 * Regulates the update Rate of a particle based on it's tellability
	 */

	public double determine_spacetime(int recipient, int state) {

		return 1-((Quantum) this.population[recipient]).get_position(state).get_tellabilityValue() / ((Quantum) this.population[0]).best_tellability();
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
		int n = 0;

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(this.max_personality[i][j]-this.min_personality[i][j] != 0) {
					distance += Math.pow((((Quantum) this.population[informant]).best_personality(i, j) - this.population[recipient].get_personality(i, j))/(this.max_personality[i][j]-this.min_personality[i][j]),2);
					n++;
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(this.max_happenings[i][j]-this.min_happenings[i][j]!=0) {
					distance += Math.pow((((Quantum) this.population[informant]).best_happenings(i, j) - this.population[recipient].get_happenings(i, j))/(this.max_happenings[i][j]-this.min_happenings[i][j]),2);
					n++;
				}
			}
		}

		return 1-Math.sqrt(distance/n);
	}


	/*
	 * Determines the maximum and minimum values existent in the particles chromosomes.
	 */

	public void update_Distances() {

		// Reset memory
		this.min_personality = this.max_personality;
		this.max_personality = new double[this.number_agents][5];

		this.min_happenings = this.max_happenings;
		this.max_happenings = new double[this.number_agents][this.number_happenings];

		// Find maximum values
		for(int index = 0; index < this.individual_count; index++) {
			for(int agents = 0; agents < this.number_agents; agents++) {

				for(int personality = 0; personality < 5; personality++) {

					if(this.population[index].get_personality(agents, personality) < this.min_personality[agents][personality]) {
						this.min_personality[agents][personality] = this.population[index].get_personality(agents, personality);
					}

					if(((Quantum) this.population[index]).best_personality(agents, personality) < this.min_personality[agents][personality]) {
						this.min_personality[agents][personality] = ((Quantum) this.population[index]).best_personality(agents, personality);
					}

					if(this.population[index].get_personality(agents, personality) > this.max_personality[agents][personality]) {
						this.max_personality[agents][personality] = this.population[index].get_personality(agents, personality);
					}

					if(((Quantum) this.population[index]).best_personality(agents, personality) > this.max_personality[agents][personality]) {
						this.max_personality[agents][personality] = ((Quantum) this.population[index]).best_personality(agents, personality);
					}
				}

				for(int happenings = 0; happenings < this.number_happenings; happenings++) {

					if(this.population[index].get_happenings(agents, happenings) < this.min_happenings[agents][happenings]) {
						this.min_happenings[agents][happenings] = this.population[index].get_happenings(agents, happenings);
					}

					if(((Quantum) this.population[index]).best_happenings(agents, happenings) < this.min_happenings[agents][happenings]) {
						this.min_happenings[agents][happenings] = ((Quantum) this.population[index]).best_happenings(agents, happenings);
					}

					if(this.population[index].get_happenings(agents, happenings) > this.max_happenings[agents][happenings]) {
						this.max_happenings[agents][happenings] = this.population[index].get_happenings(agents, happenings);
					}

					if(((Quantum) this.population[index]).best_happenings(agents, happenings) > this.max_happenings[agents][happenings]) {
						this.max_happenings[agents][happenings] = ((Quantum) this.population[index]).best_happenings(agents, happenings);
					}
				}
			}
		}
	}


	@Override
	public boolean check_parameters() {

		if(this.number_agents <= 0 || this.number_happenings <= 0) {
			System.out.println("Bad Configuration!");
			return false;
		}

		// number_informants should not be negative
		if(this.number_informants < 0) {
			this.number_informants = 1;
			System.out.println("number_informants defaulted to: " + this.number_informants);
		}

		// number_informants must be smaller than pop_size
		while(this.number_informants>=this.individual_count) {
			this.number_informants/=2;
			System.out.println("number_informants reduced to: " + this.number_informants);
		}

		return true;
	}

	@Override
	protected void evaluate_population() {

		double best = ((Quantum) this.population[0]).best_tellability();
		double average = 0;

		for(int i = 0; i < this.individual_count; i++) {

			average += ((Quantum) this.population[i]).best_tellability();
		}

		average /= this.individual_count;

		// Determine if there was improvement
		if(this.population_best.size()>0) {
			if(best>this.population_best.get(this.population_best.size()-1)) {
				this.found_best = this.analyzed_neighbors;
			}

			if(this.population_best.get(this.population_best.size()-1)==best && this.population_average.get(this.population_average.size()-1)==average) {
				no_improvement++;
			}else {
				no_improvement=0;
			}
		}

		this.population_best.add(best);
		this.population_average.add(average);

	}

	@Override
	public void run_iteration() {
		this.crossover();
		this.mutate();
		this.move_particles();
		this.update_movement();
	}

	/*
	 * Instantiates new fitness object and hands it over to the Individual to be instantiated.
	 * @param pers: Chromosome containing personality information
	 * @param hap: Chromosome containing happening information
	 * @return: Instantiated Individual
	 */
	public Quantum new_quantum(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap) {

		return this.new_quantum(pers,velocity_pers,hap,velocity_hap,this.determineLength(hap));
	}

	public Quantum new_quantum(ChromosomePersonality pers,ChromosomePersonality velocity_pers,ChromosomeHappenings hap, ChromosomeHappenings velocity_hap, Integer steps) {

		Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV,this.verbose,this.level);

		return new Quantum(this.individual_count, pers, velocity_pers, hap, velocity_hap, steps, fit);
	}

	public QuantumPosition new_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap) {

		return this.new_quantumPosition(quant,state,pers,hap,this.determineLength(hap));
	}

	public QuantumPosition new_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap, Integer steps) {

		Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV,this.verbose,this.level);

		return new QuantumPosition(pers, quant.get_position(state).get_persVelocity(), hap,quant.get_position(state).get_hapVelocity(), steps, 0, fit);
	}

	public void add_quantumPosition(Quantum quant, int state, ChromosomePersonality pers,ChromosomeHappenings hap) {

		Fitness<EnvType,ModType> fit = new Fitness<>(this.EVO_ENV,this.verbose,this.level);

		quant.add_Position(state, pers, hap, this.determineLength(hap), fit);
	}


	@Override
	protected void initialize_population() {
		this.population = new Quantum[this.individual_count];

		// Initialize arrays for tracking minimum and maximum values
		this.min_personality = new double[this.number_agents][5];
		this.max_personality = new double[this.number_agents][5];

		this.min_happenings = new double[this.number_agents][this.number_happenings];
		this.max_happenings = new double[this.number_agents][this.number_happenings];

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

		// Set used initializers for happenings
		List<Integer> velInitializer = new ArrayList<>();

		for(int i = 0; i < velInitBool.length; i++) {
			if(velInitBool[i]) {
				velInitializer.add(i);
			}
		}



		// Initialize population
		for(int index=0; index<this.individual_count; index++) {

			// Create new chromosomes
			ChromosomePersonality pers_velocity = new ChromosomePersonality(this.number_agents);
			ChromosomeHappenings hap_velocity = new ChromosomeHappenings(this.number_agents,this.number_happenings);

			if(!velInitializer.isEmpty()) {

				int velType = velInitializer.get((int)Math.round(Math.random()*velInitializer.size()-0.5));

				switch(velType) {
				case 0:
					pers_velocity = this.randomPersonalityVelocityInitializer();
					hap_velocity = this.randomHappeningsVelocityInitializer();
					break;
				case 1:
					pers_velocity = this.discretePersonalityVelocityInitializer();
					hap_velocity = this.discreteHappeningsVelocityInitializer();
					break;
				default:
					System.out.println("Fatal Error @ Velocity Initialization Selection!");
					break;
				}
			}

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

			this.population[index] = this.new_quantum(personality,pers_velocity,happenings,hap_velocity,this.estimated_max_steps);
		}

		Arrays.sort(this.population);
		// Update max distances
		this.update_Distances();
		// Update min distances
		this.update_Distances();
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
	 * Inserts random numbers between 0 and estimated_max_steps into the chromosome.
	 * Numbers are discretized to be multiples of estimated_max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings randomHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				happenings.values[i][j] = (int)Math.round(Math.random()*(this.estimated_max_steps/this.number_happenings+1)-0.5)*this.number_happenings;
			}
		}
		return happenings;
	}


	/*
	 * Instantiates a happening with probability 1/number_agents
	 * Inserts random numbers between 0 and estimated_max_steps into the chromosome.
	 * Numbers are discretized to be multiples of estimated_max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings probabilisticHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				if(Math.random()<1/this.number_agents) {
					happenings.values[i][j] = (int)Math.round(Math.random()*(this.estimated_max_steps/this.number_happenings)+0.5)*this.number_happenings;
				}
			}
		}
		return happenings;
	}


	/*
	 * Instantiates every happening exactly once and assigns it to a random agent
	 * Inserts random numbers between 0 and estimated_max_steps into the chromosome.
	 * Numbers are discretized to be multiples of estimated_max_steps/number_happenings
	 * @Return: Instantiated Chromosome
	 */

	public ChromosomeHappenings steadyHappeningsInitializer() {

		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {

			int j = (int)Math.round(Math.random()*this.number_agents-0.5);

			happenings.values[i][j] = (int)Math.round(Math.random()*(this.estimated_max_steps/this.number_happenings)+0.5)*this.number_happenings;
		}
		return happenings;
	}


	public ChromosomePersonality randomPersonalityVelocityInitializer() {

		ChromosomePersonality pers_velocity = new ChromosomePersonality(this.number_agents);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < 5; j++) {

				pers_velocity.values[i][j] = this.round(Math.random()*0.2-0.1);
			}
		}
		return pers_velocity;
	}


	public ChromosomeHappenings randomHappeningsVelocityInitializer() {

		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {

				hap_velocity.values[i][j] = (int)Math.round((Math.random()*2-1)*this.estimated_max_steps/this.number_happenings);
			}
		}
		return hap_velocity;
	}


	public ChromosomePersonality discretePersonalityVelocityInitializer() {

		ChromosomePersonality pers_velocity = new ChromosomePersonality(this.number_agents);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < 5; j++) {

				pers_velocity.values[i][j] = discretePersVelocity[(int)Math.round(Math.random()*discretePersVelocity.length-0.5)];
			}
		}
		return pers_velocity;

	}


	public ChromosomeHappenings discreteHappeningsVelocityInitializer() {

		ChromosomeHappenings hap_velocity = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents;i++) {
			for(int j = 0; j < this.number_happenings; j++) {
				hap_velocity.values[i][j] = discreteHapVelocity[(int)Math.round(Math.random()*discreteHapVelocity.length-0.5)];
			}
		}
		return hap_velocity;
	}

	public List<Integer> select(int amount){

		List<Integer> unique_positions = new ArrayList<>();

		double total_tellability = 0;
		int valid_particles = 0;

		for(int i = 0; i < this.individual_count; i++) {

			total_tellability += ((Quantum) this.population[i]).best_tellability();
			if(((Quantum) this.population[i]).best_tellability()>0) {
				valid_particles++;
			}
		}

		if(valid_particles>amount) {

			int i = 0;

			while(i<valid_particles) {

				double roulette = Math.random()*total_tellability;
				int pos = 0;

				while(roulette > ((Quantum) this.population[pos]).best_tellability()) {

					roulette -= ((Quantum) this.population[pos]).best_tellability();
					pos++;
				}

				if(!unique_positions.contains(pos)) {
					unique_positions.add(pos);
					i++;
				}
			}

		} else {

			for(int pos = 0; pos < amount; pos++) {
				unique_positions.add(pos);
			}
		}

		return unique_positions;
	}

	public void crossover() {

		if(this.verbose) {
			System.out.println("Crossover:");
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

		int mode = crossoverList.get((int)Math.round(Math.random()*crossoverList.size()-0.5));

		int amount = 2;

		if(mode == 3) {
			amount += Math.random()*(this.individual_count-2);
		}

		List<Integer> positions = this.select(amount);

		int state = ((Quantum) this.population[positions.get(0)]).choosePosition();



		switch(mode) {
		case 0:
			this.simpleCrossover((Quantum) this.population[positions.get(0)], (Quantum) this.population[positions.get(1)],state);
			break;
		case 1:
			this.binomialCrossover((Quantum) this.population[positions.get(0)], (Quantum) this.population[positions.get(1)],state);
			break;
		case 2:
			this.xPointCrossover((Quantum) this.population[positions.get(0)], (Quantum) this.population[positions.get(1)],state);
			break;
		case 3:
			this.voteCrossover(positions,state);
			break;
		}

		this.analyzed_neighbors+=2;

		if(this.verbose) {
			System.out.println("");
		}
	}

	/*
	 * "Simply" exchanges ChromosomePersonality and ChromosomeHappenings
	 * Is equal to onePointCrossover with a fixed crossover Point
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void simpleCrossover(Quantum recipient, Quantum donor, int state) {

		QuantumPosition one = this.new_quantumPosition(recipient,state,recipient.get_personality(state),donor.best_happenings());
		QuantumPosition two = this.new_quantumPosition(recipient,state,donor.best_personality(),recipient.get_happenings(state));

		if(one.get_tellabilityValue() > two.get_tellabilityValue()) {
			recipient.add_Position(one, state);
		} else {
			recipient.add_Position(two, state);
		}

	}



	/*
	 * Exchanges Allele with probability crossover_prob
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void binomialCrossover(Quantum recipient, Quantum donor, int state) {

		ChromosomePersonality personalityOne = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		boolean change = false;

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(Math.random()<this.crossover_prob) {
					personalityOne.values[i][j] = donor.best_personality(i,j);
					personalityTwo.values[i][j] = recipient.get_position(state).get_personality(i,j);
					change = true;
				}else {
					personalityOne.values[i][j] = recipient.get_position(state).get_personality(i,j);
					personalityTwo.values[i][j] = donor.best_personality(i,j);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.crossover_prob) {
					happeningsOne.values[i][j] = donor.best_happenings(i,j);
					happeningsTwo.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					change = true;
				}else {
					happeningsOne.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					happeningsTwo.values[i][j] = donor.best_happenings(i,j);
				}
			}
		}

		if(change) {

			QuantumPosition one = this.new_quantumPosition(recipient,state,personalityOne,happeningsOne);
			QuantumPosition two = this.new_quantumPosition(recipient,state,personalityTwo,happeningsTwo);

			if(one.get_tellabilityValue() > two.get_tellabilityValue()) {
				recipient.add_Position(one, state);
			} else {
				recipient.add_Position(two, state);
			}

		}else {
			this.binomialCrossover(recipient, donor, state);
		}
	}


	/*
	 * Exchanges allele between crossover points.
	 * Crossover points are generated by the function setCrossoverPoints()
	 * @param one, two: The candidates to be crossed over
	 * @param index: position in offspring
	 */

	public void xPointCrossover(Quantum recipient, Quantum donor, int state) {

		boolean[][] crossPersonality = new boolean[this.number_agents][5];
		this.setCrossoverPoints(crossPersonality);

		boolean[][] crossHappenings = new boolean[this.number_agents][this.number_happenings];
		this.setCrossoverPoints(crossHappenings);

		ChromosomePersonality personalityOne = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityTwo = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsOne = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsTwo = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		boolean change = false;

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(crossPersonality[i][j]) {
					personalityOne.values[i][j] = donor.best_personality(i,j);
					personalityTwo.values[i][j] = recipient.get_position(state).get_personality(i,j);
					change=true;
				}else {
					personalityOne.values[i][j] = recipient.get_position(state).get_personality(i,j);
					personalityTwo.values[i][j] = donor.best_personality(i,j);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(crossHappenings[i][j]) {
					happeningsOne.values[i][j] = donor.best_happenings(i,j);
					happeningsTwo.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					change=true;
				}else {
					happeningsOne.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					happeningsTwo.values[i][j] = donor.best_happenings(i,j);
				}
			}
		}

		if(change) {

			QuantumPosition one = this.new_quantumPosition(recipient,state,personalityOne,happeningsOne);
			QuantumPosition two = this.new_quantumPosition(recipient,state,personalityTwo,happeningsTwo);

			if(one.get_tellabilityValue() > two.get_tellabilityValue()) {
				recipient.add_Position(one, state);
			} else {
				recipient.add_Position(two, state);
			}

		}else {
			this.xPointCrossover(recipient, donor, state);
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

					if(Math.random()<this.crossover_prob) {
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
					List<Integer> xcopy = new ArrayList<>();
					xcopy.addAll(xlist);

					// Get a random x position
					int xPos = (int)Math.round(Math.random()*(x-i)-0.5);
					Integer xCoord = xcopy.get(xPos);
					xcopy.remove(xPos);

					if(Math.random()<this.crossover_prob) {
						cross = !cross;
					}

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

		ChromosomePersonality personalityRandom = new ChromosomePersonality(this.number_agents);
		ChromosomePersonality personalityAverage = new ChromosomePersonality(this.number_agents);

		ChromosomeHappenings happeningsRandom = new ChromosomeHappenings(this.number_agents,this.number_happenings);
		ChromosomeHappenings happeningsAverage = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				int sum = 0;

				for(int k = 0; k < positions.size(); k++) {

					sum += ((Quantum) this.population[positions.get(k)]).best_personality(i,j);
				}

				int pos = positions.get((int)Math.round(Math.random()*positions.size()-0.5));

				personalityRandom.values[i][j] = ((Quantum) this.population[pos]).best_personality(i,j);
				personalityAverage.values[i][j] = this.round(sum/positions.size());
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				int sum = 0;

				for(int k = 0; k < positions.size(); k++) {

					sum += ((Quantum) this.population[positions.get(k)]).best_happenings(i,j);
				}

				int pos = positions.get((int)Math.round(Math.random()*positions.size()-0.5));

				happeningsRandom.values[i][j] = ((Quantum) this.population[pos]).best_happenings(i,j);
				happeningsAverage.values[i][j] = Math.round(sum/positions.size());
			}
		}

		QuantumPosition one = this.new_quantumPosition((Quantum) this.population[positions.get(0)], state,personalityRandom, happeningsRandom);
		QuantumPosition two = this.new_quantumPosition((Quantum) this.population[positions.get(0)], state,personalityAverage, happeningsAverage);

		if(one.get_tellabilityValue() > two.get_tellabilityValue()) {
			((Quantum) this.population[positions.get(0)]).add_Position(one, state);
		} else {
			((Quantum) this.population[positions.get(0)]).add_Position(two, state);
		}

	}

	public void mutate() {

		if(this.verbose) {
			System.out.println("Mutation:");
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

		int mode = mutationList.get((int)Math.round(Math.random()*mutationList.size()-0.5));

		int amount = 1;

		if(mode == 3) {
			amount++;
		}

		List<Integer> positions = this.select(amount);

		int state = ((Quantum) this.population[positions.get(0)]).choosePosition();

		switch(mode) {
		case 0:
			this.randomMutator((Quantum) this.population[positions.get(0)],state);
			break;
		case 1:
			this.toggleMutator((Quantum) this.population[positions.get(0)],state);
			break;
		case 2:
			this.orientedMutator((Quantum) this.population[positions.get(0)],state);
			break;
		case 3:
			this.guidedMutator((Quantum) this.population[positions.get(0)], (Quantum) this.population[positions.get(1)],state);
			break;
		default:
			System.out.println("Fatal Error @ Mutation Operator Selection!");
			break;
		}

		this.analyzed_neighbors+=2;

		if(this.verbose) {
			System.out.println("");
		}
	}


	/*
	 * Random Mutation
	 *
	 * Iterates the Chromosome and chooses a new random value for a position with probability mutation_prob
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public void randomMutator(Quantum recipient, int state) {

		boolean change = false;

		ChromosomePersonality positivePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings positiveHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		ChromosomePersonality negativePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings negativeHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				if(Math.random()<this.mutation_prob) {

					positivePersonality.values[i][j] = this.round(Math.random()*2-1);
					negativePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);
					change = true;

				}else {
					positivePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);
					negativePersonality.values[i][j] = this.round(Math.random()*2-1);
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.mutation_prob) {

					positiveHappenings.values[i][j] = (int)Math.round(Math.random()*recipient.get_position(state).get_actualLength()-0.5);
					negativeHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					change = true;

				}else {
					positiveHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					negativeHappenings.values[i][j] = (int)Math.round(Math.random()*recipient.get_position(state).get_actualLength()-0.5);
				}
			}
		}

		if(change) {

			QuantumPosition positive_resultant = this.new_quantumPosition(recipient,state,positivePersonality,positiveHappenings);
			QuantumPosition negative_resultant = this.new_quantumPosition(recipient,state,negativePersonality,negativeHappenings);

			if(positive_resultant.get_tellabilityValue() > negative_resultant.get_tellabilityValue()) {
				recipient.add_Position(positive_resultant, state);
			} else {
				recipient.add_Position(negative_resultant, state);
			}

		}else {

			this.randomMutator(recipient,state);
		}
	}


	/*
	 * Toggle Mutation
	 *
	 * Iterates the Chromosome and makes changes based on the current values. Happenings get instantiated or turned off
	 * while personality parameters get multiplied by -1.
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public void toggleMutator(Quantum recipient, int state) {

		boolean change = false;

		ChromosomePersonality positivePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings positiveHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		ChromosomePersonality negativePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings negativeHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				positivePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);
				negativePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);

				if(Math.random()<this.mutation_prob) {

					positivePersonality.values[i][j] *= -1;
					change = true;

				}else {

					negativePersonality.values[i][j] *= -1;
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				if(Math.random()<this.mutation_prob) {

					if(recipient.get_happenings(i,j) > 0) {

						positiveHappenings.values[i][j] = 0;
						negativeHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);

					}else {

						positiveHappenings.values[i][j] = (int)Math.round(Math.random()*(recipient.get_position(state).get_actualLength()-1)+0.5);
						negativeHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);
					}

					change = true;

				}else {

					if(recipient.get_happenings(i,j) > 0) {

						positiveHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);
						negativeHappenings.values[i][j] = (int)Math.round(Math.random()*(recipient.get_position(state).get_actualLength()-1)+0.5);

					}else {

						positiveHappenings.values[i][j] = recipient.get_position(state).get_happenings(i,j);
						negativeHappenings.values[i][j] = 0;
					}
				}
			}
		}

		if(change) {

			QuantumPosition positive_resultant = this.new_quantumPosition(recipient,state,positivePersonality,positiveHappenings);
			QuantumPosition negative_resultant = this.new_quantumPosition(recipient,state,negativePersonality,negativeHappenings);

			if(positive_resultant.get_tellabilityValue() > negative_resultant.get_tellabilityValue()) {
				recipient.add_Position(positive_resultant, state);
			} else {
				recipient.add_Position(negative_resultant, state);
			}

		}else {

			this.toggleMutator(recipient,state);
		}
	}


	/*
	 * Oriented Mutation
	 *
	 * Mutate value towards or away from another internal value in the same chromosome but at a different position
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public void orientedMutator(Quantum recipient, int state) {

		boolean change = false;

		ChromosomePersonality positivePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings positiveHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		ChromosomePersonality negativePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings negativeHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			// Personality
			for(int j = 0; j < 5; j++) {

				positivePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);
				negativePersonality.values[i][j] = recipient.get_position(state).get_personality(i,j);

				if(Math.random()<this.mutation_prob) {

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
					double distance = recipient.best_personality(xPos,yPos) - recipient.get_position(state).get_personality(i,j);

					if(ratio > 0) {
						positivePersonality.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							positivePersonality.values[i][j] += ratio * (-1-recipient.get_position(state).get_personality(i,j));
						} else {
							positivePersonality.values[i][j] += ratio * (1-recipient.get_position(state).get_personality(i,j));
						}
					}

				}else {

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
					double distance = recipient.best_personality(xPos,yPos) - recipient.get_position(state).get_personality(i,j);

					if(ratio > 0) {
						negativePersonality.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							negativePersonality.values[i][j] += ratio * (-1-recipient.get_position(state).get_personality(i,j));
						} else {
							negativePersonality.values[i][j] += ratio * (1-recipient.get_position(state).get_personality(i,j));
						}
					}
				}
			}

			// Happenings
			for(int j = 0; j < this.number_happenings; j++) {

				positiveHappenings.values[i][j] = recipient.get_happenings(i,j);
				negativeHappenings.values[i][j] = recipient.get_happenings(i,j);

				if(Math.random()<this.mutation_prob) {

					change = true;

					// Generate other position to look at
					int xPos = i;
					int yPos = j;

					while(i==xPos && j==yPos) {

						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*this.number_agents-0.5);
						} else {
							// FIXME: here was an error with 5 used instead of happ. num.
							yPos = (int)Math.round(Math.random()*this.number_happenings-0.5);
						}
					}

					double ratio = Math.random()*2-1;
					double distance = recipient.best_happenings(xPos,yPos) - recipient.get_position(state).get_happenings(i,j);

					if(ratio > 0) {
						positiveHappenings.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							positiveHappenings.values[i][j] -= ratio * recipient.get_position(state).get_happenings(i,j);
						} else {
							positiveHappenings.values[i][j] += ratio * (recipient.best_actualLength() - recipient.get_position(state).get_happenings(i,j));
						}
					}

				} else {

					// Generate other position to look at
					int xPos = i;
					int yPos = j;

					while(i==xPos && j==yPos) {

						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*this.number_agents-0.5);
						} else {
							// FIXME: here was an error with 5 used instead of happ. num.
							yPos = (int)Math.round(Math.random()*this.number_happenings-0.5);
						}
					}

					double ratio = Math.random()*2-1;
					double distance = recipient.best_happenings(xPos,yPos) - recipient.get_position(state).get_happenings(i,j);

					if(ratio > 0) {
						negativeHappenings.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							negativeHappenings.values[i][j] -= ratio * recipient.get_position(state).get_happenings(i,j);
						} else {
							negativeHappenings.values[i][j] += ratio * (recipient.best_actualLength() - recipient.get_position(state).get_happenings(i,j));
						}
					}
				}
			}
		}

		if(change) {

			QuantumPosition positive_resultant = this.new_quantumPosition(recipient,state,positivePersonality,positiveHappenings);
			QuantumPosition negative_resultant = this.new_quantumPosition(recipient,state,negativePersonality,negativeHappenings);

			if(positive_resultant.get_tellabilityValue() > negative_resultant.get_tellabilityValue()) {
				recipient.add_Position(positive_resultant, state);
			} else {
				recipient.add_Position(negative_resultant, state);
			}

		}else {

			this.orientedMutator(recipient,state);
		}
	}

	/*
	 * Guided Mutation
	 *
	 * Mutate a value towards or away from the corresponding value of another candidate with probability mutation_prob
	 *
	 * @param recipient: Individual to be mutated
	 * @return: mutated Individual
	 */

	public void guidedMutator(Quantum recipient, Quantum mutator, int state) {

		boolean change = false;

		ChromosomePersonality positivePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings positiveHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		ChromosomePersonality negativePersonality = new ChromosomePersonality(this.number_agents);
		ChromosomeHappenings negativeHappenings = new ChromosomeHappenings(this.number_agents, this.number_happenings);

		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				positivePersonality.values[i][j] = recipient.get_position(state).get_personality().values[i][j];
				negativePersonality.values[i][j] = recipient.get_position(state).get_personality().values[i][j];

				if(Math.random()<this.mutation_prob) {

					change = true;

					double ratio = Math.random()*2-1;
					double distance = mutator.best_personality(i,j) - recipient.get_position(state).get_personality(i,j);

					if(ratio > 0) {
						positivePersonality.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							positivePersonality.values[i][j] += ratio * (-1-recipient.get_position(state).get_personality(i,j));
						} else {
							positivePersonality.values[i][j] += ratio * (1-recipient.get_position(state).get_personality(i,j));
						}
					}

				}else {

					double ratio = Math.random()*2-1;
					double distance = mutator.best_personality(i,j) - recipient.get_position(state).get_personality(i,j);

					if(ratio > 0) {
						negativePersonality.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							negativePersonality.values[i][j] += ratio * (-1-recipient.get_position(state).get_personality(i,j));
						} else {
							negativePersonality.values[i][j] += ratio * (1-recipient.get_position(state).get_personality(i,j));
						}
					}
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				positiveHappenings.values[i][j] = recipient.get_happenings().values[i][j];
				negativeHappenings.values[i][j] = recipient.get_happenings().values[i][j];

				if(Math.random()<this.mutation_prob) {

					change = true;

					double ratio = Math.random()*2-1;
					double distance = mutator.best_happenings(i,j) - recipient.get_position(state).get_happenings(i,j);

					if(ratio > 0) {
						positiveHappenings.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							positiveHappenings.values[i][j] -= ratio * recipient.get_position(state).get_happenings(i,j);
						} else {
							positiveHappenings.values[i][j] += ratio * (mutator.best_actualLength() - recipient.get_position(state).get_happenings(i,j));
						};
					}

				}else {

					double ratio = Math.random()*2-1;
					double distance = mutator.best_happenings(i,j) - recipient.get_position(state).get_happenings(i,j);

					if(ratio > 0) {
						negativeHappenings.values[i][j] += ratio * distance;
					}else {
						ratio = -ratio;
						if(distance>0) {
							negativeHappenings.values[i][j] -= ratio * recipient.get_position(state).get_happenings(i,j);
						} else {
							negativeHappenings.values[i][j] += ratio * (mutator.best_actualLength() - recipient.get_position(state).get_happenings(i,j));
						};
					}
				}
			}
		}

		if(change) {

			QuantumPosition positive_resultant = this.new_quantumPosition(recipient,state,positivePersonality,positiveHappenings);
			QuantumPosition negative_resultant = this.new_quantumPosition(recipient,state,negativePersonality,negativeHappenings);

			if(positive_resultant.get_tellabilityValue() > negative_resultant.get_tellabilityValue()) {
				recipient.add_Position(positive_resultant, state);
			} else {
				recipient.add_Position(negative_resultant, state);
			}

		}else {

			 this.guidedMutator(recipient, mutator, state);
		}
	}



	public void move_particles() {

		for(int i = 0; i < this.individual_count; i++) {

			for(int state = 0; state < ((Quantum) this.population[i]).amount_positions(); state++) {

				((Quantum) this.population[i]).move(state,new Fitness<EnvType,ModType>(this.EVO_ENV,this.verbose,this.level));
				this.analyzed_neighbors++;
			}
		}

		Arrays.sort(this.population);

		if(this.floatingParameters) {
			this.update_Distances();
		}
	}


	public void update_movement() {

		for(int index = 0; index < this.individual_count; index++) {

			for(int state = 0; state < ((Quantum) this.population[index]).amount_positions(); state++) {

				List<Integer> informants = this.select_particles(index, state);

				if(this.floatingParameters) {
					this.floating_Updater(index, state, informants);
				} else {
					this.static_Updater(index, state, informants);
				}

			}
			((Quantum) this.population[index]).update_lifespan();
		}
	}


	public List<Integer> select_particles(int individual, int state) {

		if(this.fitnessBasedSelection) {
			return this.gravity_selector(individual, state);
		}

		return this.best_selector(individual);
	}


	public List<Integer> best_selector(int individual) {

		List<Integer> selected_particles = new ArrayList<>();

		// Every particle is an informant to himself
		selected_particles.add(individual);

		// increment in order to avoid adding self to the selection
		int increment = 0;

		for(int i = 0; i < this.number_informants; i++) {

			if(i == individual) {
				increment = 1;
			}

			selected_particles.add(i + increment);
		}

		return selected_particles;
	}


	public List<Integer> gravity_selector(int individual, int state) {

		// Pick Particles
		List<Integer> selected_particles = new ArrayList<>();

		for(int i = 0; i < this.individual_count; i++) {

			// A Particles qualifies as an informant if it's fitness is greater than the particle to be updated
			if(((Quantum) this.population[i]).best_tellability() >= ((Quantum) this.population[individual]).get_position(state).get_tellabilityValue()) {
				selected_particles.add(i);
			}

		}
		return selected_particles;
	}


	public void static_Updater(int recipient, int state, List<Integer> informants) {

		double[][] update_personality = new double[this.number_agents][5];
		double[][] update_happenings = new double[this.number_agents][this.number_happenings];

		double random_factor = 1;

		for(int index = 0; index < informants.size(); index++) {

			double force = this.decay_rate;

			// determine if force is pulling towards or pushing away
			if(((Quantum) this.population[informants.get(index)]).best_tellability() <= ((Quantum) this.population[recipient]).get_tellability(state)) {
				force*=-1;
			}

			// copy information
			for(int i = 0; i < this.number_agents; i++) {

				for(int j = 0; j < 5; j++) {

					if(!this.deterministic) {
						random_factor = Math.random();
					}

					update_personality[i][j] += random_factor*force*(((Quantum) this.population[informants.get(index)]).best_personality(i, j) - ((Quantum) this.population[recipient]).get_position(state).get_personality(i, j));
				}

				for(int j = 0; j < this.number_happenings; j++) {

					if(!this.deterministic) {
						random_factor = Math.random();
					}

					update_happenings[i][j] += random_factor*force*(((Quantum) this.population[informants.get(index)]).best_happenings(i, j) - ((Quantum) this.population[recipient]).get_position(state).get_happenings(i, j));
				}
			}
		}


		// Average out the velocity influence and update the chromosomes of current particle
		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(this.deterministic) {
					((Quantum) this.population[recipient]).get_position(state).update_persVelocity(i, j, update_personality[i][j]/informants.size(),this.decay_rate);
				} else {
					((Quantum) this.population[recipient]).get_position(state).update_persVelocity(i, j, update_personality[i][j]/informants.size(),this.decay_rate/2);
				}
			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(this.deterministic) {
					((Quantum) this.population[recipient]).get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),this.decay_rate);
				} else {
					((Quantum) this.population[recipient]).get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),this.decay_rate/2);
				}
			}
		}
	}


	public void floating_Updater(int recipient, int state, List<Integer> informants) {

		double[][] update_personality = new double[this.number_agents][5];
		double[][] update_happenings = new double[this.number_agents][this.number_happenings];

		double total_force = 0;
		double random_factor = 1;

		for(int index = 0; index < informants.size(); index++) {

			// determine strength of interaction
			double energy = this.fitness_rating(recipient, state, informants.get(index));
			double distance = this.distance_rating(recipient, informants.get(index));
			double inertia = this.determine_spacetime(recipient,state);

			double force = energy*inertia*Math.pow(distance,2);

			total_force += force;

			// copy information
			for(int i = 0; i < this.number_agents; i++) {

				for(int j = 0; j < 5; j++) {

					if(!this.deterministic) {
						random_factor = Math.random();
					}

					update_personality[i][j] += random_factor*force*((Quantum) this.population[informants.get(index)]).best_personality(i, j) - ((Quantum) this.population[recipient]).get_position(state).get_personality(i, j);
				}

				for(int j = 0; j < this.number_happenings; j++) {

					if(!this.deterministic) {
						random_factor = Math.random();
					}

					update_happenings[i][j] += random_factor*force*(((Quantum) this.population[informants.get(index)]).best_happenings(i, j) - ((Quantum) this.population[recipient]).get_position(state).get_happenings(i, j));
				}
			}
		}

		// Average out the velocity influence and update the chromosomes of current particle
		for(int i = 0; i < this.number_agents; i++) {

			for(int j = 0; j < 5; j++) {

				if(this.deterministic) {
					((Quantum) this.population[recipient]).get_position(state).update_persVelocity(i, j, update_personality[i][j]/informants.size(),total_force/informants.size());
				} else {
					((Quantum) this.population[recipient]).get_position(state).update_persVelocity(i, j, update_personality[i][j]/informants.size(),total_force/(informants.size()*2));
				}

			}

			for(int j = 0; j < this.number_happenings; j++) {

				if(this.deterministic) {
					((Quantum) this.population[recipient]).get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),total_force/informants.size());
				} else {
					((Quantum) this.population[recipient]).get_position(state).update_hapVelocity(i, j, (int)Math.round(update_happenings[i][j]/informants.size()),total_force/(informants.size()*2));
				}
			}
		}
	}

	@Override
	public void to_file(CandidateSolution best, String epilogue) {
		String qsoEpilogue = "<QSO Epilogue: Found Best / Analyzed Neighbors>\n";
		qsoEpilogue += String.valueOf(this.found_best) + " " + String.valueOf(this.analyzed_neighbors) + "\n";

		epilogue = qsoEpilogue + epilogue;
		super.to_file(best, epilogue);
	}
}
