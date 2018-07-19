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
import jason.runtime.MASConsoleGUI;
import plotmas.PlotCycle;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.visitor.EdgeLayoutVisitor;
import plotmas.helper.Tellability;

public class RedHenCycle extends PlotCycle {
	
	private static String outFile = "results.csv";
	
	private static int startCycle = 0;
	private static int endCycle = -1;
	private static int cycleNum = -1;
	
	private static int flushInterval = 30;
	
	private static boolean closeOnComplete = false;
	
	private PlotDirectedSparseGraph bestGraph = null;
	private double bestTellability = -1f;
	private Personality[] bestPersonalities = null;
	
	private List<Personality[]> personalityList;
	private Iterator<Personality[]> personalityIterator;
	
	private Personality[] lastPersonalities;
	private RedHenLauncher lastRunner;
	
	private PrintWriter csvOut;

	protected RedHenCycle() {
		// Create PlotCycle with needed agents.
		super(new String[] { "hen", "dog", "cow", "pig" }, "agent");
		
		// Open a file for writing results
		try {
			FileWriter fw = new FileWriter(outFile, startCycle > 0);
		    BufferedWriter bw = new BufferedWriter(fw);
		    csvOut = new PrintWriter(bw);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Print CSV header
		if(startCycle == 0)
			csvOut.println("tellability,functional_units,polyvalent_vertices,o0,c0,e0,a0,n0,o1,c1,e1,a1,n1");
		
		// Generate all possible personalities.
		Personality[] personalitySpace = createPersonalitySpace(new double[] { -1.0, 0, 1.0 });
		personalityList = createPlotSpace(personalitySpace, 2, true);
		personalityIterator = personalityList.iterator();
		
		for(int i = 0; i < startCycle && personalityIterator.hasNext(); i++)
			personalityIterator.next();
		
		if(endCycle == -1) {
			endCycle = personalityList.size();
		}
		
		// Log how many personalities there are.
		log("Running " + (endCycle - startCycle) + " cycles...");
	}
	
	public static void main(String[] args) {
		boolean shouldRun = true;
		for(int i = 0; shouldRun && i < args.length; i += 2) {
			shouldRun = handleArgument(args, i);
		}
		if(!shouldRun) {
			printHelp();
			return;
		}
		if(cycleNum > -1) {
			endCycle = startCycle + cycleNum;
		}
		RedHenCycle cycle = new RedHenCycle();
		cycle.run();
	}
	
	private static boolean handleArgument(String[] args, int i) {
		switch(args[i]) {
			case "-close":
				closeOnComplete = true;
				return true;
		}
		if(args.length == i + 1) {
			return false;
		}
		switch(args[i]) {
			case "-out":
				outFile = args[i + 1];
				break;
			case "-start":
				startCycle = Integer.parseInt(args[i + 1]);
				break;
			case "-end":
				if(endCycle > -1) {
					System.err.println("You may only use either \"-cycle\" or \"-end\".");
					return false;
				}
				endCycle = Integer.parseInt(args[i + 1]);
				break;
			case "-cycles":
				if(endCycle > -1) {
					System.err.println("You may only use either \"-cycle\" or \"-end\".");
					return false;
				}
				cycleNum = Integer.parseInt(args[i + 1]);
				break;
			case "-flush":
				flushInterval = Integer.parseInt(args[i + 1]);
				break;
			default:
				return false;
		}
		return true;
	}
	
	private static void printHelp() {
		System.out.println("The following arguments are valid:");
		System.out.println("\t[-out <file name>]\tSets the output file to the given file name.");
		System.out.println("\t[-close]\t\tIf given this argument, the application will close upon completion of the last cycle.");
		System.out.println("\t[-start <cycle>]\tThis lets the application skip all cycles before the provided one.");
		System.out.println("\t[-end <cycle>]\t\tThis determines the first cycle the application will not complete. Do not use with \"-cycles\".");
		System.out.println("\t[-cycles <amount>]\tThis determines how many cycles should be run. Do not use with \"-end\".");
		System.out.println("\t[-flush <interval>]\tSets how many cycles should be run before the output gets written to a file.");
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
		if(startCycle % flushInterval == 0) {
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
		if(!personalityIterator.hasNext() || startCycle >= endCycle) {
			return new ReflectResult(null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		log("Cycle " + startCycle);
		startCycle++;
		
		return new ReflectResult(lastRunner = new RedHenLauncher(), new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
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
		
		// Flush and close file
		csvOut.flush();
		csvOut.close();
		
		if(closeOnComplete) {
			lastRunner.finish();
		}
	}
	
	public List<Personality[]> createPlotSpace(Personality[] personalitySpace, int characters, boolean repeat) {
		List<int[]> values = allCombinations(characters, personalitySpace.length, repeat);
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
		log("Cycle " + startCycle);
		startCycle++;
		return rr;
	}

}
