package inBloom.graph;

import java.util.LinkedList;
import java.util.logging.Logger;

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
		this.eventList.addFirst(vertex);
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
		// check whether this is a percept that reports action outcome: not a happening & must be event addition, so starts with + & has same literal as an action we previously executed
		if(!vertex.getLabel().startsWith("-") & vertex.getCause().isEmpty()) {
			for(Vertex targetEvent : this.eventList) {
				if( targetEvent.getType() == Vertex.Type.ACTION && vertex.getWithoutAnnotation().equals("+" + targetEvent.getWithoutAnnotation()) )	{
					// Extract annotations from vertex and add them to target, which is an action and has no own annotation
					targetEvent.setLabel(TermParser.mergeAnnotations(targetEvent.getLabel(), vertex.getLabel()));

					// remove this percept because it was merged, do not add it to eventList cause it is no potential target for edges
					this.removeVertex(vertex);
					return;
				}
			}
		}

		this.eventList.addFirst(vertex);
	}


	@Override
	public void visitSpeech(Vertex vertex) {
		this.eventList.addFirst(vertex);
	}

	@Override
	public void visitIntention(Vertex vertex) {
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

		if(vertex.getType() == Vertex.Type.PERCEPT) {
			this.eventList.addFirst(vertex);
		}
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
