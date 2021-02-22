package inBloom.nia.qso;

import java.util.ArrayList;
import java.util.List;

import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;

public class Quantum extends CandidateSolution implements Comparable<Quantum> {
	private ChromosomePersonality best_personality;
	private ChromosomeHappenings best_happenings;
	private double best_tellability;
	private Integer best_simLength;
	private Integer best_actualLength;
	
	private List<QuantumPosition> positions = new ArrayList<QuantumPosition>();
	
	private double threshold;

	
	public Quantum(int individual_count, ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, Fitness<?,?> fit){
		this.best_personality = personality;
		this.best_happenings = happenings;
		this.best_simLength = simLength;
		this.threshold = Math.pow(0.5, individual_count/2-1);
		
		QuantumPosition pos = new QuantumPosition(personality, velocity_personality, happenings, velocity_happenings, simLength, 1, fit);
		
		this.best_actualLength = pos.get_actualLength();
		this.best_tellability = pos.get_tellability();
		
		this.positions.add(pos);
	}
	
	public boolean superPosition() {
		return positions.size()>0;
	}
	
	public int amount_positions() {
		return positions.size();
	}

	public ChromosomePersonality get_personality() {
		return get_personality(choosePosition());
	}

	public ChromosomePersonality get_personality(int z) {
		return positions.get(z).get_personality();
	}

	public double get_personality(int x, int y) {
		return get_personality(x,y,choosePosition());
	}

	public double get_personality(int x, int y, int state) {
		return positions.get(state).get_personality(x, y);
	}

	public ChromosomeHappenings get_happenings() {
		return get_happenings(choosePosition());
	}

	public ChromosomeHappenings get_happenings(int z) {
		return positions.get(z).get_happenings();
	}

	public int get_happenings(int x, int y) {
		return get_happenings(x,y,choosePosition());
	}

	public int get_happenings(int x, int y, int z) {
		return positions.get(z).get_happenings().values[x][y];
	}

	public Integer get_simLength() {
		return get_simLength(choosePosition());
	}

	public Integer get_simLength(int z) {
		return positions.get(z).get_simLength();
	}
	

	public ChromosomePersonality best_personality() {
		return best_personality;
	}

	public ChromosomeHappenings best_happenings() {
		return best_happenings;
	}

	public double best_personality(int x, int y) {
		return best_personality.values[x][y];
	}

	public int best_happenings(int x, int y) {
		return best_happenings.values[x][y];
	}

	public Integer best_simLength() {
		return best_simLength;
	}

	public void set_actualLength(int length) {
		best_actualLength = length;
	}

	public int best_actualLength() {
		return best_actualLength;
	}
	
	public double best_tellability() {
		return best_tellability;
	}

	public double get_tellability() {
		return get_tellability(choosePosition());
	}
	
	public double get_tellability(int z) {
		return positions.get(z).get_tellability();
	}

	public int choosePosition() {
		
		int state = 0;
		
		if(superPosition()) { 
			
			double roulette = Math.random()*(1-2*threshold);
		
			while(roulette > positions.get(state).get_lifespan()) {
				
				roulette -= positions.get(state).get_lifespan();
				state +=1;
			}
		}
		return state;
	}

	public QuantumPosition get_position(int state) {
		
		return positions.get(state);
	}
	
	
	public void move(int state, Fitness<?,?> fit) {
		
		positions.get(state).move();
		positions.get(state).update_tellability(fit);
		
		if(positions.get(state).get_tellability() >= best_tellability) {
			
			this.best_tellability = positions.get(state).get_tellability();
			this.best_personality = positions.get(state).get_personality();
			this.best_happenings = positions.get(state).get_happenings();
			this.best_simLength = positions.get(state).get_simLength();
			this.best_actualLength = positions.get(state).get_actualLength();
		}
	}
	
	
	public void update_lifespan() {
		
		if(superPosition()) {
		
			double time = 0;
			
			for(int i = positions.size()-1; i > 0; i--) {
				
				positions.get(i).update_lifespan(time);
				
				if(positions.get(i).get_lifespan()<threshold) {
					
					time = positions.get(i).get_lifespan();
					positions.remove(i);
					
				} else {
				
					time = positions.get(i).get_lifespan()/2;
					positions.get(i).update_lifespan(-time);
					
				}
			}
			positions.get(0).update_lifespan(time);
		}
	}
	
	
	public void add_Position(int state, ChromosomePersonality personality, ChromosomeHappenings happenings, int length, Fitness <?,?> fit) {
		
		double time = positions.get(state).get_lifespan()/2;
		positions.get(state).update_lifespan(-time);
		
		positions.add(new QuantumPosition(personality, positions.get(state).get_persVelocity(), happenings, positions.get(state).get_hapVelocity(), length, time, fit));
		
		if(positions.get(positions.size()-1).get_tellability() >= best_tellability) {
			
			this.best_tellability = positions.get(positions.size()-1).get_tellability();
			this.best_personality = positions.get(positions.size()-1).get_personality();
			this.best_happenings = positions.get(positions.size()-1).get_happenings();
			this.best_simLength = positions.get(positions.size()-1).get_simLength();
			this.best_actualLength = positions.get(positions.size()-1).get_actualLength();
		}
		positions.sort(null);
	}
	
	
	public void add_Position(QuantumPosition qPos, int state) {
		
		double time = positions.get(state).get_lifespan()/2;
		positions.get(state).update_lifespan(-time);
		
		qPos.update_lifespan(time);
		
		positions.add(qPos);

		if(positions.get(positions.size()-1).get_tellability() >= best_tellability) {
			
			this.best_tellability = positions.get(positions.size()-1).get_tellability();
			this.best_personality = positions.get(positions.size()-1).get_personality();
			this.best_happenings = positions.get(positions.size()-1).get_happenings();
			this.best_simLength = positions.get(positions.size()-1).get_simLength();
			this.best_actualLength = positions.get(positions.size()-1).get_actualLength();
		}
		positions.sort(null);
	}
	
	
	public int compareTo(Quantum other) {
		
		// smaller operator returns array in descending order
		if(best_tellability < other.best_tellability())
			return 1;
		
		return -1;
	}
	
	
	public boolean equals(ChromosomePersonality other_personality, ChromosomeHappenings other_happenings) {
		
		for(int i = 0; i < positions.size(); i++) {

			if(positions.get(i).get_happenings().equals(other_happenings))
				return true;
			if(positions.get(i).get_personality().equals(other_personality))
				return true;
		}
		return false;
	}
	
	
	@Override
	public String to_String() {
		return this.to_String(best_personality, best_happenings, best_simLength, best_actualLength);
	}
}
