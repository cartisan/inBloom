package inBloom.nia.utils;

import inBloom.nia.NIEnvironment;
import inBloom.nia.ga.GeneticAlgorithm;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class MultipleRunsG {
	public static final int RUN_NUM = 10;
	public static final Class<?> NI_ENV_CLASS = FarmNIEnvironment.class;

	public static void main(String[] args) throws Exception {
		long timestamp = System.currentTimeMillis();

		System.out.println("************ Executing " + RUN_NUM + " GA runs ************");
		for(int i = 0; i < RUN_NUM; i+=1) {
			System.out.println("************ Starting run " + i + " ************");
			// init location
			NIEnvironment<?,?> niEnvironment = (NIEnvironment<?,?>) NI_ENV_CLASS.newInstance();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
			// selection size
			int selection_size = 4;
			// maximum time in seconds (no time limit: time < 0 or leave it out as default value is -1)
			int time = 3600;
			// number of iterations without improvement till shutdown
			int max_repetitions = 10;

			// path and name of file
			String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
			String filename = "GENstatic_indc20_sels4_time3600_maxrep5" + timestamp + "_run" + String.valueOf(i) + ".log";

			GeneticAlgorithm<?,?> ga = niEnvironment.get_GA(args,init_stepnumber,individual_count,selection_size);

			ga.setFileName(path+filename);
			ga.setExit(false);

			// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
			ga.setPersInit(true, true, true);
			// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
			ga.setHapInit(true, true, true);

			// randomSelector, steadySelector
			ga.setSelection(true,true);

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

			ga = null;
			System.out.println("************ Finished run ************");
		}
		System.out.println("************ Finished all GA runs ************");
		System.exit(0);
	}
}
