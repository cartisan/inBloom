package inBloom.test.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

import inBloom.helper.SymmetryAnalyzer;

public class SymmetryAnalyzerTest {
	protected static Logger logger = Logger.getLogger(SymmetryAnalyzerTest.class.getName());

	// [0,0,0,0], [0,1,0,1], [0,1,2,0]
	private ArrayList<List<String>> ascendingSequences = new ArrayList<>();

	// [A,B,A,B], [A,A,B,B], [A,A,A,B]
	private ArrayList<List<String>> blockSequences = new ArrayList<>();

    @SuppressWarnings("unused")
	private void fillAscendingSequences(int maxLength) {
    	List<String> sequence = new ArrayList<>();

        // create maxLength sequences
        for(int i = 1; i <= maxLength; i++) {
        	sequence.clear();

            while(true) {
            	// fill the sequences with increasing number "pairs/sequences":
            	// first entry = 0,0,0,0
            	// second  = 0,1,0,1
                for(int j = 0; j < i; j++) {
                	sequence.add(Integer.toString(j));
                }

                // termination condition
                if(sequence.size() >= maxLength){
                    // add the sequences
                	List<String> tmp = new ArrayList<>(sequence.subList(0, maxLength));
          			this.ascendingSequences.add(tmp);
                    break;
                }
            }
        }
    }

    private void fillBlockSequnces(int maxLength) {
    	List<String> sequence = new ArrayList<>();

        for (int i = 1; i <= maxLength; i++) {
        	sequence.clear();

        	while(true) {
        		for(int a = 0; a < i; a ++) {
            		sequence.add("A");
            	}
          		for(int b = 0; b < i; b ++) {
          			sequence.add("B");
            	}

          		if(sequence.size() >= maxLength) {
          			List<String> tmp = new ArrayList<>(sequence.subList(0, maxLength));
          			this.blockSequences.add(tmp);
          			break;
            	}
        	}
        }
    }

	@Test
	public void testComputeTranslational() {
		List<String> sequence = new ArrayList<>();

		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeTranslational(sequence));

