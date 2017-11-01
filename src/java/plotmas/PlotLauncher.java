package plotmas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.AffectiveTransitionSystem;
import jason.asSemantics.Personality;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;
import plotmas.graph.MoodGraph;
import plotmas.graph.PlotGraph;

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
public class PlotLauncher extends RunCentralisedMAS {
	protected static Logger logger = Logger.getLogger(PlotLauncher.class.getName());
	public static PlotLauncher runner = null;
    
    /** 
     * Subclasses need to set ENV_CLASS to the class of their PlotEnvironment implementation, e.g.
     * {@code ENV_CLASS = FarmEnvironment.class;}
     */
	@SuppressWarnings("rawtypes")
	protected static Class ENV_CLASS;
    static Class<PlotAwareAgArch> AG_ARCH_CLASS = PlotAwareAgArch.class;
    static Class<PlotAwareAg> AG_CLASS = PlotAwareAg.class;
	private JButton pauseButton;
    
	
	public void pauseExecution() {
        MASConsoleGUI.get().setPause(true);
        this.pauseButton.setText("Continue");
	}
	
	
	public void continueExecution() {
		this.pauseButton.setText("Pause");
        MASConsoleGUI.get().setPause(false);
	}
	
	@Override
	public void finish() {
		pauseExecution();
		
		try {
			while (PlotGraph.isDisplayed) {
					Thread.sleep(500);
				}
		} catch (InterruptedException e) {
			e.printStackTrace();
			super.finish();
		} finally {
			super.finish();
		}
	}
	
	@Override
	protected void createButtons() {
		createDrawButton();
		super.createButtons();
	}
	
	protected void createDrawButton() {
		JButton btDraw = new JButton("Draw Plot");
		btDraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				runner.pauseExecution();
				PlotGraph.getPlotListener().visualizeGraph();
				MoodGraph.getMoodListener().visualizeGraph();
			}
		});
		MASConsoleGUI.get().addButton(btDraw);
	}
	
	@Override
    protected void createStopButton() {
		logger.info("creating plot aware stop button");
		// add Button
        JButton btStop = new JButton("Stop", new ImageIcon(RunCentralisedMAS.class.getResource("/images/suspend.gif")));
        btStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                runner.finish();
            }
        });
        MASConsoleGUI.get().addButton(btStop);
    }
	
	@Override
    protected void createPauseButton() {
        final JButton btPause = new JButton("Pause", new ImageIcon(RunCentralisedMAS.class.getResource("/images/resume_co.gif")));
        btPause.addActionListener(
        	new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	            	if (MASConsoleGUI.get().isPause()) {
	                    runner.continueExecution();
	                } else {
	                	runner.pauseExecution();
	                }

            }
        });
        
        MASConsoleGUI.get().addButton(btPause);
        this.pauseButton = btPause;
    }
	
	
	protected void createMas2j(Collection<LauncherAgent> agents) {
		String fileName = "launcher.mas2j";

		try{
		    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		    
		    writer.println("MAS launcher {");
		    writer.println("	environment: " + ENV_CLASS.getName());
		    writer.println("");
		    writer.println("	agents:");
		    
		    for (LauncherAgent agent : agents) {
		    	String line = "		" + agent.name + 
		    			MessageFormat.format(" general_animal[beliefs=\"{0}\", goals=\"{1}\"]",
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
		    
		    logger.info("Generated project config: " + fileName);
		    
		} catch (IOException e) {
			logger.severe("Couldn't create mas2j file");
		}
	}
	
	/**
	 * Initializes the personality of the AffectiveAgents used to execute character agents. This is a workaround until
	 * we can initialize personality from mas2j files.
	 * @param agents
	 */
	protected void initializeAffectiveAgents(ImmutableList<LauncherAgent> agents) {
		// initialize personalities
		for (LauncherAgent ag: agents) {
			if(ag.personality != null) {
				AffectiveAgent affAg = ((AffectiveTransitionSystem) this.getAg(ag.name).getTS()).getAffectiveAg();
				try {
					affAg.initializePersonality(ag.personality);
				} catch (JasonException e) {
					logger.severe("Failed to initialize mood based on personality: " + ag.personality);
					e.printStackTrace();
				}
			}
		}
		
	}
	
	protected void initzializeEnvironment(ImmutableList<LauncherAgent> agents) {
		PlotEnvironment env = (PlotEnvironment) runner.env.getUserEnvironment();
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
	 * @throws JasonException
	 */
	public void run (String[] args, ImmutableList<LauncherAgent> agents) throws JasonException  {
        if (ENV_CLASS == null) {
        	throw new RuntimeException("PlotLauncher.ENV_CLASS must be set to the class of your custom"
        			+ " environment before executing this method");
        }
		
		PlotGraph.instantiatePlotListener(agents);
        
		this.createMas2j(agents);
		this.init(args);
		this.create();
        
		this.initializeAffectiveAgents(agents);
		this.initzializeEnvironment(agents);
        
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
