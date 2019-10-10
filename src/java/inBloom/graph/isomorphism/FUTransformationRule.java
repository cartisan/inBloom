package inBloom.graph.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;

public class FUTransformationRule implements BiFunction<Vertex, PlotDirectedSparseGraph, PlotDirectedSparseGraph>, Predicate<Vertex> {
	protected static Logger logger = Logger.getLogger(FUTransformationRule.class.getName());

	/**  For each original FU this table caches the FU's that were created by applying all transformations to it's vertex i
	 * (FU-graph, i) -> {Set of transformed FUs} */
	private static HashBasedTable<PlotDirectedSparseGraph, Integer, Collection<PlotDirectedSparseGraph>> transformationCache = HashBasedTable.create();

	/**  All valid transformation rules */
	public static List<FUTransformationRule> TRANSFORMATIONS;
	static {
		PlotDirectedSparseGraph replacement;
		Vertex v1, v2;
		Predicate<Vertex> posEmoTrigger = new Predicate<Vertex>() {
			public boolean test(Vertex v) {
				return UnitVertexType.typeOf(v).matches(UnitVertexType.POSITIVE);
			}
		};
		Predicate<Vertex> negEmoTrigger = new Predicate<Vertex>() {
			public boolean test(Vertex v) {
				return UnitVertexType.typeOf(v).matches(UnitVertexType.NEGATIVE);
			}
		};
		Predicate<Vertex> intTrigger = new Predicate<Vertex>() {
			public boolean test(Vertex v) {
				return UnitVertexType.typeOf(v).matches(UnitVertexType.INTENTION);
			}
		};

		// transformation rules for positive vertices
		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeIntention(0, replacement);
		v2 = FunctionalUnits.makePositive(1, replacement);
		replacement.addEdge(FunctionalUnits.makeActualization(), v1, v2);
		FUTransformationRule POS1 = new FUTransformationRule(replacement, posEmoTrigger);

		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeAction(0, replacement);
		v2 = FunctionalUnits.makePositive(1, replacement);
		replacement.addEdge(FunctionalUnits.makeCausality(), v1, v2);
		FUTransformationRule POS2 = new FUTransformationRule(replacement, posEmoTrigger);

		// transformation rules for negative vertices
		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeIntention(0, replacement);
		v2 = FunctionalUnits.makeNegative(1, replacement);
		replacement.addEdge(FunctionalUnits.makeActualization(), v1, v2);
		FUTransformationRule NEG1 = new FUTransformationRule(replacement, negEmoTrigger);

		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeAction(0, replacement);
		v2 = FunctionalUnits.makeNegative(1, replacement);
		replacement.addEdge(FunctionalUnits.makeCausality(), v1, v2);
		FUTransformationRule NEG2 = new FUTransformationRule(replacement, negEmoTrigger);

		// transformation rules for intention vertices
		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeIntention(0, replacement);
		v2 = FunctionalUnits.makeIntention(1, replacement);
		replacement.addEdge(FunctionalUnits.makeEquivalence(), v1, v2);
		FUTransformationRule INT1 = new FUTransformationRule(replacement, intTrigger);

		replacement = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeIntention(0, replacement);
		v2 = FunctionalUnits.makeIntention(1, replacement);
		replacement.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		FUTransformationRule INT2 = new FUTransformationRule(replacement, intTrigger);

		TRANSFORMATIONS = new ArrayList<>();
		TRANSFORMATIONS.add(POS1);
		TRANSFORMATIONS.add(POS2);
		TRANSFORMATIONS.add(NEG1);
		TRANSFORMATIONS.add(NEG2);
		TRANSFORMATIONS.add(INT1);
		TRANSFORMATIONS.add(INT2);

		Class cls = PlotGraphController.class;
	}

	/**
	 * Returns all graphs that can be created from fuGraph by valid transformations of vertex at position pos.
	 * This method gets called once for each vertex in plot graph for each FU, so to avoid regenerating modified
	 * FU Graph each time it caches its results.
	 * @param pos
	 * @param fuGraph
	 * @return
	 */
	public static Collection<PlotDirectedSparseGraph> getAllTransformations(int pos, PlotDirectedSparseGraph fuGraph) {
		if(transformationCache.contains(fuGraph, pos)) {
			return transformationCache.get(fuGraph, pos);
		}

		// If different transformations result in same modified FU, no need to add both. Check sameness using string representation of ordered vertex list
		TreeSet<PlotDirectedSparseGraph> all = new TreeSet<>(
			new Comparator<PlotDirectedSparseGraph>() {
				@Override
				public int compare(PlotDirectedSparseGraph o1, PlotDirectedSparseGraph o2) {
					if (o1.getOrderedVertexList().toString().equals(o2.getOrderedVertexList().toString())) {
						return 0;
					}
					return -1;
				}});

		for (FUTransformationRule rule: TRANSFORMATIONS) {
			PlotDirectedSparseGraph fuNew = fuGraph.clone();	//clone FU such that changes in vertices won't affect original FU
			Vertex v = fuNew.getVertex(pos);

			if (!v.getLabel().equals(fuGraph.getVertex(pos).getLabel())) {
				throw new RuntimeException("Vertex at pos " + pos + " changed from " +
							fuGraph.getVertex(pos).getLabel() + " to " + v.getLabel() +
							" while cloning FU for transformation during inexact macthing");
			};

			if (rule.test(v)) {
				all.add(rule.apply(v, fuNew));
			}
		}

		transformationCache.put(fuGraph, pos, all);
		return all;
	}


