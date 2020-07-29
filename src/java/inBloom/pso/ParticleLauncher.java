package inBloom.pso;

public class ParticleLauncher {

	public static void main(String[] args) { 
		
		ParticleIsland island = new ParticleIsland(20);
		int time = 3600;
		int max_repetitions = 50;
		boolean customize = true;
		
		if(customize) {
			
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
		}else {
			island.run_PSO(args, time);
		}
	}
}
