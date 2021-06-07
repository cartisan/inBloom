package inBloom.graph.visitor;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jason.asSemantics.Emotion;

import inBloom.graph.Vertex;
import inBloom.helper.TermParser;

/**
 * This post-process visitor is intended to be used on the full ('raw') plot graph as the first post-process visitor.
 * It will collapse emotions, percepts and action into a single vertex.
 * @author Sven Wilke
 */
public class VertexMergingPPVisitor extends PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(VertexMergingPPVisitor.class.getName());

	private LinkedList<Vertex> eventList;
	private Vertex currentRoot;

	public VertexMergingPPVisitor() {
		this.eventList = new LinkedList<>();
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
		// Create emotion from toString() representation of emotion
		// (instead of previously used literal representation)
		Emotion emotion = TermParser.emotionFromString(vertex.getLabel());
		if(emotion == null) {
			logger.severe("Emotion in PP was invalid. " + vertex.toString());
			return;
		}

		String cause = TermParser.removeAnnots(emotion.getCause());

		for(Vertex targetEvent : this.eventList) {
			if(!targetEvent.getIntention().isEmpty()) {
				continue;
			}

			String targetString = targetEvent.getLabel();

			// Needs to match with or without '+'; with for percepts and without for actions
			if((TermParser.removeAnnots(targetString).equals(cause) ||
					TermParser.removeAnnots(targetString).equals("+" + cause)) &&
						!targetEvent.hasEmotion(emotion.getName())) {
				// all annotations in cause should be present in target (but not the other way around!)
				boolean annotationMismatch = false;
				Map<String, String> causeAnnotMap= TermParser.getAnnotationsMap(emotion.getCause());
				for (Entry<String, String> entry : causeAnnotMap.entrySet()) {
					String targetTerm = TermParser.getAnnotation(targetString, entry.getKey());
					if(targetTerm.equals("") | !targetTerm.equals(entry.getValue())) {
						annotationMismatch = true;
					}
				}
				if(!annotationMismatch) {
					targetEvent.addEmotion(emotion.getName());
					this.removeVertex(vertex);
					break;
				}
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

	private void removeVertex(Vertex vertex) {
		this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
	}
}
