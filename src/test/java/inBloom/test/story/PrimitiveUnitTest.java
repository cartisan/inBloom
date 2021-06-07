package inBloom.test.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;

import inBloom.LauncherAgent;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.UnitFinder;
import inBloom.helper.Tellability;
import inBloom.storyworld.ScheduledHappeningDirector;
import inBloom.test.story.helperClasses.AbstractPlotTest;
import inBloom.test.story.helperClasses.HappeningsCollection;
import inBloom.test.story.helperClasses.TestModel;
import inBloom.test.story.helperClasses.TestUnits;

public class PrimitiveUnitTest extends AbstractPlotTest {

	@BeforeClass
	public static void setUp() throws Exception {
		VISUALIZE = false;
		DEBUG = false;
		Tellability.GRAPH_MATCHING_TOLERANCE = 0;

		// initialize agents
        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);

        agents.get(0).inventory.add(new TestModel.Wallet());

        // Initialize happenings
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();

        // happening to start action, by loosing wallet
        hapDir.scheduleHappening(HappeningsCollection.looseWallet);
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
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.RESOLUTION.getGraph(), analyzedGraph);
		assertEquals(1, mappings.size());
	}

	@Test
	public void testLoss() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.LOSS.getGraph(), analyzedGraph);
		assertEquals(1, mappings.size());
	}

	@Test
	public void testPerseverance() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.PERSEVERANCE.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testPositiveTradeoff() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.POSITIVE_TRADEOFF.getGraph(), analyzedGraph);
		assertEquals(2, mappings.size());	// two types of pos. tradeoff in Wilke thesis p.19
	}

	@Test
	public void testMotivation() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.MOTIVATION.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 8);
	}

	@Test
	public void testProblem() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.PROBLEM.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testEnablement() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.ENABLEMENT.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testSuccess() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.SUCCESS.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testFailure() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.FAILURE.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testComplexPos() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.COMPLEX_POS_EVENT.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}

	@Test
	public void testChangeOfMind() {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.CHANGE_OF_MIND.getGraph(), analyzedGraph);
		assertTrue(mappings.size() == 1);
	}

	@Test
	public void testHiddenBlessing() throws InterruptedException {
		UnitFinder finder = new UnitFinder();
		Set<Map<Vertex, Vertex>> mappings = finder.findUnits(TestUnits.HIDDEN_BLESSING.getGraph(), analyzedGraph);
		assertTrue(mappings.size() >= 1);
	}
}
