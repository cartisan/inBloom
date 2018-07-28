package plotmas.storyworld;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class Happening<T extends Model<?>> {

	static protected Logger logger = Logger.getLogger(Happening.class.getName());
	
	private Predicate<T> trigger;
	private Consumer<T> effect;

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
	 */
	public Happening(Predicate<T> function, Consumer<T> effect) {
		this.trigger = function;
		this.effect = effect;
	}
	
	/**
	 * Constructor used only to instantiate subclasses, which do not need to set the fields {@link trigger} and 
	 * {@link effect} but instead can implement their functionality by overriding the methods 
	 * {@link triggered} and {@link execute}.
	 */
	public Happening() {
		// subclasses implementing concrete happenings can 
		this.trigger = null;
		this.effect = null; 
	}
	
	public boolean triggered(T model) {
		return trigger.test(model);
	}
	
	public void execute(T model) {
		effect.accept(model);
	};
}
