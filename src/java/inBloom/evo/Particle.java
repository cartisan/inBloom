package inBloom.evo;

import jason.JasonException;

public class Particle implements Comparable<Particle>,Individual {
	
	private ChromosomePersonality current_personality;
	private ChromosomeHappenings current_happenings;
	private ChromosomePersonality best_personality;
	private ChromosomeHappenings best_happenings;
	private ChromosomePersonality velocity_personality;
	private ChromosomeHappenings velocity_happenings;
	private double current_tellability;
	private double best_tellability;
	private double spacetime=1;
	private Integer simulation_length;
	private Integer best_simLength;
	
	
	public Particle(ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, Fitness<?,?> fit){
		
		this.velocity_personality = velocity_personality;
		this.velocity_happenings = velocity_happenings;
		this.current_personality = personality;
		this.current_happenings = happenings;
		this.simulation_length = simLength;
		
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

	public void set_spacetime(double time) {
		this.spacetime = time;
	}

	public double get_spacetime() {
		return spacetime;
	}
	
	/*
	 * Velocity Update
	 */
	
	public void update_persVelocity(int x, int y, double update) {
		
		velocity_personality.values[x][y] += update; 
	}
	
	public void update_persVelocity(int x, int y, double update, double factor) {

		velocity_personality.values[x][y] *= 1-factor; 
		velocity_personality.values[x][y] += update; 
	}
	
	public void update_hapVelocity(int x, int y, int update) {
		
		velocity_happenings.values[x][y] += update; 
	}
	
	public void update_hapVelocity(int x, int y, int update, double factor) {

		velocity_happenings.values[x][y] *= 1-factor; 
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
				
				if(current_happenings.values[i][j] < 0)
					current_happenings.values[i][j] = 0;
				
				if(current_happenings.values[i][j] > length)
					length=current_happenings.values[i][j];
			}
		}
		
		// Determine extra length
		Integer buffer = (int)Math.round(Math.random()*Math.sqrt(length));
		
		// Let the simulation run for at least 1 more step than the last happening 
		simulation_length = length+buffer+1;
	}
	
	/*
	 * Start simulation
	 */
	
	public void update_tellability(Fitness<?,?> fit) {
		
		try {
			
			System.out.println("Starting new Simulation: " + simulation_length);
			current_tellability = fit.evaluate_individual(this);
			
		} catch (JasonException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
		
		if(current_tellability >= best_tellability) {
			best_personality = current_personality;
			best_happenings = current_happenings;
			best_tellability = current_tellability;
			best_simLength = simulation_length;
		}
		
		System.out.println("Finish");
	}
	
	/*
	 * compareTo gets called by Arrays.sort()
	 */
	
	@Override
	public int compareTo(Particle other) {
		
		// smaller operator returns array in descending order
		if(best_tellability < other.best_tellability())
			return 1;
		return -1;
	}
}
