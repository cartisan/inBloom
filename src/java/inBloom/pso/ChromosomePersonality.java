package inBloom.pso;

public class ChromosomePersonality {
	
	/*
	 * Values contains the personality parameters in a range between [-1,1]
	 * The first dimension of values encodes, to which person the traits belong
	 * The second dimension contains the 5 parameters in order to construct a personality
	 */
	public double[][] values;
	
	public ChromosomePersonality(int AgentCount){
		
		this.values = new double[AgentCount][5];
		
	}
	
	/*
	 * Equals(ChromosomePersonality other) compares similarity of personality values
	 * @param:
	 * 		other = Another candidates personality Chromosome. 
	 * @return:
	 * 		boolean: True if Values are roughly the same (no difference greater than 0.05). False otherwise
	 */
	public boolean equals(ChromosomePersonality other) {
		boolean equality = true;
		
		for(int i = 0;i< other.values.length;i++) {
			for(int j = 0; j<other.values[i].length;j++) {
				if(this.values[i][j] - other.values[i][j] > 0.05) {
					equality = false;
				}
			}
		}
		
		return equality;
	}

}
