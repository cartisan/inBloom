package little_red_hen;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.infra.centralised.RunCentralisedMAS;

public class Launcher extends RunCentralisedMAS {
    
	protected static Launcher runner = null;
    static Logger logger = Logger.getLogger(Launcher.class.getName());
    static Class ENV_CLASS = FarmEnvironment.class;
	
	private void createMas2j(Collection<Agent> agents) {
		String fileName = "launcher.mas2j";

		try{
		    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		    
		    writer.println("MAS launcher {");
		    writer.println("	environment: " + ENV_CLASS.getName());
		    writer.println("");
		    writer.println("	agents:");
		    
		    for (Agent agent : agents) {
		    	String line = "		" + agent.name + 
		    			MessageFormat.format(" general_animal[beliefs=\"{0}\", goals=\"{1}\"];",
		    								 agent.beliefs,
		    								 agent.goals);
		    	writer.println(line);
		    }
		    
		    writer.println("");
		    writer.println("	aslSourcePath:");
		    writer.println("		\"src/asl\";");
		    writer.println("}");
		    writer.close();
		    
		    logger.info("Generated project config: " + fileName);
		    
		} catch (IOException e) {
			logger.severe("Couldn't create mas2j file");
		}
	}
	
	private void setUpEnvironment(ImmutableList<Agent> agents) {
		FarmEnvironment env = (FarmEnvironment) runner.env.getUserEnvironment();

		// set up environment with agent-aware model
        HashMap<String, Agent> nameAgentMap = new HashMap<String, Agent>();
        for (Agent agent : agents) {
        	nameAgentMap.put(agent.name, agent);
        }
        FarmModel model = new FarmModel(nameAgentMap);
        		
        env.setModel(model);
	}
	
	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!"); 
        
        ImmutableList<Agent> agents = ImmutableList.of(
							new Agent("hen",
									ImmutableList.of("is_communal(self)"),
									ImmutableList.of("make_great_again(farm)")
							),
							new Agent("dog",
									ImmutableList.of("is_lazy(self)"),
									ImmutableList.of("cazzegiare")
							),
							new Agent("cow",
									ImmutableList.of("is_lazy(self)"),
									ImmutableList.of("cazzegiare")
							),
							new Agent("pig",
									ImmutableList.of("is_lazy(self)"),
									ImmutableList.of("cazzegiare")
							)
						);
		
		runner = new Launcher();
		runner.createMas2j(agents);
        runner.init(args);
        runner.create();
        runner.setUpEnvironment(agents);
        runner.start();
        runner.waitEnd();
        runner.finish();
	}
}
