package plotmas.storyworld;

public abstract class Item {
	public abstract String getItemName();

	public abstract String literal();
	
	public boolean isEdible() {
		return false;
	}

	public String toString() {
		return literal();
	}
}
