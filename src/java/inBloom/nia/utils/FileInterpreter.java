package inBloom.nia.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.nia.CandidateSolution;
import inBloom.nia.ChromosomeHappenings;
import inBloom.nia.ChromosomePersonality;
import inBloom.nia.Fitness;
import inBloom.nia.NIEnvironment;
import inBloom.nia.ga.Individual;
import inBloom.stories.little_red_hen.FarmNIEnvironment;

public class FileInterpreter<EnvType extends PlotEnvironment<ModType>, ModType extends PlotModel<EnvType>> {
	@SuppressWarnings("unused")
	private NIEnvironment<EnvType,ModType> EVO_ENV;

	public String filename;
	public String filepath;

	public List<Double> population_best = new ArrayList<>();
	public List<Double> population_average = new ArrayList<>();
	private CandidateSolution best_individual;

	private int number_agents;
	private int number_happenings;
	private int simulation_length;
	private int actual_length;
	private Fitness<EnvType,ModType> fit;


	public FileInterpreter(NIEnvironment<EnvType,ModType> EVO_ENV, String path, String name, boolean showGui) {
		this.EVO_ENV = EVO_ENV;
		this.filename = name;
		this.filepath = path;

		this.fit = new Fitness<>(EVO_ENV, false, Level.INFO, showGui);
		Fitness.TIMEOUT = 100000;
	}

	public CandidateSolution getBest_individual() {
		return this.best_individual;
	}

	public void setBest_individual(Individual best_individual) {
		this.best_individual = best_individual;
	}

	public int getActual_length() {
		return this.actual_length;
	}

	public void setActual_length(int actual_length) {
		this.actual_length = actual_length;
	}

	private String readNextContentLine(BufferedReader in) throws IOException {
		String line = in.readLine();

		// Skip lines that contain annotations, which are embedded in < and >.
		while (line.startsWith("<")) {
			line = in.readLine();
		}

		return line;
	}

	public void readFile() {
		File file = new File(this.filepath+this.filename);

		try {

			BufferedReader in = new BufferedReader(new FileReader(file));

			// Read population_best
			String line = this.readNextContentLine(in);
			StringTokenizer tk = new StringTokenizer(line);

			while(tk.hasMoreTokens()) {
				this.population_best.add(Double.parseDouble(tk.nextToken()));
			}

			// Read population_average
			line = this.readNextContentLine(in);
			tk = new StringTokenizer(line);

			while(tk.hasMoreTokens()) {
				this.population_average.add(Double.parseDouble(tk.nextToken()));
			}

			// Read best individual
			this.number_agents = Integer.parseInt(this.readNextContentLine(in));
			this.number_happenings = Integer.parseInt(this.readNextContentLine(in));

			this.simulation_length = Integer.parseInt(this.readNextContentLine(in));
			this.setActual_length(Integer.parseInt(this.readNextContentLine(in)));

			// Personality
			ChromosomePersonality pers = new ChromosomePersonality(this.number_agents);

			for(int i = 0; i<this.number_agents;i++) {
				line = this.readNextContentLine(in);
				tk = new StringTokenizer(line);
				for(int j = 0; j < 5; j++) {
					pers.values[i][j] = Double.parseDouble(tk.nextToken());
				}
			}

			// Happenings
			ChromosomeHappenings hap = new ChromosomeHappenings(this.number_agents,this.number_happenings);

			for(int i = 0; i<this.number_agents;i++) {
				line = this.readNextContentLine(in);
				tk = new StringTokenizer(line);
				for(int j = 0; j < this.number_happenings; j++) {
					hap.values[i][j] = Integer.parseInt(tk.nextToken());
				}
			}

			// get best individual
			this.setBest_individual(new Individual(pers,hap,this.simulation_length,this.fit));
			in.close();

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
		for(int i = 0; i<this.population_best.size();i++) {

			best.add(i,this.population_best.get(i));
			average.add(i,this.population_average.get(i));
		}

		dataset.addSeries(best);
		dataset.addSeries(average);


		// Initialize Chart
		int width = 1000;
		int height = 600;

		Chart chart = new Chart("Results",this.filename,dataset,width,height);

		chart.pack();
	    RefineryUtilities.centerFrameOnScreen( chart );
	    chart.setVisible( true );
	}

	public static void main(String[] args) {
		String path = "C:\\Users\\Leon\\Desktop\\InBloomNIA\\";
		String name = "GEN_run.log";

		FileInterpreter<?,?> fi = new FileInterpreter<>(new FarmNIEnvironment(), path, name, true);

		fi.readFile();

		fi.get_chart();
	}
}
