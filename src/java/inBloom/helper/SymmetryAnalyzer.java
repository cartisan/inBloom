package inBloom.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;

import jason.util.Pair;

public class SymmetryAnalyzer {
    static Logger logger = Logger.getLogger(SymmetryAnalyzer.class.getName());

    /**
     * Computes the overall symmetry score for a sequence, represented by string elements in a list.
     * @param sequence a list of strings encoding the sequence to be analyzed
     * @return the combined, normalized symmetry score of sequence
     */
    public static Float compute(List<String> sequence) {
    	Map<List<String>, Integer> translational = computeTranslational(sequence);
    	Map<List<String>, Integer> reflectional = computeReflectional(sequence);

		logger.info("Translational symmetries: " + translational);
		logger.info("Reflectional symmetries: " + reflectional);

		Pair<List<String>, Integer> maxTrans = findBestIndividualChain(translational);
		Pair<List<String>, Integer> maxRefl = findBestIndividualChain(reflectional);

		logger.info("Best trans chain: " + maxTrans.getFirst() + " with normalized value: " + (float) maxTrans.getSecond() / sequence.size());
		logger.info("Best refl chain: " + maxRefl.getFirst() + " with normalized value: " + (float) maxRefl.getSecond() / sequence.size());

		Float normalizedScore = ((float) maxTrans.getSecond() + maxRefl.getSecond()) / (2 * sequence.size());
		logger.fine("Overall normalized score: " + normalizedScore);
		logger.fine("\n");

    	return normalizedScore;
    }

    /**
     * Calculates reflectional symmetry for sequence, where a subsequence (i.e. chain) is palindromic.
     * A chain has to be of at least two elements, and includes the whole palindrome instead of just the reflected part.
     * @param sequence a list of strings encoding the sequence to be analyzed
     * @return a map of chains and their respective reflective symmetry value
     */
	public static Map<List<String>, Integer> computeReflectional(List<String> sequence) {
    	logger.fine("Analyzing sequence for reflectional sym: " + sequence);

		// maps sequences to the list of starting positions
		Map<List<String>, ArrayList<Integer>> sequenceStartposMap = new HashMap<>();

		// loop over the sequence and find all subsequences and their starting position
		for (int start = 0; start < sequence.size(); start++) {
			for (int end = sequence.size(); end > start+1; end--) {
				List<String> subsequence = sequence.subList(start, end);
				if(subsequence.equals(reverse(subsequence))) {
					// if new sequence, create empty list of starting positions
					if (!sequenceStartposMap.containsKey(subsequence)) {
						sequenceStartposMap.put(subsequence, new ArrayList<Integer>());
					}

					sequenceStartposMap.get(subsequence).add(start);
				}
			}
    	}
		logger.fine("   chain-startposition map: " + sequenceStartposMap);

		Map<List<String>, ArrayList<Integer>> noOverlapSequenceMap = filterOverlappingChains(sequenceStartposMap);
		logger.fine("   without overlapping chains: " + noOverlapSequenceMap);


		Map<List<String>, Integer> sequenceSymmetryPairs = noOverlapSequenceMap.entrySet().stream()
															.collect(Collectors.toMap(entry -> entry.getKey(),
																	                  entry -> entry.getValue().size() * entry.getKey().size()));
		return sequenceSymmetryPairs;
    }

    /**
     * Calculates translational symmetry for sequence, where the same chain is repeated several times in the sequence.
     * A chain has to be of at least two elements, and repetitions only count when occurring more often than once.
     * @param sequence a list of strings encoding the sequence to be analyzed
     * @return a map of chains and their respective translational symmetry value
     */
	public static Map<List<String>, Integer> computeTranslational(List<String> sequence) {
    	logger.fine("Analyzing sequence for translational sym: " + sequence);

		// maps sequences to the list of starting positions
		Map<List<String>, ArrayList<Integer>> sequenceStartposMap = new HashMap<>();

		// loop over the sequence and find all subsequences and their starting position
		for (int start = 0; start < sequence.size(); start++) {
			for (int end = sequence.size(); end > start+1; end--) {
				List<String> currentSeq = new ArrayList<>(sequence.subList(start, end));

				// if new sequence, create empty list of starting positions
				if (!sequenceStartposMap.containsKey(currentSeq)) {
					sequenceStartposMap.put(currentSeq, new ArrayList<Integer>());
				}
				// add current starting position to map
				List<Integer> startingPositions = sequenceStartposMap.get(currentSeq);
				startingPositions.add(start);
			}
    	}
		logger.fine("   chain-startposition map: " + sequenceStartposMap);

		Map<List<String>, ArrayList<Integer>> noOverlapSequenceMap = filterOverlappingChains(sequenceStartposMap);
		logger.fine("   without overlapping chains: " + noOverlapSequenceMap);

		Map<List<String>, Integer> sequenceSymmetryPairs = noOverlapSequenceMap.entrySet().stream()
															.filter(entry -> entry.getValue().size() > 1)
															.collect(Collectors.toMap(entry -> entry.getKey(),
																	                  entry -> entry.getValue().size() * entry.getKey().size()));
		return sequenceSymmetryPairs;
    }

