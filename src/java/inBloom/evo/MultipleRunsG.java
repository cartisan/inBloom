package inBloom.evo;

public class MultipleRunsG {
	
public static void main(String[] args) { 
		
		for(int i = 0; i < 10; i+=1) {
		
			// init location
			NIRobinsonIsland island = new NIRobinsonIsland();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// selection size
			int selection_size = 4;
			// decay_rate
			double decay_rate = 0.1;
			
			// crossover probability
			double crossover_prob = 0.2;
			// mutation probability
			double mutation_prob = 0.1;
			
			// maximum time in seconds (no time limit: time < 0 or leave it out as default value is -1)
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;
			
			// path and name of file
			String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\Current\\";
			String filename = "GEN " + String.valueOf(i);

//			GeneticAlgorithm<?,?> ga = island.get_GA(args,init_stepnumber,individual_count,selection_size,crossover_prob,mutation_prob);
			GeneticAlgorithm<?,?> ga = island.get_GA(args,init_stepnumber,individual_count,selection_size,decay_rate);
			
			ga.setFileName(path+filename);
			
					
			ga.setExit(false);
				
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			ga.setPersInit(false, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			ga.setHapInit(true, true, false);
			
			// randomSelector, steadySelector
			ga.setSelection(false,true);
			
			// simpleCrossover,binomialCrossover,xPointCrossover,voteCrossover
			ga.setCrossover(true, true, true, true);

			// randomMutator,toggleMutator,orientedMutator,guidedMutator
			ga.setMutation(true, true, true, true);
			
			// true -> SteadyReplacer, false -> partiallyRandomReplacer
			ga.setReplaceMode(true);
			
			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			ga.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			ga.setTermination(max_repetitions);
			
			ga.run();
			
			ga = null;
		}
		System.exit(0);
	}
}
