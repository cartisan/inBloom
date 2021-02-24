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

	private List<QuantumPosition> positions = new ArrayList<>();

	private double threshold;


	public Quantum(int individual_count, ChromosomePersonality personality, ChromosomePersonality velocity_personality, ChromosomeHappenings happenings, ChromosomeHappenings velocity_happenings, Integer simLength, Fitness<?,?> fit){
		this.best_personality = personality;
		this.best_happenings = happenings;
		this.best_simLength = simLength;
		this.threshold = Math.pow(0.5, individual_count/2-1);

		QuantumPosition pos = new QuantumPosition(personality, velocity_personality, happenings, velocity_happenings, simLength, 1, fit);

		this.best_actualLength = pos.get_actualLength();
		this.best_tellability = pos.get_tellabilityValue();

		this.positions.add(pos);
	}

	public boolean superPosition() {
		return this.positions.size()>0;
	}

	public int amount_positions() {
		return this.positions.size();
	}

	public ChromosomePersonality get_personality() {
		return this.get_personality(this.choosePosition());
	}

	public ChromosomePersonality get_personality(int z) {
		return this.positions.get(z).get_personality();
	}

	public double get_personality(int x, int y) {
		return this.get_personality(x,y,this.choosePosition());
	}

	public double get_personality(int x, int y, int state) {
		return this.positions.get(state).get_personality(x, y);
	}

	public ChromosomeHappenings get_happenings() {
		return this.get_happenings(this.choosePosition());
	}

	public ChromosomeHappenings get_happenings(int z) {
		return this.positions.get(z).get_happenings();
	}

	public int get_happenings(int x, int y) {
		return this.get_happenings(x,y,this.choosePosition());
	}

	public int get_happenings(int x, int y, int z) {
		return this.positions.get(z).get_happenings().values[x][y];
	}

	public Integer get_simLength() {
		return this.get_simLength(this.choosePosition());
	}

	public Integer get_simLength(int z) {
		return this.positions.get(z).get_simLength();
	}


	public ChromosomePersonality best_personality() {
		return this.best_personality;
	}

	public ChromosomeHappenings best_happenings() {
		return this.best_happenings;
	}

	public double best_personality(int x, int y) {
		return this.best_personality.values[x][y];
	}

	public int best_happenings(int x, int y) {
		return this.best_happenings.values[x][y];
	}

	public Integer best_simLength() {
		return this.best_simLength;
	}

	public void set_actualLength(int length) {
		this.best_actualLength = length;
	}

	public int best_actualLength() {
		return this.best_actualLength;
	}

	public double best_tellability() {
		return this.best_tellability;
	}

	public double get_tellabilityValue() {
		return this.get_tellability(this.choosePosition());
	}

	public double get_tellability(int z) {
		return this.positions.get(z).get_tellabilityValue();
	}

	public int choosePosition() {

		int state = 0;

		if(this.superPosition()) {

			double roulette = Math.random()*(1-2*this.threshold);

			while(roulette > this.positions.get(state).get_lifespan()) {

				roulette -= this.positions.get(state).get_lifespan();
				state +=1;
			}
		}
		return state;
	}

	public QuantumPosition get_position(int state) {

		return this.positions.get(state);
	}

	public void move(int state, Fitness<?,?> fit) {
		this.positions.get(state).move();
		this.positions.get(state).update_tellability(fit);

		if(this.positions.get(state).get_tellabilityValue() > this.best_tellability) {
			this.best_tellability = this.positions.get(state).get_tellabilityValue();
			// FIXME: Fixed bug where best_personality would be same instance as personality, and change with every move
			this.best_personality = this.positions.get(state).get_personality().clone();
			this.best_happenings = this.positions.get(state).get_happenings().clone();
			this.best_simLength = this.positions.get(state).get_simLength();
			this.best_actualLength = this.positions.get(state).get_actualLength();

			this.tellability = fit.tellability;
			this.updateNotes();
		}
	}


	public void update_lifespan() {

		if(this.superPosition()) {

			double time = 0;

			for(int i = this.positions.size()-1; i > 0; i--) {

				this.positions.get(i).update_lifespan(time);

				if(this.positions.get(i).get_lifespan()<this.threshold) {

					time = this.positions.get(i).get_lifespan();
					this.positions.remove(i);

				} else {

					time = this.positions.get(i).get_lifespan()/2;
					this.positions.get(i).update_lifespan(-time);

				}
			}
			this.positions.get(0).update_lifespan(time);
		}
	}


	public void add_Position(int state, ChromosomePersonality personality, ChromosomeHappenings happenings, int length, Fitness <?,?> fit) {
		double time = this.positions.get(state).get_lifespan()/2;
		this.positions.get(state).update_lifespan(-time);

		this.positions.add(new QuantumPosition(personality, this.positions.get(state).get_persVelocity(), happenings, this.positions.get(state).get_hapVelocity(), length, time, fit));

		if(this.positions.get(this.positions.size()-1).get_tellabilityValue() > this.best_tellability) {
			this.best_tellability = this.positions.get(this.positions.size()-1).get_tellabilityValue();
			// FIXME: Fixed bug where best_personality would be same instance as personality, and change with every move
			this.best_personality = this.positions.get(this.positions.size()-1).get_personality().clone();
			this.best_happenings = this.positions.get(this.positions.size()-1).get_happenings().clone();
			this.best_simLength = this.positions.get(this.positions.size()-1).get_simLength();
			this.best_actualLength = this.positions.get(this.positions.size()-1).get_actualLength();

			this.tellability = fit.tellability;
			this.updateNotes();
		}
		this.positions.sort(null);
	}


	public void add_Position(QuantumPosition qPos, int state) {
		double time = this.positions.get(state).get_lifespan()/2;
		this.positions.get(state).update_lifespan(-time);

		qPos.update_lifespan(time);

		this.positions.add(qPos);

		if(this.positions.get(this.positions.size()-1).get_tellabilityValue() >= this.best_tellability) {
			this.best_tellability = this.positions.get(this.positions.size()-1).get_tellabilityValue();
			// FIXME: Fixed bug where best_personality would be same instance as personality, and change with every move
			this.best_personality = this.positions.get(this.positions.size()-1).get_personality().clone();
			this.best_happenings = this.positions.get(this.positions.size()-1).get_happenings().clone();
			this.best_simLength = this.positions.get(this.positions.size()-1).get_simLength();
			this.best_actualLength = this.positions.get(this.positions.size()-1).get_actualLength();

			this.notes = qPos.notes;
		}
		this.positions.sort(null);
	}


	public int compareTo(Quantum other) {

		// smaller operator returns array in descending order
		if(this.best_tellability < other.best_tellability()) {
			return 1;
		}

		return -1;
	}


	public boolean equals(ChromosomePersonality other_personality, ChromosomeHappenings other_happenings) {

		for(int i = 0; i < this.positions.size(); i++) {

			if(this.positions.get(i).get_happenings().equals(other_happenings)) {
				return true;
			}
			if(this.positions.get(i).get_personality().equals(other_personality)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public String to_String() {
		return this.to_String(this.best_personality, this.best_happenings, this.best_simLength, this.best_actualLength);
	}
}
