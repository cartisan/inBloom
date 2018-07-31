package plotmas.storyworld;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.logging.Logger;

import plotmas.PlotModel;

public class Happening<T extends PlotModel<?>> {

	static protected Logger logger = Logger.getLogger(Happening.class.getName());
	
	private BiPredicate<T, Integer> trigger;
	private Consumer<T> effect;

	/**
	 * Constructor to be used by inplace creation of happenings, using anonymous functions. Example:
	 *<pre><code>
	 *{@code Happening<FarmModel> findCorn = new Happening<FarmModel>}( 
     *    new {@code BiPredicate<FarmModel, Integer>()}{ 
     *  	public boolean test(FarmModel model, int step) {
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
	 */
	public Happening(BiPredicate<T, Integer> function, Consumer<T> effect) {
		this.trigger = function;
		this.effect = effect;
	}
	
	/**
	 * Constructor used only to instantiate subclasses, which do not need to set the field {@link effect} but instead 
	 * implement its functionality by overriding the corresponding method: {@link execute}.
	 */
	public Happening(BiPredicate<T, Integer> trigger) {
		// subclasses implementing concrete happenings can 
		this.trigger = trigger;
		this.effect = null; 
	}
	
	public boolean triggered(T model, int step) {
		return trigger.test(model, step);
	}
	
	public void execute(T model) {
		effect.accept(model);
	};
	
	public BiPredicate<T, Integer> getTrigger() {
		return trigger;
	}

	public void setTrigger(BiPredicate<T, Integer> trigger) {
		this.trigger = trigger;
	}

	public Consumer<T> getEffect() {
		return effect;
	}

	public void setEffect(Consumer<T> effect) {
		this.effect = effect;
	}
}
