package inBloom.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import jason.asSemantics.Mood;

/**
 * Helper class that manages the mapping between plot-time and agent's mood changes. Maintained by
 * {@link inBloom.jason.PlotAwareAg}, which updates the mapping. Used by {@link inBloom.graph.MoodGraph} when it creates the
 * graph that shows the development of character's pleasure over time.
 * @author Leonid Berov
 */
public class MoodMapper {
	static protected Logger logger = Logger.getLogger(MoodMapper.class.getName());

	private Table<String, Long, List<Mood>> timedMoodMap = Tables.synchronizedTable(HashBasedTable.create());
	public List<Long> startTimes = Collections.synchronizedList(new LinkedList<>());
	public Map<Integer, Long> stepReasoningcycleNumMap = new HashMap<>();	// maps from env steps to agent reasoning cycle num at that step

	public Map<Long, List<Mood>> getMoodByAgent(String agName) {
		Map<Long, List<Mood>> timeMoodMap = this.timedMoodMap.row(agName);
		return timeMoodMap;
	}

	/**
	 * Needs to be called each time the agent's pleasure value changes, in order to make this change accessible for
	 * later analysis, as e.g. by {@link inBloom.graph.MoodGraph}.
	 * @param agName
	 * @param time
	 * @param pleasure
	 */
	public void addMood(String agName, Long time, Mood mood) {
		if(!this.timedMoodMap.containsRow(agName)) {
			this.startTimes.add(time);
		}

		if (!this.timedMoodMap.contains(agName, time)) {
			this.timedMoodMap.put(agName, time, new LinkedList<>());
		}

		this.timedMoodMap.get(agName, time).add(mood.clone());
	}

	/**
	 * Finds and returns the start time of the agent that started its reasoning cycle last, in order to avoid analysing
	 * the mood development before all agents are running.
	 * @return time in ms
	 */
	public Long latestStartTime(){
		return this.startTimes.stream().max((x1,x2) -> Long.compare(x1, x2)).get();
	}

	/**
	 * Returns the end time of the agent that ended its reasoning cycle last.
	 * @return time in ms
	 */
	public Long latestEndTime() {
		Long endTime = 0l;
		for (String agName: this.mappedAgents()) {
			Long agEndTime = this.latestMoodEntry(agName);
			endTime = agEndTime > endTime ? agEndTime : endTime;
		}
		return endTime;
	}

	/**
	 * Returns the names of all agents whose moods were mapped during the execution
	 * @return as set of agent names
	 */
	public Set<String> mappedAgents() {
		return this.timedMoodMap.rowKeySet();
	}

	/**
	 * Finds and returns the time of the last mood change mapped for the given agent, e.g. to know when to cut
	 * the mood graph.
	 * @param agName
	 * @return time in ms
	 */
	public Long latestMoodEntry(String agName) {
		Map<Long, List<Mood>> timeMoodMap = this.timedMoodMap.row(agName);
		return timeMoodMap.keySet().stream().mapToLong(l -> l).max().getAsLong();
	}

	/**
	 * Identifies the mood for the given agent at the given time by interpolating between the mapped changes
	 * @param agName
	 * @param time
	 * @return mood value in the interval [-1.0, 1.0], or null if time lies outside of mapped interval
	 */
	public Mood sampleMood(String agName, Long time) {
		Map<Long, List<Mood>> timeMoodMap = this.timedMoodMap.row(agName);

		Long min = timeMoodMap.keySet().stream().mapToLong(l -> l).min().orElse(Integer.MAX_VALUE);
		if (time < min) {
			return null;
		}

		// find the last (time, mood) pair before sampling time
		Long sampleTime = timeMoodMap.keySet().stream().filter(x -> x <= time) //get all pairs before sampling time
														.max( (x1, x2) -> Long.compare(x1, x2) ) // get last entry before sampling time
														.get();

		List<Mood> moods = timeMoodMap.get(sampleTime);
		double avgP = moods.stream().map(m -> m.getP()).mapToDouble(l -> l).average().getAsDouble();
		double avgA = moods.stream().map(m -> m.getA()).mapToDouble(l -> l).average().getAsDouble();
		double avgD = moods.stream().map(m -> m.getD()).mapToDouble(l -> l).average().getAsDouble();

		return new Mood(avgP, avgA, avgD);
	}

	public String toString() {
		return this.timedMoodMap.toString();
	}
}
