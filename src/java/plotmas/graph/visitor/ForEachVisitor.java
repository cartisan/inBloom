package plotmas.graph.visitor;

import java.util.function.Consumer;

import plotmas.graph.Edge;
import plotmas.graph.Vertex;

/**
 * Calls a given action on all vertices of a graph.
 * Example usage:
 * {@code
 * 	ForEachVisitor visitor = new ForEachVisitor(vertex -> vertex.setLabel(graph.getAgent(vertex)));
 *	graph.accept(visitor);
 * }
 * @author Sven Wilke
 */
public class ForEachVisitor extends PlotGraphVisitorAdapter {

	private Consumer<Vertex> action;

	public ForEachVisitor(Consumer<Vertex> action) {
		this.action = action;
	}
	
	@Override
	public void visitVertex(Vertex vertex) {
		action.accept(vertex);
	}
	
	@Override
	public void visitRoot(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitEvent(Vertex vertex) {
		this.visitVertex(vertex);
	}
	
	@Override
	public void visitAction(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitPercept(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitListen(Vertex vertex) {
		this.visitVertex(vertex);
	}

	@Override
	public void visitIntention(Vertex vertex) {
		this.visitVertex(vertex);
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

}
