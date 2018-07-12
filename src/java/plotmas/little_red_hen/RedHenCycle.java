package plotmas.little_red_hen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import jason.asSemantics.Personality;
import plotmas.PlotCycle;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.visitor.EdgeLayoutVisitor;
import plotmas.helper.Tellability;

public class RedHenCycle extends PlotCycle {
	
	int cycle = 0;
	
	double[] scale = new double[] { -0.9, 0.9 };
	
	private PlotDirectedSparseGraph bestGraph = null;
	private double bestTellability = -1f;
	private Personality[] bestPersonalities = null;
	
	private List<Personality[]> personalityList;
	private Iterator<Personality[]> personalityIterator;
	
	private Personality[] lastPersonalities;
	
	private PrintWriter csvOut;

	protected RedHenCycle() {
		// Create PlotCycle with needed agents.
		super(new String[] { "hen", "dog", "cow", "pig" }, "agent");
		
		// Open a file for writing results
		try {
			FileWriter fw = new FileWriter("results.csv");
		    BufferedWriter bw = new BufferedWriter(fw);
		    csvOut = new PrintWriter(bw);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Print CSV header
		csvOut.println("tellability,functional_units,polyvalent_vertices,o0,c0,e0,a0,n0,o1,c1,e1,a1,n1");
		
		// Generate all possible personalities.
		personalityList = createPlotSpace(createPersonalitySpace(new double[] { -0.9, 0, 0.9 }), 2);
		personalityIterator = personalityList.iterator();
		
		// Log how many personalities there are.
		log("Running " + personalityList.size() + " cycles...");
	}
	
	public static void main(String[] args) {
		RedHenCycle cycle = new RedHenCycle();
		cycle.run();
	}
	
	private void onCycleResult(Personality[] personalities, Tellability tellability) {
		StringBuilder csv = new StringBuilder();
		Formatter f = new Formatter(csv);
		f.format(Locale.ENGLISH, "%f,%d,%d,", tellability.functionalPolyvalence, tellability.numFunctionalUnits, tellability.numPolyvalentVertices);
		for(Personality p : personalities) {
			f.format(Locale.ENGLISH, "%f,%f,%f,%f,%f,",
								p.O, p.C, p.E, p.A, p.N);
		}
		csv.deleteCharAt(csv.length() - 1);
		csvOut.println(csv);
		f.close();
		if(cycle % 30 == 0) {
			csvOut.flush();
		}
	}
	
	@Override
	protected void finalize() {
		if(csvOut != null) {
			csvOut.close();
		}
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		onCycleResult(lastPersonalities, er.getTellability());
		
		// Save tellability, graph and hen personality if it was better than the best before
		if(er.getTellability().functionalPolyvalence > bestTellability) {
			bestTellability = er.getTellability().functionalPolyvalence;
			log("New best: " + bestTellability);
			bestGraph = er.getPlotGraph();
			bestPersonalities = lastPersonalities;
		}
		
		// Stop cycle if there are no other personality combinations
		if(!personalityIterator.hasNext()) {
			return new ReflectResult(null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		log("Cycle " + cycle);
		cycle++;
		return new ReflectResult(new RedHenLauncher(), new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
//		return new ReflectResult(new RedHenLauncher(), new Personality[] {lastPersonalities[0], new Personality(0, -1, 0, -0.7, -0.8), new Personality(0, -1, 0, -0.7, -0.8), new Personality(0, -1, 0, -0.7, -0.8)});
	}
	
	@Override
	protected void finish() {
		// Print results
		log("Best tellability: " + bestTellability);
		log("Personalities:");
		for(Personality p : bestPersonalities) {
			log("\t" + p.toString());
		}
		
		// Add best graph to PlotGraphController
		bestGraph.setName("Plot graph with highest tellability");
		bestGraph.accept(new EdgeLayoutVisitor(bestGraph, 9));
		PlotGraphController.getPlotListener().addGraph(bestGraph);
	}
	
	public List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters) {
		List<int[]> values = allCombinations(characters, personalitySpace.length, false);
		List<Personality[]> allPersonalityCombinations = new LinkedList<Personality[]>();
		for(int[] charPersonalities : values) {
			Personality[] personalityArray = new Personality[characters];
			for(int i = 0; i < characters; i++) {
				personalityArray[i] = personalitySpace[charPersonalities[i]];
			}
			allPersonalityCombinations.add(personalityArray);
		}
		return allPersonalityCombinations;
	}
	
	public Personality[] createPersonalitySpace(double[] scale) {
		List<Personality> personalities = new LinkedList<Personality>();
		List<int[]> values = allCombinations(5, scale.length, true);
		for(int[] ocean : values) {
			personalities.add(new Personality(
					scale[ocean[0]],
					scale[ocean[1]], 
					scale[ocean[2]],
					scale[ocean[3]],
					scale[ocean[4]]));
		}
		Personality[] result = new Personality[personalities.size()];
		return personalities.toArray(result);
	}

	private List<int[]> allCombinations(int n, int k, boolean repeat) {
		List<int[]> res = new ArrayList<int[]>((int)Math.pow(k, n));
		allCombinations(new int[n], k, res, 0, 0, repeat);
		return res;
	}
	
	private void allCombinations(int[] v, int k, List<int[]> result, int index, int min, boolean repeat) {
		if(index == v.length) {
			result.add(v);
			return;
		}
		for(int i = min; i < k; i++) {
			v[index] = i;
			allCombinations(v.clone(), k, result, index + 1, repeat ? min : i, repeat);
		}
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		lastPersonalities = personalityIterator.next();
		ReflectResult rr = new ReflectResult(new RedHenLauncher(), new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
//		ReflectResult rr = new ReflectResult(new RedHenLauncher(), new Personality[] {lastPersonalities[0], new Personality(0, -1, 0, -0.7, -0.8), new Personality(0, -1, 0, -0.7, -0.8), new Personality(0, -1, 0, -0.7, -0.8)});
		log("Cycle " + cycle);
		cycle++;
		return rr;
	}

}
