package inBloom.test.graph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnits;

public class PlotDirectedSparseGraphTest {


	@Test
	public void testEquals() {
		// test equality
		PlotDirectedSparseGraph graph1 = new PlotDirectedSparseGraph();
		Vertex v1 = FunctionalUnits.makeNegative(1, graph1);
		Vertex v2 = FunctionalUnits.makeIntention(2, graph1);
		Vertex v3 = FunctionalUnits.makePositive(3, graph1);
		graph1.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		graph1.addEdge(FunctionalUnits.makeActualization(), v2, v3);
		graph1.addEdge(FunctionalUnits.makeTermination(), v3, v1);

		PlotDirectedSparseGraph graph2 = new PlotDirectedSparseGraph();
		v1 = FunctionalUnits.makeNegative(1, graph2);
		v2 = FunctionalUnits.makeIntention(2, graph2);
		v3 = FunctionalUnits.makePositive(3, graph2);
		graph2.addEdge(FunctionalUnits.makeMotivation(), v1, v2);
		graph2.addEdge(FunctionalUnits.makeActualization(), v2, v3);
		graph2.addEdge(FunctionalUnits.makeTermination(), v3, v1);

		assertTrue(graph1.equals(graph2));
		assertTrue(graph2.equals(graph1));

		// test non-equality vertices
		Vertex v4 = FunctionalUnits.makeNegative(4, graph2);
		graph2.addVertex(v4);
		graph2.getOrderedVertexList();	// regenerates orderedVertexArray so that new vertex is proeprly incorporates
		assertFalse(graph1.equals(graph2));
		assertFalse(graph2.equals(graph1));

		// test non-euqlity edges
		graph2.removeVertex(v4);
		assertTrue(graph1.equals(graph2));
		graph2.addEdge(FunctionalUnits.makeMotivation(), v2, v3);
		assertFalse(graph1.equals(graph2));
		assertFalse(graph2.equals(graph1));
	}

}
