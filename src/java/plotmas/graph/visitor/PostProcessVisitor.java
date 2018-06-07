package plotmas.graph.visitor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import jason.asSemantics.Emotion;
import jason.asSyntax.parser.ParseException;
import plotmas.graph.Edge;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.Vertex;

public class PostProcessVisitor implements PlotGraphVisitor {

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
		String[] parts = label.split("\\[motivation\\(");
		if(parts.length > 1) {
			String motivation = parts[1].substring(0, parts[1].length() - 2).split("\\[")[0];
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
				targetEvent.addEmotion(emotion.getName());
				
				this.removeVertex(vertex);
				break;
			}
		}
	}

	@Override
	public void visitPercept(Vertex vertex) {
		if(!this.eventList.isEmpty() &&
			eventList.getFirst().getType() == Vertex.Type.EVENT &&
			eventList.getFirst().getFunctor().equals(vertex.getFunctor())) {
			
			this.removeVertex(vertex);
		} else {
			this.eventList.addFirst(vertex);
		}
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
				motivation = motivation.split("\\[")[0];

				for(Vertex target : this.eventList) {
					boolean isMotivation = false;
					
					// Check for intentions
					isMotivation = isMotivation ||
							motivation.equals(target.getIntention());
					
					// Check for percepts
					isMotivation = isMotivation ||
							motivation.equals(target.getLabel().split("\\[")[0]);
					
					// Check for listens
					isMotivation = isMotivation ||
							motivation.equals(target.getLabel().split("\\[")[0].substring(1));
					
					if(isMotivation && !motivationVertices.contains(target)) {
						this.graph.addEdge(new Edge(Edge.Type.MOTIVATION), target, vertex);
						//vertex.setMotivation(target);
						motivationVertices.add(target);
						
						break;
					}
				}
			}
			
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
