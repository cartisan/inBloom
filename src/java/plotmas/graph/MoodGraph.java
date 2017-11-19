package plotmas.graph;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.LongStream;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import plotmas.PlotAwareAg;


@SuppressWarnings("serial")
public class MoodGraph extends JFrame {
	protected static Logger logger = Logger.getLogger(MoodGraph.class.getName());
	private static MoodGraph moodListener = null;
	private DefaultCategoryDataset moodData = null;

	public MoodGraph() {
		super("Mood Graph");
		moodData = new DefaultCategoryDataset();
	}
	
	public MoodGraph(String title) {
		super("Mood Graph " + title);
		moodData = new DefaultCategoryDataset();
	}
	
	public void createGraph() {
		this.deleteGraphData();
		
		Long startTime = PlotAwareAg.moodMapper.latestStartTime();
		startTime = startTime - (startTime % 10) + 10;		// round up start time to next multiple of 10
				
		for(String agName: PlotAwareAg.moodMapper.mappedAgents()) {
			Long endTime = PlotAwareAg.moodMapper.latestMoodEntry(agName);
			
			// for every 10ms from start time until end time sample mood and put it into the graph
			Iterator<Long> it = LongStream.iterate(startTime, n -> n+1).limit(endTime / 1 + 1).iterator();
			while(it.hasNext()) {
				Long x_val = it.next();
				Double sampledMood = PlotAwareAg.moodMapper.sampleMood(agName, x_val);
				this.addMoodPoint(sampledMood, x_val, agName);
			}
		}
	}
	
	public void deleteGraphData() {
		this.moodData.clear();
	}
	
	public void visualizeGraph() {
		this.visualizeGraph(this.moodData);
	}
	
	public void visualizeGraph(DefaultCategoryDataset data) {
		String title = "Pleasure development over time"; 
		if (data.getRowCount() == 0)
			title = "No mood points have been reported to MoodGraph";
		
		JFreeChart lineChart = ChartFactory.createLineChart(
				title,
				"plot time in ms", "Pleasure",
				data,
				PlotOrientation.VERTICAL,
				true,true,false);

		ChartPanel chartPanel = new ChartPanel(lineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ));
		setContentPane(chartPanel);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	MoodGraph.getMoodListener().dispose();
		        }
		    }
		);
		
		this.pack( );
		RefineryUtilities.positionFrameOnScreen(this, 0.8, 0.1);
		this.setVisible(true);
	}
	
	public void addMoodPoint(Double value, Long time, String agName) {
		this.moodData.addValue(value, agName, time);
	}

	public static MoodGraph getMoodListener() {
		if (MoodGraph.moodListener==null) {
			MoodGraph.moodListener = new MoodGraph();
		};
		return MoodGraph.moodListener;
	}
	
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
