package plotmas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;
import plotmas.graph.MoodGraph;
import plotmas.graph.PlotGraphController;

/**
 * Encapsulates the changes to the Jason GUI that are needed by {@link PlotLauncher}. Doesn't provide any 
 * plot relevant functionality apart from changing the GUI.
 * @author Leonid Berov
 */
public class PlotControlsLauncher extends RunCentralisedMAS {
	public static PlotLauncher runner = null;
	protected static boolean COMPRESS_GRAPH = false;	// used to determine if PlotGraph should compressed before drawing
	
	private JButton pauseButton;
	private JButton drawButton;
	private JFrame plotGraph;
	private JFrame moodGraph;
	protected boolean isDraw = false;

	protected void pauseExecution() {
	    MASConsoleGUI.get().setPause(true);
	    this.pauseButton.setText("Continue");
	}

	protected void continueExecution() {
		this.pauseButton.setText("Pause");
	    MASConsoleGUI.get().setPause(false);
	}

	protected void drawGraphs() {
		this.drawButton.setText("Close Graphs");
		this.pauseExecution();
		
		// create and visualize plot graph
		this.plotGraph = PlotGraphController.getPlotListener().visualizeGraph(COMPRESS_GRAPH);
		
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