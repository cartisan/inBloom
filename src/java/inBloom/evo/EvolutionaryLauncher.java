package inBloom.evo;

import inBloom.stories.little_red_hen.FarmEvoEnvironment;

public class EvolutionaryLauncher {

	public static void main(String[] args) {

		// init location
		FarmEvoEnvironment evolutionaryEnvironment = new FarmEvoEnvironment();
		// simulation length at initialization
		int init_stepnumber = 30;
		// number individuals
		int individual_count = 20;
//		int individual_count = 2;
		// selection size
		int selection_size = 4;
//		int selection_size = 2;
		// crossover probability
		double crossover_prob = 0.025;
		// mutation probability
		double mutation_prob = 1.0;

		// maximum time in seconds
		// no time limit: time < 0 or leave it out as default value is -1
		// time == 0 : Only Initialization
		int time = 1800;
		// number of iterations without improvement till shutdown
		int max_repetitions = 25;

		// path and name of file
		String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
		String filename = "_run.log";

		/**
		 * Choose a Mode:
		 */

		String algorithm = "GEN";
//		String algorithm = "PSO";
//		String algorithm = "Coupled";

		GeneticAlgorithm<?,?> ga = evolutionaryEnvironment.get_GA(args, init_stepnumber, individual_count, selection_size, crossover_prob, mutation_prob, true);

		switch(algorithm) {
			case "Coupled":
			case "GEN":

				ga.setFileName(path+"GEN"+filename);

				if(algorithm=="Coupled") {

					ga.setExit(false);
				}

				// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
				ga.setPersInit(false, true, true);
				// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
				ga.setHapInit(true, true, false);
				// randomSelector, rouletteWheelSelector
				ga.setSelection(false,true);
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

				// TODO: @Felix what is this?
				if(algorithm=="Evolutionary") {
					break;
				}

			case "PSO":

				PSO<?,?> pso = evolutionaryEnvironment.get_PSO(args,init_stepnumber, individual_count);

				pso.setFileName(path+"PSO"+filename);

				if(algorithm=="Coupled") {

					pso.setGenInit(10, ga.get_genPool());
				}

				// Decay Rate of the Velocity update function
				pso.setDecayRate(0.5);
				// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
				pso.setPersInit(false, true, true);
				// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
				pso.setHapInit(true, true, false);
				// randomVelocityInitializer, discreteVelocityInitializer
				pso.setVelInit(true, true);
				// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
				pso.setVelocityInformants(7);
				// true -> Roulette Wheel, false -> choose best
				pso.setSelectionManner(false);
				// true activates the floating parameters feature
				pso.setFloatingParameters(false);
				// true activates the spacetime feature
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
