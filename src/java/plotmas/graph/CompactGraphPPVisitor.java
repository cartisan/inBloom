package plotmas.graph;

import java.util.Collection;
import java.util.LinkedList;

import jason.asSemantics.Emotion;
import plotmas.graph.Edge.Type;
import plotmas.graph.visitor.EdgeVisitResult;
import plotmas.graph.visitor.PlotGraphVisitor;
import plotmas.helper.TermParser;

/**
 * This post-process visitor is supposed to operate on the compact graph
 * representation which was created by FullGraphPPVisitor.
 * The graph should have vertices of emotions and perceptions collapsed
 * into the corresponding action, as well as added edges of the types
 * motivation, termination, actualization.
 * 
 * Currently unused.
 * @author Sven Wilke
 *
 */
public class CompactGraphPPVisitor implements PlotGraphVisitor {
	
	private PlotDirectedSparseGraph graph;
	
	private Vertex currentRoot = null;
	private LinkedList<Vertex> stateList = new LinkedList<Vertex>();
	
	public CompactGraphPPVisitor(PlotDirectedSparseGraph graph) {
		this.graph = graph;
	}

	@Override
	public void visitRoot(Vertex vertex) {
		currentRoot = vertex;
	}

	@Override
	public void visitEvent(Vertex vertex) { }
	
	@SuppressWarnings("unused")
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
	public void visitEmotion(Vertex vertex) { }

	@Override
	public void visitPercept(Vertex vertex) {
		boolean isInvolved = handlePositiveTradeoff(vertex);
		for(String emotion : Emotion.getAllEmotions()) {
			if(vertex.hasEmotion(emotion)) {
				handleAffectiveState(vertex);
				return;
			}
		}
		if(!isInvolved)
			this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
	}
	
	
	
	private void handleAffectiveState(Vertex vertex) {
		handleLossAndResolution(vertex);
	}
	
	private boolean handlePositiveTradeoff(Vertex vertex) {
		String source = vertex.getSource();
		if(source.isEmpty()) {
			return false;
		}
		
		source = TermParser.removeAnnots(source);
		
		Vertex src = null;
		// Look for source
		for(Vertex target : stateList) {
			if(TermParser.removeAnnots(target.getLabel()).substring(1).equals(source)) {
				// Source found! We only need it if it's positive though!
				for(String emotion : Emotion.getAllEmotions()) {
					if(target.hasEmotion(emotion) && Emotion.getEmotion(emotion).getP() > 0) {
						src = target;
						break;
					}
				}
			}
		}
		if(src != null) {
			// Let's find an opposite mental note (addition if this is subtraction and vice-versa).
			for(Vertex target : stateList) {
				if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
					if(!target.getWithoutAnnotation().substring(0, 1).equals(vertex.getWithoutAnnotation().substring(0, 1))) {
						// Great, they are opposite. Now let's look whether the opposite one has a positive emotion as well
						for(String emotion : Emotion.getAllEmotions()) {
							if(target.hasEmotion(emotion) && Emotion.getEmotion(emotion).getP() > 0) {
								// It actually has one! We found a positive tradeoff!
								graph.addEdge(new Edge(Edge.Type.TERMINATION), src, target);
								return false;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private void handleLossAndResolution(Vertex vertex) {
		//Vertex toRemove = null;
		for(Vertex target : stateList) {
			// If both vertices are the same event (i.e. -has(bread) and +has(bread))
			if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
				// If the one is an addition while the other is a substraction of a percept
				if(!target.getWithoutAnnotation().substring(0, 1).equals(vertex.getWithoutAnnotation().substring(0, 1))) {
					boolean isPositive = false;
					boolean isNegative = false;
					for(String em : Emotion.getAllEmotions()) {
						if(vertex.hasEmotion(em)) {
							isPositive |= Emotion.getEmotion(em).getP() > 0;
							isNegative |= Emotion.getEmotion(em).getP() < 0;
						}
					}
					// This is either loss or resolution (second vertex has both valences!)
					if(isPositive && isNegative) {
						createTermination(vertex, target);
						//toRemove = target;
						break;
					// If there is only one valence check the first vertex:
					} else {
						for(String em : Emotion.getAllEmotions()) {
							if(target.hasEmotion(em)) {
								// This is a loss!
								if(!isPositive && Emotion.getEmotion(em).getP() > 0) {
									createTermination(vertex, target);
									//toRemove = target;
									break;
								} else
								// This is a resolution!
								if(!isNegative && Emotion.getEmotion(em).getP() < 0) {
									createTermination(vertex, target);
									//toRemove = target;
									break;
								}
							}
						}
					}
				}
			}
		}
		stateList.addFirst(vertex);
	}
	
	private void createTermination(Vertex from, Vertex to) {
		graph.addEdge(new Edge(Edge.Type.TERMINATION), from, to);
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