	/**  The graph that will be used to replace a vertex. Before inserting into another graph, this will be cloned. */
	public PlotDirectedSparseGraph replacementGraphPrototype;
	/**  Predicate that returns true when this rule can be applied.*/
	public Predicate<Vertex> trigger;

	public FUTransformationRule(PlotDirectedSparseGraph replacementGraph, Predicate<Vertex> trigger) {
		this.replacementGraphPrototype = replacementGraph;
		this.trigger = trigger;
	}

	/**
	 * Creates a target graph based on fuGraph, where the vertex toReplace is replaced by the replacementGraphPrototype
	 * of this transformation.
	 * @param toReplace vertex to be replaced
	 * @param fuGraph a graph whose vertices may be changed by this method, e.g. the clone of an FU graph
	 * @return
	 */
	@Override
	public PlotDirectedSparseGraph apply(Vertex toReplace, PlotDirectedSparseGraph fuGraph) {
		// clone replacement and fu graph so this rule uses a new graph for replacement every time
		PlotDirectedSparseGraph replacementGraph = this.replacementGraphPrototype.clone();
		Vertex replacementRoot = replacementGraph.getOrderedVertexList().get(0);
		Vertex replacementLeave = replacementGraph.getOrderedVertexList().get(this.replacementGraphPrototype.getVertexCount() - 1);

		// create target graph
		PlotDirectedSparseGraph target = new PlotDirectedSparseGraph();
		Collection<Vertex> allV = new ArrayList<>();
		allV.addAll(fuGraph.getVertices());
		allV.addAll(replacementGraph.getVertices());
		allV.remove(toReplace);
		for (Vertex v: allV) {
			target.addVertex(v);
		}

		// carry over edges from set of 'incoming' vertices to start of replacement graph -- usually wildcard edges cause we dont know vertex types
		// in case of cross character edges use them instead, to maintain inter-char character of edge
		for (Edge edge : fuGraph.getInEdges(toReplace)) {
			if (edge.getType() == Edge.Type.CROSSCHARACTER) {
				target.addEdge(edge, fuGraph.getSource(edge), replacementRoot);
			} else {
				target.addEdge(new Edge(Edge.Type.WILDCARD), fuGraph.getSource(edge), replacementRoot);
			}
		}

		// carry over edges from set of 'outgoing' vertices to end of replacement graph -- usually wildcard edges cause we dont know vertex types
		// in case of cross character edges use them instead, to maintain inter-char character of edge
		for (Edge edge : fuGraph.getOutEdges(toReplace)) {
			if (edge.getType() == Edge.Type.CROSSCHARACTER) {
				target.addEdge(edge, replacementLeave, fuGraph.getDest(edge));
			} else {
				target.addEdge(new Edge(Edge.Type.WILDCARD), replacementLeave, fuGraph.getDest(edge));
			}
		}

		// carry over edges from both this and replacement, if both, the edge's source and destination, were previously imported into target
		for (Edge edge : fuGraph.getEdges()) {
			if ( target.getVertices().contains(fuGraph.getSource(edge)) &
					target.getVertices().contains(fuGraph.getDest(edge)) ) {
				target.addEdge(edge, fuGraph.getSource(edge), fuGraph.getDest(edge));
			}
		}
		for (Edge edge : replacementGraph.getEdges()) {
			if ( target.getVertices().contains(replacementGraph.getSource(edge)) &
					target.getVertices().contains(replacementGraph.getDest(edge)) ) {
				target.addEdge(edge, replacementGraph.getSource(edge), replacementGraph.getDest(edge));
			}
		}

		// update steps in replacement based on previous step
		int prevStep = toReplace.getStep();
		for (Vertex v : replacementGraph.getOrderedVertexList()) {
			v.setStep(prevStep + v.getStep());
		}

		// update steps in fuGraph following toReplace
		int stepDelta = replacementLeave.getStep() - replacementRoot.getStep();
		for (Vertex v: fuGraph.getOrderedVertexList()) {
			if (v.getStep() > toReplace.getStep() & v != toReplace) {
				v.setStep(v.getStep() + stepDelta);
			}
		}

		return target;
	}

	/**
	 * Tests if this TransformationRule is applicable to v, using its {@link #trigger} field.
	 * @param v Vertex to be tested
	 */
	@Override
	public boolean test(Vertex v) {
		return this.trigger.test(v);
	}
}
