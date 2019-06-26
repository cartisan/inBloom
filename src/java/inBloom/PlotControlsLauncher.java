package inBloom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import inBloom.framing.FramingGenerator;
import inBloom.graph.MoodGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.PlotmasGraph;
import inBloom.helper.PlotFormatter;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;

/**
 * Encapsulates the changes to the Jason GUI that are needed by {@link PlotLauncher}. Doesn't provide any 
 * plot relevant functionality apart from changing the GUI.
 * @author Leonid Berov
 */
public class PlotControlsLauncher extends RunCentralisedMAS {
	public static PlotLauncher<?,?> runner = null;
	
	protected static Level LOG_LEVEL = Level.INFO;
//	protected static Level LOG_LEVEL = Level.FINE;
	
	private JButton pauseButton;
	private JButton drawButton;
	private JButton summaryButton;
	private LinkedList<PlotmasGraph> graphs = new LinkedList<PlotmasGraph>();
	private Long pauseStart = 0L;
	
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
		
		// reset old handlers
        Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) { 
            Logger.getLogger("").removeHandler(hs[i]); 
        }
        
        // set console handler to display important messages, so they don't get lost
        Handler ch = new ConsoleHandler();
        ch.setLevel(Level.WARNING);
        ch.setFormatter(new PlotFormatter()); 
        Logger.getLogger("").addHandler(ch);
        
        // everything should also be logged in the GUI
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
		this.pauseStart = System.nanoTime();
	}

	protected void continueExecution() {
		this.pauseButton.setText("Pause");
	    MASConsoleGUI.get().setPause(false);
	    
	    this.setupPlotLogger();
	    PlotEnvironment.notePause(System.nanoTime() - this.pauseStart);	    
	    ((PlotEnvironment<?>) this.env.getUserEnvironment()).wake();
	}

	@SuppressWarnings("unchecked")
	protected void drawGraphs() {
		if(!MASConsoleGUI.get().isPause())
			this.pauseExecution();
		
		// create and visualize plot graph
		this.graphs.add(PlotGraphController.getPlotListener().visualizeGraph());
		
		// create and visualize mood graph
		MoodGraph.getMoodListener().createData(((PlotEnvironment<PlotModel<?>>)this.getEnvironmentInfraTier().getUserEnvironment()).getModel().moodMapper);
		this.graphs.add(MoodGraph.getMoodListener().visualizeGraph());
		
		this.isDraw = true;
		this.drawButton.setText("Close Graphs");
	}

	
	public void graphClosed(PlotmasGraph g) {
		this.graphs.remove(g);
		if(this.graphs.isEmpty()) {
			this.resetGraphView();
		}

	}
	
	protected void closeGraphs() {
		// close windows graph
		Iterator<PlotmasGraph> it = this.graphs.iterator();
		while(it.hasNext()){
			PlotmasGraph g = it.next();
			it.remove();
			g.closeGraph();
		}
	}
		
	public void resetGraphView() {
		// release pointers
		this.graphs = new LinkedList<PlotmasGraph>();
		this.isDraw = false;
		this.drawButton.setText("Show Graphs");
	}

	@Override
	public void finish() {
		pauseExecution();
		super.finish();
	}

	@Override
	protected void createButtons() {
		createPauseButton();
		createAnalysisButton();
		createSummaryButton();
		createDrawButton();
        createStopButton();
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
					summaryButton.setEnabled(true);
				}
			}
	
		});
		
		MASConsoleGUI.get().addButton(btAnalyze);
	}
	
	protected void createSummaryButton() {
		JButton btSummary = new JButton("Summarize Plot");
		
		btSummary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				PlotGraphController.getPlotListener().getAnalysisResult().connectivityGraph.removeEntailed();
				PlotGraphController.getPlotListener().getAnalysisResult().connectivityGraph.prunePrimitives();
				PlotGraphController.getPlotListener().getAnalysisResult().connectivityGraph.mergeTimeEquivalents();
				PlotGraphController.getPlotListener().getAnalysisResult().connectivityGraph.display();
				
				logger.info("Summary: " + FramingGenerator.generateFraming(PlotGraphController.getPlotListener().getAnalysisResult().connectivityGraph));
			}
		});
		
		btSummary.setEnabled(false);
		MASConsoleGUI.get().addButton(btSummary);
		this.summaryButton = btSummary; 
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