package plotmas.test.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;
import jason.util.Pair;
import plotmas.LauncherAgent;
import plotmas.graph.Vertex;
import plotmas.graph.Vertex.Type;

public class TellabilityTest extends AbstractPlotTest {
	
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
	public void testSuspense() {
		assertTrue(2 < analysis.suspense);		// actual suspense varies between 4 and 5
		
		// test that its the right intention that is most suspensefull
		assertEquals("+lost(wallet)", analysis.counter.mostSuspensefulIntention.getSecond().getWithoutAnnotation());
		assertEquals("search(wallet)", analysis.counter.mostSuspensefulIntention.getThird().getLabel());
		
	}
	
	@Test
	public void testProdConf() {
		// test that productive conflicts are of right type
		for (Pair<Vertex, Vertex> conflict : analysis.counter.productiveConflicts.get("jeremy")) {
			assertTrue( (conflict.getFirst().getType() == Type.PERCEPT)  | (conflict.getFirst().getType() == Type.INTENTION) );
			assertTrue( (conflict.getSecond().getType() == Type.ACTION)  | (conflict.getSecond().getType() == Type.INTENTION) );
		}
	}
	
}
