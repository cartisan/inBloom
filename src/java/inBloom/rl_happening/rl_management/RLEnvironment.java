/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.LinkedList;

import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.helper.EnvironmentListener;
import inBloom.rl_happening.happenings.ConditionalHappening;
import inBloom.rl_happening.happenings.ShipRescueHappening;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * @author Julia Wippermann
 * @version 24.07.2020
 *
 */
public abstract class RLEnvironment<ModType extends PlotModel<?>> extends PlotEnvironment<ModType> {

	// needs to be a list because theoretically there could be more than one Happening triggered per step
//	public LinkedList<Happening<?>> lastPerformedHappenings = null;
	
	
	
	@Override
	protected synchronized void stepStarted(int step) {
		
		
		if (this.step > 0) {
			this.step++;
			
			
			System.out.println("\n--------------------------- STEP " + (this.step) + " ---------------------------");
			
			if(model!=null) {
				SarsaLambda sarsa = ((AutomatedHappeningDirector<ModType>)this.model.happeningDirector).getSarsa();
				sarsa.rlCycle.log("\nStep " + this.step);
				
				// for FileWriting Data Gathering purposes
				((FeaturePlotModel)model).addPresentFeatures();
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
		
		logger.info("STEP FINISHED");
		
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
	
	
	protected void checkPause() {
		if (this.initialized & !PlotLauncher.getRunner().isDebug()) {
			// same action was repeated Launcher.MAX_REPEATE_NUM number of times by all agents:
	    	if (this.narrativeExquilibrium()) {
	    		// reset counter
	    		logger.info("Auto-paused execution of simulation, because all agents repeated the same action sequence " +
	    				String.valueOf(MAX_REPEATE_NUM) + " # of times.");
	    		this.resetAllAgentActionCounts();
	    		PlotLauncher.runner.pauseExecution();
	    		for(EnvironmentListener l : this.listeners) {
	    			l.onPauseRepeat();
	    		}
	    	}
	    	if (MAX_STEP_NUM > -1 && this.getStep() % MAX_STEP_NUM == 0) {
	    		logger.info("Auto-paused execution of simulation, because system ran for MAX_STEP_NUM steps.");

	    		PlotLauncher.runner.pauseExecution();
	    		for(EnvironmentListener l : this.listeners) {
	    			l.onPauseRepeat();
	    		}
	    	}
	    	
//	    	if(this.lastPerformedHappenings!=null) {
//	    		ConditionalHappening<?> happening = (ConditionalHappening)this.lastPerformedHappenings.getFirst();
//	    		if((happening instanceof ShipRescueHappening) && (happening.hasHadEffect)) {
//	    			logger.info("Auto-paused execution of simulation, because the agent was rescued from the island.");
//
//	    			PlotLauncher.runner.pauseExecution();
//	    			for(EnvironmentListener l : this.listeners) {
//	    				l.onPauseRepeat();
//	    			}
//	    		}
//	    	}
		}
	}
}
