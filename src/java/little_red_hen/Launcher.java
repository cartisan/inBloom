package little_red_hen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
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
import jason.util.Pair;
import little_red_hen.jason.FarmEnvironment;
import little_red_hen.jason.PlotAwareAg;
import little_red_hen.jason.PlotAwareAgArch;


public class Launcher extends RunCentralisedMAS {
	static Logger logger = Logger.getLogger(Launcher.class.getName());
	public static Launcher runner = null;
    
	public static final Integer MAX_REPEATE_NUM = 10;
    static Class<FarmEnvironment> ENV_CLASS = FarmEnvironment.class;
    static Class<PlotAwareAgArch> AG_ARCH_CLASS = PlotAwareAgArch.class;
    static Class<PlotAwareAg> AG_CLASS = PlotAwareAg.class;
	private JButton pauseButton;
    
	
	private void createMas2j(Collection<AgentModel> agents) {
		String fileName = "launcher.mas2j";

		try{
		    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		    
		    writer.println("MAS launcher {");
		    writer.println("	environment: " + ENV_CLASS.getName());
		    writer.println("");
		    writer.println("	agents:");
		    
		    for (AgentModel agent : agents) {
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
	
	public void pauseExecution() {
        MASConsoleGUI.get().setPause(true);
        this.pauseButton.setText("Continue");
	}
	
	
	public void continueExecution() {
		this.pauseButton.setText("Pause");
        MASConsoleGUI.get().setPause(false);
	}
	

	private void initializeAgents(ImmutableList<AgentModel> agents) {
		// initialize personalities
		for (AgentModel ag: agents) {
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
	
	private void initzializeEnvironment(ImmutableList<AgentModel> agents) {
		FarmEnvironment env = (FarmEnvironment) runner.env.getUserEnvironment();

		// set up environment with agent-aware model
        HashMap<String, AgentModel> nameAgentMap = new HashMap<String, AgentModel>();
        HashMap<String, Pair<String, Integer>> agentActionCount = new HashMap<>();
        
        for (AgentModel agent : agents) {
        	nameAgentMap.put(agent.name, agent);
        	agentActionCount.put(agent.name, new Pair<String, Integer>("", 1));
        	agent.setEnvironment(env);
        }
        FarmModel model = new FarmModel(nameAgentMap, env);
        		
        env.setModel(model);
        env.setAgentActionCount(agentActionCount);
	}
	
	@Override
	public void finish() {
		pauseExecution();
		
//		PlotGraph.getPlotListener().visualizeGraph();

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

	public static void main(String[] args) throws JasonException {
        logger.info("Starting up from Launcher!"); 
        
        ImmutableList<AgentModel> agents = ImmutableList.of(
							new AgentModel("hen",							// works with Mood.MAX_DECAY_TIME = 50 and MAX_UPDATE_TIME = 5
									new Personality(0, 1, 0.7, 0.2, 0.3)   //punishment
//									new Personality(0, 1, 0.7, 0.7, 0.3)   //sharing
//									new Personality(0, -1, 0.7, 0.7, 0.3)  //no plot (graph making brakes?!)
//									new Personality(0, 1, 0, 0.2, 0.3)     //no help requests, no sharing
//									new Personality(0, 1, 0, 0.7, 0.3)     //no help requests, sharing
							),
							new AgentModel("dog",
									new Personality(0, -1, 0, -0.7, -0.8)
							),
							new AgentModel("cow",
									new Personality(0, -1, 0, -0.7, -0.8)
							),
							new AgentModel("pig",
									new Personality(0, -1, 0, -0.7, -0.8)
							)
						);
        
        PlotGraph.instantiatePlotListener(agents);
        
		runner = new Launcher();
		runner.createMas2j(agents);
        runner.init(args);
        runner.create();
        
        runner.initializeAgents(agents);
        runner.initzializeEnvironment(agents);
        
        runner.start();
        runner.waitEnd();
        runner.finish();
        
	}
}
