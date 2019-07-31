package inBloom.graph.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.test.story.helperClasses.TestUnits;

public class FUTransformationRule implements BiFunction<Vertex, PlotDirectedSparseGraph, PlotDirectedSparseGraph>, Predicate<Vertex> {

	/**  All valid transformation rules */
	public static List<FUTransformationRule> TRANSFORMATIONS;
	static {
		PlotDirectedSparseGraph replacement;
		Vertex v1, v2;

		replacement = new PlotDirectedSparseGraph();
		v1 = TestUnits.makeIntention(0, replacement);
		v1.setLabel("REPL Int");
		v2 = TestUnits.makePositive(1, replacement);
		v2.setLabel("REPL Pos");
		replacement.addEdge(TestUnits.makeActualization(), v1, v2);
		Predicate<Vertex> posEmoTrigger = new Predicate<Vertex>() {
			public boolean test(Vertex v) {
				return UnitVertexType.typeOf(v).equals(UnitVertexType.POSITIVE);
			}
		};

		FUTransformationRule POS1 = new FUTransformationRule(replacement, posEmoTrigger);


		replacement = new PlotDirectedSparseGraph();
		v1 = TestUnits.makeAction(1, replacement);
		v2 = TestUnits.makePositive(2, replacement);
		replacement.addEdge(TestUnits.makeCausality(), v1, v2);
		FUTransformationRule POS2 = new FUTransformationRule(replacement, posEmoTrigger);

		TRANSFORMATIONS = new ArrayList<>();
		TRANSFORMATIONS.add(POS1);
		TRANSFORMATIONS.add(POS2);

		Class cls = PlotGraphController.class;
	}

	/**
	 * Returns all graphs that can be created from fuGraph by valid transformations.
	 * @param fuGraph
	 * @return
	 */
	public static Collection<PlotDirectedSparseGraph> getAllTransformations(PlotDirectedSparseGraph fuGraph) {
		Collection<PlotDirectedSparseGraph> all = new ArrayList<>();
		for (int i = 0; i < fuGraph.getVertices().size(); ++i) {
			all.addAll(getAllTransformations(i, fuGraph));
		}

		return all;
	}

	/**
	 * Returns all graphs that can be created from fuGraph by valid transformations of vertex at position pos.
	 * @param pos
	 * @param fuGraph
	 * @return
	 */
	public static List<PlotDirectedSparseGraph> getAllTransformations(int pos, PlotDirectedSparseGraph fuGraph) {
		List<PlotDirectedSparseGraph> all = new ArrayList<>();
		for (FUTransformationRule rule: TRANSFORMATIONS) {
			PlotDirectedSparseGraph fuNew = fuGraph.clone();	//clone FU such that changes in vertices won't affect original FU
			Vertex v = fuNew.getVertex(pos);
			if (rule.test(v)) {
				all.add(rule.apply(v, fuNew));
			}
		}

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

		// create new edges from set of 'incoming' vertices to start of replacement graph
		for (Edge edge : fuGraph.getInEdges(toReplace)) {
			target.addEdge(edge, fuGraph.getSource(edge), replacementRoot);
		}

		// create new edges from end of replacement graph to set of 'outgoing' vertices
		for (Edge edge : fuGraph.getOutEdges(toReplace)) {
			target.addEdge(edge, replacementLeave, fuGraph.getDest(edge));
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
			if (v.getStep() > toReplace.getStep()) {
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
		// TODO: Prevent inserting a node when it doesn't fit the in/out edges of vertex? Or use wildcard edges for connections?
		// e.g. rule: [+] -> [I]-m-[+], input: [-]-m-[I]-a-[+]
		// result: [-]-m-[I]-a-[I]-a-[+]    and [I]-a-[I] being illegal
		return this.trigger.test(v);
	}
}
