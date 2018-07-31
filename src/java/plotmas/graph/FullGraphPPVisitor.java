package plotmas.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.asSemantics.Emotion;
import jason.asSyntax.parser.ParseException;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.helper.TermParser;

/**
 * This post-process visitor is intended to be used on the
 * full ('raw') plot graph as the first post-process visitor.
 * It will create edges relevant for functional units and
 * collapse emotions, percepts and events into a single vertex.
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
		try {
			emotion = Emotion.parseString(vertex.getLabel());
		} catch(ParseException e) {
			return;
		}
		
		for(Vertex targetEvent : this.eventList) {
			
			if(!targetEvent.getIntention().isEmpty()) {
				continue;
			}
			
			if((targetEvent.getWithoutAnnotation().equals(emotion.getCause())) 
					& !(targetEvent.hasEmotion(emotion.getName()))) {
				boolean hasAnyEmotion = false;
				for(String emot : Emotion.getAllEmotions()) {
					if(targetEvent.hasEmotion(emot)) {
						hasAnyEmotion = true;
						break;
					}
				}
				if(hasAnyEmotion) {
					this.removeVertex(vertex);
					Vertex clonedEvent = targetEvent.clone();
					Collection<Edge> edges = graph.getOutEdges(targetEvent);
					Edge temp = null;
					for(Edge e : edges) {
						if(e.getType() == Edge.Type.TEMPORAL) {
							temp = e;
							break;
						}
					}
					Vertex next = graph.getDest(temp);
					graph.removeEdge(temp);
					graph.addVertex(clonedEvent);
					clonedEvent.addEmotion(emotion.getName());
					graph.addEdge(new Edge(Edge.Type.TEMPORAL), targetEvent, clonedEvent);
					graph.addEdge(new Edge(Edge.Type.TEMPORAL), clonedEvent, next);
					graph.addEdge(new Edge(Edge.Type.EQUIVALENCE), targetEvent, clonedEvent);
					break;
				} 
				
				targetEvent.addEmotion(emotion.getName());
				this.removeVertex(vertex);
				break;
			}
		}
	}

	@Override
	public void visitPercept(Vertex vertex) {
		if(!this.eventList.isEmpty()) {
			for(Vertex targetEvent : this.eventList) {
				if(targetEvent.getType() == Vertex.Type.EVENT &&
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
		this.attachMotivation(vertex);
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
		if(vertex.getFunctor().equals("rejected_request")) {
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
		}
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
