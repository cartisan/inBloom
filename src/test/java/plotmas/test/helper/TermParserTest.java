package plotmas.test.helper;

import java.util.LinkedList;
import java.util.List;

import jason.util.Pair;
import junit.framework.TestCase;
import plotmas.helper.TermParser;

public class TermParserTest extends TestCase {
	
	public void testExtractPersonalityAnnotation() {
		String annotation = "affect(personality(extraversion,positive)";
		List<?> results = TermParser.extractPersonalityAnnotation(annotation);
		List<Pair<String, String>> expected = new LinkedList<>();
		expected.add(new Pair<String, String>("extraversion","positive"));
		
		assertEquals(expected, results);
		
		annotation = "affect(or(and(personality(extraversion,positive),not (mood(dominance,low))),personality(openness,low)),source(self)";
		results = TermParser.extractPersonalityAnnotation(annotation);
		expected = new LinkedList<>();
		expected.add(new Pair<String, String>("extraversion","positive"));
		expected.add(new Pair<String, String>("openness","low"));
		
		assertEquals(expected, results);
	}

}
