package inBloom.evo;

public class EvolutionaryLauncher {
	
	public static void main(String[] args) { 
		
		// init location
		EvoIsland island = new EvoIsland();
		// simulation length at initialization
		int init_stepnumber = 50;
		// number individuals
		int individual_count = 20;
		
		// maximum time in seconds (0 = no time limit)
		int time = 60;
		// number of iterations without improvement till shutdown
		int max_repetitions = 50;
		
		// path and name of file
		String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\";
		String filename = "";
		
		/**
		 * Choose 1 Mode:
		 */
		
		//String algorithm = "Evolutionary";
		//String algorithm = "PSO";
		String algorithm = "Coupled";
		
		GeneticAlgorithm<?,?> ga = island.get_GA(args,init_stepnumber,individual_count,4,0.2,0.1);
		
		switch(algorithm) {
		
		case("Coupled"):
		
		case("Evolutionary"):
		
			ga.setFileName(path+"Evolutionary"+filename);
		
			if(algorithm=="Coupled") {
				
				ga.setExit(false);
			}
			
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			ga.setPersInit(true, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			ga.setHapInit(true, true, true);
			// randomSelector, rouletteWheelSelection
			ga.setSelection(true, true);
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
			
			if(algorithm=="Evolutionary")
				break;
		
		case("PSO"):
			
			PSO<?,?> pso = island.get_PSO(args,init_stepnumber, individual_count);
			
			pso.setFileName(path+"PSO"+filename);
			
			if(algorithm=="Coupled") {

				pso.setGenInit(10, ga.get_genPool());
			}
			
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			pso.setPersInit(true, false, false);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			pso.setHapInit(true, true, true);
			// 
			pso.setVelInit(true, true);
			//
			pso.setVelocityInformants(7);
			// randomSelector, rouletteWheelSelection
			pso.setSelectionManner(false);
			//
			pso.setSpacetime(false);
			
			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			pso.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			pso.setTermination(max_repetitions);
			
			pso.run();
			
			break;
		}			
	}
}
