package inBloom.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.asSemantics.Emotion;

import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;
import inBloom.helper.TermParser;

/**
 * This post-process visitor is intended to be used on the
 * full ('raw') plot graph as the first post-process visitor.
 * It will create edges relevant for functional units and
 * collapse emotions, percepts and action into a single vertex.
 * @author Sven Wilke
 */
public class FullGraphPPVisitor implements PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(FullGraphPPVisitor.class.getName());

	private static final boolean KEEP_MOTIVATION = true;


	private PlotDirectedSparseGraph graph;

	private LinkedList<Vertex> eventList;
	private Vertex currentRoot;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph.clone();
		this.eventList = new LinkedList<>();
		this.graph.accept(this);
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
			Logger.getGlobal().info("Emotion in PP was invalid. " + vertex.toString());
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
		if(!this.eventList.isEmpty()) {
			String cause = vertex.getCause();

			for(Vertex targetEvent : this.eventList) {
				// create causality edge from cause annotation of a happening-perception
				if(targetEvent.getWithoutAnnotation().equals(cause) |  //our cause was an action, so targetEvent was perceived as-is
				  targetEvent.getWithoutAnnotation().equals("+" + cause))  // our cause was a happening, so targetEvent was perceived as +cause
				{
					this.graph.addEdge(new Edge(Edge.Type.CAUSALITY), targetEvent, vertex);
					cause = "";
					break;
				}

				// merge percepts that report action outcomes (which are always event additions) into respective action vertex
				if(targetEvent.getType() == Vertex.Type.ACTION && targetEvent.getFunctor().equals(vertex.getFunctor()) && !vertex.getLabel().startsWith("-")) {
					// Extract annotations from vertex and add them to target, which is an action and has no own annotation
					// TODO: Make sure action doesn't already have annotations from previous merge
					String annots = TermParser.getAnnots(vertex.getLabel());
					targetEvent.setLabel(targetEvent.getLabel() + annots);

					this.removeVertex(vertex);
					return;
				}
			}
		}
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

		this.lookForPerseverance(vertex);
		this.attachMotivation(vertex);
	}

	private void lookForPerseverance(Vertex vertex) {
		for(Vertex target : this.eventList) {
			if(target.getIntention().equals(vertex.getIntention())) {
				this.graph.addEdge(new Edge(Edge.Type.EQUIVALENCE), vertex, target);
				return;
			}
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

	@Override
	public void visitListen(Vertex vertex) {
		/*if(vertex.getFunctor().equals("rejected_request")) {
			String label = vertex.getLabel();
			String request = "";
			int reqStart = label.indexOf('(');
			int inbetweenParantheses = 0;
			for(int i = reqStart + 1; i <= label.length(); i++) {
				switch(label.charAt(i)) {
					case '(':
						inbetweenParantheses++;
						break;
					case ')':
						inbetweenParantheses--;
					break;
					default:
						break;
				}
				if(inbetweenParantheses < 0) {
					request = label.substring(reqStart + 1, i);
					break;
				}
			}

			for(Vertex target : this.eventList) {
				if(target.getType() == Vertex.Type.SPEECHACT) {
					if(target.getFunctor().startsWith("achieve")) {
						String achvReq = target.getLabel().split(target.getFunctor())[1];
						String reqMatcher = "(" + request + ")";
						if(achvReq.equals(reqMatcher)) {
							this.graph.addEdge(new Edge(Edge.Type.TERMINATION), vertex, target);
							break;
						}
					}
				}
			}
		}*/
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
		//Vertex predecessor = this.eventList.isEmpty() ? this.currentRoot : this.eventList.getFirst();
		//this.graph.removeVertexAndPatchGraph(vertex, predecessor);
		this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
	}
}
