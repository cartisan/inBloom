package inBloom.evo;

import java.util.logging.Level;

public class MultipleRunsQ {
	
	public static void main(String[] args) { 
		
		
		for(int i = 0; i < 4; i++) {
			// init location
			EvoIsland island = new EvoIsland();
			// simulation length at initialization
			int init_stepnumber = 30;
			// number individuals
			int individual_count = 20;
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
			int max_repetitions = 25;
			
			// path and name of file
			String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\Current\\";
			String filename = "QSO: " + String.valueOf(i);
	
			QSO<?,?> qso = island.get_QSO(args,init_stepnumber, individual_count);
			
			qso.setLevel(Level.OFF);
			
			qso.setFileName(path+filename);
	
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
			qso.setFloatingParameters(false);
			// true -> using spacetime modifier
			qso.setSpacetime(false);
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
		}
		System.exit(0);
	}
}
