package plotmas.graph;

import java.util.UUID;

/**
 * Represents a typed edge in the plot graph.
 * @author Leonid Berov
 */
public class Edge {
	
	public enum Type { ROOT, TEMPORAL, MOTIVATION, COMMUNICATION }
	
	private String id;
	private Type type;
	
	public Edge() {
		this(Type.TEMPORAL);
	}
	
	public Edge(Type type) {
		this.id = UUID.randomUUID().toString();
		this.type = type;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
