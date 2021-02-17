package inBloom.nia;

public abstract class CandidateSolution {
	// Functions that get called by the Fitness class in order to start a simulation
	public abstract ChromosomePersonality get_personality();
	public abstract ChromosomeHappenings get_happenings();
	
	public abstract double get_personality(int x, int y);
	public abstract int get_happenings(int x, int y);

	public abstract Integer get_simLength();
	public abstract void set_actualLength(int length);
	public abstract double get_tellability();
	
	/**
	 * to_String should convert all information of a candidate into a string.
	 * The result will be used to save a candidate into a file.
	 */
	public abstract String to_String();
	
	public String to_String(ChromosomePersonality personality, ChromosomeHappenings happenings, int simulation_length, int actual_length) {
		int number_agents = personality.values.length;
		int number_happenings = happenings.values[0].length;
		
		String string = "<Agent Num \\ Happening Num \\ Simulation Len \\ Actual Len>\n";
		string += String.valueOf(number_agents) + "\n" + String.valueOf(number_happenings) + "\n" + String.valueOf(simulation_length) + "\n" +  String.valueOf(actual_length) + "\n";
		
		string += "<Personality Parameters Per Agent>\n";
		for(int i = 0; i < number_agents; i++) {
			for(int j = 0; j < 5; j++) {
				string += String.valueOf(personality.values[i][j]);
				if(j<4)
					string +=  " ";
			}
			string += "\n";
		}
		
		string += "<Scheduled Step of Happenings Per Agent>\n";
		for(int i = 0; i < number_agents; i++) {	
			for(int j = 0; j < number_happenings; j++) {
				string += String.valueOf(happenings.values[i][j]);	
				if(j<number_happenings-1)
					string +=  " ";	
			}
			string += "\n";
		}
		
		return string;
	}
}
