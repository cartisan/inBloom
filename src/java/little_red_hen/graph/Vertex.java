package little_red_hen.graph;

import java.util.UUID;

public class Vertex {
	String id;
	String label;
	
	public Vertex(String label) {
		this.label = label;
		this.id = UUID.randomUUID().toString();
	}
	
	public String toString() {
		return this.label;
	}
}
