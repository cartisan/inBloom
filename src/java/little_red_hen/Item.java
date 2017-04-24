package little_red_hen;

public abstract class Item {
	public abstract String getItemName();

	public abstract String literal();
	
	public boolean isEdible() {
		return false;
	}

}
