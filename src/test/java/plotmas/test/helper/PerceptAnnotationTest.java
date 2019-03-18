package plotmas.test.helper;

import junit.framework.TestCase;
import plotmas.helper.PerceptAnnotation;

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
		pa.addAnnotation("owner(gollum)");
		expectation = "[owner(gollum)]";
		assertEquals(expectation, pa.toString());
		
		pa.addAnnotation("location(cave)");
		expectation = "[owner(gollum),location(cave)]";
		assertEquals(expectation, pa.toString());
		
		
		pa = new PerceptAnnotation("joy", "love");
		pa.setCause("find_ring");
		pa.addAnnotation("owner(gollum)");
		pa.addAnnotation("location(cave)");
		assertTrue(pa.toString().contains("emotion(joy)"));
		assertTrue(pa.toString().contains("emotion(love)"));
		assertTrue(pa.toString().contains("cause(find_ring)"));
		assertTrue(pa.toString().contains("owner(gollum)"));
		assertTrue(pa.toString().contains("location(cave)"));
	}
	
	public void testFromEmotion() {
//		PerceptAnnotation pa = PerceptAnnotation.fromEmotion("joy");
	}
	
	public void testFromCause() {
		
	}

	public void testAddAnnotation() {
		
	}
	
	public void testAddTargetedEmotion() {
		
	}
	
}
