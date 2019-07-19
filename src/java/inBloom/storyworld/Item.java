package inBloom.storyworld;

/**
 * Abstract superclass for domain-specific items. Each item should have a name on the conceptual level (e.g. bread).
 * @author Leonid Berov
 */
public abstract class Item extends Existent {

	/**
	 * Each item should have a human-readable item name like: "chair" used for logging and ASL representation purposes.
	 * @return a String representation of the item name implemented by this class
	 */
	public abstract String getItemName();

	public boolean isEdible() {
		return false;
	}

	@Override
	public String toString() {
		return this.getItemName();
	}
}
