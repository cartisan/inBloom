/**
 * 
 */
package inBloom.rl_happening.happenings;

import java.util.function.Predicate;

import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;

/**
 * A Happening that only has effects on the patient and the model under certain preconditions
 * 
 * @author Julia Wippermann
 */
public abstract class ConditionalHappening<T extends PlotModel<?>> extends Happening<T> {
	
	/**
	 * Constructor with trigger, patient and causal property. Creates a percept "happening" for the patient.
	 * 
	 * @param trigger
	 * 			see {@link Happening}
	 * @param patient
	 * 			The agent whom the Happening is happening to
	 * @param causalProperty
	 * 			see {@link Happening}
	 */
	public ConditionalHappening(Predicate<T> trigger, String patient, String causalProperty) {
		// TODO der percept "happening" scheint nicht bei dem Agent anzukommen, er reagiert nie darauf
		// der percept, den wir später von hand hinzufügen, kommt jedoch an.
		super(trigger, causalProperty, "happening");
		this.patient = patient;
	}
	
	/**
	 * Constructor with trigger and patient, where the causal property is left empty.
	 * 
	 * @param trigger
	 * 			see Happening
	 * @param patient
	 * 			The agent whom the Happening is happening to
	 */
	public ConditionalHappening(Predicate<T> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	/**
	 * Executes the Happening, if the preconditions, implemented in {@link ConditionalHappening.hasEffect} are fulfilled.
	 * This includes the effects onto the the model {@link executeModelEffects}, as well as the percept {@link getConditionalPercept}
	 * and the emotion {@link getConditionalEmotion} that the agent receives if the happening is executed.
	 * 
	 * If the preconditions of {@link hasEffect} are not fullfiled, the Happening has no effect on patient nor model.
	 * 
	 * @param model
	 * 			The model onto which the Happening has its effects
	 * 
	 * TODO why do these links not work properly?
	 */
	public void execute(T model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(hasEffect(model, chara)) {
			this.executeModelEffects(model, chara);
			this.setPercept(this.getConditionalPercept());
			this.setAnnotation(PerceptAnnotation.fromEmotion(this.getConditionalEmotion()));
		}	

	}

	/**
	 * Returns true if the preconditions for the Happening having any effect on agent or model are fulfilled
	 * 
	 * @param model
	 * 			The model onto which the Happening should be executed
	 * @param chara
	 * 			The patient effected by the Happening
	 * @return	true
	 * 				if the preconditions for the Happening having any effect are fulfilled
	 * 			false
	 * 				if the preconditions for the Happening are not fulfilled
	 */
	protected abstract boolean hasEffect(T model, Character chara);
	
	/**
	 * Executes the effects that the Happening will have on the Model (and possibly the agent)
	 * 
	 * @param model
	 * 			The model onto which the Happening has its effects
	 * @param chara
	 * 			The character onto which the Happening has its effects
	 */
	protected abstract void executeModelEffects(T model, Character chara);
	
	/**
	 * Returns the perception that the patient receives if the Happening is executed
	 * 
	 * @return the perception that the patient receives
	 */
	protected abstract String getConditionalPercept();
	
	/**
	 * Returns the emotion that the patient receives if the Happening is executed
	 * 
	 * @return the emotion that the patient receives
	 */
	protected abstract String getConditionalEmotion();
	
}


// TODO dinge, die ich als causalProperty haben will, muss ich als @ModelState annotieren
// für Locations und Characters kann ich das machen