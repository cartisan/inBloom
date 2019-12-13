/**
 * 
 */
package inBloom.rl_happening;

import inBloom.helper.MoodMapper;

/**
 * @author juwi
 *
 */
public class MoodTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(int i = 0; i < 3; i++) {
			MoodMapper map = getMapper();
			System.out.println(map.toString());
			System.out.println(map.toString().hashCode());
		}

	}
	
	public static MoodMapper getMapper() {
		MoodMapper map = new MoodMapper();
		//map.addMood("robinson", time, mood);
		return map;
	}

}
