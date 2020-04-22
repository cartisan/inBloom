package inBloom.graph;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Mood;

import inBloom.graph.Edge.Type;
import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;

/**
 * This post-process visitor is supposed to operate one the fully analysed plot graph created by
 * {@linkplain EdgeGenerationPPVisitor}.
 * The graph should have vertices of emotions and perceptions collapsed into the corresponding action, as well as
 * added semantic edges of the types motivation, actualization, equality, causation and x-character.
 * Used to filter irrelevant vertices from the final version of the graph.
 * @author Sven Wilke
 */
public class VisualizationFilterPPVisitor implements PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(VisualizationFilterPPVisitor.class.getName());

	private PlotDirectedSparseGraph graph;
	private Vertex currentRoot;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph.clone();

		this.graph.accept(this);
		this.postProcessing();

		return this.graph;
	}

	@Override
	public void visitRoot(Vertex vertex) {
		this.currentRoot = vertex;
	}

	@Override
	public void visitEvent(Vertex vertex) {
		logger.severe("No EVENT vertices should be left by this stage of preprocessing: " + vertex.getLabel());
	}

	@Override
	public void visitAction(Vertex vertex) {
	}

	@Override
	public void visitEmotion(Vertex vertex) { }

	@Override
	public void visitPercept(Vertex vertex) {
		// TODO: merge percepts into action here instead of VertexMergingPPVisitor?
		// Would allow to get rid of addition of + during causality matching in EdgeGenerationPPVisitor#visitPercept.
		/* remove percept if it is not
		 *  - a wish or obligation that is being added or removed 					[for pwt visualization]
		 *  - the start of a mood, or the end of a mood that triggers something		[for visual clarity]
		 *	- has relevant edges attached to it										[for FU]
		 *  - has at least one emotion												[for FU]
		 */
		if (! (this.isRelevantMood(vertex) | this.isWishObligation(vertex) | this.hasEdges(vertex) | vertex.hasEmotion()) ) {
			this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
			return;
		}
	}

	private boolean hasEdges(Vertex vertex) {
		// lets ignore initial beliefs for now, to make graphs less cluttered
		if (vertex.getStep() == 0) {
				return false;
		}
		return vertex.getIncidentEdges().stream().anyMatch(e -> e.getType().equals(Edge.Type.MOTIVATION) |
//																e.getType().equals(Edge.Type.TERMINATION)|
																e.getType().equals(Edge.Type.ACTUALIZATION)|
																e.getType().equals(Edge.Type.CAUSALITY) |
																e.getType().equals(Edge.Type.EQUIVALENCE));
	}

	private boolean isWishObligation(Vertex vertex) {
		return vertex.getLabel().contains("wish") | vertex.getLabel().contains("obligation");
	}

	private boolean isRelevantMood(Vertex vertex) {
		if (vertex.toString().contains(Mood.ANNOTATION_FUNCTOR)) {
			// include mood vertices that are important for other vertices
			boolean connected = this.graph.getIncidentEdges(vertex).stream().map(e -> e.getType())
																.filter(e -> e != Type.TEMPORAL & e != Type.ROOT)
																.collect(Collectors.toList())
																.size() > 0;
			boolean addition = vertex.toString().startsWith("+");
			return connected | addition;
		}
		return false;
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

	/**
	 * Clones a vertex v and inserts the clone as the predecessor of v in a subgraph
	 * @param v vertex to be cloned
	 * @return clone of vertex v
	 */
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
		Vertex u = v.clone(this.graph);
		this.graph.addVertex(u);
		for(Edge e : edgesIn) {
			if(e == edge) {
				continue;
			}
			this.graph.addEdge(e.clone(), this.graph.getSource(e), u);
		}
		for(Edge e : edgesOut) {
			if(e.getType() == Type.TEMPORAL) {
				continue;
			}
			this.graph.addEdge(e.clone(), u, this.graph.getDest(e));
		}
		this.graph.addEdge(new Edge(Edge.Type.TEMPORAL), w, u);
		this.graph.addEdge(new Edge(Edge.Type.TEMPORAL), u, v);
		return u;
	}

	/**
	 * Performs all post-processing tasks needed to finish the compact graph. This includes:
	 * <ul>
     * 	 <li> Trimming the repeated actions at the end of the plot that caused execution to pause.</li>
     * </ul>
	 */
	private void postProcessing() {
		// TODO: Remove repeating pattern at end?
	}
}