	private static Map<List<String>, ArrayList<Integer>> filterOverlappingChains(
			Map<List<String>, ArrayList<Integer>> sequenceStartposMap) {
		// maps sequences to the list of starting positions, ignoring overlapping sequences
		Map<List<String>, ArrayList<Integer>> noOverlapSequenceMap = new HashMap<>();
		for (Map.Entry<List<String>, ArrayList<Integer>> entry : sequenceStartposMap.entrySet()) {
			// unpack entry
			List<String> subsequence = entry.getKey();
			@SuppressWarnings("unchecked")
			ArrayList<Integer> startingPositions = (ArrayList<Integer>) entry.getValue().clone();

			// initialize noOverlapMap with empty list
			ArrayList<Integer> startingPositionsNoOverlap = new ArrayList<>();
			noOverlapSequenceMap.put(subsequence, startingPositionsNoOverlap);

			// add first starting position into the list that contains positions to be used to remove overlaps
			ArrayList<Integer> tmpLst = new ArrayList<>();
			tmpLst.add(startingPositions.remove(0));
			while(tmpLst.size() > 0) {
				int includePosition = tmpLst.remove(0);

				// search over all starting positions that have not been removed yet as overlaps
				Iterator<Integer> staPoIt = startingPositions.listIterator();
				while(staPoIt.hasNext()) {
					int p = staPoIt.next();
					// if starting position p is located before the end of the currently used position, remove p
					// otherwise, transfer p into list of positions to be used to remove overlaps and abort
					// that way, we find the first p that is not to be removed, and instantly switch to using it
					if(includePosition + subsequence.size() > p) {
						staPoIt.remove();
					} else {
						staPoIt.remove();
						tmpLst.add(p);
						break;
					}
				}
				// since we removed everything that overlapped with current position, we can safe it as overlap free
				startingPositionsNoOverlap.add(includePosition);
			}
		}
		return noOverlapSequenceMap;
	}

    private static List<String> reverse(List<String> list) {
    	List<String> reversed = new ArrayList<>();
    	for(String e: list) {
    		reversed.add(0, e);
    	}
    	return reversed;
    }

    /**
     * Combines results of two symmetry types by finding sequence that performs best according to both, where best is
     * defined as the sum of the two individual scores.
     * @param translational
     * @param reflectional
     * @return
     */
    @SuppressWarnings("unused")
	private static Pair<List<String>, Integer> findBestOverallChain(Map<List<String>, Integer> translational, Map<List<String>, Integer> reflectional) {
		Map<List<String>, Integer> overall = new HashMap<>(translational);
		reflectional.forEach((k, v) -> overall.merge(k, v, (i1,i2) -> i1 + i2));
		logger.info("Overall symmetries: " + overall);

		Pair<List<String>, Integer> max =  overall.entrySet().stream()
								.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
								.max(new SymmetryPairComparator())
								.orElseGet(new EmptySymmetryPairSupplier());

		return max;
    }

    /**
     * Finds sequence that performs best according to the values determined by one symmetry scoring.
     * @param chainScoreMap
     * @return
     */
    private static Pair<List<String>, Integer> findBestIndividualChain(Map<List<String>, Integer> chainScoreMap) {
		// return the (sequence, symmetry value) pair with the highest symmetry value
    	Pair<List<String>, Integer> max = chainScoreMap.entrySet().stream()
												.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
												.max(new SymmetryPairComparator())
												.orElseGet(new EmptySymmetryPairSupplier());

    	return max;
    }


    /**
     * Comparator for pairs of (chain,symmetry value), which returns the pair with the highest symmetry value and in case
     * of compares by chain length.
     * @author Leonid Berov
     */
    private static class SymmetryPairComparator implements Comparator<Pair<List<String>, Integer>> {

    	public int compare(Pair<List<String>, Integer> pair1, Pair<List<String>, Integer> pair2) {
			int symValueComparison = pair1.getSecond().compareTo(pair2.getSecond());
			if(symValueComparison != 0) {
				return symValueComparison;
			}
			// choose between subsequences with same symmetry value by choosing the one with the higher sequence length
			return ((Integer) pair1.getFirst().size()).compareTo(pair2.getFirst().size());
		}
    }



    /**
     * Supplier of (chain,symmetry value) pairs that represent the absence of a symmetric chain.
     * @author Leonid Berov
     */
    private static class EmptySymmetryPairSupplier implements Supplier<Pair<List<String>, Integer>> {

		public Pair<List<String>, Integer> get() {
			return new Pair<>(new ArrayList<>(), 0);
		}
    }
}
