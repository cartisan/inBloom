package inBloom.nia.utils;

import java.util.logging.Level;

import inBloom.nia.NIEnvironment;
import inBloom.nia.qso.QSO;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class MultipleRunsQ {
	public static final int RUN_NUM = 10;
	public static final Class<?> NI_ENV_CLASS = FarmNIEnvironment.class;
	
	public static void main(String[] args) throws Exception { 
		long timestamp = System.currentTimeMillis();
		
		System.out.println("************ Executing " + RUN_NUM + " QSO runs ************");
		for(int i = 0; i < RUN_NUM; i++) {
			System.out.println("************ Starting run " + i + " ************");
			// init location
			NIEnvironment<?,?> niEnvironment = (NIEnvironment<?,?>) NI_ENV_CLASS.newInstance();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 16;

//			int informants = 1;
			int informants = individual_count-1;
			// decay
			double decay_rate = 0.1;
			
			// maximum time in seconds 
			// no time limit: time < 0 or leave it out as default value is -1
			// time == 0 : Only Initialization
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 25;
			
			// path and name of file
			String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
			String filename = "QSO_" + timestamp + "_run" + String.valueOf(i) + ".log";
	
			QSO<?,?> qso = niEnvironment.get_QSO(args,init_stepnumber, individual_count);
			
			qso.setLevel(Level.OFF);
			
			qso.setFileName(path+filename);
			
			qso.setDeterministic(true);
			// Decay Rate of the Velocity update function
			qso.setDecayRate(decay_rate);
			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			qso.setPersInit(false, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			qso.setHapInit(true, true, false);
			// randomVelocityInitializer, discreteVelocityInitializer
			qso.setVelInit(true, true);
			// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
			qso.setVelocityInformants(informants);
			// true -> Gravity Search, false -> choose best
			qso.setSelectionManner(true);
			// true activates the floating parameters feature
			qso.setFloatingParameters(true);
				
			// simpleCrossover,binomialCrossover,xPointCrossover,voteCrossover
			qso.setCrossover(false, true, true, true);
			// randomMutator,toggleMutator,orientedMutator,guidedMutator
			qso.setMutation(true, true, true, true);
			
			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			qso.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			qso.setTermination(max_repetitions);
			
			qso.setExit(false);
			
			qso.run();
			System.out.println("************ Finished run ************");
		}
		System.out.println("************ Finished all QSO runs ************");
		System.exit(0);
	}
}
