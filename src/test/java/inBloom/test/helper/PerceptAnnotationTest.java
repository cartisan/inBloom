package inBloom.test.helper;

import java.util.List;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;

import inBloom.helper.PerceptAnnotation;

import junit.framework.TestCase;

public class PerceptAnnotationTest extends TestCase {

	public void testToString() {
		PerceptAnnotation pa = new PerceptAnnotation("joy", "love");
		String expectation = "[emotion(joy),emotion(love)]";
		assertEquals(expectation, pa.toString());


		pa = new PerceptAnnotation();
		pa.setCause("find_ring");
		expectation = "[cause(find_ring)]";
		assertEquals(expectation, pa.toString());


		pa = new PerceptAnnotation();
		pa.addAnnotation("owner", "gollum");
		expectation = "[owner(gollum)]";
		assertEquals(expectation, pa.toString());

		pa.addAnnotation("location", "cave", "wall");
		expectation = "[owner(gollum),location(cave,wall)]";
		assertEquals(expectation, pa.toString());


		pa = new PerceptAnnotation("joy", "love");
		pa.setCause("find_ring");
		pa.addAnnotation("owner", "gollum");
		pa.addAnnotation("location", "cave", "wall");
		assertTrue(pa.toString().contains("emotion(joy)"));
		assertTrue(pa.toString().contains("emotion(love)"));
		assertTrue(pa.toString().contains("cause(find_ring)"));
		assertTrue(pa.toString().contains("owner(gollum)"));
		assertTrue(pa.toString().contains("location(cave,wall)"));
	}

	public void testToTerms() throws ParseException {
		PerceptAnnotation pa = new PerceptAnnotation("joy");
		pa.setCause("find_ring");

		List<Term> l = pa.toTerms();
		assertTrue(l.size() == 2);
		assertEquals("emotion(joy)", l.get(0).toString());					//test via string
		assertEquals(ASSyntax.parseTerm("cause(find_ring)"), l.get(1));		//test via term
		assertEquals(Literal.parseLiteral("emotion(joy)"), l.get(0));		//test via literal
	}
}
