package inBloom.helper;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;

import jason.util.Pair;


public class TestSymmetry
{
	// [0,0,0,0], [0,1,0,1], [0,1,2,0]
	private static ArrayList<List<String>> ascendingSequences = new ArrayList<>();

	// [A,B,A,B], [A,A,B,B], [A,A,A,B]
	private static ArrayList<List<String>> blockSequences = new ArrayList<>();


    private static void fillAscendingSequences(int maxLength)
    {
    	List<String> sequence = new ArrayList<>();

        // create maxLength sequences
        for(int i = 1; i <= maxLength; i++)
        {
        	sequence.clear();

            while(true)
            {
            	// fill the sequences with increasing number "pairs/sequences":
            	// first entry = 0,0,0,0
            	// second  = 0,1,0,1
                for(int j = 0; j < i; j++)
                {
                	sequence.add(Integer.toString(j));
                }

                // termination condition
                if(sequence.size() >= maxLength)
                {
                    // add the sequences
                	List<String> tmp = new ArrayList<>(sequence.subList(0, maxLength));
          			ascendingSequences.add(tmp);
                    break;
                }
            }
        }
    }


    private static void fillBlockSequnces(int maxLength)
    {
    	List<String> sequence = new ArrayList<>();

        for (int i = 1; i <= maxLength; i++) // i equals the repetition of char j
        {
        	sequence.clear();

        	while(true)
        	{
        		for(int a = 0; a < i; a ++)
            	{
            		sequence.add("A");
            	}
          		for(int b = 0; b < i; b ++)
            	{
          			sequence.add("B");
            	}

          		if(sequence.size() >= maxLength)
          		{
          			List<String> tmp = new ArrayList<>(sequence.subList(0, maxLength));
          			blockSequences.add(tmp);
          			break;
            	}
        	}
        }
    }


    /*
     * Calculates the normal, direction and negative distances based on the formulas described
     * below
     * @return int array containing the three symmetry values
     */
    private static Pair<List<String>, Integer> sequenceAnalyser(List<String> sequence) {
		// maps sequences to the list of starting positions
		Map<List<String>, ArrayList<Integer>> sequenceStartposMap = new HashMap<>();

		// loop over the sequence and find all subsequences and their starting position
		for (int start = 0; start < sequence.size(); start++) {
			for (int end = sequence.size(); end > start; end--) {
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

		// maps sequences to the list of starting positions, ignoring overlapping sequences
		Map<List<String>, ArrayList<Integer>> noOverlapSequenceMap = new HashMap<>();
		for (Map.Entry<List<String>, ArrayList<Integer>> entry : sequenceStartposMap.entrySet()) {
			// unpack entry
			List<String> subsequence = entry.getKey();
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

		// compute symmetry value of each subsequence, where symmetry value is: sequence length * number of occurrence
		// filter out sequences that occur only once, since there is no symmetry in there
		List<Pair<List<String>, Integer>> sequenceSymmetryPairs = noOverlapSequenceMap.entrySet().stream()
																	   .filter(entry -> entry.getValue().size() > 1)
									   								   .map(entry -> new Pair<>(entry.getKey(), entry.getValue().size() * entry.getKey().size()))
									   								   .collect(Collectors.toList());

		// return the sequence and symmetry value with the highest symmetry value
		return sequenceSymmetryPairs.stream()
									.max( new Comparator<Pair<List<String>, Integer>>() {
										public int compare(Pair<List<String>, Integer> pair1, Pair<List<String>, Integer> pair2) {
											int symValueComparison = pair1.getSecond().compareTo(pair2.getSecond());
											if(symValueComparison != 0) {
												return symValueComparison;
											}
											// choose between subsequences with same symmetry value by choosing the one with the higher sequence length
											return ((Integer) pair1.getFirst().size()).compareTo(pair2.getFirst().size());
										}
									})
									.orElseGet(new Supplier<Pair<List<String>,Integer>>() {
										public Pair<List<String>, Integer> get() {
											return new Pair<>(new ArrayList<>(), 0);
										}
									});
    }


    public static void saveResults(int[][] ascendingSymValues, int[][] blockSymValues)
	{
        FileWriter writer;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();

        File file = new File("C:/Users/Leon/Desktop/results" + dtf.format(now) + ".csv");

        try
        {
	        // creates the file
        	file.createNewFile();
        	// set to true to avoid overwriting existing results
        	writer = new FileWriter(file, true);



        	for(int i = 0; i < ascendingSequences.size(); i++)
        	{
        		List<String> currentSequence = new ArrayList<>(ascendingSequences.get(i));

        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
        		writer.append("," + ascendingSymValues[i][0] + "," + ascendingSymValues[i][1] + "," + ascendingSymValues[i][2]);
        		writer.append("\n"); // new line after each sequence
        	}

    		writer.append("\n"); // new line after each sequence

        	for(int i = 0; i < blockSequences.size(); i++)
        	{
        		List<String> currentSequence = new ArrayList<> (blockSequences.get(i));

        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
        		writer.append("," + blockSymValues[i][0] + "," + blockSymValues[i][1] + "," + blockSymValues[i][2]);
        		writer.append("\n"); // new line after each sequence
        	}


	        writer.flush();
	        writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

	public static void main(String[] args)
	{

		// create the test sequences: ascendingSequences are sequences containing pairs of numbers
		// blockSequences consist of the letter A and B with varying repetition
        fillAscendingSequences(6);
        fillBlockSequnces(6);

        // analyse the test sequences with the sequenceAnalyser from here (uses all three formulas)
        List<Pair<List<String>, Integer>> ascendingSymValues = new ArrayList<>();
        List<Pair<List<String>, Integer>>  blockSymValues = new ArrayList<>();

        for (int i = 0; i < ascendingSequences.size(); i++)
        {
        	ascendingSymValues.add(sequenceAnalyser(ascendingSequences.get(i)));

        }

        for (int i = 0; i < blockSequences.size(); i++)
        {
        	blockSymValues.add(sequenceAnalyser(blockSequences.get(i)));
        }

        // save the results in a csv file
        saveResults(null, null);
	}
}
