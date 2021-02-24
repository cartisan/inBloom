package inBloom.nia.ga;

import jason.JasonException;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;

public class Individual extends CandidateSolution implements Comparable<Individual>{

	public Individual(ChromosomePersonality personality, ChromosomeHappenings happenings, Integer simLength, Fitness<?,?> fit){
		super(personality, happenings, simLength);

		try {
			this.tellabilityValue = fit.evaluate_individual(this);
			this.tellability = fit.tellability;
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}

	}

	/**
	 * Equals(Individual other) tests similarity to another Individual
	 * @param:
	 * 		other = Another candidate this gets compared with
	 * @return:
	 * 		boolean: true if similar. false if not
	 */
	public boolean equals(Individual other) {

		if(!this.personality.equals(other.personality)) {
			return false;
		}
		if(!this.happenings.equals(other.happenings,this.actual_length)) {
			return false;
		}

		return true;
	}

	/**
	 * Works similar to the previous equals but does not require Individual to be instantiated to compare.
	 * This saves runtime since we do not need to run a simulation in order to determine the tellabilityValue.
	 */
	public boolean equals(ChromosomePersonality other_personality, ChromosomeHappenings other_happenings) {
		if(!this.personality.equals(other_personality)) {
			return false;
		}
		if(!this.happenings.equals(other_happenings,this.actual_length)) {
			return false;
		}
		return true;
	}

	/*
	 * isContainedIn checks if equals is true for an array of candidates
	 */

	public boolean isContainedIn(Individual[] others) {

		for(int i = others.length-1; i >= 0; i--) {
			if(others[i]!=null) {
				if(this.equals(others[i])) {
					if(this.tellabilityValue>others[i].get_tellabilityValue()) {
						others[i] = this;
					}
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
	public int compareTo(Individual other) {

		// smaller operator returns array in descending order
		if(this.tellabilityValue < other.get_tellabilityValue()) {
			return 1;
		}
		return -1;
	}

	@Override
	public String to_String() {
		return this.to_String(this.personality, this.happenings, this.simulation_length, this.actual_length);
	}
}
