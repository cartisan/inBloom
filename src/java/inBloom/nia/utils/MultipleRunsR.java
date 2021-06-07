package inBloom.nia.utils;

import inBloom.nia.NIEnvironment;
import inBloom.nia.random.RandomSearch;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class MultipleRunsR {
	public static final int RUN_NUM = 10;
	public static final Class<?> NI_ENV_CLASS = FarmNIEnvironment.class;

	public static void main(String[] args) throws Exception {
		long timestamp = System.currentTimeMillis();

		System.out.println("************ Executing " + RUN_NUM + " RAN runs ************");
		for(int i = 0; i < RUN_NUM; i+=1) {
			System.out.println("************ Starting run " + i + " ************");
			// init location
			NIEnvironment<?,?> niEnvironment = (NIEnvironment<?,?>) NI_ENV_CLASS.newInstance();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// maximum time in seconds (no time limit: time < 0 or leave it out as default value is -1)
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 10;

			// path and name of file
			String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
			String filename = "RAN_" + timestamp + "_run" + String.valueOf(i) + ".log";

			RandomSearch<?,?> ran =niEnvironment.get_RAN(args, init_stepnumber, individual_count);

			ran.setFileName(path+filename);
			ran.setExit(false);

			// Termination Criteria
			// Runtime in seconds (-1 to deactivate)
			ran.setMaxRuntime(time);
			// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
			ran.setTermination(max_repetitions);

			ran.run();

			ran = null;
			System.out.println("************ Finished run ************");
		}
		System.out.println("************ Finished all RAN runs ************");
		System.exit(0);
	}
}
