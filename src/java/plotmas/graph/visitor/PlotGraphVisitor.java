package plotmas.graph.visitor;

import plotmas.graph.Edge;
import plotmas.graph.Vertex;

public interface PlotGraphVisitor {

	public void visitRoot		(Vertex vertex);
	public void visitEvent		(Vertex vertex);
	public void visitEmotion	(Vertex vertex);
	public void visitPercept	(Vertex vertex);
	public void visitSpeech		(Vertex vertex);
	public void visitListen		(Vertex vertex);
	public void visitIntention	(Vertex vertex);
	
	public EdgeVisitResult visitEdge(Edge edge);
}
