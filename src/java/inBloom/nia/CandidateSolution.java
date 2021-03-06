package inBloom.nia;

import inBloom.helper.Tellability;

public abstract class CandidateSolution {
	protected String id = String.valueOf(this.hashCode());

	protected ChromosomePersonality personality;
	protected ChromosomeHappenings happenings;
	protected ChromosomeLength simulation_length;

	protected Double tellabilityValue;
	protected Tellability tellability;
	protected Integer actual_length;

	/** Additional information about this solution, added to log file during {@linkplain NIAlgorithm#to_file(CandidateSolution, String)}*/
	public String notes;

	/**
	 * to_String should convert all information of a candidate into a string.
	 * The result will be used to save a candidate into a file.
	 */
	public abstract String to_String();

	public CandidateSolution(ChromosomePersonality personality, ChromosomeHappenings happenings, Integer simLength){
		this.personality = personality;
		this.happenings = happenings;
		this.simulation_length = new ChromosomeLength(simLength);
		this.actual_length = this.simulation_length.value;
	}

	public CandidateSolution() {
		this.personality = null;
		this.happenings = null;
		this.simulation_length = null;
		this.actual_length = null;
	}

	protected String to_String(ChromosomePersonality perso, ChromosomeHappenings happ, Integer simLength, int actLength) {
		int number_agents = perso.values.length;
		int number_happenings = happ.values[0].length;

		String string = "<Agent Num / Happening Num / Simulation Len / Actual Len>\n";
		string += String.valueOf(number_agents) + "\n" + String.valueOf(number_happenings) + "\n" + String.valueOf(simLength) + "\n" +  String.valueOf(actLength) + "\n";

		string += "<Personality Parameters Per Agent>\n";
		for(int i = 0; i < number_agents; i++) {
			for(int j = 0; j < 5; j++) {
				string += String.valueOf(perso.values[i][j]);
				if(j<4) {
					string +=  " ";
				}
			}
			string += "\n";
		}

		string += "<Scheduled Step of Happenings Per Agent>\n";
		for(int i = 0; i < number_agents; i++) {
			for(int j = 0; j < number_happenings; j++) {
				string += String.valueOf(happ.values[i][j]);
				if(j<number_happenings-1) {
					string +=  " ";
				}
			}
			string += "\n";
		}

		this.updateNotes();
		string += "<Notes>\n";
		string += this.notes;

		return string;
	}

	protected void updateNotes() {
		this.notes = "<Candidate ID>\n";
		this.notes += this.id + "\n";
		this.notes += "<Tellability Summary>\n";
		this.notes += "FP: " + this.tellability.balancedFunctionalPolyvalence + " SYM: " + this.tellability.balancedSymmetry +
				" OPO: " + this.tellability.balancedOpposition + " SUS: " + this.tellability.balancedSuspense + "\n";
		this.notes += "<Tellability Details>\n";
		this.notes += this.tellability.detailedLog;
	}

	public Integer get_actualLength() {
		return this.actual_length;
	}

	public void set_actualLength(int length) {
		this.actual_length = length;
	}

	public ChromosomeLength get_simLength() {
		return this.simulation_length;
	}

	public double get_tellabilityValue() {
		return this.tellabilityValue;
	}

	public ChromosomePersonality get_personality() {
		return this.personality;
	}

	public double get_personality(int x, int y) {
		return this.personality.values[x][y];
	}

	public ChromosomeHappenings get_happenings() {
		return this.happenings;
	}

	public int get_happenings(int x, int y) {
		return this.happenings.values[x][y];
	}

	public void cleanTellabilityLog() {
		if (this.tellability != null) {
			this.tellability.detailedLog = "";
		}
	}
}
