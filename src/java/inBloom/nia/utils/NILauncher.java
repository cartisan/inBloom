package inBloom.nia.utils;

import java.util.logging.Level;

import inBloom.nia.NIEnvironment;
import inBloom.nia.ga.GeneticAlgorithm;
import inBloom.nia.pso.PSO;
import inBloom.nia.qso.QSO;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class NILauncher {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		// init location
		NIEnvironment<?,?> niEnvironment = new FarmNIEnvironment();
//		NIEnvironment<?,?> niEnvironment = new IslandNIEnvironment();
		// simulation length at initialization
		int init_stepnumber = 30;
		// number individuals
		int individual_count = 10;
		// selection size
		int selection_size = 4;
		// decay
		double decay_rate = 0.05;
		// crossover probability
		double crossover_prob = 0.1;
		// mutation probability
		double mutation_prob = 0.05;

		// maximum time in seconds
		// no time limit: time < 0 or leave it out as default value is -1
		// time == 0 : Only Initialization
		int time = 3600;
		// number of iterations without improvement till shutdown
		int max_repetitions = 10;

		// path and name of file
		String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
		String filename = "_run.log";

		/**
		 * Choose a Mode:
		 */

		String algorithm = "GEN";
//		String algorithm = "PSO";
//		String algorithm = "Coupled";
//		String algorithm = "QSO";

		//GeneticAlgorithm<?,?> ga = island.get_GA(args,init_stepnumber,individual_count,selection_size,crossover_prob,mutation_prob);
		GeneticAlgorithm<?,?> ga = niEnvironment.get_GA(args,init_stepnumber,individual_count,selection_size,decay_rate);

		switch(algorithm) {
			case "Coupled":
			case "GEN":
				ga.setLevel(Level.OFF);
	
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
	
				if(algorithm=="Evolutionary") {
					break;
				}
	
			case "PSO":
				PSO<?,?> pso = niEnvironment.get_PSO(args,init_stepnumber, individual_count);
	
				pso.setLevel(Level.OFF);
	
				pso.setFileName(path+"PSO"+filename);
	
				if(algorithm=="Coupled") {
	
					pso.setGenInit(10, ga.get_genPool());
				}
	
				pso.setDeterministic(true);
				// Decay Rate of the Velocity update function
				pso.setDecayRate(0.05);
				// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
				pso.setPersInit(false, true, true);
				// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
				pso.setHapInit(true, true, false);
				// randomVelocityInitializer, discreteVelocityInitializer
				pso.setVelInit(true, true);
				// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
				pso.setVelocityInformants(1);
				// true -> Roulette Wheel, false -> choose best
				pso.setSelectionManner(false);
				// true activates the floating parameters feature
				pso.setFloatingParameters(true);
	
				// Termination Criteria
				// Runtime in seconds (-1 to deactivate)
				pso.setMaxRuntime(time);
				// Number of times the main loop is repeated without adding a new (relevant) candidate to gen_pool
				pso.setTermination(max_repetitions);
	
				pso.run();
	
				break;
	
			case "QSO":
				QSO<?,?> qso = niEnvironment.get_QSO(args,init_stepnumber, individual_count);
	
				qso.setLevel(Level.OFF);
	
				qso.setFileName(path+"QSO"+filename);
	
				// Decay Rate of the Velocity update function
				qso.setDecayRate(0.05);
				// randomPersonalityInitializer, discretePersonalityInitializer, steadydiscretePersonalityInitializer
				qso.setPersInit(false, true, true);
				// randomHappeningsInitializer, probabilisticHappeningsInitializer, steadyHappeningsInitializer
				qso.setHapInit(true, true, false);
				// randomVelocityInitializer, discreteVelocityInitializer
				qso.setVelInit(true, true);
				// The number of informants (other particles) a particle makes use of additionally to itself to update its velocity
				qso.setVelocityInformants(1);
				// true -> Roulette Wheel, false -> choose best
				qso.setSelectionManner(false);
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
	
				qso.run();
	
				break;
		}
	}
}
