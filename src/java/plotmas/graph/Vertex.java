package plotmas.graph;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Represents a typed node in the plot graph.
 * @author Leonid Berov
 */
public class Vertex {
	
	public enum Type { ROOT, EVENT, EMOTION, SPEECHACT, PERCEPT }

	private String id;
	private String label;
	private Type type;
	private LinkedList<String> emotions = new LinkedList<>();
	

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

	public String getFunctor() {
		String removedAnnots = getLabel().split("\\[")[0];
		String removedTerms = removedAnnots.split("\\(")[0];
		
		return removedTerms;
	}
	
	public String toString() {
		String result = this.getLabel();
		
		if(!this.emotions.isEmpty()) {
			result += this.emotions.toString();
		}
		
		if(this.type == Type.PERCEPT) {
			result = "+" + result;
		}
		
		return result;
	}
	
	public void addEmotion(String emo) {
		this.emotions.add(emo);
	}
	
}
