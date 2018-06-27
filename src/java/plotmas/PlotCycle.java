package plotmas;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;

public abstract class PlotCycle implements Runnable {
	
	private String[] agentNames;
	private String agentSrc;
	
	private boolean isPaused;
	
	private JFrame cycleFrame;
	private JTextArea logTextArea = new JTextArea(10, 40);
	
	protected PlotCycle(String[] agentNames, String agentSrc) {
		this.agentNames = agentNames;
		this.agentSrc = agentSrc;
		initGui();
	}
	
	private void initGui() {
		cycleFrame = new JFrame("Plot Cycle");
		cycleFrame.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(logTextArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		cycleFrame.add(scroll, BorderLayout.CENTER);
		
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener()
			{
			  	public void actionPerformed(ActionEvent e)
			  	{
			  		isPaused = !isPaused;
			  		((JButton)e.getSource()).setText(isPaused ? "Continue" : "Pause");
			  	}
			});
		
		cycleFrame.add(pauseButton, BorderLayout.SOUTH);
		
		cycleFrame.pack();
		cycleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cycleFrame.setVisible(true);
	}
	
	protected void closeGui() {
		cycleFrame.setVisible(false);
		cycleFrame.dispose();
	}
	
	protected abstract ReflectResult reflect(EngageResult er);
	protected abstract ReflectResult createInitialReflectResult();
	
	@SuppressWarnings("deprecation")
	protected EngageResult engage(ReflectResult rr) {
		PlotLauncher runner = rr.getRunner();
		Personality[] personalities = rr.getPersonalities();
		Thread t = new Thread(this.new Cycle(runner, new String[0], createAgs(runner, personalities), this.agentSrc));
		t.start();
		MASConsoleGUI.get().setPause(false);
		long startTime = System.currentTimeMillis();
		long timeout = 1900;
		while(t.isAlive()) {
			try {
				if(MASConsoleGUI.hasConsole()) {
					//MASConsoleGUI.get().getFrame().setVisible(false);
					if(!isPaused) {
						if(MASConsoleGUI.get().isPause() || System.currentTimeMillis() - startTime > timeout) {
							runner.reset();
							t.stop();
						}
					}
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				break;
			}
		}
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		return new EngageResult(graph, PlotGraphController.getPlotListener().analyze(graph));
	}
	
	private ImmutableList<LauncherAgent> createAgs(PlotLauncher runner, Personality[] personalities) {
		if(personalities.length != this.agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(runner.new LauncherAgent(agentNames[i], personalities[i]));
		}
		return ImmutableList.copyOf(agents);
	}
	
	@Override
	public void run() {
		ReflectResult rr = this.createInitialReflectResult();
		while(rr.shouldContinue) {
			EngageResult er = engage(rr);
			rr = this.reflect(er);
		}
		this.finish();
	}
	
	protected void finish() {
	}
	
	protected void log(String string) {
		logTextArea.append(string + "\n");
		logTextArea.setCaretPosition(logTextArea.getText().length());
		logTextArea.repaint();
	}
	
	private class Cycle implements Runnable {
		
		private PlotLauncher runner;
		private String[] args;
		private ImmutableList<LauncherAgent> agents;
		private String agSrc;
		
		Cycle(PlotLauncher runner, String[] args, ImmutableList<LauncherAgent> agents, String agSrc) {
			this.runner = runner;
			this.args = args;
			this.agents = agents;
			this.agSrc = agSrc;
		}

		@Override
		public void run() {
			try {
				runner.run(args, agents, agSrc);
			} catch (JasonException e) {
				log("JasonException!");
			}
		}
	}
	
	public class ReflectResult {
		private PlotLauncher runner;
		private Personality[] personalities;
		private boolean shouldContinue;
		
		public ReflectResult(PlotLauncher runner, Personality[] personalities) {
			this(runner, personalities, true);
		}
		
		public ReflectResult(PlotLauncher runner, Personality[] personalities, boolean shouldContinue) {
			this.runner = runner;
			this.personalities = personalities;
			this.shouldContinue = shouldContinue;
		}
		
		public PlotLauncher getRunner() {
			return this.runner;
		}
		
		public Personality[] getPersonalities() {
			return this.personalities;
		}
		
		public boolean shouldContinue() {
			return this.shouldContinue;
		}
	}
	
	public class EngageResult {
		private PlotDirectedSparseGraph plotGraph;
		private float tellabilityScore;
		
		public EngageResult(PlotDirectedSparseGraph plotGraph, float tellabilityScore) {
			this.plotGraph = plotGraph;
			this.tellabilityScore = tellabilityScore;
		}
		
		public PlotDirectedSparseGraph getPlotGraph() {
			return this.plotGraph;
		}
		
		public float getTellability() {
			return this.tellabilityScore;
		}
	}
}
