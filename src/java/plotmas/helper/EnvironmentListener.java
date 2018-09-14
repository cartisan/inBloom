package plotmas.helper;

/**
 * This interface is for listeners which can be added to a plot environment.
 * @author Sven Wilke
 *
 */
public interface EnvironmentListener {
	
	/**
	 * Gets called during PlotEnvironment#pauseOnRepeat whenever the execution
	 * pauses automatically because all agents repeated the same action 7 times
	 * in a row. The call to this method happens AFTER pausing the execution
	 * and AFTER resetting the agents action counts.
	 */
	public void onPauseRepeat();
}
