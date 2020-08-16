package inBloom.evo;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

public class Chart extends ApplicationFrame{
	
	private int width;
	private int height;
	
	//public JFreeChart chart;
	
	public Chart(String title, String name, XYDataset set, int width, int height) {
		
		super(title);
		
		this.width = width;
		this.height = height;
		
		JFreeChart lineChart = ChartFactory.createXYLineChart(name, "Generation", "Tellability", set, PlotOrientation.VERTICAL,true,true,false);
		
		//chart = lineChart;
		
		ChartPanel chartPanel = new ChartPanel(lineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(width, height));
		setContentPane(chartPanel);
      
	}
}	
