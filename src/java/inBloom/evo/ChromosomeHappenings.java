package inBloom.evo;

public class ChromosomeHappenings {
	
	/*
	 * values contains Information at what time step a Happening occurs
	 * The first dimension encodes which agent is the patient of a Happening
	 * The second dimension Encodes which happening takes place
	 * The Integer at a certain position reflects the time step a Happening shall take place
	 * If the number is negative or zero (work in progress) the Happening will not take place
	 */
	public int[][] values;
	
	public ChromosomeHappenings(int agentCount, int happeningCount) {
		
		values = new int[agentCount][happeningCount];
		
	}
	
	/*
	 * Equals(ChromosomeHappenings other) compares similarity of personality values
	 * Has a tolerance window in order to improve genetic diversity.
	 * @param:
	 * 		other = Another candidates happening Chromosome. 
	 * @return:
	 * 		boolean: True if Values are the same within a window of tolerance. False otherwise
	 */
	public boolean equals(ChromosomeHappenings other, int length) {

		for(int i = 0;i< values.length;i++) {
			for(int j = 0; j< values[i].length;j++) {
				
				// If both Happenings are instantiated
				if(this.values[i][j] > 0 && this.values[i][j] <= length && other.values[i][j] > 0 && other.values[i][j] <= length) {
					if(this.values[i][j] != other.values[i][j])
						return false;
					
				// Determine if both happenings have the same activation state
				}else if(this.values[i][j] != other.values[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	
}
