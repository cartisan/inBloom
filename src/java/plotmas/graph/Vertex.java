package plotmas.graph;

import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;
import jason.asSyntax.parser.ParseException;

/**
 * Represents a typed vertex in the plot graph. The text of the vertex is stored in {@link #label}, the type 
 * in {@link #type}.
 * @author Leonid Berov
 */
public class Vertex implements Cloneable {

	//removes annotations from literal-style stings
	public static Pattern NO_ANNOT_PATTERN = Pattern.compile("(.+?\\(.+?\\))");
	
	public enum Type { ROOT, EVENT, EMOTION, SPEECHACT, LISTEN, PERCEPT }

	private String id;
	private String label;
	private Type type;
	
	private LinkedList<String> emotions = new LinkedList<>();
	

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Creates a default instance of vertex, with type {@link Vertex.Type#EVENT}.
	 * @param label vertex content
	 */
	public Vertex(String label) {
		this(label, Vertex.Type.EVENT);
	}
	
	/**
	 * Creates an instance of vertex of arbitrary label and type .
	 * @param label vertex content
	 * @param type possible types see {@link Vertex.Type}
	 */
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

	/**
	 * Returns the functor part of the vertex' label.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	eat(bread)[source(self)] -> eat
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	public String getFunctor() {
		String removedAnnots = getLabel().split("\\[")[0];
		String removedTerms = removedAnnots.split("\\(")[0];
		
		return removedTerms;
	}
	
	
	/**
	 * Returns the the vertex' label without annotations.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	eat(bread)[source(self)] -> eat(bread)
	 * }
	 * </pre>
	 * 
	 * @return
	 */	
	public String getWithoutAnnotation() {
		String removedAnnots = getLabel().split("\\[")[0];
		if(removedAnnots.startsWith("!")) {
			removedAnnots = removedAnnots.substring(1);
		}
		return removedAnnots;
	}
	
	/**
	 * Implements the string representations of the vertex depending on its type. This determines how vertices will be
	 * displayed in the final graph.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = this.getLabel();
		
		switch(this.type) {
		case SPEECHACT:	result = "SPEECH>>" + result;
						result = appendEmotions(result);
						break;
		case LISTEN:	result = "LISTEN<<" + result;
						result = appendEmotions(result);
						break;
		case PERCEPT: 	Matcher m = NO_ANNOT_PATTERN.matcher(result);
				        if (m.find())
				            result = "+" + m.group(1);
						result = appendEmotions(result);
						break;
		case EMOTION: 	try {
							Emotion em = Emotion.parseString(result);
							result = em.toString();
						} catch (ParseException e) {
							e.printStackTrace();
							return null;
						}
						break;
		default: 		result = appendEmotions(result);
						break;
		
		}
		

		
		return result;
	}
	
	@Override
	public Vertex clone() {
		return new Vertex(this.label, this.type);
	}

	/**
	 * Creates a string representation of this vertex emotion list and appends it to the provided string, which is 
	 * usually a representation of this vertex.
	 * @param vertexString
	 * @return
	 */
	private String appendEmotions(String vertexString) {
		if(!this.emotions.isEmpty()) {
							vertexString += this.emotions.stream().map(em -> em + "(" + (Emotion.getEmotion(em).getP()  > 0 ? "+" : "-") + ")")
															.collect(Collectors.toList())
															.toString();
						};
		return vertexString;
	}
	
	public void addEmotion(String emo) {
		this.emotions.add(emo);
	}

	public boolean hasEmotion(String emo) {
		return this.emotions.contains(emo);
	}
	
}
