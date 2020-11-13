package inBloom.evo;

public class MultipleRunsP {
	
	public static void main(String[]args) {
	
		for(int i = 0; i < 4; i++) {
			
			// path and name of file
			String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\Current\\";
			String filename = "PSO " + String.valueOf(i);
			
			// init location
			EvoIsland island = new EvoIsland();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			
			// maximum time in seconds 
			// no time limit: time < 0 or leave it out as default value is -1
			// time == 0 : Only Initialization
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;
					
			PSO<?,?> pso = island.get_PSO(args,init_stepnumber, individual_count);
			
			pso.setFileName(path+filename);
			
			// Decay Rate of the Velocity update function
			pso.setDecayRate(0.1);
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			pso.setPersInit(false, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			pso.setHapInit(true, true, false);
			// randomVelocityInitializer, discreteVelocityInitializer
			pso.setVelInit(true, true);

			// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
			if(i < 2) 
				pso.setVelocityInformants(3);
			else
				pso.setVelocityInformants(7);
				
			// true -> Roulette Wheel, false -> choose best
			if(i%2==0)
				pso.setSelectionManner(false);
			else
				pso.setSelectionManner(true);
			// true activates the floating parameters feature
			pso.setFloatingParameters(false);
			// true activates the spacetime feature
			pso.setSpacetime(false);
			
			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			pso.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			pso.setTermination(max_repetitions);
			
			pso.setExit(false);
			
			pso.run();
		}
		System.exit(0);
	}
}
