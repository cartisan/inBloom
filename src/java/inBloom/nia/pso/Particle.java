package inBloom.nia.pso;

import jason.JasonException;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.ga.Individual;

public class Particle extends CandidateSolution implements Comparable<Particle>{
	protected Integer simulation_length;
	protected Integer actual_length;
	private ChromosomePersonality current_personality;
	private ChromosomeHappenings current_happenings;
	private ChromosomePersonality best_personality;
	private ChromosomeHappenings best_happenings;
	private ChromosomePersonality velocity_personality;
	private ChromosomeHappenings velocity_happenings;
	private double current_tellability;
	private double best_tellability;
	private Integer best_simLength;
	private Integer best_actualLength;
	
	
	public Particle(ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, Fitness<?,?> fit){
		
		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;
		this.current_personality = personality;
		this.current_happenings = happenings;
		this.simulation_length = simLength;
		this.actual_length = simulation_length;
		
		best_tellability=0;
		update_tellability(fit);
		
	}
	
	public Particle(Individual candidate, ChromosomePersonality velocity_personality, ChromosomeHappenings velocity_happenings, Fitness<?,?> fit) {
		
		this.current_personality = candidate.get_personality();
		this.current_happenings = candidate.get_happenings();
		this.simulation_length = candidate.get_simLength();
		this.current_tellability = candidate.get_tellability();

		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;

		best_tellability=0;
		update_tellability(fit);
	}
	
	
	// Get-Methods
	
	public Integer get_simLength() {
		return simulation_length;
	}
	
	public Integer best_simLength() {
		return best_simLength;
	}

	public void set_actualLength(int length) {
		actual_length = length;
	}
	
	public ChromosomePersonality get_personality() {
		return current_personality;
	}
	
	public double get_personality(int x, int y) {
		return current_personality.values[x][y];
	}
	
	public ChromosomePersonality best_personality() {
		return best_personality;
	}
	
	public double best_personality(int x, int y) {
		return best_personality.values[x][y];
	}
	
	public ChromosomeHappenings get_happenings() {
		return current_happenings;
	}
	
	public int get_happenings(int x, int y) {
		return current_happenings.values[x][y];
	}
	
	public ChromosomeHappenings best_happenings() {
		return best_happenings;
	}
	
	public int best_happenings(int x, int y) {
		return best_happenings.values[x][y];
	}
	
	public double get_tellability() {
		return current_tellability;
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
		
		for(int i = 0; i < current_personality.values.length; i++) {
			
			for(int j = 0; j < current_personality.values[i].length; j++) {
				
				current_personality.values[i][j] += velocity_personality.values[i][j];
				
				if(Math.abs(current_personality.values[i][j]) > 1)
					current_personality.values[i][j] /= Math.abs(current_personality.values[i][j]);
			}
			
			for(int j = 0; j < current_happenings.values[i].length; j++) {
				
				current_happenings.values[i][j] += velocity_happenings.values[i][j];
				
				//if(current_happenings.values[i][j] < 0)
					//current_happenings.values[i][j] = 0;
				
				if(current_happenings.values[i][j] > length)
					length=current_happenings.values[i][j];
			}
		}
		
		// Determine extra length
		Integer buffer = (int)Math.round(Math.sqrt(length));
		
		// Let the simulation run for at least 1 more step than the last happening 
		simulation_length = length+buffer+1;
	}
	
	/*
	 * Start simulation
	 */
	
	public void update_tellability(Fitness<?,?> fit) {
		
		try {
			
			current_tellability = fit.evaluate_individual(this);

			if(current_tellability >= best_tellability) {
				best_personality = current_personality;
				best_happenings = current_happenings;
				best_simLength = simulation_length;
				best_actualLength = actual_length; 
				best_tellability = current_tellability;
			}
			
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
		
		fit = null;
	}
	
	/*
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
		return this.to_String(best_personality, best_happenings, best_simLength, best_actualLength);
	}
}
