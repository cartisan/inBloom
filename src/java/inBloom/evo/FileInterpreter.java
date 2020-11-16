package inBloom.evo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public class FileInterpreter<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	
	private EvolutionaryEnvironment<EnvType,ModType> EVO_ENV;
	
	public String filename;
	public String filepath;
	
	public List<Double> population_best = new ArrayList<Double>();
	public List<Double> population_average = new ArrayList<Double>();
	private Candidate best_individual;
	
	private int number_agents;
	private int number_happenings;
	private int simulation_length;
	private int actual_length;
	private Fitness<EnvType,ModType> fit;
	
	
	public FileInterpreter(EvolutionaryEnvironment<EnvType,ModType> EVO_ENV, String path, String name, boolean showGui) {
		
		this.EVO_ENV = EVO_ENV;
		filename = name;
		filepath = path;
		
		fit = new Fitness<EnvType,ModType>(EVO_ENV,true,Level.INFO,showGui);
		
	}
	
	public void readFile() {
		
		File file = new File(filepath+filename);
		
		try {

			BufferedReader in = new BufferedReader(new FileReader(file));
			
			// Read population_best
			String line = in.readLine();
			StringTokenizer tk = new StringTokenizer(line);
			
			while(tk.hasMoreTokens())
				population_best.add(Double.parseDouble(tk.nextToken()));
			
			// Read population_average
			line = in.readLine();
			tk = new StringTokenizer(line);
			
			while(tk.hasMoreTokens())
				population_average.add(Double.parseDouble(tk.nextToken()));
			
			// Read best individual
			number_agents = Integer.parseInt(in.readLine());
			number_happenings = Integer.parseInt(in.readLine());
			
			simulation_length = Integer.parseInt(in.readLine());
			actual_length = Integer.parseInt(in.readLine());
			
			// Personality
			ChromosomePersonality pers = new ChromosomePersonality(number_agents);
			
			for(int i = 0; i<number_agents;i++) {
				line = in.readLine();
				tk = new StringTokenizer(line);
				for(int j = 0; j < 5; j++) {
					pers.values[i][j] = Double.parseDouble(tk.nextToken());
				}
			}
			
			// Happenings
			ChromosomeHappenings hap = new ChromosomeHappenings(number_agents,number_happenings);

			for(int i = 0; i<number_agents;i++) {
				line = in.readLine();
				tk = new StringTokenizer(line);
				for(int j = 0; j < number_happenings; j++) {
					hap.values[i][j] = Integer.parseInt(tk.nextToken());
				}
			}
			
			// get best individual
			best_individual = new Candidate(pers,hap,simulation_length,fit);
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void get_chart() {
		
		// Initialize Dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		XYSeries best = new XYSeries("Population_Best");
		XYSeries average = new XYSeries("Population_average");

		// Copy Data
		for(int i = 0; i<population_best.size();i++) {

			best.add(i,population_best.get(i));
			average.add(i,population_average.get(i));
		}
		
		dataset.addSeries(best);
		dataset.addSeries(average);
		
		// Initialize Chart
		int width = 1000;
		int height = 600;
		
		Chart chart = new Chart("Results",filename,dataset,width,height);
		
		chart.pack();
	    RefineryUtilities.centerFrameOnScreen( chart );
	    chart.setVisible( true );
	}

	
	
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		
		String path = "C:\\Users\\Felix\\Desktop\\!\\Ergebnisse\\";
//		String name = "GEN";
//		String name = "PSO";
		String name = "QSO";

		@SuppressWarnings("unchecked")
		FileInterpreter fi = new FileInterpreter(new EvoIsland(),path, name, true);
		//FileInterpreter fi = new FileInterpreter(new EvoIsland(),path, name, false);
		
		fi.readFile();
		
		fi.get_chart();
		
	}
}
