package inBloom.storyworld;

import java.util.List;

import inBloom.PlotEnvironment;
import inBloom.PlotModel;

public interface HappeningDirector {

	/**
	 * Checks whether current state of model activates happenings and returns them.
	 * @param step time step provided by {@linkplain PlotEnvironment}
	 * @return a list containing happenings to be executed (or empty list)
	 */
	public List<Happening<?>> getTriggeredHappenings(int step);
	public List<Happening<?>> getAllHappenings();
	public void setModel(PlotModel<?> model);
	public HappeningDirector clone();
}