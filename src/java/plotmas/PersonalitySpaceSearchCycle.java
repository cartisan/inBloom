package plotmas;

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
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.helper.PlotFormatter;
import plotmas.helper.Tellability;

public abstract class PersonalitySpaceSearchCycle extends PlotCycle {
	
	protected static String outFile = "results.csv";
	protected static String inFile = "";
	protected static String logFile = "";
	
	protected static int endCycle = -1;
	protected static int cycleNum = -1;
	
	protected static int flushInterval = 30;
	
	protected static boolean closeOnComplete = false;
	
	protected static boolean hideGui = false;
	
	protected double bestTellability = -1f;
	protected Personality[] bestPersonalities = null;
	
	protected List<Personality[]> personalityList;
	protected Iterator<Personality[]> personalityIterator;
	
	protected Personality[] lastPersonalities;
	protected PlotLauncher<?,?> lastRunner;
	
	protected PrintWriter csvOut;
	
	
	protected static int handleArgument(String[] args, int i) {
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
				currentCycle = Integer.parseInt(args[i + 1]);
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
	
	protected static void printHelp() {
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
	
	
	public PersonalitySpaceSearchCycle(String[] agentNames, String agentSrc, int personalityNum) {
		super(agentNames, agentSrc, !hideGui);

		// Open a file for writing results
		try {
			FileWriter fw = new FileWriter(outFile, currentCycle > 0);
		    BufferedWriter bw = new BufferedWriter(fw);
		    csvOut = new PrintWriter(bw);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Create the file handler here if the log file name is invariant of the name
		// (logs all contents to a single file)
		if(!logFile.contains("%d")) {
			setupFileLogger();
		}
		
		// Print CSV header
		if(currentCycle == 0) {
			String header = "tellability,functional_units_total,";
			for(FunctionalUnit unit : FunctionalUnits.ALL) {
				header += unit.getName().toLowerCase().replace(' ', '_') + "s,";
			}
			header += "polyvalent_vertices,";
			
			for (int i = 0; i < personalityNum; ++i) {
				header += String.format("o%d,c%d,e%d,a%d,n%d,", i, i, i, i, i);
			}
			// remove last comma
			header = header.substring(0, header.length() - 1);
			
			csvOut.println(header);
		}
		
		// read old results if continuing from infile
		boolean fileRead = false;
		
		if(!inFile.isEmpty()) {
			try(FileReader fr = new FileReader(inFile);
				BufferedReader br = new BufferedReader(fr);)
			{
				String line;
				personalityList = new LinkedList<Personality[]>();
				while((line = br.readLine()) != null) {
					String[] pd = line.split(",");
					Personality[] configuration = new Personality[personalityNum];
						for (int i = 0; i < personalityNum; ++i) {
							configuration[i] = new Personality(
										Double.parseDouble(pd[0 + i*5]),
										Double.parseDouble(pd[1 + i*5]),
										Double.parseDouble(pd[2 + i*5]),
										Double.parseDouble(pd[3 + i*5]),
										Double.parseDouble(pd[4 + i*5])
									);
						}							
					personalityList.add(configuration);
				}
				fileRead = true;
			} catch(IOException e0) {
				System.err.println("Could not read input file!");
			} catch(IndexOutOfBoundsException | NumberFormatException e1) {
				System.err.println("Input file did not have the correct format.");
			}
		}
		
		// if no infile present, or reading failed
		if(!fileRead) {
			// Generate all possible personalities.
			Personality[] personalitySpace = createPersonalitySpace(new double[] { -1.0, 0, 1.0 });
			personalityList = createPlotSpace(personalitySpace, personalityNum, true);
		}
		
		personalityIterator = personalityList.iterator();
		
		if(endCycle == -1) {
			endCycle = personalityList.size();
		}

		// Log how many personalities there are.
		log("Running " + (endCycle - currentCycle) + " cycles...");
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
	
	protected void onCycleResult(Personality[] personalities, Tellability tellability) {
		StringBuilder csv = new StringBuilder();
		Formatter f = new Formatter(csv);
		f.format(Locale.ENGLISH, "%f,%d,", tellability.compute(), tellability.numFunctionalUnits);
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
		if(currentCycle % flushInterval == 0) {
			csvOut.flush();
		}
	}
	
	@Override
	protected void finalize() {
		if(csvOut != null) {
			csvOut.close();
		}
	}
	
	protected void setupFileLogger() {
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
				h = new FileHandler(String.format(logFile, currentCycle));
			}
			h.setFormatter(new PlotFormatter());
		    Logger.getLogger("").addHandler(h);
		    Logger.getLogger("").setLevel(logFile.isEmpty() ? Level.OFF : Level.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void finish(EngageResult er) {
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
			endCycle = currentCycle + cycleNum;
		}
	}
}
