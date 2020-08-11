package inBloom.evo;

import jason.JasonException;

public class Candidate implements Comparable<Candidate>, Individual{
	
	private ChromosomePersonality chromosome_personality;
	private ChromosomeHappenings chromosome_happenings;
	private double tellability;
	private Integer simulation_length;
	
	public Candidate(ChromosomePersonality personality, ChromosomeHappenings happenings, Integer simLength, Fitness<?,?> fit){
		
		this.chromosome_personality = personality;
		this.chromosome_happenings = happenings;
		this.simulation_length = simLength;
		
		try {
			tellability = fit.evaluate_individual(this);
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}

		System.out.println("Finish");
	}
	
	// Get-Methods
	
	public Integer get_simLength() {
		return simulation_length;
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
		if(!this.chromosome_happenings.equals(other.chromosome_happenings))
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
		if(!this.chromosome_happenings.equals(other_happenings))
			return false;
		return true;
	}
	
	/*
	 * isContainedIn checks if equals is true for an array of candidates
	 */
	
	public boolean isContainedIn(Candidate[] others) {
		
		for(int i = 0; i < others.length; i++) {
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
}
