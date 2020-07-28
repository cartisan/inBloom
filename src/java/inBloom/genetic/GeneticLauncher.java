package inBloom.genetic;

public class GeneticLauncher {

	public static void main(String[] args) { 
		
		GeneticIsland island = new GeneticIsland(20,4,0.2,0.1,20);
		int time = 3600;
		int max_repetitions = 50;
		boolean customize = true;
		
		if(customize) {
			
			GeneticAlgorithm<?,?> ga = island.get_GA(args);
			
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
		}else {
			island.run_GA(args, time);
		}
	}
}
