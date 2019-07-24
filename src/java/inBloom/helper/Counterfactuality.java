package inBloom.helper;

import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

import jason.asSemantics.Mood;

@SuppressWarnings("all")
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

		if(this.counterTel < this.minTel ) {
			return counterValue;
		}

		// get pleasure of protagonist

		Map<Long, List<Mood>> originalMoodMap = this.originalMood.getMoodByAgent(this.counterAgent);
		Map<Long, List<Mood>> counterMoodMap = this.counterMood.getMoodByAgent(this.counterAgent);

		for(Long k : originalMoodMap.keySet()) {
			Mood orgMood = this.originalMood.sampleMood(this.counterAgent, k);
			if(counterMoodMap.containsKey(k)) {
				Mood conMood = this.counterMood.sampleMood(this.counterAgent, k);

				counterValue = counterValue + Math.abs(this.pleasureEval(orgMood) - this.pleasureEval(conMood));
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

	private double pleasureEval(Mood mood) {
		return mood.getP();
	}

	private double arousalEval(Mood mood) {
		return mood.getA();
	}

	private double dominanceEval(Mood mood) {
		return mood.getD();
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
