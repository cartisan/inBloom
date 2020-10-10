package inBloom.evo;

public interface Individual {
	
	// Functions that get called by the Fitness class in order to start a simulation
	public ChromosomePersonality get_personality();
	public ChromosomeHappenings get_happenings();
	
	public double get_personality(int x, int y);
	public int get_happenings(int x, int y);

	public Integer get_simLength();
	public void set_actualLength(int length);
	public double get_tellability();
	
	public String to_String();

}
