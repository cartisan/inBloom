package inBloom.nia.ga;

import jason.JasonException;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;

public class Individual extends CandidateSolution implements Comparable<Individual>{

	private Fitness<?,?> fitness;

	public Individual(ChromosomePersonality personality, ChromosomeHappenings happenings, Integer simLength, Fitness<?,?> fit){
		super(personality, happenings, simLength);
		this.fitness = fit;
	}

	/**
	 * Evaluates the fitness of this individual and safes the numerical result as well as the tellability object
	 * containing the computation details. Removes reference to Fitness object, so it can be collected by GC. #
	 * Operates lazily.
	 */
	public void evaluate() {
		if (this.tellabilityValue == null) {
			try {
				this.tellabilityValue = this.fitness.evaluateSolution(this);
				this.tellability = this.fitness.tellability;
			} catch (JasonException e) {
				//e.printStackTrace();
			} catch (NullPointerException e) {
				//e.printStackTrace();
			}
		}

		this.fitness = null;
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
					if(this.get_tellabilityValue() > others[i].get_tellabilityValue()) {
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
		if(this.get_tellabilityValue() < other.get_tellabilityValue()) {
			return 1;
		}
		return -1;
	}

	@Override
	public String to_String() {
		return this.to_String(this.personality, this.happenings, this.simulation_length.value, this.actual_length);
	}

	/**
	 * Reimplements get_tellabilityValue to be a lazy getter. If tellabilityValue is set, return the value, otherwise
	 * evaluate this individual and return the value then.
	 * @see inBloom.nia.CandidateSolution#get_tellabilityValue()
	 */
	@Override
	public double get_tellabilityValue() {
		if (this.tellabilityValue == null) {
			this.evaluate();
		}
		return this.tellabilityValue;
	}
}
