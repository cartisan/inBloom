/**
 * 
 */
package inBloom.rl_happening.rl_management;

import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 * @version 24.07.2020
 *
 */
public abstract class RLEnvironment<ModType extends PlotModel<?>> extends PlotEnvironment<ModType> {

	@Override
	protected synchronized void stepStarted(int step) {
		
		
		if (this.step > 0) {
			this.step++;
			
			
			System.out.println("\n--------------------------- STEP " + (this.step) + " ---------------------------");
			
			if(model!=null) {
				SarsaLambda sarsa = ((AutomatedHappeningDirector<ModType>)this.model.happeningDirector).getSarsa();
				sarsa.rlCycle.log("\nStep " + this.step);
			}
			
			
			

			if(!PlotLauncher.getRunner().isDebug()) {
				logger.info("Step " + this.step + " started for environment");
			}

			if (this.model != null) {
				// Give model opportunity to check for and execute happenings
				
				
				this.model.checkHappenings(this.step);
			} else {
				logger.warning("field model was not set, but a step " + this.step + " was started");
			}
		} else {
			// ignore mood data before environment step 1 started
			if (this.model != null) {
				this.getModel().moodMapper.startTimes.add(getPlotTimeNow());
			}
		}
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
		
		System.out.println("STEP FINISHED");
		
		this.logger.info("STEP FINISHED");
		
		if(this.model != null) {
			SarsaLambda sarsa = ((AutomatedHappeningDirector<ModType>)this.model.happeningDirector).getSarsa();
			sarsa.updateWeights(0.0);
		}
		
		// if environment is initialized && agents are done setting up && one of the agents didn't choose an action
		if (this.model != null && byTimeout && step > 5 ) {
			for (Character chara : this.model.getCharacters()) {
				Object action = this.getActionInSchedule(chara.getName());
				if(action == null) {
					this.agentActions.get(chara.getName()).add("--");		// mark inaction by --
				}
			}
			
		}
		// check if pause mode is enabled, wait with execution while it is
		this.waitWhilePause();
	}
	
}
