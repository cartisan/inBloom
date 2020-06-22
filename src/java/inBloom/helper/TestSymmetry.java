package inBloom.helper;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestSymmetry
{

	private static ArrayList<List<String>> SingleSequences = new ArrayList<>();
	private static ArrayList<List<String>> BlockSequences = new ArrayList<>();


    private static void fillSingleSequences(int maxLength)
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
          			SingleSequences.add(tmp);
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
          			BlockSequences.add(tmp);
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
    private static int[] sequenceAnalyser(List<String> testSequence)
    {
		// saves a sequence as key and its number of appearance as value
		Map<List<String>, List<Integer>> sequenceMap = new HashMap<>();

		// loop over the current test sequence and create its (sub)sequences
		for (int start = 0; start < testSequence.size(); start++)
		{
			for (int end = testSequence.size(); end > start + 1; end--)
			{
				List<String> currentSeq = new ArrayList<>(testSequence.subList(start, end));

				// if the sequence already in the list, only increase the counter
				if (sequenceMap.containsKey(currentSeq))
				{
					List<Integer> newSeq = sequenceMap.get(currentSeq);
					newSeq.add(start);

					sequenceMap.put(currentSeq, newSeq);
				}
				else
				{
					List<Integer> newSeq = new ArrayList<>();
					newSeq.add(start);

					sequenceMap.put(currentSeq, newSeq);
				}
			}
    	}

		/*
		* calculate symmetry values for all of the sequences
		* in the best case, punish overlapping sequences to avoid duplications
		* are two different entries but in theory the first contains the second
		* three possible formulas for calculation:
		* 1) Neg: currentSequenceStart - length of prevSequence
		* 2) Dir: similar to (1), but only adds 1 if they do NOT overlap; else -1
		* 3) Normal: currentSequenceStart - prevSequenceStart
		*/

		int weightedNeg = 0;
		int weightedDir = 0;
		int weightedNormal = 0;

		int sequenceValueNeg = 0;
		int sequenceValueDir = 0;
		int sequenceValueNormal = 0;

		for (Map.Entry<List<String>, List<Integer>> entry : sequenceMap.entrySet())
		{
			// if a sequence appears more than once, weight them by their
			// number of appearances and save the values in a new list
			if (entry.getValue().size() > 1)
			{
				// sum of each sequence
				sequenceValueNeg = 0;
				sequenceValueDir = 0;
				sequenceValueNormal = 0;

				// sum the distances between the start indices within a sequence
				for (int prevIdx = 0; prevIdx < entry.getValue().size(); prevIdx ++)
				{
					for (int currentIdx = prevIdx+1; currentIdx < entry.getValue().size(); currentIdx ++)
					{
						sequenceValueNeg += entry.getValue().get(currentIdx) - (entry.getValue().get(prevIdx) + entry.getKey().size());
						sequenceValueDir += entry.getValue().get(currentIdx) - (entry.getValue().get(prevIdx) + entry.getKey().size()) >= 0 ? 1 : -1;
						sequenceValueNormal += entry.getValue().get(currentIdx) - entry.getValue().get(prevIdx);
					}
				}

				weightedNeg += sequenceValueNeg * entry.getKey().size();
				weightedDir += sequenceValueDir * entry.getKey().size();
				weightedNormal += sequenceValueNormal * entry.getKey().size();
			}
		}

		// return the weighted sum of the sequences
    	return new int[] { weightedNeg, weightedDir, weightedNormal};
    }


    public static void SaveResults(int[][] singleValues, int[][] blockValues)
	{
        FileWriter writer;
        File file = new File("C:/Users/Leon/Desktop/Results.csv");

        try
        {
	        // creates the file
        	file.createNewFile();
        	// set to true to avoid overwriting existing results
        	writer = new FileWriter(file, true);



        	for(int i = 0; i < SingleSequences.size(); i++)
        	{
        		List<String> currentSequence = new ArrayList<>(SingleSequences.get(i));

        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
        		writer.append("," + singleValues[i][0] + "," + singleValues[i][1] + "," + singleValues[i][2]);
        		writer.append("\n"); // new line after each sequence
        	}

        	for(int i = 0; i < BlockSequences.size(); i++)
        	{
        		List<String> currentSequence = new ArrayList<> (BlockSequences.get(i));

        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
        		writer.append("," + blockValues[i][0] + "," + blockValues[i][1] + "," + blockValues[i][2]);
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

		// create the test sequences: SingleSequences are sequences containing pairs of numbers
		// BlockSequences consist of the letter A and B with varying repetition
        fillSingleSequences(5);
		System.out.println("\n\n---------------------------------------------------------------\n");
        fillBlockSequnces(5);

        // analyse the test sequences with the sequenceAnalyser from here (uses all three formulas)
        int[][] singleValues = new int[SingleSequences.size()][3];
        int[][] blockValues = new int[BlockSequences.size()][3];

        for (int i = 0; i < SingleSequences.size(); i++)
        {
        	singleValues[i] = sequenceAnalyser(SingleSequences.get(i));

        }

        for (int i = 0; i < BlockSequences.size(); i++)
        {
        	blockValues[i] = sequenceAnalyser(BlockSequences.get(i));
        }

        // save the results in a csv file
        SaveResults(singleValues, blockValues);
	}
}
