/**
 * 
 */
package inBloom.rl_happening.archive;

/**
 * Should run 100 or similar versions of the RL algorithm
 * 
 * @author juwi
 *
 */
public class RLTrainer {

	private static final int numberOfTrainings = 10;
	private static final int numberOfEpisodes = 100;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for(int i=1; i<=numberOfTrainings; i++) {
			String fileName = generateFileName(i);
			
			// TODO hand over the #episodes
			String[] robinsonArgs = {fileName};
			
			RobinsonCycleMultipleTrainings.main(robinsonArgs);
		}

	}
	
	
	private static String generateFileName(int training) {
		String fileName = "episodes" + training + ".csv";
		return fileName;
	}

}
