package inBloom.helper;

import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

import jason.asSemantics.Mood;

public class Counterfactuality {
	// first try: only the mood Data is of interest
	// this class is going to be more and more sophisticated
	
	private MoodMapper originalMood;
	private MoodMapper counterMood;
	private double counterTel;
	private double minTel = 0;
	private String counterAgent;
	
	
	public Counterfactuality(MoodMapper originalMood, MoodMapper counterMood, double counterTel, String counterAgent) {
		this.originalMood = originalMood;
		this.counterMood = counterMood;
		this.counterTel = counterTel;
		this.counterAgent = counterAgent;
	}
	
	public void setMinimumTellability(double min) {
		this.minTel = min;
	}
	public double compute() {
		double counterValue = 0;
		
		if(counterTel < this.minTel ) {
			return counterValue;
		}
		
		// get pleasure of protagonist
		
		Map<Long, List<Mood>> originalMoodMap = originalMood.getMoodByAgent(counterAgent);
		Map<Long, List<Mood>> counterMoodMap = counterMood.getMoodByAgent(counterAgent);
		
		for(Long k : originalMoodMap.keySet()) {
			List<Mood> orgMood = originalMoodMap.get(k);
			if(counterMoodMap.containsKey(k)) {
				List<Mood> conMood = counterMoodMap.get(k);
				
				counterValue = counterValue + Math.abs(pleasureEval(orgMood) - pleasureEval(conMood));
			}
			
		}
		
		/*if(originalMood.getSeriesCount() != counterMood.getSeriesCount()) {
			throw new IllegalArgumentException("@Counterfactuality: Original Mood and Counterfactual Mood do not have the same number of series.");
		} else {
			
			// nur mood von protagonist
			
			// iterating through all agents and getting their mood
			for(int i = 0; i < originalMood.getSeriesCount(); i++) {
				List<XYDataItem> orgMoodAgent = originalMood.getSeries(i).getItems();
				List<XYDataItem> conMoodAgent = counterMood.getSeries(i).getItems();
				double orgMoodAgentVal = averageMood(orgMoodAgent);
				double conMoodAgentVal = averageMood(conMoodAgent);
				counterValue = counterValue + Math.abs(orgMoodAgentVal - conMoodAgentVal);
			}
		}*/
		return counterValue;
	}
	
	private double pleasureEval(List<Mood> moods) {
		return moods.stream().map(m -> m.getP()).mapToDouble(l -> l).average().getAsDouble();
	}
	
	private double arousalEval(List<Mood> moods) {
		return moods.stream().map(m -> m.getA()).mapToDouble(l -> l).average().getAsDouble();
	}
	
	private double dominanceEval(List<Mood> moods) {
		return moods.stream().map(m -> m.getD()).mapToDouble(l -> l).average().getAsDouble();
	}
	
	// TODO where are the different moods stored??
	
	private double averageMood(List<XYDataItem> mood) {
		double sum = 0.0;
		int total = 0;
		for(XYDataItem singleMood : mood) {
			double moodVal = singleMood.getYValue();
			sum = sum + moodVal;
			total++;
		}
		return sum/total;
	}
	
}
