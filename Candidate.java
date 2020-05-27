package inBloom.genetic;

import jason.JasonException;

public class Candidate implements Comparable<Candidate>{
	
	private ChromosomePersonality chromosome_personality;
	private ChromosomeHappenings chromosome_happenings;
	private double tellability;
	
	public Candidate(ChromosomePersonality personality, ChromosomeHappenings happenings, Fitness<?,?> fit){
		
		this.chromosome_personality = personality;
		this.chromosome_happenings = happenings;
		
		try {
			tellability = fit.evaluate_Candidate(this);
		} catch (JasonException e) {
			e.printStackTrace();
		}
	}
	
	// Get-Methods
	
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
		return this.chromosome_personality.equals(other.chromosome_personality) && this.chromosome_happenings.equals(other.chromosome_happenings);
	}
	
	/*
	 * Works similar to the previous equals but does not require Candidate to be instantiated to compare.
	 * This saves runtime since we do not need to run a simulation in order to determine the tellability.
	 */
	
	public boolean equals(ChromosomePersonality other_personality, ChromosomeHappenings other_happenings) {
		return this.chromosome_personality.equals(other_personality) && this.chromosome_happenings.equals(other_happenings);
	}
	
	/*
	 * isContainedIn checks if equals is true for an array of candidates
	 */
	
	public boolean isContainedIn(Candidate[] others) {
		
		boolean contained = false;
		
		for(int i = 0; i < others.length; i++) {
			if(others[i]!=null) {
				if(this.equals(others[i])) {
					contained = true;
				}
			}
		}
		return contained;
	}
	
	
	/*
	 * compareTo gets called by Arrays.sort()
	 */
	
	@Override
	public int compareTo(Candidate other) {
		
		// Redundant since we do not have any duplicates.
		//if(equals(other))
		//	return 0;
		
		// smaller operator returns array in descending order
		if(tellability < other.get_tellability())
			return 1;
		return -1;
	}
}
