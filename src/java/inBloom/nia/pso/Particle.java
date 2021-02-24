package inBloom.nia.pso;

import jason.JasonException;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.ga.Individual;

public class Particle extends CandidateSolution implements Comparable<Particle>{
	private ChromosomePersonality best_personality;
	private ChromosomeHappenings best_happenings;
	private ChromosomePersonality velocity_personality;
	private ChromosomeHappenings velocity_happenings;
	private double best_tellability;
	private Integer best_simLength;
	private Integer best_actualLength;

	public Particle(ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, Fitness<?,?> fit){
		super(personality, happenings, simLength);
		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;

		this.best_tellability = 0;
		this.update_tellability(fit);
	}

	public Particle(Individual candidate, ChromosomePersonality velocity_personality, ChromosomeHappenings velocity_happenings, Fitness<?,?> fit) {
		super(candidate.get_personality(), candidate.get_happenings(), candidate.get_simLength());
		this.actual_length  = candidate.get_actualLength();
		this.tellabilityValue = candidate.get_tellabilityValue();

		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;

		this.best_tellability=0;
		this.update_tellability(fit);
	}


	// Get-Methods
	public Integer best_simLength() {
		return this.best_simLength;
	}

	public ChromosomePersonality best_personality() {
		return this.best_personality;
	}

	public double best_personality(int x, int y) {
		return this.best_personality.values[x][y];
	}

	public ChromosomeHappenings best_happenings() {
		return this.best_happenings;
	}

	public int best_happenings(int x, int y) {
		return this.best_happenings.values[x][y];
	}

	public double best_tellability() {
		return this.best_tellability;
	}

	/*
	 * Velocity Update
	 */

	public void update_persVelocity(int x, int y, double update, double decayRate) {

		this.velocity_personality.values[x][y] *= 1-decayRate;
		this.velocity_personality.values[x][y] += update;
	}

	public void update_hapVelocity(int x, int y, int update, double decayRate) {

		this.velocity_happenings.values[x][y] *= 1-decayRate;
		this.velocity_happenings.values[x][y] += update;
	}

	/*
	 *  Move the particle
	 */

	public void move() {

		Integer length = 0;

		for(int i = 0; i < this.personality.values.length; i++) {

			for(int j = 0; j < this.personality.values[i].length; j++) {

				this.personality.values[i][j] += this.velocity_personality.values[i][j];

				if(Math.abs(this.personality.values[i][j]) > 1) {
					this.personality.values[i][j] /= Math.abs(this.personality.values[i][j]);
				}
			}

			for(int j = 0; j < this.happenings.values[i].length; j++) {

				this.happenings.values[i][j] += this.velocity_happenings.values[i][j];

				//if(happenings.values[i][j] < 0)
					//happenings.values[i][j] = 0;

				if(this.happenings.values[i][j] > length) {
					length=this.happenings.values[i][j];
				}
			}
		}

		// Determine extra length
		Integer buffer = (int)Math.round(Math.sqrt(length));

		// Let the simulation run for at least 1 more step than the last happening
		this.simulation_length = length+buffer+1;
	}

	/**
	 * Start simulation
	 */
	public void update_tellability(Fitness<?,?> fit) {
		try {
			this.tellabilityValue = fit.evaluate_individual(this);

			if(this.tellabilityValue > this.best_tellability) {
				// FIXME: Fixed bug where best_personality would be same instance as personality, and change with every move
				this.best_personality = this.personality.clone();
				this.best_happenings = this.happenings.clone();
				this.best_simLength = this.simulation_length;
				this.best_actualLength = this.actual_length;
				this.best_tellability = this.tellabilityValue;

				this.tellability = fit.tellability;
				this.updateNotes();
			}

		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}

		fit = null;
	}

	/**
	 * compareTo gets called by Arrays.sort()
	 */
	public int compareTo(Particle other) {

		// smaller operator returns array in descending order
		if(this.best_tellability < other.best_tellability()) {
			return 1;
		}
		return -1;
	}

	@Override
	public String to_String() {
		return this.to_String(this.best_personality, this.best_happenings, this.best_simLength, this.best_actualLength);
	}
}
