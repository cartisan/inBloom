package plotmas.test.helper;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import jason.util.Pair;
import junit.framework.TestCase;
import plotmas.helper.TermParser;

public class TermParserTest extends TestCase {
	
	public void testExtractPersonalityAnnotation() {
		String annotation = "affect(personality(extraversion,positive))";
		List<?> results = TermParser.extractPersonalityAnnotation(annotation);
		List<Pair<String, String>> expected = new LinkedList<>();
		expected.add(new Pair<String, String>("extraversion","positive"));
		
		assertEquals(expected, results);
		
		annotation = "affect(or(and(personality(extraversion,positive),not (mood(dominance,low))),personality(openness,low))),source(self)";
		results = TermParser.extractPersonalityAnnotation(annotation);
		expected = new LinkedList<>();
		expected.add(new Pair<String, String>("extraversion","positive"));
		expected.add(new Pair<String, String>("openness","low"));
		
		assertEquals(expected, results);
	}

	public void testSolutionsForPersonalityAnnotation() {
		String annotation = "affect(personality(extraversion,positive))";
		List<List<Pair<String,Double>>> results = TermParser.solutionsForPersonalityAnnotation(annotation);
		
		assertEquals(2, results.size());
		assertEquals("extraversion", results.get(0).get(0).getFirst());
		assertEquals(0.3, results.get(0).get(0).getSecond());
		assertEquals("extraversion", results.get(1).get(0).getFirst());
		assertEquals(1.0, results.get(1).get(0).getSecond());
		
		annotation = "affect(or(personality(extraversion,high),personality(openness,low))),source(self)";
		results = TermParser.solutionsForPersonalityAnnotation(annotation);
		assertEquals(7, results.size());
				
		annotation = "affect(or(personality(extraversion,positive),personality(openness,low))),source(self)";
		results = TermParser.solutionsForPersonalityAnnotation(annotation);
		assertEquals(10, results.size());
		
		// TODO: also not simplified
		annotation = "affect(or(and(personality(extraversion,positive),not (mood(dominance,low))),personality(openness,low))),source(self)";
		List<List<Pair<String,Double>>> results2 = TermParser.solutionsForPersonalityAnnotation(annotation);
		assertEquals(ImmutableSet.copyOf(results), ImmutableSet.copyOf(results2));
	}
}
