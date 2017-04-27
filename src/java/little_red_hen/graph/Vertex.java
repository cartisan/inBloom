package little_red_hen.graph;

import java.util.UUID;

public class Vertex {
	public enum Type { ROOT, EVENT, EMOTION, SPEECHACT }

	private String id;
	private String label;
	private Type type;
	

	public void setType(Type type) {
		this.type = type;
	}

	public Vertex(String label) {
		this(label, Vertex.Type.EVENT);
	}
	
	public Vertex(String label, Type type) {
		this.label = label;
		this.id = UUID.randomUUID().toString();
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public Type getType() {
		return type;
	}

	public String toString() {
		return this.label;
	}
}
