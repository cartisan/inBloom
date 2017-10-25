package little_red_hen;

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
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;
import little_red_hen.jason.FarmEnvironment;
import little_red_hen.jason.PlotAwareAg;
import little_red_hen.jason.PlotAwareAgArch;

public class PlotLauncher extends RunCentralisedMAS {
	protected static Logger logger = Logger.getLogger(RedHenLauncher.class.getName());
	public static RedHenLauncher runner = null;
    
    static Class<FarmEnvironment> ENV_CLASS = FarmEnvironment.class;
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
	
	
	protected void createMas2j(Collection<AgentModel> agents) {
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
	
	protected void initializeAgents(ImmutableList<AgentModel> agents) {
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
	
	protected void initzializeEnvironment(ImmutableList<AgentModel> agents) {
		FarmEnvironment env = (FarmEnvironment) runner.env.getUserEnvironment();
		env.initialize(agents);
	}
}
