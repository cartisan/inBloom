package inBloom.evo;

public class EvolutionaryLauncher {
	
	public static void main(String[] args) { 
		
		EvoIsland island = new EvoIsland(20);
		int time = 600;
		int max_repetitions = 50;
		//String algorithm = "Evolutionary";
		String algorithm = "PSO";
		
		switch(algorithm) {
		
		case("Evolutionary"):
			
			GeneticAlgorithm<?,?> ga = island.get_GA(args,4,0.2,0.1);
			
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
			
			break;
		
		case("PSO"):

			
			PSO<?,?> pso = island.get_PSO(args);
			
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
