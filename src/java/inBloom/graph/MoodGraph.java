package inBloom.graph;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import com.google.common.collect.ImmutableMap;

import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.PlotModel;


/**
 * Responsible for creating and visualizing the graph the represents the development of the pleasure value of all
 * characters during the development of the plot.
 * Class provides a singleton instance: <i>moodListener</i>, which is accessible throughout inBloom for creating
 * mood graphs. 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class MoodGraph extends JFrame implements PlotmasGraph {

	public static final Map<Boolean, String> X_LABEL_IS_TIME = ImmutableMap.of(
		    true, "plot time in ms",
		    false, "plot time in environment steps"
	);
	protected static Logger logger = Logger.getLogger(MoodGraph.class.getName());
	public static String[] MOOD_DIMS = new String[] {"pleasure", "arousal", "dominance"};
	public static int SAMPLING_STEP = 10;
	
	private static MoodGraph moodListener = null;
	
	private XYSeriesCollection moodData = null;
	private String selectedMoodDimension = null;
	private JFreeChart chart = null;
	private PlotModel<?> model = null;

	public static MoodGraph getMoodListener() {
		if (MoodGraph.moodListener==null) {
			MoodGraph.moodListener = new MoodGraph();
		};
		return MoodGraph.moodListener;
	}
	
	public MoodGraph() {
		super("Mood Graph");
		moodData = new XYSeriesCollection();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}
	
	public MoodGraph(String title) {
		super("Mood Graph " + title);
		moodData = new XYSeriesCollection();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}
	
	public void createData(PlotModel<?> model) {
		this.createData(SAMPLING_STEP, model);
	}
	
	public void createData(int samplingStep, PlotModel<?> model) {
		MoodGraph.SAMPLING_STEP = samplingStep;
		this.deleteGraphData();
		this.model  = model;
		
		logger.fine("Using following mood data to create mood graph:\n" + this.model.moodMapper.toString());
		Long startTime = this.model.moodMapper.latestStartTime();
		Long endTime = this.model.moodMapper.latestEndTime();
		startTime = startTime - (startTime % 10) + 10;		// round up start time to next multiple of 10
				
		for(String agName: this.model.moodMapper.mappedAgents()) {
			this.moodData.addSeries(new XYSeries(agName));
			
			
			// for every 10ms from start time until end time sample mood and put it into the graph
			for (Long x_val = startTime; x_val < endTime + 1; x_val += samplingStep) {
				Double sampledMood = this.model.moodMapper.sampleMood(agName, x_val).get(selectedMoodDimension);
				this.addMoodPoint(sampledMood, x_val, agName);
			}
		}
	}
	
	private void createChart(XYSeriesCollection data) {
		String title = "Mood Development Over Time"; 
		if (data.getSeries().isEmpty())
			title = "No mood points have been reported to MoodGraph";
		
		JFreeChart lineChart = ChartFactory.createXYLineChart(
				title,
				X_LABEL_IS_TIME.get(PlotModel.X_AXIS_IS_TIME),
				this.selectedMoodDimension,
				data,
				PlotOrientation.VERTICAL,
				true,true,false);
		
		this.chart = lineChart;
	}
	
	public void deleteGraphData() {
		this.moodData.removeAllSeries();
		this.model = null;
	}
	
	public MoodGraph visualizeGraph() {
		return this.visualizeGraph(this.moodData);
	}
	
	public MoodGraph visualizeGraph(XYSeriesCollection data) {
		// create chart
		this.createChart(data);
		
		// set up axes for good readability
        NumberAxis xAxis = (NumberAxis) ((XYPlot)this.chart.getPlot()).getDomainAxis();
        xAxis.setRange(data.getDomainLowerBound(false), data.getDomainUpperBound(false));
        // choose step distance so we always have around 15 values on xAxis, and round it to the deca level
        long tickDistance = Math.round((data.getDomainUpperBound(false) - data.getDomainLowerBound(false)) / 15 ) / 10 * 10;
        xAxis.setTickUnit(new NumberTickUnit(tickDistance));
        
        NumberAxis yAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis();
        yAxis.setRange(-1.1, 1.1);
        yAxis.setTickUnit(new NumberTickUnit(0.2));
		
        // setup chart
		ChartPanel chartPanel = new ChartPanel(this.chart);
		chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ));
		
		// create dropdown to select mood dimension
		JComboBox<String> moodDimensionList = new JComboBox<>(MOOD_DIMS);
		moodDimensionList.setSelectedItem(this.selectedMoodDimension);
		moodDimensionList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				@SuppressWarnings("unchecked")
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
				String selectedDimension = (String) combo.getSelectedItem();
				
				MoodGraph.getMoodListener().selectedMoodDimension = selectedDimension;
				MoodGraph.getMoodListener().createData(model);
				
				((XYPlot) MoodGraph.getMoodListener().chart.getPlot()).getRangeAxis().setLabel(
						MoodGraph.getMoodListener().selectedMoodDimension
				);
				
				MoodGraph.getMoodListener().repaint();
			}
		});
		
		this.add(chartPanel, BorderLayout.CENTER);
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

	
	public void closeGraph() {
		this.dispose();
    	
    	PlotControlsLauncher gui = PlotLauncher.getRunner();
    	gui.graphClosed(this);
	}
	
	private void addMoodPoint(Double value, Long time, String agName) {
		this.moodData.getSeries(agName).add(time, value);
	}

	public XYSeriesCollection getData() {
		if (null == moodData)
			throw new RuntimeException("Mood data was not initialized, make sure to execute #createData() first");
		return moodData;
	}

	public void setData(XYSeriesCollection moodData) {
		this.moodData = moodData;
	}
}
