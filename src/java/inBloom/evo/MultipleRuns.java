package inBloom.evo;

public class MultipleRuns {
	
public static void main(String[] args) { 
		
		for(int i = 0; i < 4; i++) {
		
			// init location
			EvoIsland island = new EvoIsland();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// selection size
			int selection_size = 4;
			// crossover probability
			double crossover_prob = 0.2;
			// mutation probability
			double mutation_prob = 0.1;
			
			// maximum time in seconds (no time limit: time < 0 or leave it out as default value is -1)
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;
			
			// path and name of file
			String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\G\\0201\\";
			String filename = "GEN " + String.valueOf(i);
			
			GeneticAlgorithm<?,?> ga = island.get_GA(args,init_stepnumber,individual_count,4,0.2,0.1);
			
			ga.setFileName(path+"GEN"+filename);
			
					
			ga.setExit(false);
				
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			ga.setPersInit(false, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			ga.setHapInit(true, true, false);
			
			// true -> rouletteWheelSelection, false -> randomSelector
			if(i<2)
				ga.setSelection(true);
			else
				ga.setSelection(false);
			// simpleCrossover,binomialCrossover,xPointCrossover,voteCrossover
			ga.setCrossover(true, true, true, true);
			// randomMutator,toggleMutator,orientedMutator,guidedMutator
			ga.setMutation(true, true, true, true);
			
			// true -> SteadyReplacer, false -> partiallyRandomReplacer
			if(i%2 == 0)
				ga.setReplaceMode(true);
			else
				ga.setReplaceMode(false);
			
			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			ga.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			ga.setTermination(max_repetitions);
			
			ga.run();
		}
	}
}
