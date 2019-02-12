package plotmas.ERcycle;

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
import plotmas.PlotLauncher;
import plotmas.graph.CounterfactualityLauncher;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.helper.PlotFormatter;
import plotmas.helper.Tellability;

/**
 * Abstract version of a cycle that searches the personality space.
 * Provides helpful program arguments as well as input and output functionality.
 * @author Sven Wilke
 */
public abstract class PersonalitySpaceSearchCycle extends PlotCycle {
	
	protected static Logger logger = Logger.getLogger(CounterfactualityLauncher.class.getName());
	/**
	 * The name of the file the results will be written to.
	 */
	protected static String outFile = "results.csv";
	/**
	 * The name of the file personalities should be written from.
	 * If no name is specified, this class creates a personality
	 * space by itself.
	 */
	protected static String inFile = "";
	/**
	 * The name of the file the MAS console log should be written to.
	 * Can include "%d" (without quotes) to save to a different file
	 * depending on the cycle number.
	 */
	protected static String logFile = "";
	
	/**
	 * The index of the last cycle to run. Can be set via a program argument,
	 * or is calculated when cycleNum was set.
	 */
	protected static int endCycle = -1;
	
	/**
	 * The number of cycles to run. Can be set via a program argument.
	 * If -1, will run all cycles.
	 */
	protected static int cycleNum = -1;
	
	/**
	 * The number of cycles to run before saving the results to
	 * the output file. Note: results are always saved after
	 * the last cycle has been run.
	 */
	protected static int flushInterval = 30;
	
	/**
	 * Whether the JVM should close after it completed the last
	 * cycle.
	 */
	protected static boolean closeOnComplete = false;
	
	/**
	 * Whether the MAS GUI should be hidden during execution.
	 */
	protected static boolean hideGui = false;
	
	/**
	 * The best tellability score.
	 */
	protected double bestTellability = -1f;
	/**
	 * The personalities corresponding to the
	 * best tellability score.
	 */
	protected Personality[] bestPersonalities = null;
	
	/**
	 * The list of personalities for all cycles to run.
	 */
	protected List<Personality[]> personalityList;
	/**
	 * An iterator used to iterate over the personalities.
	 */
	protected Iterator<Personality[]> personalityIterator;
	
	/**
	 * The personalities of the last cycle.
	 */
	protected Personality[] lastPersonalities;
	/**
	 * The launcher of the last cycle.
	 */
	protected PlotLauncher<?,?> lastRunner;
	
	/**
	 * The writer for the output file.
	 */
	protected PrintWriter csvOut;
	
	/**
	 * Handles the i-th argument of the given program arguments.
	 * Returns a number x, such that i + x is the index of the next
	 * argument to handle. x is -1 if an error occurred (invalid number of arguments
	 * or unknown argument).
	 * @param args Arguments given to the program
	 * @param i Index of the argument to handle
	 * @return number of steps to the next argument, or -1 if an error occurred.
	 */
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
	
	/**
	 * Prints help stating valid arguments.
	 */
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
	/**
	 * Initializes a cycle with given agents. Has default values for the personality values of an agent.
	 * The default values are: -1.0, 0, 1.0
	 * Default value for hiding the GUI is false, consequently the GUI will be shown
	 * @param agentNames The names of the agents
	 * @param agentSrc The AgentSpeak source file for all agents
	 * @param personalityNum Defines how many unique personalities the cycle should iterate over.
	 */
	public PersonalitySpaceSearchCycle(String[] agentNames, String agentSrc, int personalityNum) {
		this(agentNames, agentSrc, personalityNum, new double[] { -1.0, 0, 1.0 }, false, false);
	}
	
	/**
	 * Initializes a cycle with given agents.
	 * @param agentNames The names of the agents
	 * @param agentSrc The AgentSpeak source file for all agents
	 * @param personalityNum Defines how many unique personalities the cycle should iterate over.
	 * @param personalityValues All possible values of the personality of one agent
	 * @param hideGui Indicates whether GUI should be hidden or not
	 */
	public PersonalitySpaceSearchCycle(String[] agentNames, String agentSrc, int personalityNum, double[] personalityValues, boolean hideGui, boolean counterfact) {
		super(agentNames, agentSrc, hideGui);
		
		if(!counterfact) {
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
				Personality[] personalitySpace = createPersonalitySpace(personalityValues);
				personalityList = createPlotSpace(personalitySpace, personalityNum, true);
			}
			
			personalityIterator = personalityList.iterator();
			
			if(endCycle == -1) {
				endCycle = personalityList.size();
			}
	
			// Log how many personalities there are.
			log("Running " + (endCycle - currentCycle) + " cycles...");
		} else if (counterfact) {
			Personality[] personalitySpace = createPersonalitySpace(personalityValues);
			//personalityNum instead of 2 does not work -> too much
			//TODO check out how we solve this
			personalityList = createPlotSpace(personalitySpace, personalityNum, true);
			personalityIterator = personalityList.iterator();
			
			if(endCycle == -1) {
				endCycle = personalityList.size();
			}
	
			// Log how many personalities there are.
			log("Running " + (endCycle - currentCycle) + " cycles...");
		}
	}

	/**
	 * From a list of possible personalities, generates a list of personality sets for a given
	 * number of characters.
	 * @param personalitySpace valid personalities an agent can have
	 * @param characters number of agents
	 * @param repeat whether the characters are symmetric (i.e. is <a, b> the same as <b, a>?)
	 * @return list of personality arrays stating different combinations of personalities for the agents.
	 */
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
	
	/**
	 * Creates all variations of a personality by generating permutations
	 * using the values given in the scale argument.
	 * @param scale The values each trait can assume
	 * @return array of possible personalities using the given values
	 */
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
	
	/**
	 * To be called by a subclass.
	 * Writes the results to the output file and flushes if needed.
	 * @param personalities personality that was used in the cycle
	 * @param tellability tellability analysis that was generated by the simulation
	 */
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
	
	/**
	 * Changes the logging to write to a file.
	 */
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
	
	public List<Personality[]> getPersonalityList() {
		return this.personalityList;
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
