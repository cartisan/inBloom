package inBloom.evo;

import jason.JasonException;

public class Candidate implements Individual,Comparable<Candidate>{
	
	private ChromosomePersonality chromosome_personality;
	private ChromosomeHappenings chromosome_happenings;
	private double tellability;
	private Integer simulation_length;
	private Integer actual_length;
	
	public Candidate(ChromosomePersonality personality, ChromosomeHappenings happenings, Integer simLength, Fitness<?,?> fit){
		
		this.chromosome_personality = personality;
		this.chromosome_happenings = happenings;
		this.simulation_length = simLength;
		this.actual_length = simulation_length;
		
		try {
			tellability = fit.evaluate_individual(this);
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
	}
	
	// Get-Methods
	
	public Integer get_simLength() {
		return simulation_length;
	}
	
	public void set_actualLength(int length) {
		actual_length = length;
	}
	
	public ChromosomePersonality get_personality() {
		return chromosome_personality;
	}
	
	public double get_personality(int x, int y) {
		return chromosome_personality.values[x][y];
	}
	
	public ChromosomeHappenings get_happenings() {
		return chromosome_happenings;
	}
	
	public int get_happenings(int x, int y) {
		return chromosome_happenings.values[x][y];
	}
	
	public double get_tellability() {
		return tellability;
	}
	
	/*
	 * Equals(Candidate other) tests similarity to another Candidate
	 * @param:
	 * 		other = Another candidate this gets compared with
	 * @return:
	 * 		boolean: true if similar. false if not
	 */
	
	public boolean equals(Candidate other) {

		if(!this.chromosome_personality.equals(other.chromosome_personality))
			return false;
		if(!this.chromosome_happenings.equals(other.chromosome_happenings,actual_length))
			return false;
		
		return true;
	}
	
	/*
	 * Works similar to the previous equals but does not require Candidate to be instantiated to compare.
	 * This saves runtime since we do not need to run a simulation in order to determine the tellability.
	 */
	
	public boolean equals(ChromosomePersonality other_personality, ChromosomeHappenings other_happenings) {
		
		if(!this.chromosome_personality.equals(other_personality))
			return false;
		if(!this.chromosome_happenings.equals(other_happenings,actual_length))
			return false;
		return true;
	}
	
	/*
	 * isContainedIn checks if equals is true for an array of candidates
	 */
	
	public boolean isContainedIn(Candidate[] others) {
		
		for(int i = others.length-1; i >= 0; i--) {
			if(others[i]!=null) {
				if(this.equals(others[i])) {
					if(tellability>others[i].get_tellability())
						others[i] = this;
					return true;
				}
			}
		}
		return false;
	}
	
	
	/*
	 * compareTo gets called by Arrays.sort()
	 */
	
	@Override
	public int compareTo(Candidate other) {
		
		// smaller operator returns array in descending order
		if(tellability < other.get_tellability())
			return 1;
		return -1;
	}
	
	/*
	 * to_String converts all information of a candidate into a string.
	 * The result will be used to save a candidate into a file.
	 */
	
	public String to_String() {
		
		int number_agents = chromosome_personality.values.length;
		int number_happenings = chromosome_happenings.values[0].length;
		
		String string = String.valueOf(number_agents) + "\n" + String.valueOf(number_happenings) + "\n" + String.valueOf(simulation_length) + "\n" +  String.valueOf(actual_length) + "\n";
		
		
		for(int i = 0; i < number_agents; i++) {
			for(int j = 0; j < 5; j++) {
				string += String.valueOf(chromosome_personality.values[i][j]);
				if(j<4)
					string +=  " ";
			}
			string += "\n";
		}
		
		for(int i = 0; i < number_agents; i++) {	
			for(int j = 0; j < number_happenings; j++) {
				string += String.valueOf(chromosome_happenings.values[i][j]);	
				if(j<number_happenings-1)
					string +=  " ";	
			}
			string += "\n";
		}
		
		return string;
	}
}
