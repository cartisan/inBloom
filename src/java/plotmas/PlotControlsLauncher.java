package plotmas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;
import plotmas.graph.MoodGraph;
import plotmas.graph.PlotGraphController;
import plotmas.helper.PlotFormatter;

/**
 * Encapsulates the changes to the Jason GUI that are needed by {@link PlotLauncher}. Doesn't provide any 
 * plot relevant functionality apart from changing the GUI.
 * @author Leonid Berov
 */
public class PlotControlsLauncher extends RunCentralisedMAS {
	public static PlotLauncher runner = null;
	protected static Level LOG_LEVEL = Level.INFO;
//	protected static Level LOG_LEVEL = Level.FINE;
	
	private JButton pauseButton;
	private JButton drawButton;
	private JFrame plotGraph;
	private JFrame moodGraph;
	protected boolean isDraw = false;

	protected boolean showGui = true;
	
	public void setShowGui(boolean showGui) {
		this.showGui = showGui;
	}
	
	@Override
	public synchronized void setupLogger() {
		if(showGui) {
			super.setupLogger();
		}
	}
	
	/**
	 * Has to be executed after initialization is complete because it depends
	 * on PlotEnvironment being already initialized with a plotStartTime.
	 */
	public synchronized void setupPlotLogger() {
		if(!showGui) {
			return;
		}
        Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) { 
            Logger.getLogger("").removeHandler(hs[i]); 
        }
        Handler h = PlotFormatter.handler();
        Logger.getLogger("").addHandler(h);
        Logger.getLogger("").setLevel(LOG_LEVEL);
	}
	
	/**
	 * Changes all logging to appear on stdout. This is helpful because logging during paused state (e.g. during
	 * plotting) is impossible due to paused Jason console (?).
	 */
	public synchronized void setupConsoleLogger() {
		if(!showGui) {
			return;
		}
        Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) { 
            Logger.getLogger("").removeHandler(hs[i]); 
        }
        
        ConsoleHandler h = new ConsoleHandler();
        h.setFormatter(new PlotFormatter());
        Logger.getLogger("").addHandler(h);
        Logger.getLogger("").setLevel(LOG_LEVEL);
	}
	
	protected void pauseExecution() {
	    MASConsoleGUI.get().setPause(true);
	    this.pauseButton.setText("Continue");

		setupConsoleLogger();
	}

	protected void continueExecution() {
		this.pauseButton.setText("Pause");
	    MASConsoleGUI.get().setPause(false);
	    
	    this.setupPlotLogger();
	    ((PlotEnvironment<?>) this.env.getUserEnvironment()).wake();
	}

	protected void drawGraphs() {
		this.drawButton.setText("Close Graphs");
		this.pauseExecution();
		
		// create and visualize plot graph
		this.plotGraph = PlotGraphController.getPlotListener().visualizeGraph();
		
		// create and visualize mood graph
		MoodGraph.getMoodListener().createData();
		this.moodGraph = MoodGraph.getMoodListener().visualizeGraph();
		
		this.isDraw = true;
	}

	protected void closeGraphs() {
		this.drawButton.setText("Show Graphs");
		
		// close windows graph
		this.plotGraph.dispose();
		this.moodGraph.dispose();
		
		this.isDraw = false;
		
		// release pointers
		this.plotGraph = null;
		this.moodGraph = null;
	}

	@Override
	public void finish() {
		pauseExecution();
		super.finish();
	}

	@Override
	protected void createButtons() {
		createDrawButton();
		createAnalysisButton();
		super.createButtons();
	}

	protected void createDrawButton() {
		JButton btDraw = new JButton("Show Graphs");
		btDraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!runner.isDraw) {
					runner.drawGraphs();
				}
				else {
					runner.closeGraphs();
				}
			}
	
		});
		
		MASConsoleGUI.get().addButton(btDraw);
		this.drawButton = btDraw;
	}
	
	protected void createAnalysisButton() {
		JButton btAnalyze = new JButton("Analyze Graph");
		btAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(MASConsoleGUI.get().isPause()) {
					PlotGraphController.getPlotListener().analyze();
				}
			}
	
		});
		
		MASConsoleGUI.get().addButton(btAnalyze);
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

}