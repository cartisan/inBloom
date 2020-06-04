package inBloom.genetic;

public class GeneticLauncher {

	public static void main(String[] args) { 
		
		GeneticIsland island = new GeneticIsland(4,1,0.05,0.01);
		int time = 45;
		int max_repetitions = 50;
		boolean customize = true;
		
		if(customize) {
			
			GeneticAlgorithm<?,?> ga = island.get_GA(args);
			
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			ga.setPersInit(true, false, false);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			ga.setHapInit(true, false, false);
			// randomSelector, rouletteWheelSelection
			ga.setSelection(true, true);
			// simpleCrossover,binomialCrossover,xPointCrossover,voteCrossover
			ga.setCrossover(true, false, false, false);
			// randomMutator,toggleMutator,orientedMutator,guidedMutator
			ga.setMutation(true, false, false, false);
			// true -> SteadyReplacer, false -> partiallyRandomReplacer
			ga.setReplaceMode(true);		
			
			// Termination Criteria
			// Runtime in seconds
			ga.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			ga.setTermination(max_repetitions);
			
			ga.run();
		}else {
			island.run_GA(args, time);
		}
	}
}
