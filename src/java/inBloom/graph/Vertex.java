package inBloom.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;

import inBloom.helper.TermParser;

/**
 * Represents a typed vertex in the plot graph. The text of the vertex is stored in {@link #label}, the type
 * in {@link #type}.
 * @author Leonid Berov
 */
public class Vertex implements Cloneable {
	static protected Logger logger = Logger.getLogger(Vertex.class.getName());

	public enum Type { EVENT,			// Abstract vertex type for denoting vertices with unclear semantics
					   ACTION,			// goal-directed events that have an agent, can contain emotions after analysis
					   PERCEPT,			// changes of an agent's belief base, can contain emotions after analysis
					   EMOTION,			// primary or secondary event appraisal
					   SPEECHACT,		// source of a communication edge
					   LISTEN,			// target of a communication edge
					   INTENTION,		// commitment to bring about a desired state
					   WILDCARD,		// vertex of arbitrary but fixed type, used to define schemata in FuntionalUnits
					   ACTIVE,			// vertex of type action or speech, used to define schemata in FuntionalUnits
					   ROOT,			// root node of plot graph, contains character name but is semantically empty
					   AXIS_LABEL,		// represents environment-steps on the vertical time axis
					 }

	/* NOTE: each new attribute should also be considered in #clone() */
	private PlotDirectedSparseGraph graph;
	private String id;
	private String label;
	private Type type;
	private int step;
	private boolean isPolyvalent;

	/**
	 * Stores emotions that have been attached to this Vertex. Only PERCEPT-type vertices can contain emotions, and
	 * these emotions are collapsed into the percept during graph analysis by {@linkplain FullGraphPPVisitor}, which
	 * means the emotions a vertex has are not usually known during vertex creation.
	 */
	private LinkedList<String> emotions = new LinkedList<>();

	/**
	 * The minimum width divided by two this vertex should have
	 * in the plot graph visualisation. Set by EdgeLayoutVisitor.
	 */
	public int minWidth;


	/**
	 * Creates a default instance of vertex, with type {@link Vertex.Type#EVENT}.
	 * @param label vertex content
	 */
	public Vertex(String label, int step, PlotDirectedSparseGraph graph) {
		this(label, Vertex.Type.EVENT, step, graph);
	}

	/**
	 * Creates an instance of vertex of arbitrary label and type .
	 * @param label vertex content
	 * @param type possible types see {@link Vertex.Type}
	 */
	public Vertex(String label, Type type, int step, PlotDirectedSparseGraph graph) {
		this.label = label;
		this.id = UUID.randomUUID().toString();
		this.type = type;
		this.step = step;
		this.isPolyvalent = false;
		this.graph = graph;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getStep() {
		return this.step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	/**
	 * Returns the functor part of the vertex' label.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	!eat(bread)[source(self)] -> eat
	 * }
	 * </pre>
	 *
	 * @return
	 */
	public String getFunctor() {
		String removedAnnots = TermParser.removeAnnots(this.getLabel());
		String removedTerms = removedAnnots.split("\\(")[0];
		if(removedTerms.startsWith("+") || removedTerms.startsWith("-")) {
			removedTerms = removedTerms.substring(1);
		}
		if(removedTerms.startsWith("!")) {
			removedTerms = removedTerms.substring(1);
		}
		return removedTerms;
	}


	/**
	 * Returns the the vertex' label without annotations.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	!eat(bread)[source(self)] -> !eat(bread)
	 * }
	 * </pre>
	 *
	 * @return
	 */
	public String getWithoutAnnotation() {
		return TermParser.removeAnnots(this.getLabel());
	}

	/**
	 * Returns the source of this vertex.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	eat(bread)[source(self)] -> self
	 * }
	 * </pre>
	 *
	 * @return
	 */
	public String getSource() {
		return TermParser.getAnnotation(this.getLabel(), "source");
	}

	/**
	 * Returns the cause of this vertex.
	 * Example: <br />
	 * <pre>
	 * {@code
	 * 	found(wheat)[cause(farm_work)] -> farm_work
	 * }
	 * </pre>
	 *
	 * @return
	 */
	public String getCause() {
		return TermParser.getAnnotation(this.getLabel(), Edge.Type.CAUSALITY.toString());
	}

	public void setPolyvalent() {
		this.isPolyvalent = true;
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

		if (this.isPolyvalent) {
			result = "* " + result;
		}

		switch(this.type) {
		case EMOTION: 	break;
		case PERCEPT:
		default: 		result = TermParser.removeAnnots(result);
						result = this.appendEmotions(result);
						break;

		}

		return result;
	}

	/**
	 * Returns the label of this string if it is an intention.
	 * It only returns something other than the empty string,
	 * if the label starts with "!".
	 * For vertices of type INTENTION or SPEECHACT, this returns
	 * the complete label without annotations, without "!".
	 * For vertices of type LISTEN, this returns the vertex label
	 * without annotations and without the leading "+!".
	 * For all other vertices, this returns an empty string.
	 */
	public String getIntention() {
		switch(this.type) {
			case INTENTION:
			case SPEECHACT:
				String removedAnnots = TermParser.removeAnnots(this.label);
				if(removedAnnots.startsWith("!")) {
					return removedAnnots.substring(1);
				} else {
					return "";
				}
			default:
				return "";
		}
	}

	public Vertex clone(PlotDirectedSparseGraph graph) {
		Vertex clone = new Vertex(this.label, this.type, this.step, graph);

		clone.minWidth = this.minWidth;
		clone.isPolyvalent = this.isPolyvalent;

		for (String e: this.emotions) {
			clone.addEmotion(e);
		}

		return clone;
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

	public void removeEmotion(String emo) {
		this.emotions.remove(emo);
	}

	public boolean hasEmotion(String emo) {
		return this.emotions.contains(emo);
	}

	public boolean hasEmotion() {
		return !this.emotions.isEmpty();
	}

	public List<String> getEmotions() {
		return this.emotions;
	}

	public Collection<Edge> getIncidentEdges() {
		return this.graph.getIncidentEdges(this);
	}

	/**
	 * Returns the character/root node for this vertex by recursively traversing backwards through
	 * temporal and root egdes.
	 * @return
	 */
	public Vertex getRoot() {
		Vertex pred = this.graph.getCharPredecessor(this);

		if (pred == null) {
			if(this.type.equals(Vertex.Type.ROOT)) {
				return this;
			} else {
				throw new RuntimeException("Found non-root vertex without predecessor: " + this.getLabel());
			}
		} else {
			return pred.getRoot();
		}
	}
}
