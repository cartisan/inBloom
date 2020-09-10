package inBloom.evo;

public class ChromosomeHappeningsFloat extends ChromosomeHappenings {
	
	/*
	 * values contains Information at what time step a Happening occurs
	 * The first dimension encodes which agent is the patient of a Happening
	 * The second dimension Encodes which happening takes place
	 * The Integer at a certain position reflects the time step a Happening shall take place
	 * If the number is negative or zero (work in progress) the Happening will not take place
	 */
	public float[][] values;
	
	public ChromosomeHappeningsFloat(int agentCount, int happeningCount) {
		
		super(agentCount, happeningCount);
		values = new float[agentCount][happeningCount];
		
	}
	
	/*
	 * Equals(ChromosomeHappeningsFloat other) compares similarity of personality values
	 * Has a tolerance window in order to improve genetic diversity.
	 * @param:
	 * 		other = Another candidates happening Chromosome. 
	 * @return:
	 * 		boolean: True if Values are the same within a window of tolerance. False otherwise
	 */
	public boolean equals(ChromosomeHappeningsFloat other) {

		for(int i = 0;i< other.values.length;i++) {
			for(int j = 0; j<other.values[i].length;j++) {
				
				// If both Happenings are instantiated take tolerance into account
				if(this.values[i][j] > 0 && other.values[i][j] > 0) {
					if(Math.abs(this.values[i][j] - other.values[i][j])>1)
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
