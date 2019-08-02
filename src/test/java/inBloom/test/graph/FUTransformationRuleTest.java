package inBloom.test.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FUTransformationRule;
import inBloom.graph.isomorphism.UnitVertexType;
import inBloom.test.story.helperClasses.TestUnits;

public class FUTransformationRuleTest {

	PlotDirectedSparseGraph fuGraph;
	Vertex v1, v2, v3;

	@Before
	public void setUpGraph() {
		// graph:[-]-m-[I]-a-[+]
		//        ^-----t-----|
		this.fuGraph = new PlotDirectedSparseGraph();
		this.v1 = TestUnits.makeNegative(1, this.fuGraph);
		this.v1.setLabel("FU Neg");
		this.v2 = TestUnits.makeIntention(2, this.fuGraph);
		this.v2.setLabel("FU Int");
		this.v3 = TestUnits.makePositive(3, this.fuGraph);
		this.v3.setLabel("FU Pos");
		this.fuGraph.addEdge(TestUnits.makeMotivation(), this.v1, this.v2);
		this.fuGraph.addEdge(TestUnits.makeActualization(), this.v2, this.v3);
		this.fuGraph.addEdge(TestUnits.makeTermination(), this.v3, this.v1);
	}

	@Test
	public void testTransformStart() {
		// replacement rule: [v1] -> [I]-m-[+]
		// result: [I]-m-[+]-m-[I]-a-[+]
		//          ^--------t--------|
		PlotDirectedSparseGraph newGraph = FUTransformationRule.TRANSFORMATIONS.get(0).apply(this.v1, this.fuGraph);

		assertEquals(4, newGraph.getVertexCount());
		assertNotSame(this.v1, newGraph.getVertex(0));
		assertSame(Vertex.Type.INTENTION, newGraph.getVertex(0).getType());
		assertSame(UnitVertexType.POSITIVE, UnitVertexType.typeOf(newGraph.getVertex(1)));
		assertSame(this.v2, newGraph.getVertex(2));
	}

	@Test
	public void testTransformMiddle() {
		// replacement rule: [v2] -> [I]-m-[+]
		// result: [-]-m-[I]-m-[+]-a-[+]
		//          ^--------t--------|
		PlotDirectedSparseGraph newGraph = FUTransformationRule.TRANSFORMATIONS.get(0).apply(this.v2, this.fuGraph);

		assertEquals(4, newGraph.getVertexCount());
		assertSame(this.v1, newGraph.getVertex(0));
		assertNotSame(this.v2, newGraph.getVertex(1));
		assertSame(Vertex.Type.INTENTION, newGraph.getVertex(1).getType());
		assertSame(UnitVertexType.POSITIVE, UnitVertexType.typeOf(newGraph.getVertex(2)));
		assertSame(this.v3, newGraph.getVertex(3));
	}

	@Test
	public void testTransformEnd() {
		// replacement rule: [v3] -> [I]-m-[+]
		// result: [-]-m-[I]-a-[I]-a-[+]
		//          ^--------t--------|
		PlotDirectedSparseGraph newGraph = FUTransformationRule.TRANSFORMATIONS.get(0).apply(this.v3, this.fuGraph);

		assertEquals(4, newGraph.getVertexCount());
		assertSame(this.v1, newGraph.getVertex(0));
		assertSame(this.v2, newGraph.getVertex(1));
		assertNotSame(this.v3, newGraph.getVertex(2));
		assertSame(Vertex.Type.INTENTION, newGraph.getVertex(2).getType());
		assertSame(UnitVertexType.POSITIVE, UnitVertexType.typeOf(newGraph.getVertex(3)));
	}
}
