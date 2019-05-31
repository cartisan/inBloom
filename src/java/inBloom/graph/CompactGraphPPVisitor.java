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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import inBloom.graph.Edge.Type;
import inBloom.graph.visitor.EdgeVisitResult;
import inBloom.graph.visitor.PlotGraphVisitor;
import inBloom.helper.TermParser;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;

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
	/** Saves which perception was generated at which step, so that shared events can be identified during post-processing.
	 *  Maps: (step, percept label w/out annotations) -> List of vertices fitting that description.
	 *  Whenever several vertices are in one cell they are shared events. **/
	private Table<Integer, String, List<Vertex>> stepPerceptTable;

	public PlotDirectedSparseGraph apply(PlotDirectedSparseGraph graph) {
		this.graph = graph;
		this.stateList = new LinkedList<Vertex>();
		this.stepPerceptTable = HashBasedTable.create();
		
		this.graph.accept(this);
		this.postProcessing();
		
		return this.graph;
	}
	
	@Override
	public void visitRoot(Vertex vertex) {
		currentRoot = vertex;
	}

	@Override
	public void visitEvent(Vertex vertex) {
		logger.severe("No EVENT vertices should be left by this stage of preprocessing: " + vertex.getLabel());
	}
	
	@Override
	public void visitAction(Vertex vertex) { }

	@Override
	public void visitEmotion(Vertex vertex) { }
	
	@Override
	public void visitPercept(Vertex vertex) {
		boolean isInTradeoff = handleTradeoff(vertex);
		
		if(vertex.hasEmotion()) {
			handleAffectiveState(vertex);
		}
		
		/* remove percept if it is not
		 *  - a wish or obligation that is being added or removed
		 *  - the start of a mood, or the end of a mood that triggers something 
		 *	- has motivation edges attached to it
		 *  - has at least one emotion
		 *  - part of a trade-off simple FU
		 */
		if (! (isInTradeoff | isRelevantMood(vertex) | isWishObligation(vertex) | hasMotivation(vertex) | vertex.hasEmotion()) ) {
			this.graph.removeVertexAndPatchGraphAuto(this.currentRoot, vertex);
			return;
		}
		
		// only perceptions of physical, non-listen events are candidates for shared events
		if (!isWishObligation(vertex) & !isRelevantMood(vertex) & !isListen(vertex) & vertex.getLabel().startsWith("+")) {
			this.safeSharedEventCandidate(vertex);
		}
	}

	private boolean isListen(Vertex vertex) {
		if( this.graph.getInEdges(vertex).stream().anyMatch(e -> e.getType().equals(Edge.Type.CROSSCHARACTER))) {
			return true;
		}
		return false;
	}

	/**
	 * Safes the vertex in stepPerceptTable because it is a candidate for a shared perception.
	 * Checks if appropriate cell is already initialized with a list. If true just adds new candidate vertex,
	 * else initialize with a list first.
	 * @param vertex
	 */
	private void safeSharedEventCandidate(Vertex vertex) {
		if (this.stepPerceptTable.contains(vertex.getStep(), vertex.getWithoutAnnotation())) {
			this.stepPerceptTable.get(vertex.getStep(), vertex.getWithoutAnnotation()).add(vertex);
		} else {
			List<Vertex> l = Lists.newArrayList(vertex);
			this.stepPerceptTable.put(vertex.getStep(), vertex.getWithoutAnnotation(), l);
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
		return (vertex.getLabel().contains("wish") | vertex.getLabel().contains("obligation"));
	}
	
	private boolean isRelevantMood(Vertex vertex) {
		if (vertex.toString().contains(Mood.ANNOTATION_FUNCTOR)) {
			// include mood vertices that are important for other vertices
			boolean connected = graph.getIncidentEdges(vertex).stream().map(e -> e.getType())
																.filter(e -> e != Type.TEMPORAL & e != Type.ROOT)
																.collect(Collectors.toList())
																.size() > 0;
			boolean addition = vertex.toString().startsWith("+");
			return connected | addition;
		}
		return false;
	}
	
	private void handleAffectiveState(Vertex vertex) {
		handleLossAndResolution(vertex);
	}
	
	private boolean handleTradeoff(Vertex vertex) {
		String source = vertex.getSource();
		if(source.isEmpty() || vertex.getLabel().startsWith("+")) {
			return false;
		}
		
		source = TermParser.removeAnnots(source);
		
		Vertex src = null;
		// Look for source
		for(Vertex target : stateList) {
			if(TermParser.removeAnnots(target.getLabel()).substring(1).equals(source)) {
				// Source found! We take every source!
				src = target;
				break;
			}
		}
		if(src != null) {
			// Let's find the corresponding addition of this mental note.
			for(Vertex target : stateList) {
				if(target.getWithoutAnnotation().substring(1).equals(vertex.getWithoutAnnotation().substring(1))) {
					if(target.getWithoutAnnotation().substring(0, 1).equals("+")) {
						// Great, found the addition!
						createTermination(src, target);
						return true;
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
					for(String em : vertex.getEmotions()) {
						isPositive |= Emotion.getEmotion(em).getP() > 0;
						isNegative |= Emotion.getEmotion(em).getP() < 0;
					}
					// This is either loss or resolution (second vertex has both valences!)
					if(isPositive && isNegative) {
						createTermination(vertex, target);
						//toRemove = target;
						break;
					// If there is only one valence check the first vertex:
					} else {
						for(String em : target.getEmotions()) {
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
	
	/**
	 * Performs all post-processing tasks needed to finish the compact graph. This includes:
	 * <ul>
	 *   <li> Creating cross-character edges for all perception vertices with same content and step but perceived 
	 *        by different agents. That is conecting the vertices of each cell in {@linkplain #stepPerceptTable} that 
	 *        has multiple entries. </li>
     * 	 <li> Trimming the repeated actions at the end of the plot that caused execution to pause.</li>
     * </ul>
	 */
	private void postProcessing() {
		for (Cell<Integer, String, List<Vertex>> cell : this.stepPerceptTable.cellSet()) {
			// create edges between all vertices in cell (cell obv. should have more than one vertex
			if (cell.getValue().size() > 1) {
				// create all pairwise combinations for vertices in cell, connect all pairs
				Set<Set<Vertex>> pairs = Sets.combinations(new HashSet<Vertex>(cell.getValue()), 2);
				
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
