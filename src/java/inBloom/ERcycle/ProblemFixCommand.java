package inBloom.ERcycle;

import inBloom.ERcycle.PlotCycle.EngageResult;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;

/**
 * Abstract superclass of problem fixes that can be suggested by {@link ProblemDetectionState} subclasses. Each 
 * ProblemFixCommand subclass encapsulates one operation that changes the state of an {@link EngageResult} as defined by
 * its {@link #execute(EngageResult)} method, and also contains instructions to {@link #undo(EngageResult)} these state changes.
 * Commands are executed by the reflection reasoning cycle controller as for instance {@link RedHenHappeningCycle}. <br>
 * 
 *  Example subclass see e.g {@link ScheduleHappening}.
 * @author Leonid Berov
 */
public interface ProblemFixCommand {
	public void execute(EngageResult er);
	public void undo(EngageResult er);
	public String message();
}
