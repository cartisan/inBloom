package plotmas.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jason.util.Pair;

public class MoodMapper {
	private HashMap<String, List<Pair<Long,Double>>> timedMoodMap = new HashMap<>();
	private LinkedList<Long> startTimes = new LinkedList<>();
	
	public void addMood(String agName, Long time, double pleasure) {
		if(timedMoodMap.containsKey(agName)) {
			List<Pair<Long, Double>> moodList = timedMoodMap.get(agName);
			moodList.add(new Pair<>(time, pleasure));	
		} else {
			startTimes.add(time);

			List<Pair<Long, Double>> moodList = new LinkedList<>();
			moodList.add(new Pair<>(time, pleasure));				
			timedMoodMap.put(agName, moodList);
		}
	}
	
	public Long latestStartTime(){
		return startTimes.stream().max((x1,x2) -> Long.compare(x1, x2)).get();
	}
	
	public Set<String> mappedAgents() {
		return this.timedMoodMap.keySet();
	}
	
	public Long latestMoodEntry(String agName) {
		List<Pair<Long, Double>> timeMoodList = timedMoodMap.get(agName);
		return timeMoodList.stream().mapToLong(pair -> pair.getFirst()).max().getAsLong();
	}
	
	public double sampleMood(String agName, Long time) {
		List<Pair<Long, Double>> timeMoodList = timedMoodMap.get(agName);
		
		// find the last (time, mood) pair before sampling time
		Pair<Long, Double> sample = timeMoodList.stream().filter(x -> x.getFirst() <= time) //get all pairs before sampling time
																			.max( (x1, x2) -> Long.compare(x1.getFirst(), x2.getFirst()) ) // get last entry before sampling time
																			.get();
		return sample.getSecond();
	}
}