		// unary sequences are not valid chains
		sequence.add("A");
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeTranslational(sequence));

		// neither are chains of length 1 allowed
		sequence.add("A");
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeTranslational(sequence));

		// the AAA case has no translational symmetry, because all dual chains overlap
		// it should have translational symmetry, though
		sequence.add("A");
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeTranslational(sequence));

		// AAAA finally has a chain of AA, with length two and two reps it covers 4 elements of the sequence
		sequence.add("A");
		Map<List<String>, Integer> res = SymmetryAnalyzer.computeTranslational(sequence);
		List<String> key = Arrays.asList("A", "A");
		assertTrue(res.containsKey(key));
		assertEquals(4, (int)res.get(key));

		// AABAA -> we also find chains that are interrupted
		sequence.add(2, "B");
		res = SymmetryAnalyzer.computeTranslational(sequence);
		key = Arrays.asList("A", "A");
		assertTrue(res.containsKey(key));
		assertEquals(4, (int)res.get(key));
		assertFalse(res.containsKey(Arrays.asList("B")));

		// AABBBBAA -> we also find chains that are interrupted
		sequence.add(2, "B");
		sequence.add(2, "B");
		sequence.add(2, "B");
		res = SymmetryAnalyzer.computeTranslational(sequence);
		key = Arrays.asList("A", "A");
		assertTrue(res.containsKey(key));
		assertEquals(4, (int)res.get(key));

		key = Arrays.asList("B", "B");
		assertTrue(res.containsKey(key));
		assertEquals(4, (int)res.get(key));
	}

	@Test
	public void testComputeReflectional() {
		List<String> sequence = new ArrayList<>();
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeReflectional(sequence));

		// unary sequences are not valid chains
		sequence.add("A");
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeReflectional(sequence));

		// AA is not reflective
		sequence.add("A");
		Map<List<String>, Integer> res = SymmetryAnalyzer.computeReflectional(sequence);
		assertEquals(new HashMap<List<String>, Integer>(), SymmetryAnalyzer.computeReflectional(sequence));

		// AAA is reflective, too!
		sequence.add("A");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		List<String> key = Arrays.asList("A", "A");
		assertTrue(res.containsKey(key));
		assertEquals(3, (int)res.get(key));

		// ABA is reflective, too!
		sequence.remove(1);
		sequence.add(1, "B");
		key = Arrays.asList("A", "B");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		assertTrue(res.containsKey(key));
		assertEquals(3, (int)res.get(key));

		// ABAZ, adding unrelated stuff doesn't hurt
		sequence.add("Z");
		key = Arrays.asList("A", "B");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		assertTrue(res.containsKey(key));
		assertEquals(3, (int)res.get(key));

		// ABAZZZ, has two sequences
		sequence.add("Z");
		sequence.add("Z");
		key = Arrays.asList("A", "B");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		assertTrue(res.containsKey(key));
		assertEquals(3, (int)res.get(key));

		key = Arrays.asList("Z", "Z");
		assertTrue(res.containsKey(key));
		assertEquals(3, (int)res.get(key));

		// ABABBA, sequences can also be interspersed
		sequence = Arrays.asList("A", "B", "A", "B", "B", "A");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		key = Arrays.asList("A", "B");
		assertTrue(res.containsKey(key));
		assertEquals(6, (int)res.get(key));

		// ABABBAAB, second BA also finds AB in the end
		sequence = Arrays.asList("A", "B", "A", "B", "B", "A","A", "B");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		key = Arrays.asList("B", "A");
		assertTrue(res.containsKey(key));
		assertEquals(7, (int)res.get(key));

		// ABBA does not have ABB chain, since that would be overlap of 2
		sequence = Arrays.asList("A", "B", "B","A");
		res = SymmetryAnalyzer.computeReflectional(sequence);
		key = Arrays.asList("A", "B", "B");
		assertFalse(res.containsKey(key));
	}

	@Test
	public void testComputeSymmetry() {
		this.fillBlockSequnces(6);
        List<Float> blockSymValues = new ArrayList<>();

        for (int i = 0; i < this.blockSequences.size(); i++) {
        	blockSymValues.add(SymmetryAnalyzer.computeSymmetry(this.blockSequences.get(i)));
        }

        // [A, A, B, B, A, A] > [A, A, A, A, B, B]
        assertTrue(blockSymValues.get(1) > blockSymValues.get(3));

        // highest symmetry is [A, A, A, A, A, A] (at pos 5)
        float maxV = (float) blockSymValues.stream().mapToDouble(f -> f).max().getAsDouble();
        int maxIndex = blockSymValues.indexOf(maxV);
        assertEquals(5, maxIndex);

        // lowest symmetry is [A, A, A, B, B, B] (at pos 2)
        float minV = (float) blockSymValues.stream().mapToDouble(f -> f).min().getAsDouble();
        int minIndex = blockSymValues.indexOf(minV);
        assertEquals(2, minIndex);

		assertEquals(0.9166667, blockSymValues.get(0), 0.001);		// Sequence: [A, B, A, B, A, B]
		assertEquals(0.8333333, blockSymValues.get(1), 0.001);		// Sequence: [A, A, B, B, A, A]
		assertEquals(0.25, blockSymValues.get(2), 0.001);			// Sequence: [A, A, A, B, B, B]
		assertEquals(0.6666667, blockSymValues.get(3), 0.001);		// Sequence: [A, A, A, A, B, B]
		assertEquals(0.75, blockSymValues.get(4), 0.001);			// Sequence: [A, A, A, A, A, B]
		assertEquals(1.0, blockSymValues.get(5), 0.001);			// Sequence: [A, A, A, A, A, A]
	}


	@Test
	public void testComputeParallelism() {
		List<String> l1 = Arrays.asList("A", "B", "C", "E");
		List<String> l2 = Arrays.asList();
		assertEquals(0f, SymmetryAnalyzer.computeParallelism(l1, l2), 0.001);
		assertEquals(1f, SymmetryAnalyzer.computeParallelism(l1, l1), 0.001);

		l2 = Arrays.asList("B");
		assertEquals(1f, SymmetryAnalyzer.computeParallelism(l1, l2), 0.001);	// we divide overlap length by the length of the shorter of the two sequences
		assertEquals(1f, SymmetryAnalyzer.computeParallelism(l2, l1), 0.001);

		l2 = Arrays.asList("B", "C");
		assertEquals(1f, SymmetryAnalyzer.computeParallelism(l1, l2), 0.001);	// we divide overlap length by the length of the shorter of the two sequences

		l2 = Arrays.asList("B", "C", "D");
		assertEquals(2/3f, SymmetryAnalyzer.computeParallelism(l1, l2), 0.001);

		l2 = Arrays.asList("B", "C", "D", "E");
		assertEquals(1/2f, SymmetryAnalyzer.computeParallelism(l1, l2), 0.001);	// no interspersing allowed
	}
}
