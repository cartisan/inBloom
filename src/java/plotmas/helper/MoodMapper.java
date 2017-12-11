package plotmas.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jason.util.Pair;

/**
 * Helper class that manages the mapping between plot-time and agent's mood changes. Maintained by 
 * {@link plotmas.PlotAwareAg}, which updates the mapping. Used by {@link plotmas.graph.MoodGraph} when it creates the
 * graph that shows the development of character's pleasure over time.
 * @author Leonid Berov
 */
public class MoodMapper {
	private HashMap<String, List<Pair<Long,Double>>> timedMoodMap = new HashMap<>();
	private LinkedList<Long> startTimes = new LinkedList<>();
	
	/**
	 * Needs to be called each time the agent's pleasure value changes, in order to make this change accessible for
	 * later analysis, as e.g. by {@link plotmas.graph.MoodGraph}. 
	 * @param agName
	 * @param time
	 * @param pleasure
	 */
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
	
	/**
	 * Finds and returns the start time of the agent that started its reasoning cycle last, in order to avoid analysing
	 * the mood development before all agents are running.
	 * @return time in ms
	 */
	public Long latestStartTime(){
		return startTimes.stream().max((x1,x2) -> Long.compare(x1, x2)).get();
	}
	
	/**
	 * Returns the names of all agents whose moods were mapped during the execution
	 * @return as set of agent names
	 */
	public Set<String> mappedAgents() {
		return this.timedMoodMap.keySet();
	}
	
	/**
	 * Finds and returns the time of the last mood change mapped for the given agent, e.g. to know when to cut 
	 * the mood graph.
	 * @param agName
	 * @return time in ms
	 */
	public Long latestMoodEntry(String agName) {
		List<Pair<Long, Double>> timeMoodList = timedMoodMap.get(agName);
		return timeMoodList.stream().mapToLong(pair -> pair.getFirst()).max().getAsLong();
	}
	
	/**
	 * Identifies the pleasure value for the given agent at the given time by interpolating between the mapped changes
	 * @param agName
	 * @param time
	 * @return pleasure value in the interval [-1.0, 1.0]
	 */
	public double sampleMood(String agName, Long time) {
		List<Pair<Long, Double>> timeMoodList = timedMoodMap.get(agName);
		
		// find the last (time, mood) pair before sampling time
		Pair<Long, Double> sample = timeMoodList.stream().filter(x -> x.getFirst() <= time) //get all pairs before sampling time
																			.max( (x1, x2) -> Long.compare(x1.getFirst(), x2.getFirst()) ) // get last entry before sampling time
																			.get();
		return sample.getSecond();
	}
}
