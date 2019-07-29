package inBloom.test.story;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;
import inBloom.test.story.helperClasses.TestUnits;

import junit.framework.TestCase;

public class ComplexUnitTest  extends TestCase {

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
		Vertex v1 = TestUnits.makeNegative(1, plotGraph);
		Vertex v2 = TestUnits.makeIntention(2, plotGraph);
		Vertex v3 = TestUnits.makePositive(3, plotGraph);
		plotGraph.addEdge(TestUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(TestUnits.makeActualization(), v2, v3);
		plotGraph.addEdge(TestUnits.makeTermination(), v3, v1);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph);

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
		Vertex v1 = TestUnits.makeNegative(1, plotGraph);
		Vertex v2 = TestUnits.makeIntention(2, plotGraph);
		Vertex v3 = TestUnits.makePositive(3, plotGraph);
		Vertex v4 = TestUnits.makeIntention(4, plotGraph);
		plotGraph.addEdge(TestUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(TestUnits.makeActualization(), v2, v3);
		plotGraph.addEdge(TestUnits.makeTermination(), v3, v1);
		plotGraph.addEdge(TestUnits.makeMotivation(), v3, v4);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph);

		assertEquals(1, mappings.size());
	}
}
