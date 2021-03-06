package inBloom.graph;

import java.util.UUID;

/**
 * Represents a typed edge in the plot graph.
 * @author Leonid Berov
 */
public class Edge implements Cloneable {

	public enum Type {ROOT,
					  TEMPORAL,
					  MOTIVATION {public String toString() { return "motivation"; } },
					  CROSSCHARACTER  {public String toString() { return "crossChar"; } },
					  ACTUALIZATION,
					  TERMINATION,
					  EQUIVALENCE,
					  WILDCARD,
					  CAUSALITY {public String toString() { return "cause"; } }}

	private String id;
	private Type type;

	// This is just used for rendering.
	// Should be seperated into a class
	// responsible for view.
	private int offset;

	public Edge() {
		this(Type.TEMPORAL);
	}

	public Edge(Type type) {
		this.id = UUID.randomUUID().toString();
		this.type = type;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return this.offset;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public Edge clone() {
		Edge clone = new Edge(this.type);
		clone.setOffset(this.offset);

		return clone;
	}

	public String toString() {
		return this.getType().toString().toLowerCase();
	}
}
