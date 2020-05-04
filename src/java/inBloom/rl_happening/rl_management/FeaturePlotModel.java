/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotModel;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Character;

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
	private LinkedList<String> allPossibleFeatures;
	// IDEA: Make has HashSet<String, boolean> -> only have one variable
	// a list of all currently activated features, meaning all features present in the current state of the model
	private LinkedList<String> presentFeatures;
	
	private Collection<Character> allCharacters;
	
	private final String defaultValue = "default";
	// MoodType will look like this: character name + moodType. F.e. "robinsonbored"
	private String currentMoodType = defaultValue;
	// MoodType will look like this: character name + moodStrength. F.e. "robinsonslightly"
	private String currentMoodStrength = defaultValue;
	
	// TODO so far only one character can have a mood
	private final String[] moodTypes = {"bored",
										"disdainfaul",
										"anxious",
										"hostile",
										"docile",
										"relaxed",
										"dependent",
										"exuberant"};
	private final String[] moodStrength = {"slightly",
										   "moderately",
										   "fully"};
	
	
	// reflections.getSubTypesOf(aClazz))
	
	// How to get the name of a mood:
	// character.getMood().getFullName()
	
	
	
	/**
	 * CONSTRUCTOR
	 */
	
	public FeaturePlotModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		
		super(agentList, hapDir);
		
		// The Character HashMap has been initialized and filled with all Characters in the super Constructor
		this.allCharacters = this.getCharacters();
		
		// Create a list of all domain-specific features
		this.allPossibleFeatures = this.getDomainDependentFeatures();
		// Add all domain independent features
		for(Character character: this.allCharacters) {
			this.createMoodFeatures(this.allCharacters, this.moodTypes);
			this.createMoodFeatures(this.allCharacters, this.moodStrength);
		}
		
		// If possible, activate all features that should be initially activated before the start of the story
		List<String> initiallyActivatedFeatures = this.getInitiallyActivatedFeatures();
		if(initiallyActivatedFeatures != null) {
			for(String feature: initiallyActivatedFeatures) {
				this.activateFeature(feature);
			}
		}
	}
	
	
	
	
	/**
	 * METHODS FOR GETTING FEATURE LISTS
	 */
	
	public LinkedList<String> getAllPossibleFeatures() {
		return this.allPossibleFeatures;
	}
	
	public abstract LinkedList<String> getDomainDependentFeatures();
	
	/**
	 * Returns a list of features that should already be activated before the story starts.
	 * 
	 * @return A list of features that should already be activated before the story starts
	 */
	public List<String> getInitiallyActivatedFeatures() {
		return null;
	}
	
	public List<String> getPresentFeatures() {
		this.updateMoodFeatures();
		return this.presentFeatures;
	}
	
	
	
	
	/**
	 * METHODS TO (DE)ACTIVATE FEATURES
	 */
	
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
	
	// TODO when will this be called? Whenever we ask for the presentFeatures
	public boolean updateMoodFeatures() {
		if(!this.currentMoodType.equals(defaultValue) && !this.currentMoodType.equals(defaultValue)) {
			deactivateAllMoodFeatures();
		} // otherwise the mood Features haven't been initialized yet and don't need to be activated
		
		for(Character character: this.allCharacters) {
			String moodType = createCharacterDependentFeature(character, this.getMoodName(character));
			String moodStrength = createCharacterDependentFeature(character, this.getMoodStrength(character));

			boolean typeSuccess = this.activateFeature(moodType);
			boolean strengthSuccess = this.activateFeature(moodStrength);

			if(!typeSuccess || !strengthSuccess) {
				// Something went wrong in one of the assignments
				return false;
			}

			assert(typeSuccess): "Activation of Mood type wasn't succesful. Mood type feature had already been activated.";
			assert(strengthSuccess): "Activation of Mood strength wasn't succesful. Mood strength feature had already been activated.";
		}
		
		return true;
	}
	
	public void deactivateAllMoodFeatures() {
		boolean typeSuccess = this.deactivateFeature(currentMoodType);
		boolean strengthSuccess = this.deactivateFeature(currentMoodStrength);
		
		assert(typeSuccess): "Mood Type hadn't been activated yet.";
		assert(strengthSuccess): "Mood Strength hadn't been activated yet.";
	}
	
	
	
	
	/**
	 * METHODS TO CREATE (DOMAIN INDEPENDENT) FEATURES
	 */
	
	private String createCharacterDependentFeature(Character character, String featureName) {
		return character.name + featureName;
	}
	
	/**
	 * Adds all Features from a given String array to the list opf all possible features, which
	 * is everything needed to create a feature
	 * 
	 * @param featureNames
	 * 			String Array of all feature names to be added as features
	 */
	public void createMoodFeatures(Collection<Character> characters, String[] featureNames) {
		for(Character character: characters) {
			for(String featureName: featureNames) {
				String feature = this.createCharacterDependentFeature(character, featureName);
				this.allPossibleFeatures.add(feature);
			}
		}
	}
	
	
	
	
	/**
	 * HELPER METHODS FOR BACKGROUND INFORMATION
	 */
	
	public String getMoodName(Character character) {
		return character.getMood().getType();
	}
	
	public String getMoodStrength(Character character) {
		return character.getMood().getStrength();
	}
}
