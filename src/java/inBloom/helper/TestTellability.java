package inBloom.helper;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class TestTellability 
{

	protected static Logger logger = Logger.getLogger(TestTellability.class.getName());

	private static ArrayList<List<String>> SingleSequences = new ArrayList<List<String>>();
	private static ArrayList<List<String>> BlockSequences = new ArrayList<List<String>>();
    

    public TestTellability() { };
	

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
//                        SaveResults( SingleSequences.get(SingleSequences.size()-1).toString(), 
//                      		  sequenceAnalyser( SingleSequences.get(SingleSequences.size()-1)));
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
//                      SaveResults( BlockSequences.get(BlockSequences.size()-1).toString(), 
//                    		  sequenceAnalyser( BlockSequences.get(BlockSequences.size()-1)));
//                    
          			break;
            	}
        	}
        }
    }
    
    
    private static int[] sequenceAnalyser(List<String> graphSequence)
	{		
		// saves a sequences as key with corresponding values [counter, List of Start Indices]
		Map<List<String>, List<Integer>> sequenceMap = new HashMap<List<String>, List<Integer>>();

		// loop over the graph and create (sub)sequences
		for (int start = 0; start < graphSequence.size(); start++)
		{
			for (int end = graphSequence.size(); end > start + 1; end--)
			{
				List<String> currentSeq = graphSequence.subList(start, end);
				
				// if sequences already in list, increase the counter
				if (sequenceMap.containsKey(currentSeq))
				{
					List<Integer> newSeq = sequenceMap.get(currentSeq);
					newSeq.add(start);
					
					sequenceMap.put(currentSeq, newSeq);
				}
				else
				{					
					List<Integer> newSeq = new ArrayList<Integer>();
					newSeq.add(start);
					
					sequenceMap.put(currentSeq, newSeq);
				}
			}
		}

		// sum over all sequences
		int mNeg = 0;
		int mElena = 0;
		int mNormal = 0;
		
		for (Map.Entry<List<String>, List<Integer>> entry : sequenceMap.entrySet()) 
		{
			// if a sequence appears more than once, weight them by their 
			// number of appearance and save the values in a new list
			if (entry.getValue().size() > 1)
			{
				// sum of each sequence
				int sumDNeg = 0;
				int sumElena = 0;
				int sumDNormal = 0;

				// von vorne nach hinten bei jedem startwert anfangen...
				for( int firstIndex = 0; firstIndex < entry.getValue().size(); firstIndex ++ ) 
				{
					// ... ihn mit jedem folgendem zu vergleichen / einzubringen
					for (int secondIndex = firstIndex+1; secondIndex < entry.getValue().size(); secondIndex ++ ) 
					{
						// sum it 
						sumDNormal += entry.getValue().get(secondIndex) - entry.getValue().get(firstIndex);
						sumDNeg += entry.getValue().get(secondIndex) - (entry.getValue().get(firstIndex) + entry.getKey().size());
						sumElena += entry.getValue().get(secondIndex) - (entry.getValue().get(firstIndex) + entry.getKey().size()) >= 0 ? 1 : -1; 
						
					}
				}
				
					
				mNeg += sumDNeg * entry.getKey().size();
				mElena += sumElena * entry.getKey().size();
				mNormal += sumDNormal * entry.getKey().size();
				//logger.info("Map" + entry.toString());
//				multiplications.add((double)entry.getKey().size() * entry.getValue().size());
			}
		}
		
		//logger.info("Negative Ds: " + mNeg + ". Elenas Ds: " + mElena + ". Normal Ds: " + mNormal);
		
		
		
		// return the maximum of these weighted sequences as an indicator of the symmetry of the character's state
		return new int[] { mNeg, mElena, mNormal };
	}	
    
    
    public static void SaveResults()
	{
        FileWriter writer;
        File file = new File("Results.csv");

        try 
        {
	        // creates the file
        	file.createNewFile();
        	// set to true to avoid overwriting existing results
        	writer = new FileWriter(file, false);
        	
        	
        	List<String> currentSequence = new ArrayList<>();
        	
        	for(int i = 0; i < SingleSequences.size(); i++)
        	{
        		currentSequence = SingleSequences.get(i);
        		
        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
        		writer.append("\n"); // new line after each sequence
        	}
        	
        	for(int i = 0; i < BlockSequences.size(); i++)
        	{
        		currentSequence = BlockSequences.get(i);
        		
        		for (int j = 0; j < currentSequence.size(); j++)
        		{
            		writer.append("\t" + currentSequence.get(j));
        		}
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

		fillSingleSequences(5);
		
		logger.info("\n\n---------------------------------------------------------------\n");
		
        fillBlockSequnces(5);
        
        SaveResults();

	}
	
	/*
		// add dummy character with dummy emotions for testing (Length = 18)
		List<String> emos3 = new ArrayList<String>();
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		emos3.add("A");
		
		List<String> emos2 = new ArrayList<String>();
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");
		
		emos2.add("B");
		emos2.add("A");


		
		List<String> emos = new ArrayList<String>();
		emos.add("A");
		emos.add("B");
		emos.add("C");
		
		emos.add("A");
		emos.add("B");
		emos.add("C");
		
		emos.add("A");
		emos.add("B");
		emos.add("C");
		
		emos.add("A");
		emos.add("B");
		emos.add("C");

		emos.add("A");
		emos.add("B");
		emos.add("C");
		
		emos.add("A");
		emos.add("B");
		emos.add("C");
		
		List<String> emos1 = new ArrayList<String>();
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");
		emos1.add("A");

		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		emos1.add("B");
		
		List<String> emos6 = new ArrayList<String>();
		emos6.add("A");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");
		emos6.add("B");		
		emos6.add("B");		
		emos6.add("A");
		

		List<String> emos4 = new ArrayList<String>();
		emos4.add("A");
		emos4.add("B");
		emos4.add("C");
		emos4.add("D");
		emos4.add("E");
		emos4.add("F");
		emos4.add("G");
		emos4.add("H");
		emos4.add("I");
		emos4.add("J");
		emos4.add("K");
		emos4.add("L");
		emos4.add("M");
		emos4.add("N");
		emos4.add("O");
		emos4.add("P");
		emos4.add("Q");
		emos4.add("R");
		emos4.add("S");
		
		

		
		List<String> emos5 = new ArrayList<String>();
		emos5.add("A");
		emos5.add("A");
		emos5.add("A");
		emos5.add("B");
		emos5.add("B");
		emos5.add("B");

	 */

}
