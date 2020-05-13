package inBloom.genetic;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class GeneticAlgorithm<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	
	public String[] args;
	public GeneticEnvironment<?,?> GEN_ENV;
	
	public int number_agents;
	public int number_happenings;
	public int max_steps;
	public int pop_size;
	public int selection_size;
	public float crossover_prob;
	public float mutation_prob;
	
	public Fitness<?,?> fit;
	private Candidate[] gen_pool;
	private Candidate[] offspring;
	private Candidate[] mutated_offspring;
	
	/*
	 * Constructors for GA
	 */
	
	public GeneticAlgorithm (String[] args, GeneticEnvironment<?,?> GEN_ENV, int number_agents, int number_happenings, int max_steps, int pop_size, int number_selections, float crossover_prob, float mutation_prob) {
		
		this.args = args;
		this.GEN_ENV = GEN_ENV;
		this.number_agents = number_agents;
		this.max_steps = max_steps;
		this.number_happenings = number_happenings;
		this.pop_size = pop_size;
		this.selection_size = number_selections*2;
		this.crossover_prob = crossover_prob;
		this.mutation_prob = mutation_prob;		
		
		//<GEN_ENV.EnvType,GEN_ENV.ModType>
		this.fit = new Fitness<EnvType,ModType>(args, GEN_ENV);
		
		initialize_pop();
	}
	
	/*
	 * Initialization
	 */
	
	public void initialize_pop() {
		
		gen_pool = new Candidate[pop_size];
		offspring = new Candidate[selection_size];
		mutated_offspring = new Candidate[selection_size];
		
		int index = 0;
		
		while(index<pop_size) {
			
			//Create new Chromosomes
			//TODO: Different Initializers
			ChromosomePersonality personality = randomPersonalityInitializer();
			ChromosomeHappenings happenings = randomHappeningsInitializer();
			
			//Check for Duplicates
			for(int i = 0; i < index; i++) {
				if(!gen_pool[i].equals(personality,happenings)) {

					gen_pool[index] = new Candidate(personality,happenings,this.fit);
					index++;
				}
			}
		}
		Arrays.sort(gen_pool);
	}
	
	/*
	 * Random initializer for ChomosomePersonality
	 */
	
	public ChromosomePersonality randomPersonalityInitializer() {
		
		ChromosomePersonality personality = new ChromosomePersonality(number_agents);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < 5; j++) {
				personality.values[i][j] = Math.random()*2-1;
			}
		}
		return personality;
	}	

	/*
	 * Random initializer for ChromosomeHappening
	 */
	
	public ChromosomeHappenings randomHappeningsInitializer() {
		ChromosomeHappenings happenings = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents;i++) {
			for(int j = 0; j < number_happenings; j++) {
				happenings.values[i][j] = (int)Math.round(Math.random()*max_steps);
			}
		}
		return happenings;
	}
	
	/*
	 * Selection
	 */
	
	public List<Integer> select() {
		
		List<Integer> result = new ArrayList<Integer>();
		
		int type = (int)Math.round(Math.random());
		
		switch(type) {
		
		case(0): 
			
			result = randomSelector();
		
		case(1):
			
			result = rouletteWheelSelector();
			
		}
		return result;
	}
	
	/*
	 *  Selection by random
	 */
	
	public List<Integer> randomSelector(){
		
		List<Integer> selectedIndividuums = new ArrayList<Integer>();
		
		while(selectedIndividuums.size() < selection_size) {
			int position = (int)Math.round(Math.random()*(pop_size-1));
			if(!selectedIndividuums.contains(position)) {
				selectedIndividuums.add(position);
			}
				
		}
		return selectedIndividuums;
	}
	
	/*
	 * Fitness based Selection
	 */
	
	public List<Integer> rouletteWheelSelector() {
		
		double total_fitness = 0.0;
		double[] rouletteWheel = new double[pop_size];
		
		for(int i = 0; i < pop_size; i++) {
			total_fitness += gen_pool[i].get_tellability();
			rouletteWheel[i] = total_fitness;
		}
		
		List<Integer> selectedIndividuums = new ArrayList<Integer>();
		
		while(selectedIndividuums.size() < selection_size) {
			
			int position = 0;
			double value = Math.random()*total_fitness;
			
			while(value > rouletteWheel[position]) {
				position++;
			}
			if(!selectedIndividuums.contains(position)) {
				selectedIndividuums.add(position);
			}	
		}
		return selectedIndividuums;
	}
	
	/*
	 * Crossover
	 */
	
	public void crossover(List<Integer> positions) {
		
		for(int i = 0; i < selection_size; i+=2) {
			
			int pos = (int)Math.random()*(positions.size()-1);
			int one = positions.get(pos);
			positions.remove(pos);
			pos = (int)Math.random()*(positions.size()-1);
			int two = positions.get(pos);
			positions.remove(pos);
			
			int type = (int)Math.round(Math.random()*3);
			
			switch(type) {
			
			case(0): 
				
				simpleCrossover(gen_pool[one],gen_pool[two],i);
			
			case(1): 
				
				binomialCrossover(gen_pool[one],gen_pool[two],i);
			
			case(2): 
				
				xPointCrossover(gen_pool[one],gen_pool[two],i);
			
			case(3): 
				
				// Add 2 initial candidates to the List
				List<Candidate> candidates = new ArrayList<Candidate>();
			
				candidates.add(gen_pool[one]);
				candidates.add(gen_pool[two]);
				
				// Add additional Votes
				int additionalVotes = (int)Math.round(Math.random()*(pop_size-2));
				
				while(additionalVotes > 0) {
					
					int votePos = (int)Math.round(Math.random()*(pop_size-1));
					
					if(!candidates.contains(gen_pool[votePos])) {
						candidates.add(gen_pool[votePos]);
					}
				}
				voteCrossover(candidates,i);
			}
		}
		Arrays.sort(offspring);
	}
	
	/*
	 * "Simply" exchanges ChromosomePersonality and ChromosomeHappenings
	 * Is equal to onePointCrossover with a fixed crossover Point
	 */

	public void simpleCrossover(Candidate one, Candidate two, int index) {
		
		offspring[index] = new Candidate(one.get_personality(), two.get_happenings(),fit);
		offspring[index+1] = new Candidate(two.get_personality(), one.get_happenings(),fit);
	}
	
	/*
	 * Exchanges Allele with probability crossover_prob
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
		
		offspring[index] = new Candidate(personalityOne,happeningsOne,fit);
		offspring[index+1] = new Candidate(personalityTwo,happeningsTwo,fit);	
	}
	
	/*
	 * Exchanges allele between crossover points
	 * crossover points are generated by the function setCrossoverPoints()
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
		offspring[index] = new Candidate(personalityOne,happeningsOne,fit);
		offspring[index+1] = new Candidate(personalityTwo,happeningsTwo,fit);	
	}
	
	/*
	 * Generate crossover points
	 */
	
	public boolean[][] setCrossoverPoints(int x, int y){
		
		return setCrossoverPoints(x,y,false);
	}
	
	public boolean[][] setCrossoverPoints(int x, int y, boolean initial){
		
		boolean cross = initial;
		
		boolean[][] result = new boolean[x][y];
		
		List<Integer> xlist = new ArrayList<Integer>();
		
		for(int i = 0; i < x; i++) {
			xlist.add(i);
		}
		
		List<Integer> ylist = new ArrayList<Integer>();
		
		for(int i = 0; i < y; i++) {
			ylist.add(i);
		}
		
		int mode = (int)Math.round(Math.random());
		
		if(mode == 0) {
			
			for(int i = 0; i < x; i++) {
				
				int xPos = (int)Math.round(Math.random()*(x-1-i));
				int xCoord = xlist.get(xPos);
				xlist.remove(xPos);		
						
				for(int j = 0; j < y; j++){
					
					int yPos = (int)Math.round(Math.random()*(y-1-j));
					int yCoord = xlist.get(yPos);
					ylist.remove(yPos);
					
					if(Math.random()<crossover_prob) {
						cross = !cross;
					}
					
					result[xCoord][yCoord] = cross;
				}
			}
		}else {		
			
			for(int j = 0; j < y; j++){
		
				int yPos = (int)Math.round(Math.random()*(y-1-j));
				int yCoord = xlist.get(yPos);
				ylist.remove(yPos);
				
				for(int i = 0; i < x; i++) {
				
					int xPos = (int)Math.round(Math.random()*(x-1-i));
					int xCoord = xlist.get(xPos);
					xlist.remove(xPos);
					
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
	 * Since we need to generate 2 Candidates per crossover operation we return the average & a random generated resultant
	 */
	
	public void voteCrossover(List<Candidate> candidates, int index) {
		
		ChromosomePersonality personalityRandom = new ChromosomePersonality(number_agents);
		ChromosomePersonality personalityAverage = new ChromosomePersonality(number_agents);
		
		ChromosomeHappenings happeningsRandom = new ChromosomeHappenings(number_agents,number_happenings);
		ChromosomeHappenings happeningsAverage = new ChromosomeHappenings(number_agents,number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				int sum = 0;
				int pos = (int)Math.random()*(candidates.size()-1);
				
				for(int k = 0; k < candidates.size(); k++) {
					
					sum += candidates.get(k).get_personality(i,j);
				}
				
				personalityRandom.values[i][j] = candidates.get(pos).get_personality(i,j);
				personalityAverage.values[i][j] = (int)Math.round(sum/candidates.size());
			}
			
			for(int j = 0; j < number_happenings; j++) {
				
				int sum = 0;
				int pos = (int)Math.random()*(candidates.size()-1);
				
				for(int k = 0; k < candidates.size(); k++) {
					
					sum += candidates.get(k).get_happenings().values[i][j];
				}
				
				if(Math.random()>crossover_prob) {
					
					happeningsRandom.values[i][j] = candidates.get(pos).get_happenings(i,j);
					happeningsAverage.values[i][j] = (int)Math.round(sum/candidates.size());
				}
			}
		}
		offspring[index] = new Candidate(personalityAverage,happeningsAverage,fit);
		offspring[index+1] = new Candidate(personalityRandom,happeningsRandom,fit);	
	}
	
	/*
	 * Mutation
	 */
	
	public void mutate() {
		
		for(int i = 0; i < selection_size; i++) {
			
			int mode = (int)Math.round(Math.random()*2);
			
			switch(mode) {
			
			case(0):
				
				mutated_offspring[i]=randomMutator(offspring[i]);
				
			case(1):
				
				mutated_offspring[i]=orientedMutator(offspring[i]);
				
			case(2):
				
				int j = i;
				while(j==i) {
					j = (int)Math.round(Math.random()*(selection_size-1));
				}
				mutated_offspring[i]=guidedMutator(offspring[i],offspring[j]);
			}
		}
	}
	
	/*
	 * Random Mutation
	 */
	
	public Candidate randomMutator(Candidate recipient) {
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				if(Math.random()<mutation_prob) {
					mutatedPersonality.values[i][j] = Math.random()*2-1;
				}else {
					mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {

				if(Math.random()<mutation_prob) {
					mutatedHappenings.values[i][j] = (int)Math.round(Math.random()*max_steps);
				}else {
					mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				}
			}
		}	

		return new Candidate(mutatedPersonality, mutatedHappenings,fit);
	}
	
	/*
	 * Mutate value towards or away from another internal value (different position)
	 */
	
	public Candidate orientedMutator(Candidate recipient) {
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality(i,j);
				
				if(Math.random()<mutation_prob) {
					
					int xPos = i;
					int yPos = j;
					
					while(i==xPos && j==yPos) {
						
						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*(number_agents-1));
						}else {
							yPos = (int)Math.round(Math.random()*(4));
						}
					}
					mutatedPersonality.values[i][j] += (Math.random()*2-1) * (recipient.get_personality(xPos,yPos) - recipient.get_personality(i,j));
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings(i,j);
				
				if(Math.random()<mutation_prob) {
					
					int xPos = i;
					int yPos = j;
					
					while(i==xPos && j==yPos) {
						
						if(Math.random()>0.5) {
							xPos = (int)Math.round(Math.random()*(number_agents-1));
						}else {
							yPos = (int)Math.round(Math.random()*(4));
						}
					}
					mutatedHappenings.values[i][j] += (Math.random()*2-1) * (recipient.get_happenings(xPos,yPos) - recipient.get_happenings(i,j));
				}
			}
		}
		return new Candidate(mutatedPersonality, mutatedHappenings,fit);
	}
	
	/*
	 * Mutate value towards or away from another external value (same position)
	 */
	public Candidate guidedMutator(Candidate recipient, Candidate mutator) {
		
		ChromosomePersonality mutatedPersonality = new ChromosomePersonality(number_agents);
		ChromosomeHappenings mutatedHappenings = new ChromosomeHappenings(number_agents, number_happenings);
		
		for(int i = 0; i < number_agents; i++) {
			
			for(int j = 0; j < 5; j++) {

				mutatedPersonality.values[i][j] = recipient.get_personality().values[i][j];
				
				if(Math.random()<mutation_prob) {
					
					mutatedPersonality.values[i][j] += (Math.random()*2-1) * (mutator.get_personality(i,j) - recipient.get_personality(i,j));
				}
			}
			
			for(int j = 0; j < number_happenings; j++) {

				mutatedHappenings.values[i][j] = recipient.get_happenings().values[i][j];
				
				if(Math.random()<mutation_prob) {
					
					mutatedHappenings.values[i][j] += (Math.random()*2-1) * (mutator.get_happenings(i,j) - recipient.get_happenings(i,j));
				}
			}
		}
		return new Candidate(mutatedPersonality, mutatedHappenings,fit);
	}
	
	/*
	 * Setup next Generation
	 */
	
	public void recombine(boolean mode) {
		
		if(mode)
			gen_pool = steadyNoDuplicatesReplacer();
		else
			gen_pool = partiallyRandomNoDuplicatesReplacer();
		
		Arrays.sort(gen_pool);
		
		// remove artifacts
		offspring = new Candidate[selection_size];
		mutated_offspring = new Candidate[selection_size];
	}
	
	/*
	 * Keep best half of last generation
	 * Fill up other half with best performing individuums from offspring
	 */
	
	public Candidate[] steadyNoDuplicatesReplacer() {
		
		Candidate[] next_gen = new Candidate[pop_size];
		
		int steady = pop_size/2;
		
		// Keep best performing individuums of current Population
		for(int i = 0; i < steady; i++) {
			next_gen[i] = gen_pool[i];
		}
		
		// Create List of all newly generated Candidates sorted by fitness descending
		List<Candidate> total_offspring = new ArrayList<Candidate>();
		int posOff = 0;
		int posMut = 0;
		
		while(posOff+posMut < selection_size*2) {
			
			if(posOff<selection_size) {
				
				if(posMut<selection_size) {
					
					if(offspring[posOff].get_tellability()>mutated_offspring[posMut].get_tellability()) {
						total_offspring.add(offspring[posOff]);
						posOff++;
					}else {
						total_offspring.add(offspring[posMut]);
						posMut++;
					}
					
				}else {
					total_offspring.add(offspring[posOff]);
					posOff++;
				}
				
			}else {
				total_offspring.add(offspring[posMut]);
				posMut++;
			}
		}
		
		// Add best individuums from total_offspring to next_gen avoiding duplicates
		for(int i = steady; i<pop_size; i++) {
			
			boolean done = false;
			
			while(!done) {
				if(!total_offspring.isEmpty()) {
					if(!total_offspring.get(0).isContainedIn(next_gen)) {
						next_gen[i] = total_offspring.get(0);
						done = true;
					}
					total_offspring.remove(0);
				// If there are not enough candidates in total_offspring, reuse old population
				}else {
					next_gen[i] = gen_pool[i];
					done = true;
				}
			}
		}
		return next_gen;
	}

	/*
	 * Keep best n Candidates with n = pop_size/2
	 * Fill up other half random individuals from the population
	 */
	
	public Candidate[] partiallyRandomNoDuplicatesReplacer() {
		
		Candidate[] next_gen = new Candidate[pop_size];
		
		// Initialize a List with all Candidates sorted by fitness descending
		List<Candidate> allCandidates = new ArrayList<Candidate>();
		
		int posPop = 0;
		int posOff = 0;
		int posMut = 0;
		
		for(int i = 0; i < pop_size+selection_size*2; i++) {
			
			int best = 0;
			double bestTellability = -1;
			
			if(posPop < pop_size) {
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
			
			switch(best){
			
			case(0):
				
				System.out.println("Missing Candidates @ partiallyRandomNoDuplicatesReplacer()");
				return null;

			case(1):
				
				allCandidates.add(gen_pool[posPop]);
				posPop++;
				
			case(2):
				
				allCandidates.add(offspring[posOff]);
				posOff++;
				
			case(3):
				
				allCandidates.add(mutated_offspring[posMut]);
				posMut++;
			}	
		}
		
		// Keep best performing individuums of current Population
		int steady = pop_size/2;
		
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
		
		// Fill rest with random individuums but avoid adding duplicates
		for(int i = steady; i<pop_size; i++) {
			
			boolean done = false;
			
			while(!done) {
				int pos = (int)Math.round(Math.random()*(allCandidates.size()-1));
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
	
	/*
	 * Test for Robinson Environment
	 * Initializes the IslandEnvironment with predefined Personality traits and time points for Happenings
	 */
	
	public double robinsonTest() {
		
		ChromosomePersonality personality = new ChromosomePersonality(this.number_agents);
		
		personality.values[0][0] = 1;
		personality.values[0][1] = 0;
		personality.values[0][2] = 0.5;
		personality.values[0][3] = -0.5;
		personality.values[0][4] = 0;
		
		ChromosomeHappenings happenings = new ChromosomeHappenings(this.number_agents,this.number_happenings);

		happenings.values[0][0] = 9;
		happenings.values[0][1] = 8;
		happenings.values[0][2] = 18;
		happenings.values[0][3] = 25;
		happenings.values[0][4] = 29;
		
		double tellability = -1;
		
		Candidate robinson = new Candidate(personality,happenings,this.fit);
		tellability = robinson.get_tellability();
		
		return tellability;
		
	}
}
