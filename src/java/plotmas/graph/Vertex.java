package plotmas.graph;

import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;

/**
 * Represents a typed node in the plot graph.
 * @author Leonid Berov
 */
public class Vertex implements Cloneable {
	
	public enum Type { ROOT, EVENT, EMOTION, SPEECHACT, LISTEN, PERCEPT }

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
		
		switch(this.type) {
		case PERCEPT: 	result = "+" + result;
						break;
		case EMOTION: 	result = result + String.format("(%s)", 
													  (Emotion.getEmotion(result).getP()  > 0 ? "+" : "-"));
						break;
		case SPEECHACT:	result = "SPEECH>>" + result;
						break;
		case LISTEN:	result = "LISTEN<<" + result;
						break;
		default: 		if(!this.emotions.isEmpty()) {
							result += this.emotions.stream().map(em -> em + "(" + (Emotion.getEmotion(em).getP()  > 0 ? "+" : "-") + ")")
															.collect(Collectors.toList())
															.toString();
						};
		
		}
		

		
		return result;
	}
	
	public void addEmotion(String emo) {
		this.emotions.add(emo);
	}
	
	@Override
	public Vertex clone() {
		return new Vertex(this.label, this.type);
	}
	
}
