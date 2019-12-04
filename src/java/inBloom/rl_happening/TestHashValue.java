/**
 * 
 */
package inBloom.rl_happening;

import java.util.HashMap;

import inBloom.PlotModel;
import inBloom.rl_happening.IslandModel.CivilizedWorld;
import inBloom.storyworld.Character;
import inBloom.storyworld.Location;

/**
 * A class to save the current state of the model
 * 
 * @author Julia Wippermann
 * @version 4.12.19
 *
 */
public class TestHashValue {
	
	HashMap<String, Character> characters;
	HashMap<String, Location> locations;
	PlotModel myModel;

	@SuppressWarnings("unchecked")
	public TestHashValue(HashMap<String, Character> characters,
					  HashMap<String, Location> locations,
					  PlotModel myModel) {
		
		this.characters = (HashMap<String, Character>)characters.clone();
		this.locations = (HashMap<String, Location>)locations.clone();
		this.myModel = myModel;
		
	}
	
	public static void main(String[] args) {
		
		Character bob = new Character();
		Character cop = new Character();
		Location myWorld = new IslandModel.CivilizedWorld();
		boolean isHappy = true;
		boolean lol = true;
		
		Object myLol = (Object)lol;
		
		
		myLol.hashCode();
		
		
		System.out.println("bob's hashValue: " + bob.hashCode());
		System.out.println("cop's hashValue: " + cop.hashCode());
		System.out.println("bob's hashValue: " + bob.hashCode());
		System.out.println("my world's hashValue: " + myWorld.hashCode());
		bob.location = myWorld;
		System.out.println("bob's hashValue: " + bob.hashCode());
		
		System.out.println("isHappy: " + ((Object)isHappy).hashCode());
		System.out.println("isLolly: " + ((Object)lol).hashCode());
		System.out.println("isLolOb: " + ((Object)myLol).hashCode());
		
		System.out.println("myLol String: " + myLol.toString().hashCode());
		System.out.println("myLol String: " + ((Object)isHappy).toString().hashCode());
		
		// Objects don't have the same hashValue in different runs
		createCharacter();
		createCharacter();
		
		// Strings do give the same hashValue in different runs,
		// but different hashValue for different Strings
		createString();
		createString();
	}
	
	private static Character createCharacter() {
		Character nob = new Character();
		System.out.println("nob: " + nob.hashCode());
		return nob;
	}
	
	private static String createString() {
		String nob = "lolliepop";
		String lob = "mockipop";
		System.out.println("lolliepop: " + nob.hashCode());
		System.out.println("mockipop : " + lob.hashCode());
		return nob;
	}
	
}
