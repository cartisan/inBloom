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
		Vertex v1 = FunctionalUnits.makeNegative(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		Vertex v3 = FunctionalUnits.makePositive(3, plotGraph);
		Vertex v4 = FunctionalUnits.makeIntention(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v2, v3);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v3, v1);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v3, v4);

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
		Vertex v1 = FunctionalUnits.makeNegative(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeIntention(2, plotGraph);
		Vertex v3 = FunctionalUnits.makeIntention(3, plotGraph);
		Vertex v4 = FunctionalUnits.makePositive(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v2, v3);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v3, v4);
		plotGraph.addEdge(FunctionalUnits.makeTermination(), v4, v1);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.INTENTIONAL_PROBLEM_RESOLUTION.getGraph(), plotGraph);

		assertEquals(1, mappings.size());
	}

	@Test
	public void testCrossCharFU() {
		UnitFinder finder = new UnitFinder();
		Class cls = PlotGraphController.class;

		PlotDirectedSparseGraph plotGraph = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeSpeech(1, plotGraph);
		Vertex v2 = FunctionalUnits.makeNegative(1, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeCommunication(), v1, v2);
		Vertex v3 = FunctionalUnits.makeIntention(2, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v2, v3);
		v2 = FunctionalUnits.makeSpeech(3, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeMotivation(), v3, v2);
		v1 = FunctionalUnits.makeIntention(3, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeCommunication(), v2, v1);
		v2 = FunctionalUnits.makeNegative(4, plotGraph);
		plotGraph.addEdge(FunctionalUnits.makeActualization(), v1, v2);

		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(FunctionalUnits.RETALIATION.getGraph(), plotGraph);

		assertEquals(1, mappings.size());
	}
}
