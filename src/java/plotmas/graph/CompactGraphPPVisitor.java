package plotmas.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import jason.asSemantics.Emotion;
import plotmas.graph.Edge.Type;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;

/**
 * This post-process visitor is supposed to operate on the compact graph
 * representation which was created by FullGraphPPVisitor.
 * The graph should have vertices of emotions and perceptions collapsed
 * into the corresponding action, as well as added edges of the types
 * motivation, termination, actualization.
 * @author Sven Wilke
 *
 */
public class CompactGraphPPVisitor implements PlotGraphVisitor {
	
	private PlotDirectedSparseGraph graph;
	
	public CompactGraphPPVisitor(PlotDirectedSparseGraph graph) {
		this.graph = graph;
	}
	
	@Override
	public void visitRoot(Vertex vertex) {

	}

	@Override
	public void visitEvent(Vertex vertex) {
		boolean hasPositiveP = false;
		boolean hasNegativeP = false;
		Set<Emotion> allPositives = new HashSet<Emotion>();
		for(Supplier<Emotion> emoSupp : Emotion.EMOTIONS.values()) {
			Emotion emotion = emoSupp.get();
			double pleasure = emotion.getP();
			if(pleasure > 0 && hasPositiveP) {
				continue;
			}
			if(pleasure < 0 && hasNegativeP) {
				continue;
			}
			if(vertex.hasEmotion(emotion.getName())) {
				if(pleasure < 0) {
					hasNegativeP = true;
				} else
				if(pleasure > 0) {
					allPositives.add(emotion);
					hasPositiveP = true;
				}
			}
			if(hasPositiveP && hasNegativeP) {
				break;
			}
		}
		if(hasPositiveP && hasNegativeP) {
			Vertex wertex = cloneVertexToPredecessor(vertex);
			for(Emotion e : allPositives) {
				wertex.addEmotion(e.name);
				vertex.removeEmotion(e.name);
			}
		}
	}
	
	private Vertex cloneVertexToPredecessor(Vertex v) {
		Collection<Edge> edgesIn = this.graph.getInEdges(v);
		Collection<Edge> edgesOut = this.graph.getOutEdges(v);
		Vertex w = null;
		Edge edge = null;
		for(Edge e : edgesIn) {
			if(e.getType() == Type.TEMPORAL) {
				w = this.graph.getSource(e);
				edge = e;
				break;
			}
		}
		assert w != null;
		this.graph.removeEdge(edge);
		Vertex u = v.clone();
		this.graph.addVertex(u);
		for(Edge e : edgesIn) {
			if(e == edge)
				continue;
			this.graph.addEdge(e.clone(), this.graph.getSource(e), u);
		}
		for(Edge e : edgesOut) {
			if(e.getType() == Type.TEMPORAL)
				continue;
			this.graph.addEdge(e.clone(), u, this.graph.getDest(e));
		}
		this.graph.addEdge(new Edge(Edge.Type.TEMPORAL), w, u);
		this.graph.addEdge(new Edge(Edge.Type.TEMPORAL), u, v);
		return u;
	}

	@Override
	public void visitEmotion(Vertex vertex) {
		
	}

	@Override
	public void visitPercept(Vertex vertex) {
		
	}

	@Override
	public void visitSpeech(Vertex vertex) {
		
	}
	
	@Override
	public void visitIntention(Vertex vertex) {
		
	}

	@Override
	public void visitListen(Vertex vertex) {
		
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
