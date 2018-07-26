package plotmas.little_red_hen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import jason.asSemantics.Personality;
import plotmas.PlotCycle;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.helper.PlotFormatter;
import plotmas.helper.Tellability;

public class RedHenCycle extends PlotCycle {
	
	private static String outFile = "results.csv";
	private static String inFile = "";
	private static String logFile = "";
	
	private static int startCycle = 0;
	private static int endCycle = -1;
	private static int cycleNum = -1;
	
	private static int flushInterval = 30;
	
	private static boolean closeOnComplete = false;
	
	private static boolean hideGui = false;
	
	private double bestTellability = -1f;
	private Personality[] bestPersonalities = null;
	
	private List<Personality[]> personalityList;
	private Iterator<Personality[]> personalityIterator;
	
	private Personality[] lastPersonalities;
	private RedHenLauncher lastRunner;
	
	private PrintWriter csvOut;

	protected RedHenCycle() {
		// Create PlotCycle with needed agents.
		super(new String[] { "hen", "dog", "cow", "pig" }, "agent", !hideGui);
		
		// Open a file for writing results
		try {
			FileWriter fw = new FileWriter(outFile, startCycle > 0);
		    BufferedWriter bw = new BufferedWriter(fw);
		    csvOut = new PrintWriter(bw);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Print CSV header
		if(startCycle == 0) {
			String header = "tellability,functional_units_total,";
			for(FunctionalUnit unit : FunctionalUnits.ALL) {
				header += unit.getName().toLowerCase().replace(' ', '_') + "s,";
			}
			header += "polyvalent_vertices,o0,c0,e0,a0,n0,o1,c1,e1,a1,n1";
			csvOut.println(header);
		}
		
		boolean fileRead = false;
		
		if(!inFile.isEmpty()) {
			try(FileReader fr = new FileReader(inFile);
				BufferedReader br = new BufferedReader(fr);)
			{
				String line;
				personalityList = new LinkedList<Personality[]>();
				while((line = br.readLine()) != null) {
					String[] pd = line.split(",");
					Personality[] configuration = new Personality[] {
						new Personality(
								Double.parseDouble(pd[0]),
								Double.parseDouble(pd[1]),
								Double.parseDouble(pd[2]),
								Double.parseDouble(pd[3]),
								Double.parseDouble(pd[4])),
						new Personality(
								Double.parseDouble(pd[5]),
								Double.parseDouble(pd[6]),
								Double.parseDouble(pd[7]),
								Double.parseDouble(pd[8]),
								Double.parseDouble(pd[9]))
					};
					personalityList.add(configuration);
				}
				fileRead = true;
			} catch(IOException e0) {
				System.err.println("Could not read input file!");
			} catch(IndexOutOfBoundsException | NumberFormatException e1) {
				System.err.println("Input file did not have the correct format.");
			}
		}
		
		if(!fileRead) {
			// Generate all possible personalities.
			Personality[] personalitySpace = createPersonalitySpace(new double[] { -1.0, 0, 1.0 });
			personalityList = createPlotSpace(personalitySpace, 2, true);
		}
		
		
		personalityIterator = personalityList.iterator();
		
		for(int i = 0; i < startCycle && personalityIterator.hasNext(); i++)
			personalityIterator.next();
		
		if(endCycle == -1) {
			endCycle = personalityList.size();
		}
		
		// Create the file handler here if the log file name is invariant of the name
		// (logs all contents to a single file)
		if(!logFile.contains("%d")) {
			setupFileLogger();
		}
		// Log how many personalities there are.
		log("Running " + (endCycle - startCycle) + " cycles...");
	}
	
	private void setupFileLogger() {
		Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) {
        	hs[i].close();
        	Logger.getLogger("").removeHandler(hs[i]);
        }
        
        StreamHandler h;
		try {
			if(logFile.isEmpty()) {
				h = new StreamHandler();
			} else {
				h = new FileHandler(String.format(logFile, startCycle));
			}
			h.setFormatter(new PlotFormatter());
		    Logger.getLogger("").addHandler(h);
		    Logger.getLogger("").setLevel(logFile.isEmpty() ? Level.OFF : Level.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int nextArgument = 0;
		for(int i = 0; nextArgument > -1 && i < args.length; i += nextArgument) {
			nextArgument = handleArgument(args, i);
		}
		if(nextArgument == -1) {
			printHelp();
			return;
		}
		if(cycleNum > -1) {
			endCycle = startCycle + cycleNum;
		}
		RedHenCycle cycle = new RedHenCycle();
		cycle.run();
	}
	
	private static int handleArgument(String[] args, int i) {
		switch(args[i]) {
			case "-close":
				closeOnComplete = true;
				return 1;
			case "-nogui":
				hideGui = true;
				System.out.println("Running in headless mode. This automatically enables \"close on complete\".");
				closeOnComplete = true;
				return 1;
		}
		if(args.length == i + 1) {
			return -1;
		}
		switch(args[i]) {
			case "-in":
				inFile = args[i + 1];
				break;
			case "-out":
				outFile = args[i + 1];
				break;
			case "-start":
				startCycle = Integer.parseInt(args[i + 1]);
				break;
			case "-end":
				if(endCycle > -1) {
					System.err.println("You may only use either \"-cycle\" or \"-end\".");
					return -1;
				}
				endCycle = Integer.parseInt(args[i + 1]);
				break;
			case "-cycles":
				if(endCycle > -1) {
					System.err.println("You may only use either \"-cycle\" or \"-end\".");
					return -1;
				}
				cycleNum = Integer.parseInt(args[i + 1]);
				break;
			case "-log":
				logFile = args[i + 1];
				break;
			case "-flush":
				flushInterval = Integer.parseInt(args[i + 1]);
				break;
			case "-timeout":
				PlotCycle.TIMEOUT = Long.parseLong(args[i + 1]);
				break;
			default:
				return -1;
		}
		return 2;
	}
	
	private static void printHelp() {
		System.out.println("The following arguments are valid:");
		System.out.println("\t[-out <file name>]\tSets the output file to the given file name.");
		System.out.println("\t[-in <file name>]\tSets the input file to the given file name.");
		System.out.println("\t[-log <file name>]\tSets the log file to the given file name. Use %d in the file name to create a new log file for each cycle.");
		System.out.println("\t[-close]\t\tIf given this argument, the application will close upon completion of the last cycle.");
		System.out.println("\t[-start <cycle>]\tThis lets the application skip all cycles before the provided one.");
		System.out.println("\t[-end <cycle>]\t\tThis determines the first cycle the application will not complete. Do not use with \"-cycles\".");
		System.out.println("\t[-cycles <amount>]\tThis determines how many cycles should be run. Do not use with \"-end\".");
		System.out.println("\t[-flush <interval>]\tSets how many cycles should be run before the output gets written to a file.");
		System.out.println("\t[-nogui]\tStarts the application in headless mode.");
		System.out.println("\t[-timeout]\tSets a maximum time for a single simulation to run.");
	}
	
	private void onCycleResult(Personality[] personalities, Tellability tellability) {
		StringBuilder csv = new StringBuilder();
		Formatter f = new Formatter(csv);
		f.format(Locale.ENGLISH, "%f,%d,", tellability.functionalPolyvalence, tellability.numFunctionalUnits);
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			f.format("%d,", tellability.functionalUnitCount.get(unit));
		}
		f.format("%d,", tellability.numPolyvalentVertices);
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
			bestPersonalities = lastPersonalities;
		}
		
		// Stop cycle if there are no other personality combinations
		if(!personalityIterator.hasNext() || startCycle >= endCycle) {
			return new ReflectResult(null, null, false);
		}
		
		// Start the next cycle
		lastPersonalities = personalityIterator.next();
		log("Cycle " + startCycle);
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			setupFileLogger();
		}
		startCycle++;
		return new ReflectResult(lastRunner, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
	}
	
	@Override
	protected void finish() {
		// Print results
		log("Best tellability: " + bestTellability);
		log("Personalities:");
		for(Personality p : bestPersonalities) {
			log("\t" + p.toString());
		}
		
		// Flush and close file
		csvOut.flush();
		csvOut.close();
		
		// Close all log handlers
		Handler[] hs = Logger.getLogger("").getHandlers(); 
        for (int i = 0; i < hs.length; i++) {
        	hs[i].close();
        }
		
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
		lastRunner = new RedHenLauncher();
		lastRunner.setShowGui(false);
		ReflectResult rr = new ReflectResult(lastRunner, new Personality[] {lastPersonalities[0], lastPersonalities[1], lastPersonalities[1], lastPersonalities[1]});
		log("Cycle " + startCycle);
		// Create a new file logger if the log file name depends on the cycle number.
		if(logFile.contains("%d")) {
			setupFileLogger();
		}
		startCycle++;
		return rr;
	}

}
