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
 */
public abstract class ConditionalHappening<T extends PlotModel<?>> extends Happening<T> {
		
	public ConditionalHappening(Predicate<T> trigger, String patient, String causalProperty) {
		// TODO dieser percept ergibt halt eh nicht viel Sinn für das CONDITIONAL Happening
		super(trigger, causalProperty, "something_is_happening");
		// ich kann spaeter noch this.setPercept("bla"); machen -> Methode die ich über Happening aus Event erbe;
		// this.setAnnotation(PerceptAnnotation.fromEmotion("EMOTION"))
		// TODO -> testen, ob das funktioniert
		this.patient = patient;
		// TODO delte / outsource emotion
		// this.annotation = PerceptAnnotation.fromEmotion("resentment");
	}
	
	public ConditionalHappening(Predicate<T> trigger, String patient) {
		this(trigger, patient, "");
	}
	
	public void execute(T model) {
		
		Character chara = model.getCharacter(this.getPatient());
		
		if(hasEffect(model, chara)) {
			this.executeEffects(model, chara);
			this.setPercept(this.getConditionalPercept());
			this.setAnnotation(PerceptAnnotation.fromEmotion(this.getConditionalEmotion()));
		}	

	}

	protected abstract boolean hasEffect(T model, Character chara);
	
	protected abstract void executeEffects(T model, Character chara);
	
	protected abstract String getConditionalPercept();
	
	protected abstract String getConditionalEmotion();
	
}
