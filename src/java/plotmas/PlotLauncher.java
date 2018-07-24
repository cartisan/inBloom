package plotmas;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Personality;
import jason.bb.DefaultBeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.RConf;
import jason.mas2j.AgentParameters;
import plotmas.graph.PlotGraphController;

/**
 * Used to perform a Java-side setup and execution of a Jason MAS. <br>
 * Sets up a plot graph to draw the plot for each agent and modifies Jason's GUI to display plots graphs and (un)pause
 * a sumulation run.
 * Implements the {@link #run(String[], ImmutableList) run} method to initialize an environment and a model from
 * a list of {@link LauncherAgent parametrized agents} and start the execution of a Jason MAS. <br>
 * <b> Attention: </b> static parameter {@link #ENV_CLASS} needs to be set to the class of your custom environment before executing 
 * {@code run}.
 * 
 * @see plotmas.little_red_hen.RedHenLauncher
 * @author Leonid Berov
 */
public class PlotLauncher extends PlotControlsLauncher {
	protected static Logger logger = Logger.getLogger(PlotLauncher.class.getName());
	public static String DEAULT_FILE_NAME = "launcher.mas2j";
	
    /** 
     * Subclasses need to set ENV_CLASS to the class of their PlotEnvironment implementation, e.g.
     * {@code ENV_CLASS = FarmEnvironment.class;}
     */
	@SuppressWarnings("rawtypes")
	protected static Class ENV_CLASS;
    static Class<PlotAwareAgArch> AG_ARCH_CLASS = PlotAwareAgArch.class;
    static Class<PlotAwareAg> AG_CLASS = PlotAwareAg.class;
    
    /**
     * Resets static variables such that a new
     * cycle of simulation may be run.
     */
    public void reset() {
    	
    	control.stop();
    	control = null;
    	env.stop();
    	env = null;
    	stopAgs();
    	runner = null;
    	ags.clear();
    }
    
