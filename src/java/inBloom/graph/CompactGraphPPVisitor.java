package inBloom.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;

import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;

import inBloom.graph.Edge.Type;
import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;
import inBloom.helper.TermParser;

/**
 * This post-process visitor is supposed to operate on the compact graph
 * representation which was created by FullGraphPPVisitor.
 * The graph should have vertices of emotions and perceptions collapsed
 * into the corresponding action, as well as added edges of the types
 * motivation, termination, actualization.
 *
 * Used to perform and insert further analysis into the graph, like
 * terminatination relations between believes.
 *
 * @author Sven Wilke
 */
public class CompactGraphPPVisitor implements PlotGraphVisitor {
	protected static Logger logger = Logger.getLogger(FullGraphPPVisitor.class.getName());

	private PlotDirectedSparseGraph graph;
	private Vertex currentRoot;
	private LinkedList<Vertex> stateList;
	/** Safes which actions and perceptions were annotated with which crossChar ID.
	 *  So that {@link #postProcessing()} can create edges, when several vertices share the same ID.
	 *  Maps: ID -> Multuple Vertices */
	private ArrayListMultimap<String, Vertex> xCharIDMap;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph;
		this.stateList = new LinkedList<>();
		this.xCharIDMap = ArrayListMultimap.create();

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
		// Add entry to xCharIDMap if cross-character annotation present, so that #postProcessing can create the edges
		String crossCharID = TermParser.getAnnotation(vertex.getLabel(), Edge.Type.CROSSCHARACTER.toString());
		if (crossCharID != "") {
			this.xCharIDMap.put(crossCharID, vertex);
		}
	}

	@Override
	public void visitEmotion(Vertex vertex) { }

	@Override
	public void visitPercept(Vertex vertex) {
		boolean isInTradeoff = this.handleTradeoff(vertex);

		if(vertex.hasEmotion()) {
			this.handleAffectiveState(vertex);
		}

		/* remove percept if it is not
		 *  - a wish or obligation that is being added or removed
		 *  - the start of a mood, or the end of a mood that triggers something
		 *	- has motivation edges attached to it
		 *  - has at least one emotion
		 *  - part of a trade-off simple FU
		 */
		if (! (isInTradeoff | this.isRelevantMood(vertex) | this.isWishObligation(vertex) | this.hasMotivation(vertex) | vertex.hasEmotion()) ) {
			this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
			return;
		}

		// Add entry to xCharIDMap if cross-character annotation present, so that #postProcessing can create the edges
		String crossCharID = TermParser.getAnnotation(vertex.getLabel(), Edge.Type.CROSSCHARACTER.toString());
		if (crossCharID != "") {
			this.xCharIDMap.put(crossCharID, vertex);
		}
	}

	private boolean hasMotivation(Vertex vertex) {
		// lets ignore initial beliefs for now, to make graphs less cluttered
		if (vertex.getStep() == 0) {
				return false;
		}
		return vertex.getIncidentEdges().stream().anyMatch(e -> e.getType().equals(Edge.Type.MOTIVATION));
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

	private void handleAffectiveState(Vertex vertex) {
		this.handleLossAndResolution(vertex);
	}

	private boolean handleTradeoff(Vertex vertex) {
		String source = vertex.getSource();
		if(source.isEmpty() || vertex.getLabel().startsWith("+")) {
			return false;
		}

		source = TermParser.removeAnnots(source);

		Vertex src = null;
		// Look for source
		for(Vertex target : this.stateList) {
			if(TermParser.removeAnnots(target.getLabel()).substring(1).equals(source)) {
				// Source found! We take every source!
				src = target;
				break;
			}
		}
		if(src != null) {
			// Let's find the corresponding addition of this mental note.
			for(Vertex target : this.stateList) {
				if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
					if(target.getWithoutAnnotation().substring(0, 1).equals("+")) {
						// Great, found the addition!
						this.createTermination(src, target);
						return true;
					}
				}
			}
		}

		return false;
	}

	private void handleLossAndResolution(Vertex vertex) {
		//Vertex toRemove = null;
		for(Vertex target : this.stateList) {
			// If both vertices are the same event (i.e. -has(bread) and +has(bread))
			if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
				// If the one is an addition while the other is a substraction of a percept
				if(!target.getWithoutAnnotation().substring(0, 1).equals(vertex.getWithoutAnnotation().substring(0, 1))) {
					boolean isPositive = false;
					boolean isNegative = false;
					for(String em : vertex.getEmotions()) {
						isPositive |= Emotion.getEmotion(em).getP() > 0;
						isNegative |= Emotion.getEmotion(em).getP() < 0;
					}
					// This is either loss or resolution (second vertex has both valences!)
					if(isPositive && isNegative) {
						this.createTermination(vertex, target);
						//toRemove = target;
						break;
					// If there is only one valence check the first vertex:
					} else {
						for(String em : target.getEmotions()) {
							// This is a loss!
							if(!isPositive && Emotion.getEmotion(em).getP() > 0) {
								this.createTermination(vertex, target);
								//toRemove = target;
								break;
							} else
							// This is a resolution!
							if(!isNegative && Emotion.getEmotion(em).getP() < 0) {
								this.createTermination(vertex, target);
								//toRemove = target;
								break;
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
	 *   <li> Creating cross-character edges for all perception vertices with same content and step but perceived
	 *        by different agents. That is connecting the vertices of each cell in {@linkplain #stepPerceptTable} that
	 *        has multiple entries. </li>
     * 	 <li> Trimming the repeated actions at the end of the plot that caused execution to pause.</li>
     * </ul>
	 */
	private void postProcessing() {
		// iterate over entries of xCharIDMap, and create cross-character edges like below
		for (String id: this.xCharIDMap.keys()) {
			List<Vertex> connectedEvents = this.xCharIDMap.get(id);

			// create edges between all vertices in cell (cell obv. should have more than one vertex
			if (connectedEvents.size() > 1) {
				// create all pairwise combinations for vertices in cell, connect all pairs
				Set<Set<Vertex>> pairs = Sets.combinations(new HashSet<>(connectedEvents), 2);

				for(Set<Vertex> pair : pairs) {
					ArrayList<Vertex> pList = new ArrayList<>(pair);

					// if both vertices belong to same character, no cross-character edge is required
					if (pList.get(0).getRoot().equals(pList.get(1).getRoot())) {
						continue;
					}

					// create x-character edges
					this.graph.addEdge(new Edge(Edge.Type.CROSSCHARACTER), pList);

					// connect bi-directionally by creating reversed edges
					Collections.reverse(pList);
					this.graph.addEdge(new Edge(Edge.Type.CROSSCHARACTER), pList);
				}
			}
		}

		// TODO: Remove repeating pattern at end?
	}
}
