package inBloom.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import jason.asSemantics.Mood;

import inBloom.PlotControlsLauncher;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.helper.MoodMapper;
import inBloom.storyworld.Character;


/**
 * Responsible for creating and visualizing the graph the represents the development of the pleasure value of all
 * characters during the development of the plot.
 * Class provides a singleton instance: <i>moodListener</i>, which is accessible throughout inBloom for creating
 * mood graphs.
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class MoodGraph extends JFrame implements PlotmasGraph {

	protected static Logger logger = Logger.getLogger(MoodGraph.class.getName());
	public static String[] MOOD_DIMS = new String[] {"pleasure", "arousal", "dominance"};
	public static int SAMPLING_STEP = 1;

	private static MoodGraph moodListener = null;

	private XYSeriesCollection moodData = null;
	private String selectedMoodDimension = null;
	private String selectedAgent = null;
	private JFreeChart chart = null;
	private ChartPanel chartPanel = null;
	private Map<Integer, Long> stepReasoningcycleNumMap = new HashMap<>();

	public static MoodGraph getMoodListener() {
		if (MoodGraph.moodListener==null) {
			MoodGraph.moodListener = new MoodGraph();
		};
		return MoodGraph.moodListener;
	}

	public MoodGraph() {
		super("Mood Graph");
		this.moodData = new XYSeriesCollection();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}

	public MoodGraph(String title) {
		super("Mood Graph " + title);
		this.moodData = new XYSeriesCollection();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}

	public void createData(MoodMapper mapper) {
		this.createData(SAMPLING_STEP, mapper);
	}

	public void createData(int samplingStep, MoodMapper mapper) {
		this.deleteGraphData();

		this.stepReasoningcycleNumMap = mapper.stepReasoningcycleNumMap;

		logger.fine("Using following mood data to create mood graph:\n" + mapper.toString());
		Long startTime = this.stepReasoningcycleNumMap.get(0);
		Long endTime = Math.max(mapper.latestEndTime(), this.stepReasoningcycleNumMap.entrySet().stream()
																								.max((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
																								.map(e -> e.getValue())
																								.get()
								);

		if(this.selectedMoodDimension != null) {
			for(String agName: mapper.mappedAgents()) {
				this.moodData.addSeries(new XYSeries(agName));

				// for every 10ms from start time until end time sample mood and put it into the graph
				for (Long x_val = startTime; x_val < endTime + 1; x_val += samplingStep) {
					Double sampledMood = mapper.sampleMood(agName, x_val).get(this.selectedMoodDimension);
					this.addMoodPoint(sampledMood, x_val, agName);
				}
			}
		} else if (this.selectedAgent != null) {
			for(String dim: MOOD_DIMS) {
				this.moodData.addSeries(new XYSeries(dim));

				// for every 10ms from start time until end time sample mood and put it into the graph
				for (Long x_val = startTime; x_val < endTime + 1; x_val += samplingStep) {
					Double sampledMood = mapper.sampleMood(this.selectedAgent, x_val).get(dim);
					this.addMoodPoint(sampledMood, x_val, dim);
				}
			}
		} else {
			throw new RuntimeException("Mood Graph: Wrong dimension selected in drop down.");
		}
	}

	/**
	 * Sets the initially selected dimension to be visualized to the given agent, and removes
	 * any previous selection on mood domain.
	 * @param agentName
	 */
	public void setSelectedAgent(String agentName) {
		this.selectedMoodDimension = null;
		this.selectedAgent = agentName;
	}

	/**
	 * Sets the initially selected dimension to be visualized to the given dimension, and removes
	 * any previous selection on agent domain.
	 * @param agentName
	 */
	public void setSelectedDimension(String dimension) {
		this.selectedAgent = null;
		this.selectedMoodDimension = dimension;
	}

	public void createChart(XYSeriesCollection data) {
		String title = "Mood Development Over Time";
		if (data.getSeries().isEmpty()) {
			title = "No mood points have been reported to MoodGraph";
		}

		JFreeChart lineChart = ChartFactory.createXYLineChart(
				title,
				"reasoning cycle number",
				"value",
				data,
				PlotOrientation.VERTICAL,
				true,true,false);

		lineChart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(220, 0, 0));
		lineChart.getXYPlot().getRenderer().setSeriesPaint(1, new Color(0, 0, 220));
		lineChart.getXYPlot().getRenderer().setSeriesPaint(2, new Color(0, 220, 0));
		lineChart.getXYPlot().getRenderer().setSeriesPaint(3, Color.black);

		for(Entry<Integer,Long> stepReasCycPair : this.stepReasoningcycleNumMap.entrySet()) {
			Marker marker = new ValueMarker(stepReasCycPair.getValue());
			marker.setPaint(Color.black);
			marker.setLabelPaint(Color.black);
			marker.setLabel(stepReasCycPair.getKey().toString());
			marker.setLabelOffset(new RectangleInsets(5, 6, 15, 15));
			lineChart.getXYPlot().addDomainMarker(marker);
		}

        // setup chart
		this.chart = lineChart;
		this.chartPanel = new ChartPanel(this.chart);
		this.chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ));
	}


	/**
	 * Instead saves the mood graph of the initial dimension to disc, instead of visualizing it.
	 * The file will be located in the projects root directory.
	 * Use like: </br>
	 * <code>
	 * MoodGraph.getMoodListener().setSelectedAgent("hen");
	 * MoodGraph.getMoodListener().saveGraph(er.getLastModel().moodMapper, String.valueOf(currentCycle));
	 * </code>
	 * @param moodMapper
	 * @param suffix
	 */
	public void saveGraph(MoodMapper moodMapper, String suffix) {
		this.createData(moodMapper);
		this.createChart(this.getData());
		this.setupChart(this.getData());

		try {
			String selection = this.selectedMoodDimension == null ? this.selectedAgent : this.selectedMoodDimension;
			OutputStream out = new FileOutputStream("mood_" + selection + "_" + suffix + ".png");
		    ChartUtilities.writeChartAsPNG(out,this.chart, this.chartPanel.getPreferredSize().width, this.chartPanel.getPreferredSize().height);
		    out.close();
		} catch (IOException ex) {
			logger.warning(ex.getMessage());
		}
	}

	public void deleteGraphData() {
		this.moodData.removeAllSeries();
	}

	public MoodGraph visualizeGraph() {
		return this.visualizeGraph(this.moodData);
	}

	public MoodGraph visualizeGraph(XYSeriesCollection data) {
		// transform data into line graphs
		this.createChart(data);
		// setup details of chart visualization
		this.setupChart(data);

		// create dropdown to select mood dimension
		JComboBox<String> moodDimensionList = new JComboBox<>(MOOD_DIMS);
		for (Character chara : PlotLauncher.getRunner().getUserModel().getCharacters()) {
			moodDimensionList.addItem(chara.name);
		}

		moodDimensionList.setSelectedItem(this.selectedMoodDimension);
		moodDimensionList.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
				String selectedDimension = (String) combo.getSelectedItem();

				MoodGraph.getMoodListener().selectedMoodDimension = null;
				MoodGraph.getMoodListener().selectedAgent = null;
				if(Mood.DIMENSIONS.contains(selectedDimension)) {
					MoodGraph.getMoodListener().selectedMoodDimension = selectedDimension;
				} else {
					MoodGraph.getMoodListener().selectedAgent = selectedDimension;
				}
				MoodGraph.getMoodListener().createData(((PlotEnvironment<PlotModel<?>>)PlotLauncher.getRunner().getEnvironmentInfraTier().getUserEnvironment()).getModel().moodMapper);

				((XYPlot) MoodGraph.getMoodListener().chart.getPlot()).getRangeAxis().setLabel("value");

				MoodGraph.getMoodListener().repaint();
			}
		});

		this.add(this.chartPanel, BorderLayout.CENTER);
		this.add(moodDimensionList, BorderLayout.NORTH);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	MoodGraph.getMoodListener().closeGraph();
		        }
		    }
		);

		this.pack( );
		RefineryUtilities.positionFrameOnScreen(this, 0.8, 0.1);
		this.setVisible(true);

		return this;
	}

	public void setupChart(XYSeriesCollection data) {
		// set up axes for good readability
        NumberAxis xAxis = (NumberAxis) ((XYPlot)this.chart.getPlot()).getDomainAxis();
        xAxis.setRange(data.getDomainLowerBound(false), data.getDomainUpperBound(false));
        // choose step distance so we always have around 15 values on xAxis, and round it to the deca level
        long tickDistance = Math.round((data.getDomainUpperBound(false) - data.getDomainLowerBound(false)) / 15 ) / 10 * 10;
        xAxis.setTickUnit(new NumberTickUnit(tickDistance));

        NumberAxis yAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis();
        yAxis.setRange(-1.1, 1.1);
        yAxis.setTickUnit(new NumberTickUnit(0.2));
	}


	public void closeGraph() {
		this.dispose();

    	PlotControlsLauncher gui = PlotLauncher.getRunner();
    	gui.graphClosed(this);
	}

	private void addMoodPoint(Double value, Long time, String series) {
		this.moodData.getSeries(series).add(time, value);
	}

	public XYSeriesCollection getData() {
		if (null == this.moodData) {
			throw new RuntimeException("Mood data was not initialized, make sure to execute #createData() first");
		}
		return this.moodData;
	}

	public void setData(XYSeriesCollection moodData) {
		this.moodData = moodData;
	}
}