    /** 
     * This implements the functionality of the super class, but inserts {@link PlotAwareCentralisedAgentArch}
     * to enable the proper representation of messages in the plot graph.
     * @see jason.infra.centralised.RunCentralisedMAS#createAgs()
     * @see plotmas.PlotAwareCentralisedAgentArch
     */
    @Override
    protected void createAgs() throws JasonException {
        
        RConf generalConf = RConf.fromString(project.getInfrastructure().getParameter(0));
        
        int nbAg = 0;
        Agent pag = null;
        
        // create the agents
        for (AgentParameters ap : project.getAgents()) {
            try {
                
                String agName = ap.name;

                for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                    nbAg++;
                    
                    String numberedAg = agName;
                    if (ap.getNbInstances() > 1) {
                        numberedAg += (cAg + 1);
                        // cannot add zeros before, it causes many compatibility problems and breaks dynamic creation 
                        // numberedAg += String.format("%0"+String.valueOf(ap.qty).length()+"d", cAg + 1);
                    }
                    
                    String nb = "";
                    int    n  = 1;
                    while (getAg(numberedAg+nb) != null)
                        nb = "_" + (n++);
                    numberedAg += nb;
                    
                    logger.fine("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.getNbInstances() + ")");
                    CentralisedAgArch agArch;
                    
                    RConf agentConf;
                    if (ap.getOption("rc") == null) {
                        agentConf = generalConf;
                    } else {
                        agentConf = RConf.fromString(ap.getOption("rc"));
                    }                    
                    
                    // Get the number of reasoning cycles or number of cycles for each stage 
                    int cycles           = -1; // -1 means default value of the platform
                    int cyclesSense      = -1;
                    int cyclesDeliberate = -1; 
                    int cyclesAct        = -1;
                    
                    if (ap.getOption("cycles") != null) {
                        cycles = Integer.valueOf(ap.getOption("cycles"));
                    }
                    if (ap.getOption("cycles_sense") != null) {
                        cyclesSense = Integer.valueOf(ap.getOption("cycles_sense"));
                    }
                    if (ap.getOption("cycles_deliberate") != null) {
                        cyclesDeliberate = Integer.valueOf(ap.getOption("cycles_deliberate"));
                    }
                    if (ap.getOption("cycles_act") != null) {
                        cyclesAct = Integer.valueOf(ap.getOption("cycles_act"));
                    }
                    
                    // Create agents according to the specific architecture
                    if (agentConf == RConf.POOL_SYNCH) {
//                        agArch = new CentralisedAgArchForPool();
                    	throw new JasonException("This jason agent architecture is not supported by plotmas");
                    } else if (agentConf == RConf.POOL_SYNCH_SCHEDULED) {
                    	throw new JasonException("This jason agent architecture is not supported by plotmas");
                    } else if  (agentConf == RConf.ASYNCH || agentConf == RConf.ASYNCH_SHARED_POOLS) {
//                        agArch = new CentralisedAgArchAsynchronous();
                    	throw new JasonException("This jason agent architecture is not supported by plotmas");
                    } else {
                        agArch = new PlotAwareCentralisedAgArch();
                    }
                    
                    agArch.setCycles(cycles);
                    agArch.setCyclesSense(cyclesSense);
                    agArch.setCyclesDeliberate(cyclesDeliberate);
                    agArch.setCyclesAct(cyclesAct);

                    agArch.setConf(agentConf);
                    agArch.setAgName(numberedAg);
                    agArch.setEnvInfraTier(env);
                    if ((generalConf != RConf.THREADED) && cAg > 0 && ap.getAgArchClasses().isEmpty() && ap.getBBClass().equals(DefaultBeliefBase.class.getName())) {
                        // creation by cloning previous agent (which is faster -- no parsing, for instance)
                        agArch.createArchs(ap.getAgArchClasses(), pag, this);
                    } else {
                        // normal creation
                        agArch.createArchs(ap.getAgArchClasses(), ap.agClass.getClassName(), ap.getBBClass(), ap.asSource.toString(), ap.getAsSetts(debug, project.getControlClass() != null), this);
                    }
                    addAg(agArch);
                    
                    pag = agArch.getTS().getAg();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
            }
        }
        
        if (generalConf != RConf.THREADED) logger.info("Created "+nbAg+" agents.");
    }
    
    
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
		    	" agentArchClass " + AG_ARCH_CLASS.getName() + 
		    	" agentClass "+ AG_CLASS.getName() +
		    	";";   
		    	writer.println(line);
		    }
		    
		    writer.println("");
		    writer.println("	aslSourcePath:");
		    writer.println("		\"src/asl\";");
		    writer.println("}");
		    writer.close();
		    
		    logger.info("Generated project config: " + DEAULT_FILE_NAME);
		    
		} catch (IOException e) {
			logger.severe("Couldn't create mas2j file");
		}
	}
	
	/**
	 * Initializes the personality of the AffectiveAgents used to execute character agents. This is a workaround until
	 * we can initialize personality from mas2j files.
	 * @param agents
	 */
	protected void initializePlotAgents(ImmutableList<LauncherAgent> agents) {
		// initialize personalities
		for (LauncherAgent ag: agents) {
			if(ag.personality != null) {
				PlotAwareAg plotAg = (PlotAwareAg) this.getAg(ag.name).getTS().getAg();
				try {
					plotAg.initializePersonality(ag.personality);
				} catch (JasonException e) {
					logger.severe("Failed to initialize mood based on personality: " + ag.personality);
					e.printStackTrace();
				}
				plotAg.initializeMoodMapper();
			}
		}
	}
	
	protected void initzializePlotEnvironment(ImmutableList<LauncherAgent> agents) {
		PlotEnvironment<?> env = (PlotEnvironment<?>) this.env.getUserEnvironment();
		env.initialize(agents);
	}
	
	/**
	 * Creates a mas2j file to prepare execution of the MAS, sets up agents, environment and model and finally starts
	 * the execution of the MAS. The execution is paused if all agents repeat the same action
	 * {@link PlotEnvironment.MAX_REPEATE_NUM} number of times.
	 * <b> Attention: </b> static parameter {@link #ENV_CLASS} needs to be set to the class of your custom environment before executing 
	 * this method.
	 * 
	 * @param args contains the name of the mas2j and potentially {@code -debug} to execute in debug mode
	 * @param agents a list of agent parameters used to initialize mas2j, environment and model
	 * @param agentFileName specifies the source of the agent ASL code
	 * @param usePlotLogger set this to false if you wish to set your own logging output
	 * @throws JasonException
	 */
	public void run(String[] args, ImmutableList<LauncherAgent> agents, String agentFileName) throws JasonException  {
		String defArgs[];
		boolean debugMode=false;
		
		if (ENV_CLASS == null) {
        	throw new RuntimeException("PlotLauncher.ENV_CLASS must be set to the class of your custom"
        			+ " environment before executing this method");
        }
        
        if (args.length < 1) {
        	defArgs = new String[] {PlotLauncher.DEAULT_FILE_NAME};
        }
        else {
        	assert args[0] == "-debug";
        	defArgs = new String[] {PlotLauncher.DEAULT_FILE_NAME, "-debug"};
        	debugMode = true;
        }
        
        
		PlotGraphController.instantiatePlotListener(agents);
        
		this.createMas2j(agents, agentFileName, debugMode);
		this.init(defArgs);
		this.create();
        
		this.initzializePlotEnvironment(agents);
		this.setupPlotLogger();
		this.initializePlotAgents(agents);
		
		this.start();
		this.waitEnd();
		this.finish();
	}

	/**
	 * Helper class used to encapsulate all parameters needed to initialise ASL Agents from java code.
	 * This parameters will be used to create a mas2j file required to start a Jason multi agent system. 
	 * @author Leonid Berov
	 */
	public class LauncherAgent {
		public String name;
		public String beliefs;
		public String goals;
		public Personality personality;
		
		public LauncherAgent() {
			this.name = null;
			this.beliefs = "";
			this.goals = "";
			this.personality = null;
		}
		
		public LauncherAgent(String name) {
			this.beliefs = "";
			this.goals = "";
			this.personality = null;
			
			this.name = name;
		}

		public LauncherAgent(String name, Personality personality) {
			this.beliefs = "";
			this.goals = "";
			
			this.name = name;
			this.personality = personality;
		}
		
		public LauncherAgent(String name, Collection<String> beliefs, Collection<String> goals, Personality personality) {
			this.name = name;
			this.beliefs = createLiteralString(beliefs);
			this.goals = createLiteralString(goals);
			this.personality = personality;
		}
		
		/**
		 * Helper function that takes a collection of strings and concatenates them into a list that can be used to 
		 * generate ASL literal lists.
		 */
		private String createLiteralString(Collection<String> literalList) {
			return String.join(",", literalList);
		}
	}
}
