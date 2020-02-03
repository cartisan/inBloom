/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.storyworld.HappeningDirector;

/**
 * This is special type of PlotModel that creates and manages a set of features that can describe
 * a state of the model
 * 
 * @author Julia Wippermann
 * @version 3.2.20
 *
 */
public abstract class FeaturePlotModel<EnvType extends PlotEnvironment<?>> extends PlotModel<EnvType> {
	
	// a list of all possible domain-specific features that are relevant to the model state
	public List<String> allPossibleFeatures;
	// a list of all currently activated features, meaning all features present in the current state of the model
	public List<String> presentFeatures;

	public FeaturePlotModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		
		super(agentList, hapDir);
		
		// Create a list of all possible domain-specific features
		this.allPossibleFeatures = this.getAllPossibleFeatures();
		
		// If possible, activate all features that should be initially activated before the start of the story
		List<String> initiallyActivatedFeatures = this.getInitiallyActivatedFeatures();
		if(initiallyActivatedFeatures != null) {
			for(String feature: initiallyActivatedFeatures) {
				this.activateFeature(feature);
			}
		}
	}
	
	public abstract List<String> getAllPossibleFeatures();
	
	/**
	 * Returns a list of features that should already be activated before the story starts.
	 * 
	 * @return A list of features that should already be activated before the story starts
	 */
	public List<String> getInitiallyActivatedFeatures() {
		return null;
	}
	
	/**
	 * Activates a given feature for the PlotModel by adding the feature to the list of present features
	 * (this.presentFeatures). The feature stays activated until it is deactivated again
	 * (by this.deactivateFeature).
	 * 
	 * @param featureName
	 * 				The name of the feature that should be activated. Needs to be part of the list of
	 * 				all possible features
	 * @return true
	 * 				if there is a feature with this name and it hadn't been activated yet,
	 * 				but has been activated now
	 * 		   false
	 * 				if the feature was already activated
	 * @throws IllegalArgumentException
	 * 				if there is no feature with this name listed in the list of all possible Feature
	 */
	public boolean activateFeature(String featureName) {
		if(!this.allPossibleFeatures.contains(featureName)) {
			throw new IllegalArgumentException("No feature with this name could be found.");
		} else if (this.presentFeatures.contains(featureName)){
			// the feature is already activated
			return false;
		} else {
			// The feature is valid and hasn't been activated yet
			this.presentFeatures.add(featureName);
			return true;
		}
	}
	
	/**
	 * Deactivates a given feature for the PlotModel by removing the feature from the list of present
	 * features (this.presentFeatures). The feature stays deactivated until it is activated again
	 * (by this.activateFeature).
	 * 
	 * @param featureName
	 * 				The name of the feature that should be deactivated. Needs to be part of the list of
	 * 				present (activated) features
	 * @return true
	 * 				if a feature with this name has been activated before and has now succesfully been
	 * 				deactivated
	 * 		   false
	 * 				if the feature wasn't activated
	 */
	public boolean deactivateFeature(String featureName) {
		if(!this.presentFeatures.contains(featureName)) {
			// the feature cannot be deactivated since it hasn't been activated yet
			return false;
		} else {
			assert(this.allPossibleFeatures.contains(featureName)): "FeatureName is present in presentFeature "
																	+ "but not in allPossibleFeatures";
			this.presentFeatures.remove(featureName);
			return true;
		}
	}

}
