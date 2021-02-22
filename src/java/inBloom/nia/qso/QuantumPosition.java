package inBloom.nia.qso;

import jason.JasonException;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;

public class QuantumPosition extends CandidateSolution implements Comparable<QuantumPosition>  {
	private ChromosomePersonality velocity_personality;
	private ChromosomeHappenings velocity_happenings;
	
	private double lifespan;

	
	public QuantumPosition(ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, double lifespan, Fitness<?,?> fit){
		super(personality, happenings, simLength);
		
		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;
		this.lifespan = lifespan;
		
		update_tellability(fit);
	}
	
	
	// Get-Methods
	public ChromosomePersonality get_persVelocity() {
		return velocity_personality; 
	}
	
	public ChromosomeHappenings get_hapVelocity() {
		return velocity_happenings;
	}
	
	public double get_lifespan() {
		return lifespan;
	}
	
	public void update_lifespan(double span) {
		lifespan += span;
	}
	
	/**
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
	
	/**
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
			tellability = fit.evaluate_individual(this);
			this.updateNotes(fit);
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
	public int compareTo(QuantumPosition other) {
		
		// smaller operator returns array in descending order
		if(tellability < other.get_tellability())
			return 1;
		return -1;
	}


	@Override
	public String to_String() {
		return this.to_String(personality, happenings, simulation_length, actual_length);
	}

}
