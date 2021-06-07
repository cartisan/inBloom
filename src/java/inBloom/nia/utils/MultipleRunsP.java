package inBloom.nia.utils;

import inBloom.nia.NIEnvironment;
import inBloom.nia.pso.PSO;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class MultipleRunsP {
	public static final int RUN_NUM = 10;
	public static final Class<?> NI_ENV_CLASS = FarmNIEnvironment.class;
	
	public static void main(String[]args) throws Exception {
		long timestamp = System.currentTimeMillis();
		
		System.out.println("************ Executing " + RUN_NUM + " PSO runs ************");
		for(int i = 0; i < RUN_NUM; i++) {
			// init location
			NIEnvironment<?,?> niEnvironment = (NIEnvironment<?,?>) NI_ENV_CLASS.newInstance();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// 
			double decay_rate = 0.1;
			//
			int number_informants = 1;
			
			// maximum time in seconds 
			// no time limit: time < 0 or leave it out as default value is -1
			// time == 0 : Only Initialization
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;

			// path and name of file
			String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
			String filename = "PSO_" + timestamp + "_run" + String.valueOf(i) + ".log";
					
			PSO<?,?> pso = niEnvironment.get_PSO(args,init_stepnumber, individual_count);
			
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
			System.out.println("************ Finished run ************");
		}
		System.out.println("************ Finished all PSO runs ************");
		System.exit(0);
	}
}
