package plotmas.test.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.UnitFinder;

public class PrimitiveUnitTest extends AbstractPlotTest {

	@Test
	public void testResolution() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.RESOLUTION.getGraph());
		assertEquals(1, mappings.size());
	}
	
	@Test
	public void testLoss() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.LOSS.getGraph());
		assertEquals(1, mappings.size());
	}
	
	@Test
	public void testPerseverance() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.PERSEVERANCE.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testPositiveTradeoff() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.POSITIVE_TRADEOFF.getGraph());
		assertEquals(2, mappings.size());
	}
}
