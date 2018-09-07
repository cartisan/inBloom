package plotmas.test.story;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;

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
		
	}

	
}
