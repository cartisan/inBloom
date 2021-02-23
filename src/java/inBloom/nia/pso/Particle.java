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
		
		best_tellability = 0;
		update_tellability(fit);
	}
	
	public Particle(Individual candidate, ChromosomePersonality velocity_personality, ChromosomeHappenings velocity_happenings, Fitness<?,?> fit) {
		super(candidate.get_personality(), candidate.get_happenings(), candidate.get_simLength());
		this.actual_length  = candidate.get_actualLength();
		this.tellabilityValue = candidate.get_tellabilityValue();

		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;

		best_tellability=0;
		update_tellability(fit);
	}
	
	
	// Get-Methods
	public Integer best_simLength() {
		return best_simLength;
	}
	
	public ChromosomePersonality best_personality() {
		return best_personality;
	}
	
	public double best_personality(int x, int y) {
		return best_personality.values[x][y];
	}
	
	public ChromosomeHappenings best_happenings() {
		return best_happenings;
	}
	
	public int best_happenings(int x, int y) {
		return best_happenings.values[x][y];
	}
	
	public double best_tellability() {
		return best_tellability;
	}
	
	/*
	 * Velocity Update
	 */
	
	public void update_persVelocity(int x, int y, double update, double decayRate) {

		velocity_personality.values[x][y] *= 1-decayRate; 
		velocity_personality.values[x][y] += update; 
	}
	
	public void update_hapVelocity(int x, int y, int update, double decayRate) {

		velocity_happenings.values[x][y] *= 1-decayRate; 
		velocity_happenings.values[x][y] += update; 
	}
	
	/*
	 *  Move the particle
	 */
	
	public void move() {
		
		Integer length = 0;
		
		for(int i = 0; i < personality.values.length; i++) {
			
			for(int j = 0; j < personality.values[i].length; j++) {
				
				personality.values[i][j] += velocity_personality.values[i][j];
				
				if(Math.abs(personality.values[i][j]) > 1)
					personality.values[i][j] /= Math.abs(personality.values[i][j]);
			}
			
			for(int j = 0; j < happenings.values[i].length; j++) {
				
				happenings.values[i][j] += velocity_happenings.values[i][j];
				
				//if(happenings.values[i][j] < 0)
					//happenings.values[i][j] = 0;
				
				if(happenings.values[i][j] > length)
					length=happenings.values[i][j];
			}
		}
		
		// Determine extra length
		Integer buffer = (int)Math.round(Math.sqrt(length));
		
		// Let the simulation run for at least 1 more step than the last happening 
		simulation_length = length+buffer+1;
	}
	
	/**
	 * Start simulation
	 */
	public void update_tellability(Fitness<?,?> fit) {
		try {
			tellabilityValue = fit.evaluate_individual(this);

			if(tellabilityValue > best_tellability) {
				// FIXME: Fixed bug where best_personality would be same instance as personality, and change with every move
				this.best_personality = personality.clone();
				this.best_happenings = happenings.clone();
				this.best_simLength = simulation_length;
				this.best_actualLength = actual_length; 
				this.best_tellability = tellabilityValue;
				
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
		if(best_tellability < other.best_tellability())
			return 1;
		return -1;
	}
	
	@Override
	public String to_String() {
		return this.to_String(this.best_personality, this.best_happenings, this.best_simLength, this.best_actualLength);
	}
}
