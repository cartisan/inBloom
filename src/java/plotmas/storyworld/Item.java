package plotmas.storyworld;

import jason.asSyntax.Literal;
import jason.asSyntax.Pred;

/**
 * Abstract superclass for domain-specific items. Each item should have a name on the conceptual level (e.g. bread).
 * @author Leonid Berov
 */
public abstract class Item {
	
	/**
	 * Each item should have a human-readable item name like: "Chair" used for logging and ASL representation purposes.
	 * @return a String representation of the item name implemented by this class
	 */
	public abstract String getItemName();
	
	/**
	 * Returns an AgentSpeak {@link jason.asSyntax.Literal literal} denoting the item and potentially its
	 * current state using annotations.
	 * @return A literal denoting this item and its current state
	 * @see plotmas.stories.little_red_hen.FarmModel.Wheat
	 */
	public Literal literal() {
			return new Pred(this.toString());
	};
	
	public boolean isEdible() {
		return false;
	}

	public String toString() {
		return this.getItemName();
	}
}
