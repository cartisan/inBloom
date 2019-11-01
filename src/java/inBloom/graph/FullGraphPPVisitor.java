package inBloom.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;

import jason.asSemantics.Emotion;

import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;
import inBloom.helper.TermParser;

/**
 * This post-process visitor is intended to be used on the
 * full ('raw') plot graph as the first post-process visitor.
 * It will collapse emotions, percepts and action into a single
 * vertex and create edges based on information stored in annotations.
 * @author Sven Wilke
 */
public class FullGraphPPVisitor implements PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(FullGraphPPVisitor.class.getName());
	private static final boolean KEEP_MOTIVATION = true;

	private PlotDirectedSparseGraph graph;
	private LinkedList<Vertex> eventList;
	private Vertex currentRoot;
	/** Safes which actions and perceptions were annotated with which crossChar ID.
	 *  So that {@link #postProcessing()} can create edges, when several vertices share the same ID.
	 *  Maps: ID -> Multuple Vertices */
	private ArrayListMultimap<String, Vertex> xCharIDMap;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph.clone();
		this.eventList = new LinkedList<>();
		this.xCharIDMap = ArrayListMultimap.create();

		this.graph.accept(this);

		this.postProcessing();
		return this.graph;
	}

	@Override
	public void visitRoot(Vertex vertex) {
		this.eventList.clear();
		this.currentRoot = vertex;
	}

	@Override
	public void visitEvent(Vertex vertex) {
		logger.warning("Located semantically underspecified EVENT vertex: " + vertex.getLabel());
	}

	@Override
	public void visitAction(Vertex vertex) {
 		// create actualization edges to causing intention
		String[] parts = vertex.getLabel().split("\\[motivation\\(");
		if(parts.length > 1) {
			String motivation = TermParser.removeAnnots(parts[1].substring(0, parts[1].length() - 2));
			String resultingLabel = parts[0];
			for(Vertex target : this.eventList) {
				if(motivation.equals(target.getIntention())) {
					this.graph.addEdge(new Edge(Edge.Type.ACTUALIZATION), target, vertex);
					//vertex.setMotivation(target);
					vertex.setLabel(resultingLabel);
					break;
				}
			}
		}

		this.processCrossCharAnnotation(vertex);
		this.eventList.addFirst(vertex);
	}

	public void handleDropIntention(Vertex vertex) {
		String label = vertex.getLabel();
		Pattern pattern = Pattern.compile("drop_intention\\((?<drop>.*?)\\)\\[" + Edge.Type.CAUSALITY.toString() + "\\((?<cause>.*)\\)\\]");
		Matcher matcher = pattern.matcher(label);

		// Remove the vertex if it is somehow degenerate (pattern could not be matched)
		if(!matcher.find()) {
			this.removeVertex(vertex);
			return;
		}
		String dropString = TermParser.removeAnnots(matcher.group("drop").substring(2));
		// Determine if the intention drop is relevant
		// (whether or not the intention that was dropped is in the graph)
		Vertex droppedIntention = null;
		for(Vertex drop : this.eventList) {
			if(drop.getIntention().equals(dropString)) {
				droppedIntention = drop;
				break;
			}
		}
		// If it is irrelevant, simply remove the vertex
		if(droppedIntention == null) {
			this.removeVertex(vertex);
			return;
		}

		// Look for the cause in previous vertices
		String causeString = matcher.group(Edge.Type.CAUSALITY.toString());
		if(causeString.startsWith("+!")) {	// we need to match causes of form +self(has_prupose), but also !rethink_life, which appears as +!rethink_life
			causeString = causeString.substring(1);
		}
		Vertex cause = null;
		for(Vertex potentialCause : this.eventList) {
			if(potentialCause.getWithoutAnnotation().equals(causeString)) {
				cause = potentialCause;
				break;
			}
		}

		if(cause != null) {
			this.graph.addEdge(new Edge(Edge.Type.TERMINATION), cause, droppedIntention);
			this.removeVertex(vertex);
		} else {
			if(causeString.startsWith("!")) {
				vertex.setType(Vertex.Type.INTENTION);
			} else {
				vertex.setType(Vertex.Type.PERCEPT);
			}
			vertex.setLabel(causeString);
			this.graph.addEdge(new Edge(Edge.Type.TERMINATION), vertex, droppedIntention);
			this.eventList.add(vertex);
		}
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		Emotion emotion;

		// Create emotion from toString() representation of emotion
		// (instead of previously used literal representation)
		emotion = TermParser.emotionFromString(vertex.getLabel());

		if(emotion == null) {
			logger.info("Emotion in PP was invalid. " + vertex.toString());
			return;
		}

		String cause = TermParser.removeAnnots(emotion.getCause());

		for(Vertex targetEvent : this.eventList) {
			if(!targetEvent.getIntention().isEmpty()) {
				continue;
			}

			String targetString = targetEvent.getWithoutAnnotation();

			// Needs to match with and without '+'
			// with for percepts, without for actions
			if((targetString.equals(cause) || targetString.equals("+" + cause))
					& !targetEvent.hasEmotion(emotion.getName())) {

				targetEvent.addEmotion(emotion.getName());
				this.removeVertex(vertex);
				break;
			}
		}
	}

	@Override
	public void visitPercept(Vertex vertex) {
		// check whether this reports a happening with a cause
		String cause = vertex.getCause();
		if (!cause.isEmpty()) {
			// create causality edge from vertex corresponding to cause annotation
			for(Vertex targetEvent : this.eventList) {
				if(targetEvent.getWithoutAnnotation().equals(cause) |  //our cause was an action, so targetEvent was perceived as-is
						targetEvent.getWithoutAnnotation().equals("+" + cause))  // our cause was a happening, so targetEvent was perceived as +cause
				{
					this.graph.addEdge(new Edge(Edge.Type.CAUSALITY), targetEvent, vertex);
					break;
				}
			}
		}

		// check whether this is a percept that reports action outcome: not a happening & must be event addition, so starts with + & has same literal as an action we previously executed
		else if(!vertex.getLabel().startsWith("-")) {
			for(Vertex targetEvent : this.eventList) {
				if( targetEvent.getType() == Vertex.Type.ACTION && vertex.getWithoutAnnotation().equals("+" + targetEvent.getWithoutAnnotation()) )	{
					// Extract annotations from vertex and add them to target, which is an action and has no own annotation
					if(!TermParser.getAnnots(targetEvent.getLabel()).isEmpty()) {
						throw new RuntimeException("Trying to add percept annotations to action: " + targetEvent.toString() + ", which was already merged with a percept");
					}
					String annots = TermParser.getAnnots(vertex.getLabel());
					targetEvent.setLabel(targetEvent.getLabel() + annots);

					// If x-character annotation present, make action the target of the x-char edge
					this.processCrossCharAnnotation(targetEvent);

					// remove this percept because it was merged, do not add it to eventList cause it is no potential target for edges
					this.removeVertex(vertex);
					return;
				}
			}
		}

		this.handleTradeoff(vertex);
		this.processCrossCharAnnotation(vertex);
		this.eventList.addFirst(vertex);
	}


	@Override
	public void visitSpeech(Vertex vertex) {
		this.attachMotivation(vertex);
	}

	@Override
	public void visitIntention(Vertex vertex) {
		String label = vertex.getLabel();
		if(label.startsWith("drop_intention")) {
			this.handleDropIntention(vertex);
			return;
		}

		this.attachMotivation(vertex);
	}

	/**
	 * Add entry to xCharIDMap if cross-character annotation present, so that #postProcessing can create edges between
	 * events with same xCharIDs.
	 * @param vertex
	 */
	private void processCrossCharAnnotation(Vertex vertex) {
		String crossCharID = TermParser.getAnnotation(vertex.getLabel(), Edge.Type.CROSSCHARACTER.toString());
		if (!crossCharID.isEmpty()) {
			this.xCharIDMap.put(crossCharID, vertex);
		}
	}

	private void attachMotivation(Vertex vertex) {
		String label = vertex.getLabel();
		String[] parts = label.split("\\[" + Edge.Type.MOTIVATION.toString() + "\\(");

		if(parts.length > 1) {
			String[] motivations = parts[1].substring(0, parts[1].length() - 2).split(";");
			String resultingLabel = parts[0];
			Set<Vertex> motivationVertices = new HashSet<>();
			for(String motivation : motivations) {
				motivation = TermParser.removeAnnots(motivation);
				for(Vertex target : this.eventList) {
					boolean isMotivation = false;

					// Check for intentions
					isMotivation = isMotivation ||
							motivation.equals(target.getIntention());

					// Check for percepts
					isMotivation = isMotivation ||
							motivation.equals(TermParser.removeAnnots(target.getLabel()));

					// Check for listens
					isMotivation = isMotivation ||
							motivation.equals(TermParser.removeAnnots(target.getLabel()).substring(1));

					if(isMotivation && !motivationVertices.contains(target)) {
						this.graph.addEdge(new Edge(Edge.Type.MOTIVATION), target, vertex);
						motivationVertices.add(target);
						break;
					}
				}
			}

			if(!KEEP_MOTIVATION || !motivationVertices.isEmpty()) {
				vertex.setLabel(resultingLabel);
			}
		}

		this.eventList.addFirst(vertex);
	}

	/**
	 * Checks if this perception of vertex is the removal of a belief, caused by processing another belief:
	 * {@code -has(bread)[source(is_dropped(bread))]}.
	 * If so, creates a termination edge between the source and the vertex representing the addition of this belief.
	 * @param vertex
	 * @return whether tradeoff was found
	 */
	private boolean handleTradeoff(Vertex vertex) {
		String source = vertex.getSource();
		if(source.isEmpty() || vertex.getLabel().startsWith("+")) {
			return false;
		}

		source = TermParser.removeAnnots(source);

		Vertex src = null;
		// Look for source
		for(Vertex target : this.eventList) {
			if(TermParser.removeAnnots(target.getLabel()).equals(source) |
			TermParser.removeAnnots(target.getLabel()).equals(source.substring(1)) & target.getType().equals(Vertex.Type.ACTION)) {
				// Source found! We take every source!
				src = target;
				break;
			}
		}

		if(src != null) {
			// Let's find the corresponding addition of this mental note.
			for(Vertex target : this.eventList) {
				if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
					if(target.getWithoutAnnotation().substring(0, 1).equals("+")) {
						// Great, found the addition!
						this.graph.addEdge(new Edge(Edge.Type.TERMINATION), src, target);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Performs all post-processing tasks needed to finish the full graph processing. This includes:
	 * <ul>
	 *   <li> Creating cross-character edges for all perception vertices with same content and step but perceived
	 *        by different agents. That is connecting the vertices of each cell in {@linkplain #stepPerceptTable} that
	 *        has multiple entries. </li>
     * 	 <li> Trimming the repeated actions at the end of the plot that caused execution to pause.</li>
     * </ul>
	 */
	private void postProcessing() {
		// iterate over entries of xCharIDMap, and create cross-character edges like below
		for (String id: this.xCharIDMap.keys()) {
			List<Vertex> connectedEvents = this.xCharIDMap.get(id);

			// create edges between all vertices in cell (cell obv. should have more than one vertex
			if (connectedEvents.size() > 1) {
				// create all pairwise combinations for vertices in cell, connect all pairs
				Set<Set<Vertex>> pairs = Sets.combinations(new HashSet<>(connectedEvents), 2);

				for(Set<Vertex> pair : pairs) {
					ArrayList<Vertex> pList = new ArrayList<>(pair);

					// if both vertices belong to same character, no cross-character edge is required
					if (pList.get(0).getRoot().equals(pList.get(1).getRoot())) {
						continue;
					}

					// create x-character edges
					this.graph.addEdge(new Edge(Edge.Type.CROSSCHARACTER), pList);

					// connect bi-directionally by creating reversed edges
					Collections.reverse(pList);
					this.graph.addEdge(new Edge(Edge.Type.CROSSCHARACTER), pList);
				}
			}
		}

		// TODO: Remove repeating pattern at end?
	}


	@Override
	public void visitListen(Vertex vertex) {
		Vertex successor = this.graph.getCharSuccessor(vertex);
		String checkTell = "+" + vertex.getLabel();
		String checkAchieve = vertex.getLabel();
		while(successor != null) {
			if(successor.getType() == Vertex.Type.PERCEPT || successor.getType() == Vertex.Type.INTENTION) {
				if(TermParser.removeAnnots(successor.getLabel()).equals(checkTell)) {
					break;
				}
				if(TermParser.removeAnnots(successor.getLabel()).equals(checkAchieve)) {
					break;
				}
			}
			successor = this.graph.getCharSuccessor(successor);
		}
		if(successor == null) {
			return;
		}
		vertex.setLabel(successor.getLabel());
		vertex.setType(successor.getType());
		this.removeVertex(successor);
		this.eventList.addFirst(vertex);
	}
	
	@Override
	public EdgeVisitResult visitEdge(Edge edge) {
		switch(edge.getType()) {
			case ROOT:
			case TEMPORAL:
				return EdgeVisitResult.CONTINUE;
			default:
				return EdgeVisitResult.TERMINATE;
		}
	}
	
	private void removeVertex(Vertex vertex) {
		this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
	}
}
