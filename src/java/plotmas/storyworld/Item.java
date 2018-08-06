package plotmas.storyworld;

/**
 * Abstract superclass for domain-specific items. Each item should have a name on the conceptual level (e.g. bread).
 * @author Leonid Berov
 */
public abstract class Item {
	
	/**
	 * Each item should have a human-readable item name like: "Chair" used for logging purposes.
	 * @return a String representation of the item name implemented by this class
	 */
	public abstract String getItemName();
	
	/**
	 * Each item should implement a method that returns an AgentSpeak {@link jason.asSyntax.Literal literal} denoting
	 * the item's current state.
	 * @return a String representation of the literal denoting this item's current state
	 * @see plotmas.stories.little_red_hen.FarmModel.Wheat
	 */
	public abstract String literal();
	
	public boolean isEdible() {
		return false;
	}

	public String toString() {
		return literal();
	}
}
