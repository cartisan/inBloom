package inBloom.test.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.UnitFinder;
import inBloom.storyworld.ScheduledHappeningDirector;
import inBloom.test.story.helperClasses.AbstractPlotTest;
import inBloom.test.story.helperClasses.HappeningsCollection;
import inBloom.test.story.helperClasses.TestUnits;
import jason.asSemantics.Personality;

public class PrimitiveUnitTest extends AbstractPlotTest {
	
	@BeforeClass
	public static void setUp() throws Exception {
		VISUALIZE = false;
		DEBUG = false;
		
		// initialize agents
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
        
        // Initialize happenings
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();

        // happening for primitive unit complex positive event
        hapDir.scheduleHappening(HappeningsCollection.findFriendHap);
        // happenings for primitive unit hidden blessing
        hapDir.scheduleHappening(HappeningsCollection.breakLeg);
        hapDir.scheduleHappening(HappeningsCollection.winDamages);
        
		startSimulation("agent_primitive_unit", agents, hapDir);
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
		assertEquals(2, mappings.size());	// two types of pos. tradeoff in Wilke thesis p.19 
	}
	
	@Test
	public void testMotivation() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.MOTIVATION.getGraph());
		assertTrue(mappings.size() >= 8);
	}
	
	@Test
	public void testProblem() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.PROBLEM.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testEnablement() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.ENABLEMENT.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testSuccess() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.SUCCESS.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testFailure() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.FAILURE.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testComplexPos() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.COMPLEX_POS_EVENT.getGraph());
		assertTrue(mappings.size() >= 1);
	}
	
	@Test
	public void testChangeOfMind() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.CHANGE_OF_MIND.getGraph());
		assertTrue(mappings.size() >= 1);
	}
		
	@Test
	public void testHiddenBlessing() throws InterruptedException {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(analyzedGraph, TestUnits.HIDDEN_BLESSING.getGraph());
		assertTrue(mappings.size() >= 1);
	}
}
