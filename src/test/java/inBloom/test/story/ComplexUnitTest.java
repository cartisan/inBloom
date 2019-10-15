package inBloom.test.story;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;

public class ComplexUnitTest  {

	/**
	 * Testing:
	 *    [-]<-|
	 *     |m  |
	 *    [I]  |t
 	 *     |a  |
	 *    [+]--|
	 */
	@Test
	public void testFindMinimalMapping() {
		UnitFinder finder = new UnitFinder();

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeNegative(1, plotGraph);
		v1.setLabel("PLOT -");
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		v2.setLabel("PLOT I");
		Vertex v3 = FunctionalUnits.makePositive(3, plotGraph);
		v3.setLabel("PLOT +");
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v2, v3);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v3, v1);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph, 1);

		assertEquals(1, mappings.size());
	}

	/**
	 * Testing:
	 *    [-]<-|
	 *     |m  |
	 *    [I]  |t
	 *     |a  |
	 *    [+]--|
	 *     |m
	 *    [I]
	 */
	@Test
	public void testFindMappingInLargerGraph() {
		UnitFinder finder = new UnitFinder();

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeNegative(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		Vertex v3 = FunctionalUnits.makePositive(3, plotGraph);
		Vertex v4 = FunctionalUnits.makeIntention(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v2, v3);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v3, v1);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v3, v4);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph, 1);

		assertEquals(1, mappings.size());
	}

	/**
	 * Testing:
	 *    [-]<-|
	 *     |m  |
	 *    [I]  |t
	 *     |m  |
	 *    [I]  |
 	 *     |a  |
	 *    [+]--|
	 */
	@Test
	public void testFindExtendedMapping() {
		UnitFinder finder = new UnitFinder();

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		plotGraph.addRoot("root");
		Vertex v1 = FunctionalUnits.makeNegative(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		Vertex v3 = FunctionalUnits.makeIntention(3, plotGraph);
		Vertex v4 = FunctionalUnits.makePositive(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v2, v3);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v3, v4);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v4, v1);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph, 1);

		assertEquals(1, mappings.size());
	}

	@Test
	public void testRetaliationExpand() {
		UnitFinder finder = new UnitFinder();
		Class cls = PlotGraphController.class;

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeSpeech(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeNegative(1, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeCrosschar(), v1, v2);
		Vertex v3 = FunctionalUnits.makeIntention(2, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v2, v3);
		v2 = FunctionalUnits.makeSpeech(3, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v3, v2);
		v1 = FunctionalUnits.makeIntention(3, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeCrosschar(), v2, v1);
		v2 = FunctionalUnits.makeNegative(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v1, v2);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.RETALIATION.getGraph(), plotGraph, 1);

		assertEquals(1, mappings.size());
	}

	/**
	 * Testing: Inadvertant Aggravation with inserted [A]-c->[-]
	 *    [+]<-|
	 *         |
	 *    [I]  |t
	 *     |a  |
	 *    [A]  |
 	 *     |c  |
	 *    [-]--|
	 */
	@Test
	public void testFollowPredecessorsWithExpand() {
		UnitFinder finder = new UnitFinder();

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makePositive(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		Vertex v3 = FunctionalUnits.makeAction(3, plotGraph);
		Vertex v4 = FunctionalUnits.makeNegative(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v4, v1);
		plotGraph.addEdge(FunctionalUnits.makeCausality(), v3, v4);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v2, v3);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INADVERTENT_AGGRAVATION.getGraph(), plotGraph, 1);
		assertEquals(1, mappings.size());
	}

	/**
	 * Testing: Nested Subgoal should not match this!
	 *    [I]<-|
	 *     |a  |
	 *    [A]  |e
	 *         |
	 *    [I]--|
 	 *     |e
	 *    [I]
	 */
	@Test
	public void testRegressionTransformOutEdgeKeepType() {
		// ...they used to switch to wildcard, which resulted in many Nested Golas being detected where they should have not
		UnitFinder finder = new UnitFinder();

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeIntention(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeAction(2, plotGraph);
		Vertex v3 = FunctionalUnits.makeIntention(3, plotGraph);
		Vertex v4 = FunctionalUnits.makeIntention(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeEquivalence(), v3, v1);
		plotGraph.addEdge(FunctionalUnits.makeEquivalence(), v4, v3);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.NESTED_GOAL.getGraph(), plotGraph, 1);
		assertEquals(0, mappings.size());
	}
}
