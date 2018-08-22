package plotmas.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.asSemantics.Emotion;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.helper.TermParser;

/**
 * This post-process visitor is intended to be used on the
 * full ('raw') plot graph as the first post-process visitor.
 * It will create edges relevant for functional units and
 * collapse emotions, percepts and action into a single vertex.
 * @author Sven Wilke
 *
 */
public class FullGraphPPVisitor implements PlotGraphVisitor {

	private static final boolean KEEP_MOTIVATION = true;
	
	private PlotDirectedSparseGraph graph;
	
	private LinkedList<Vertex> eventList;
	private Vertex currentRoot;
	
	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph.clone();
		this.eventList = new LinkedList<Vertex>();
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
		String label = vertex.getLabel();
		if(label.startsWith("drop_intention")) {
			handleDropIntention(vertex);
			return;
		}
		String[] parts = label.split("\\[motivation\\(");
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
		Pattern pattern = Pattern.compile("drop_intention\\((?<drop>.*?)\\)\\[cause\\((?<cause>.*)\\)\\]");
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
		String causeString = matcher.group("cause").substring(1);
		Vertex cause = null;
		for(Vertex potentialCause : this.eventList) {
			if(potentialCause.getLabel().equals(causeString)) {
				cause = potentialCause;
				break;
			}
		}
		
		if(cause != null) {
			graph.addEdge(new Edge(Edge.Type.TERMINATION), cause, droppedIntention);
			this.removeVertex(vertex);
		} else {
			if(causeString.startsWith("!")) {
				vertex.setType(Vertex.Type.INTENTION);
			} else {
				vertex.setType(Vertex.Type.PERCEPT);
			}
			vertex.setLabel(causeString);
			graph.addEdge(new Edge(Edge.Type.TERMINATION), vertex, droppedIntention);
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
			if(((targetString.equals(cause) || targetString.equals("+" + cause))) 
					& !(targetEvent.hasEmotion(emotion.getName()))) {
				
				targetEvent.addEmotion(emotion.getName());
				this.removeVertex(vertex);
				break;
			}
		}
	}

	@Override
	public void visitPercept(Vertex vertex) {
		String cause = vertex.getCause();
		if(!this.eventList.isEmpty()) {
			for(Vertex targetEvent : this.eventList) {
				if(targetEvent.getLabel().equals(cause)) {
					this.graph.addEdge(new Edge(Edge.Type.CAUSALITY), targetEvent, vertex);
					cause = "";
				}
				if(targetEvent.getType() != Vertex.Type.PERCEPT &&
					targetEvent.getFunctor().equals(vertex.getFunctor())) {
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
		this.lookForPerseverance(vertex);
		this.attachMotivation(vertex);
	}
	
	private void lookForPerseverance(Vertex vertex) {
		for(Vertex target : this.eventList) {
			if(target.getIntention().equals(vertex.getIntention())) {
				graph.addEdge(new Edge(Edge.Type.EQUIVALENCE), vertex, target);
				return;
			}
		}
	}
	
	private void attachMotivation(Vertex vertex) {
		String label = vertex.getLabel();
		String[] parts = label.split("\\[motivation\\(");
		
		if(parts.length > 1) {
			String[] motivations = parts[1].substring(0, parts[1].length() - 2).split(";");
			String resultingLabel = parts[0];
			Set<Vertex> motivationVertices = new HashSet<Vertex>();
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
						//vertex.setMotivation(target);
						motivationVertices.add(target);
						break;
					}
				}
			}
			
			if(!KEEP_MOTIVATION || !motivationVertices.isEmpty())
				vertex.setLabel(resultingLabel);
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
		String checkTell = "+" + vertex.toString();
		String checkAchieve = vertex.toString();
		while(successor != null && !(successor.toString().equals(checkTell) || TermParser.removeAnnots(successor.getLabel()).equals(checkAchieve))) {
			successor = this.graph.getCharSuccessor(successor);
		}
		if(successor == null)
			return;
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
