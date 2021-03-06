package inBloom.storyworld;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import jason.util.Pair;

import inBloom.PlotModel;

public class Happening<T extends PlotModel<?>> extends Event {

	static protected Logger logger = Logger.getLogger(Happening.class.getName());

	protected Predicate<T> trigger = null;
	protected Consumer<T> effect = null;
	protected String causalProperty = "";

	/**
	 * Constructor to be used for in-place creation of happenings, using anonymous functions. Example:
	 *<pre><code>
	 *{@code Happening<FarmModel> findCorn = new Happening<FarmModel>}(
     *    new {@code Predicate<FarmModel>()}{
     *  	public boolean test(FarmModel model) {
     *           if(model.actionCount > 3)
     *           	return true;
     *           return false;
     *    },
     *    new {@code Consumer<FarmModel>()} {
     *  	public void accept(FarmModel model) {
     *      	Character chara = model.getCharacter("hen");
	 *			chara.addToInventory(new FarmModel.Wheat());
     *      }
     *    },
     *    "hen",
     *    "farmingProgress",
     *    "found(wheat)"
     *);
     *</code></pre>
     * @param trigger predicate that takes instances  of type T and returns true when the happening is to be executed
     * @param effect function that takes an instance of T and induces the changes that are described by this happenings
     * @param patient name of agent that happening is happening to
     * @param causalProperty fieldName of T, whose change in value can trigger this happening (i.e. on which it is causally dependent)
     * @param percept perception term (without annotations), which will be generated for the happening e.g. `found(wheat)`
	 */
	public Happening(Predicate<T> trigger, Consumer<T> effect, String patient, String causalProperty, String percept) {
		this.trigger = trigger;
		this.effect = effect;
		this.patient = patient;
		this.causalProperty = causalProperty;
		this.percept = percept;
	}

	/**
	 * Constructor used only to instantiate subclasses, which do not need to set the field {@link effect} but instead
	 * implement its functionality by overriding the corresponding method: {@link execute}.
	 *
	 * @param trigger predicate that takes instances  of type T and returns true when the happening is to be executed
	 * @param causalProperty fieldName of T, whose change in value can trigger this happening (i.e. on which it is causally dependent)
	 * @param percept perception term (without annotations), which will be generated for the happening e.g. `found(wheat)`
	 */
	public Happening(Predicate<T> trigger, String causalProperty, String percept) {
		// subclasses implementing concrete happenings can
		this.effect = null;
		this.trigger = trigger;
		this.causalProperty = causalProperty;
		this.percept = percept;
	}

	public Happening() {
		// left empty intentionally
	}

	public boolean triggered(T model) {
		return this.trigger.test(model);
	}

	public void execute(T model) {
		this.effect.accept(model);
	};

	public Predicate<T> getTrigger() {
		return this.trigger;
	}

	public void setTrigger(Predicate<T> trigger) {
		this.trigger = trigger;
	}

	public Consumer<T> getEffect() {
		return this.effect;
	}

	public void setEffect(Consumer<T> effect) {
		this.effect = effect;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.patient + " << " + this.percept;
	}

	public String getCausalProperty() {
		return this.causalProperty;
	}

	public void setCausalProperty(String cause) {
		this.causalProperty = cause;
	}

	/**
	 * Determines which agent-action was responsible for triggering the execution of this happening (if any), and updates
	 * the state of this happening accordingly, including updating annotations.
	 * @param causalityMap Maps fieldProperty to last (agentName, agentAction/happening) that changed field
	 * value in model.
	 */
	public void identifyCause(Map<String, Pair<String, String>> causalityMap) {
		if(this.getCausalProperty() == null || this.getCausalProperty().equals("")) {
			return;
		}

		Pair<String, String> causeTup = causalityMap.get(this.getCausalProperty());
		String cause = causeTup.getSecond();
		if( cause != null && !cause.equals("") ) {
			this.annotation.setCause(cause);

			// if no patient was predefined, assume that happening occurs to the agent whose actions caused the happening
			if (this.patient == null) {
				this.setPatient(causeTup.getFirst());
			}
		}
	}
}
