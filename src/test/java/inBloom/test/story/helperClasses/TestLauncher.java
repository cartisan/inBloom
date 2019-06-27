package inBloom.test.story.helperClasses;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.BaseCentralisedMAS;

public class TestLauncher extends PlotLauncher<TestEnvironment, TestModel> {

	public TestLauncher() {
		ENV_CLASS = TestEnvironment.class;
		PlotControlsLauncher.runner = this;
		BaseCentralisedMAS.runner = this;
	}
	
	/**
	 * Implements same functionality as {@link inBloom.PlotLauncher#createMas2j}, but changes directory
	 * of agent ASL files to {@code src/test/asl}.
	 */
	@Override
	protected void createMas2j(Collection<LauncherAgent> agents, String agentFileName, boolean debugMode) {
		try{
		    PrintWriter writer = new PrintWriter(DEAULT_FILE_NAME, "UTF-8");
		    
		    writer.println("MAS launcher {");
		    writer.println("	environment: " + ENV_CLASS.getName());
		    if(!debugMode) {
		    	writer.println("	executionControl: jason.control.ExecutionControl");
		    }
		    writer.println("");
		    writer.println("	agents:");
		    
		    for (LauncherAgent agent : agents) {
		    	String line = "		" + agent.name + 
		    			MessageFormat.format(" " + agentFileName + "[beliefs=\"{0}\", goals=\"{1}\"]",
		    								 agent.beliefs,
		    								 agent.goals) +
		    	" agentClass "+ AG_CLASS.getName() +
		    	";";   
		    	writer.println(line);
		    }
		    
		    writer.println("");
		    writer.println("	aslSourcePath:");
		    writer.println("		\"src/test/asl\";");
		    writer.println("}");
		    writer.close();
		    
		    logger.info("Generated project config: " + DEAULT_FILE_NAME);
		    
		} catch (IOException e) {
			logger.severe("Couldn't create mas2j file");
		}
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting TestLauncher!");
        
        runner = new TestLauncher();
        runner.ENV_CLASS = TestEnvironment.class;

        ImmutableList<LauncherAgent> agents = ImmutableList.of(
							new LauncherAgent("jeremy",
									new Personality(0,  1,  0.7,  0.3, 0.3)
							)
						);
  
        // Initialize MAS with a scheduled happening director
        ScheduledHappeningDirector hapDir = new ScheduledHappeningDirector();
        TestModel model = new TestModel(agents, hapDir);

		// Execute MAS
		runner.initialize(args, model, agents, "agent_primitive_unit");
		runner.run();
	}
}
