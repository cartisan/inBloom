package inBloom.evo;

import jason.JasonException;

public class QuantumPosition extends Individual implements Comparable<QuantumPosition>  {
	
	private ChromosomePersonality current_personality;
	private ChromosomeHappenings current_happenings;
	private ChromosomePersonality velocity_personality;
	private ChromosomeHappenings velocity_happenings;
	private double current_tellability;
	private Integer simulation_length;
	private Integer actual_length;
	
	private double lifespan;


	
	public QuantumPosition(ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, double lifespan, Fitness<?,?> fit){
		
		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;
		this.current_personality = personality;
		this.current_happenings = happenings;
		this.simulation_length = simLength;
		this.actual_length = simulation_length;
		this.lifespan = lifespan;
		
		update_tellability(fit);
		
	}
	
	
	// Get-Methods

	public Integer get_simLength() {
		return simulation_length;
	}

	public void set_actualLength(int length) {
		actual_length = length;
	}

	public int get_actualLength() {
		return actual_length;
	}
	
	public ChromosomePersonality get_personality() {
		return current_personality;
	}
	
	public double get_personality(int x, int y) {
		return current_personality.values[x][y];
	}
	
	public ChromosomePersonality get_persVelocity() {
		return velocity_personality; 
	}
	
	public ChromosomeHappenings get_happenings() {
		return current_happenings;
	}
	
	public int get_happenings(int x, int y) {
		return current_happenings.values[x][y];
	}
	
	public ChromosomeHappenings get_hapVelocity() {
		return velocity_happenings;
	}
	
	public double get_tellability() {
		return current_tellability;
	}
	
	public double get_lifespan() {
		return lifespan;
	}
	
	public void update_lifespan(double span) {
		lifespan += span;
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
	
	public int compareTo(QuantumPosition other) {
		
		// smaller operator returns array in descending order
		if(current_tellability < other.get_tellability())
			return 1;
		return -1;
	}


	@Override
	public String to_String() {
		return this.to_String(current_personality, current_happenings);
	}

}
