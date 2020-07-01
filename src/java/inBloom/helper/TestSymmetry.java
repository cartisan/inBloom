package inBloom.helper;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class TestSymmetry {
	protected static Logger logger = Logger.getLogger(TestSymmetry.class.getName());

	// [0,0,0,0], [0,1,0,1], [0,1,2,0]
	private static ArrayList<List<String>> ascendingSequences = new ArrayList<>();

	// [A,B,A,B], [A,A,B,B], [A,A,A,B]
	private static ArrayList<List<String>> blockSequences = new ArrayList<>();


    private static void fillAscendingSequences(int maxLength) {
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


    private static void fillBlockSequnces(int maxLength) {
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





    public static void saveResults(int[][] ascendingSymValues, int[][] blockSymValues) {
    	if (null == ascendingSymValues) {
    		return;
    	}

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

	public static void main(String[] args) {
		// create the test sequences: ascendingSequences are sequences containing pairs of numbers
		// blockSequences consist of the letter A and B with varying repetition
        fillAscendingSequences(6);
        fillBlockSequnces(6);

        // analyse the test sequences with the sequenceAnalyser from here (uses all three formulas)
        List<Float> ascendingSymValues = new ArrayList<>();
        List<Float> blockSymValues = new ArrayList<>();

        for (int i = 0; i < ascendingSequences.size(); i++) {
        	ascendingSymValues.add(SymmetryAnalyzer.compute(ascendingSequences.get(i)));

        }

        for (int i = 0; i < blockSequences.size(); i++) {
        	blockSymValues.add(SymmetryAnalyzer.compute(blockSequences.get(i)));
        }

        // save the results in a csv file
        saveResults(null, null);
	}
}
