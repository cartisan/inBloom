package inBloom.test.helper;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import inBloom.helper.PlotpatternAnalyzer;
import junit.framework.TestCase;

public class PlotPatternAnalyzerTest extends TestCase {
	
	public void testCountAllPatterns() {
		// finds and counts repeating of a simple pattern
		List<String> events = ImmutableList.of("a", "a", "a");
		HashMap<String, Integer> patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		assertEquals(3, (int) patternCounts.get("a"));
		
		// finds and counts repeating of several simple patterns
		events = ImmutableList.of("a", "a", "a", "b", "b", "cc", "cc");
		patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		assertEquals(3, (int) patternCounts.get("a"));		
		assertEquals(2, (int) patternCounts.get("b"));		
		assertEquals(2, (int) patternCounts.get("cc"));		
		
		// finds and counts repeating of complex pattern
		events = ImmutableList.of("a", "b", "cc", "b", "cc", "b", "cc");
		patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		assertEquals(3, (int) patternCounts.get("b cc"));		
		
		// finds and counts repeating of multiple complex patterns
		events = ImmutableList.of("a", "b", "cc", "b", "cc", "b", "cc", "a", "a");
		patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		assertEquals(3, (int) patternCounts.get("b cc"));		
		assertEquals(2, (int) patternCounts.get("a"));		
		
		// repeating doesn't count subwords
		events = ImmutableList.of("!a", "a");
		patternCounts = PlotpatternAnalyzer.countAllPatterns(events);
		assertFalse(patternCounts.containsKey("a"));		
	}

}
