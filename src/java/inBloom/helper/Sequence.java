package inBloom.helper;

import java.util.List;

public class Sequence 
{
	public List<String> sequence;
	public int startIndex;
	public int counter;


	public Sequence(List<String> sequence, int start, int counter)
	{
		this.sequence = sequence;
		this.startIndex = start;
		this.counter = counter;
	}

	public void increaseCounter()
	{
		this.counter++;
	}
	
	public List<String> getSequence()
	{
		return this.sequence;
	}
}

