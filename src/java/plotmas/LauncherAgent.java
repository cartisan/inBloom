package plotmas;

import java.util.Collection;
import java.util.LinkedList;

import jason.asSemantics.Personality;
import plotmas.storyworld.Item;

/**
 * Helper class used to encapsulate all parameters needed to initialise ASL Agents from java code.
 * This parameters will be used to create a mas2j file required to start a Jason multi agent system. 
 * @author Leonid Berov
 */
public class LauncherAgent {
	public String name;
	public String beliefs;
	public String goals;
	public Personality personality;
    public LinkedList<Item> inventory;
    public String location;
	
	public LauncherAgent() {
		this.name = null;
		this.beliefs = "";
		this.goals = "";
		this.personality = null;
		this.location = PlotModel.DEFAULT_LOCATION_NAME;
		this.inventory = new LinkedList<Item>();
	}
	
	public LauncherAgent(String name) {
		this();
		this.name = name;
	}

	public LauncherAgent(String name, Personality personality) {
		this();
		this.name = name;
		this.personality = personality;
	}
	
	public LauncherAgent(String name, Collection<String> beliefs, Collection<String> goals, Personality personality) {
		this();
		this.name = name;
		this.beliefs = createLiteralString(beliefs);
		this.goals = createLiteralString(goals);
		this.personality = personality;
	}
	
	/**
	 * Helper function that takes a collection of strings and concatenates them into a list that can be used to 
	 * generate ASL literal lists.
	 */
	private String createLiteralString(Collection<String> literalList) {
		return String.join(",", literalList);
	}
}
