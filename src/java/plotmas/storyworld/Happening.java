package plotmas.storyworld;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.google.common.collect.Table;

import plotmas.PlotModel;

public class Happening<T extends PlotModel<?>> extends Event {

	static protected Logger logger = Logger.getLogger(Happening.class.getName());
	
	private Predicate<T> trigger;
	private Consumer<T> effect;
	protected String causalProperty = "";

	/**
	 * Constructor to be used by inplace creation of happenings, using anonymous functions. Example:
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
     *           	model.	
     *      }
     *    }
     *);
     *</code></pre>
     * @param trigger predicate that takes instances  of type T and returns true when the happening is to be executed
     * @param effect function that takes an instance of T and induces the changes that are described by this happenings
     * @param causalProperty fieldName of T, whose change in value can trigger this happening (i.e. on which it is causally dependent)
     * @param percept perception term (without annotations), which will be generated for the happening e.g. `found(wheat)`
	 */
	public Happening(Predicate<T> trigger, Consumer<T> effect, String causalProperty, String percept) {
		this.trigger = trigger;
		this.effect = effect;
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
	
	public boolean triggered(T model) {
		return trigger.test(model);
	}

	public void execute(T model) {
		effect.accept(model);
	};
	
	public Predicate<T> getTrigger() {
		return trigger;
	}

	public void setTrigger(Predicate<T> trigger) {
		this.trigger = trigger;
	}

	public Consumer<T> getEffect() {
		return effect;
	}

	public void setEffect(Consumer<T> effect) {
		this.effect = effect;
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public String getCausalProperty() {
		return causalProperty;
	}

	public void setCausalProperty(String cause) {
		this.causalProperty = cause;
	}
	
	/**
	 * Determines which agent-action was responsible for triggering the execution of this happening (if any), and updates
	 * the state of this happening accordingly, including updating annotations.
	 * @param causalityTable Table that maps (agentName, fieldProperty) to last agentAction that changed field value in model 
	 */
	public void identifyCause(Table<String, String, String> causalityTable) {
		String cause = causalityTable.get(this.getPatient(), this.getCausalProperty());
		this.annotation.setCause(cause);
	}
}
