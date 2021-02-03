package inBloom.evo;

public class MultipleRunsP {
	
	public static void main(String[]args) {
	
		for(int i = 8; i < 10; i++) {
			
			// path and name of file
			String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\Current\\";
			String filename = "PSO " + String.valueOf(i);
			
			// init location
			NIRobinsonIsland island = new NIRobinsonIsland();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// 
			double decay_rate = 0.1;
			//
//			int number_informants = individual_count-1;
			int number_informants = 1;
			
			// maximum time in seconds 
			// no time limit: time < 0 or leave it out as default value is -1
			// time == 0 : Only Initialization
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;
					
			PSO<?,?> pso = island.get_PSO(args,init_stepnumber, individual_count);
			
			pso.setFileName(path+filename);
			
			pso.setDeterministic(true);
			// Decay Rate of the Velocity update function
			pso.setDecayRate(decay_rate);
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			pso.setPersInit(false, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			pso.setHapInit(true, true, false);
			// randomVelocityInitializer, discreteVelocityInitializer
			pso.setVelInit(true, true);
			// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
			pso.setVelocityInformants(number_informants);

			// true -> gravity, false -> choose best
			pso.setSelectionManner(true);
			// true activates the floating parameters feature
			pso.setFloatingParameters(true);
			
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
