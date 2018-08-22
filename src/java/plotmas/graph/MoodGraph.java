package plotmas.graph;

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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import com.google.common.collect.ImmutableMap;

import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.PlotModel;


/**
 * Responsible for creating and visualizing the graph the represents the development of the pleasure value of all
 * characters during the development of the plot.
 * Class provides a singleton instance: <i>moodListener</i>, which is accessible throughout plotmas for creating
 * mood graphs. 
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class MoodGraph extends JFrame implements PlotmasGraph {

	public static final Map<Boolean, String> X_AXIS_LABEL_MAP = ImmutableMap.of(
		    true, "plot time in ms",
		    false, "plot time in environment steps"
	);
	protected static Logger logger = Logger.getLogger(MoodGraph.class.getName());
	public static String[] MOOD_DIMS = new String[] {"pleasure", "arousal", "dominance"};
	private static MoodGraph moodListener = null;
	
	private DefaultCategoryDataset moodData = null;
	private String selectedMoodDimension = null;
	private JFreeChart chart = null;

	public static MoodGraph getMoodListener() {
		if (MoodGraph.moodListener==null) {
			MoodGraph.moodListener = new MoodGraph();
		};
		return MoodGraph.moodListener;
	}
	
	public MoodGraph() {
		super("Mood Graph");
		moodData = new DefaultCategoryDataset();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}
	
	public MoodGraph(String title) {
		super("Mood Graph " + title);
		moodData = new DefaultCategoryDataset();
		this.selectedMoodDimension = MOOD_DIMS[0];
	}
	
	public void createData() {
		this.deleteGraphData();
		
		logger.fine("Using following mood data to create mood graph:\n" + PlotModel.moodMapper.toString());
		
		Long startTime = PlotModel.moodMapper.latestStartTime();
		startTime = startTime - (startTime % 10) + 10;		// round up start time to next multiple of 10
				
		for(String agName: PlotModel.moodMapper.mappedAgents()) {
			Long endTime = PlotModel.moodMapper.latestMoodEntry(agName);
			
			// for every 10ms from start time until end time sample mood and put it into the graph
			for (Long x_val = startTime; x_val < endTime + 1; x_val += 10) {
				Double sampledMood = PlotModel.moodMapper.sampleMood(agName, x_val).get(selectedMoodDimension);
				this.addMoodPoint(sampledMood, x_val, agName);
			}
		}
	}
	
	private void createChart(DefaultCategoryDataset data) {
		String title = "Mood Development Over Time"; 
		if (data.getRowCount() == 0)
			title = "No mood points have been reported to MoodGraph";
		
		JFreeChart lineChart = ChartFactory.createLineChart(
				title,
				X_AXIS_LABEL_MAP.get(PlotModel.X_AXIS_IS_TIME), this.selectedMoodDimension,
				data,
				PlotOrientation.VERTICAL,
				true,true,false);
		
		this.chart = lineChart;
	}
	
	public void deleteGraphData() {
		this.moodData.clear();
	}
	
	public MoodGraph visualizeGraph() {
		return this.visualizeGraph(this.moodData);
	}
	
	public MoodGraph visualizeGraph(DefaultCategoryDataset data) {
		// create line chart
		this.createChart(data);
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
				MoodGraph.getMoodListener().createData();
				
				((CategoryPlot) MoodGraph.getMoodListener().chart.getPlot()).getRangeAxis().setLabel(
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
		this.moodData.addValue(value, agName, time);
	}

	/*************************** for testing purposes ***********************************/	
	public static void main( String[ ] args ) {
		MoodGraph chart = new MoodGraph();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		dataset.addValue( 15 , "schools" , "1970" );
		dataset.addValue( 30 , "schools" , "1980" );
		dataset.addValue( 60 , "schools" ,  "1990" );
		dataset.addValue( 120 , "schools" , "2000" );
		dataset.addValue( 240 , "schools" , "2010" );
		dataset.addValue( 300 , "schools" , "2014" );
		dataset.addValue( 100 , "trains" , "2000" );
		dataset.addValue( 200 , "trains" , "2010" );
		dataset.addValue( 300 , "trains" , "2014" );
		
		chart.visualizeGraph(dataset);
	}
}
