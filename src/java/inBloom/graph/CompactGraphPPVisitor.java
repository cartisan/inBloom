package inBloom.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;

import inBloom.graph.Edge.Type;
import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;

/**
 * This post-process visitor is supposed to operate on the compact graph
 * representation which was created by FullGraphPPVisitor.
 * The graph should have vertices of emotions and perceptions collapsed
 * into the corresponding action, as well as added edges of the types
 * motivation, actualization, causation and x-character.
 *
 * Used to perform and insert further analysis into the graph, based on
 * primitive FUs.
 *
 * @author Sven Wilke
 */
public class CompactGraphPPVisitor implements PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(FullGraphPPVisitor.class.getName());

	private PlotDirectedSparseGraph graph;
	private Vertex currentRoot;
	private LinkedList<Vertex> intentionList;
	private LinkedList<Vertex> stateList;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph;
		this.stateList = new LinkedList<>();
		this.intentionList = new LinkedList<>();

		this.graph.accept(this);
		this.postProcessing();

		return this.graph;
	}

	@Override
	public void visitRoot(Vertex vertex) {
		this.currentRoot = vertex;
		this.stateList.clear();
		this.intentionList.clear();
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
		if(vertex.hasEmotion()) {
			this.handleLossAndResolution(vertex);
		}

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

	private void handleLossAndResolution(Vertex vertex) {
		for(Vertex target : this.stateList) {
			// If both vertices are the same event (i.e. -has(bread) and +has(bread))
			if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
				// If the one is an addition while the other is a substraction of a percept
				if(!target.getWithoutAnnotation().substring(0, 1).equals(vertex.getWithoutAnnotation().substring(0, 1))) {
					// If both vertices belong to same character
					if(target.getRoot().equals(vertex.getRoot())) {
						boolean isPositive = false;
						boolean isNegative = false;
						for(String em : vertex.getEmotions()) {
							isPositive |= Emotion.getEmotion(em).getP() > 0;
							isNegative |= Emotion.getEmotion(em).getP() < 0;
						}
						// This is either loss or resolution (second vertex has both valences!)
						if(isPositive && isNegative) {
							this.createTermination(vertex, target);
							break;
						// If there is only one valence check the first vertex:
						} else {
							for(String em : target.getEmotions()) {
								// This is a loss!
								if(!isPositive && Emotion.getEmotion(em).getP() > 0) {
									this.createTermination(vertex, target);
									break;
								} else
								// This is a resolution!
								if(!isNegative && Emotion.getEmotion(em).getP() < 0) {
									this.createTermination(vertex, target);
									break;
								}
							}
						}
					}
				}
			}
		}
		this.stateList.addFirst(vertex);
	}

	private void createTermination(Vertex from, Vertex to) {
		this.graph.addEdge(new Edge(Edge.Type.TERMINATION), from, to);
	}

	@Override
	public void visitSpeech(Vertex vertex) {

	}

	@Override
	public void visitIntention(Vertex vertex) {
		this.lookForPerseverance(vertex);
		this.intentionList.addFirst(vertex);
	}

	private void lookForPerseverance(Vertex vertex) {
		for(Vertex target : this.intentionList) {
			if(target.getIntention().equals(vertex.getIntention()) & target.getRoot().equals(vertex.getRoot())  ) {
				this.graph.addEdge(new Edge(Edge.Type.EQUIVALENCE), vertex, target);	//equivalence edges point up
				return;
			}
		}
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
