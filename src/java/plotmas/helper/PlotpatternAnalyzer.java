package plotmas.helper;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jason.util.Pair;
import plotmas.graph.Vertex;

public class PlotpatternAnalyzer {
    static Logger logger = Logger.getLogger(PlotpatternAnalyzer.class.getName());
    
	/** regex that matches when the same sequence of events (separated by spaces) repeats several times, test using https://regex101.com/ */
	public static final String EVENT_SEP = " "; 	// should always be a white-space
	public static final String REPETITION_REGEX = "(?<pattern>(?<lastWord>" + Pattern.quote(EVENT_SEP) + "\\S+)+?)(?:\\k<pattern>)+";
	public static final Pattern REPETITION_PATTERN = Pattern.compile(REPETITION_REGEX);
	
	/**
	 * For a list of events this method finds all patterns of subsequently repeating events and counts their number of 
	 * occurrence. Stores the results in a HashMap mapping patterns to occurrence numbers, e.g:<br>
	 * <code>[a, b, c, a, a, z, b, c, b, c, b, c] -> {"a" : 2, "b c" : 3}</code> 
	 * @param plot
	 * @return HashMap mapping patterns to number of occurrences
	 */
	static public HashMap<String, Integer> countAllPatterns(List<String> events) {
		String plot = EVENT_SEP + events.stream().collect(Collectors.joining(EVENT_SEP));
		Pattern repetition_pat = Pattern.compile(REPETITION_REGEX);
		
		return countPatterns(plot, repetition_pat);
	}
	
	/**
	 * For a list of events this method finds all patterns of subsequently repeating events that finish with the last 
	 * event, and counts their number of occurrence. Stores the results in a HashMap mapping patterns to occurrence
	 * numbers, e.g:<br>
	 * <code>[a, b, c, a, a, z, b, c, b, c, b, c] -> {"b c" : 3}</code> 
	 * @param plot
	 * @return HashMap mapping patterns to number of occurrences
	 */
	static public HashMap<String, Integer> countTrailingPatterns(List<String> events) {
		String plot = EVENT_SEP + events.stream().collect(Collectors.joining(EVENT_SEP));
		Pattern repetition_pat = Pattern.compile(REPETITION_REGEX + "$");
		
		return countPatterns(plot, repetition_pat);
	}
	
	/**
	 * For a string that contains events seprated by {@linkplain EVENT_SEP} this method finds all patterns and counts
	 * their number of occurrence. Stores the results in a HashMap mapping patterns to occurrence numbers.
	 * @param plot
	 * @return HashMap mapping patterns to number of occurrences
	 */
	static public HashMap<String, Integer> countPatterns(String plot, Pattern pattern) {
		Matcher matcher = pattern.matcher(plot);
		
		HashMap<String, Integer> patternCountsMap = new HashMap<>();
		while (matcher.find()) {
			int repeats = (matcher.end() - matcher.start()) / matcher.group("pattern").length();
			patternCountsMap.put(matcher.group("pattern").trim(), repeats);
		}
		
		return patternCountsMap;
	}

	
	/**
	 * Turns a list of events into a representation that can be used to analyzed recurring patterns.
	 * @param events List of events
	 * @return
	 */
	public static String toStringRepresentation(List<Vertex> events) {
		return events.stream().map(x -> x.getWithoutAnnotation()).collect(Collectors.joining(EVENT_SEP)) + EVENT_SEP;
	}

	public static Pair<Integer, Integer> patternLocation(List<String> events, Pattern pattern) {
		String plot = events.stream().collect(Collectors.joining(EVENT_SEP)) + EVENT_SEP;
		Matcher matcher = pattern.matcher(plot);
		
		matcher.find();
		return new Pair<Integer, Integer>(matcher.start(), matcher.end());
	}
	
	

}
