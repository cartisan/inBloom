package inBloom.test.story;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.UnitFinder;
import inBloom.test.story.helperClasses.TestUnits;

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
		Vertex v1 = TestUnits.makeNegative(1, plotGraph);
		v1.setLabel("PLOT -");
		Vertex v2 = TestUnits.makeIntention(2, plotGraph);
		v2.setLabel("PLOT I");
		Vertex v3 = TestUnits.makePositive(3, plotGraph);
		v3.setLabel("PLOT +");
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
		Vertex v1 = TestUnits.makeNegative(1, plotGraph);
		Vertex v2 = TestUnits.makeIntention(2, plotGraph);
		Vertex v3 = TestUnits.makeIntention(3, plotGraph);
		Vertex v4 = TestUnits.makePositive(4, plotGraph);
		plotGraph.addEdge(TestUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(TestUnits.makeMotivation(), v2, v3);
		plotGraph.addEdge(TestUnits.makeActualization(), v3, v4);
		plotGraph.addEdge(TestUnits.makeTermination(), v4, v1);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph);

		assertEquals(1, mappings.size());
	}
}
