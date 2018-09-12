package plotmas.test.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.UnitFinder;

public class PrimitiveUnitTest extends AbstractPlotTest {
	
	@BeforeClass
	public static void getAgentFileName() throws Exception {
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
        
		startSimulation("agent_primitive_unit", agents);
	}
	
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
