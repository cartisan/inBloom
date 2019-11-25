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
 * @author Julia Wippermann
 * @version 21.11.19
 *
 * DOCME
 */
public abstract class ConditionalHappening<T extends PlotModel<?>> extends Happening<T> {
	
	//DOCME
	public ConditionalHappening(Predicate<T> trigger, String patient, String causalProperty) {
		// TODO der percept "happening" scheint nicht bei dem Agent anzukommen, er reagiert nie darauf
		// der percept, den wir später von hand hinzufügen, kommt jedoch an.
		super(trigger, causalProperty, "happening");
		this.patient = patient;
	}
	
	//DOCME
	public ConditionalHappening(Predicate<T> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	//DOCME
	public void execute(T model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(hasEffect(model, chara)) {
			this.executeEffects(model, chara);
			this.setPercept(this.getConditionalPercept());
			this.setAnnotation(PerceptAnnotation.fromEmotion(this.getConditionalEmotion()));
		}	

	}

	//DOCME
	protected abstract boolean hasEffect(T model, Character chara);
	
	//DOCME
	protected abstract void executeEffects(T model, Character chara);
	
	//DOCME
	protected abstract String getConditionalPercept();
	
	//DOCME
	protected abstract String getConditionalEmotion();
	
}
