package plotmas.ERcycle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

/**
 * Abstract superclass of all reasoning cycle states used by {@link RedHenHappeningCycle} to detect plot problems.
 * Each extending class has to implement its own {@link #performDetect(EngageResult)} method. <br>
 * Acts as singleton manager for ProblemDetectionState subclasses, so they don't have to all implement singleton 
 * functionality. <br>
 * 
 *  Example subclass see e.g {@link DetectNarrativeEquilibrium}.
 * @author Leonid Berov
 */
public abstract class ProblemDetectionState {
	protected static Logger logger = Logger.getLogger(ProblemDetectionState.class.getName());
	
    /** Stores initialized instances of ProblemDetectionState subclasses, by mapping each subclass to its instance */
    private static ConcurrentHashMap<Class<?>, Object> INSTANCES  = new ConcurrentHashMap<>();

    /**
     * Generic singleton implementation that allows subclasses of this to inherit singleton functionality. Can manage 
     * instances of all subclasses of ProblemDetectionState, either creating a new singleton instance or returning the
     * existing one.
     *  
     * @param clazz ProblemDetectionState subclass whose instance is requested
     * @param controller instance that runs the reflection reasoning cycle whose state is to be retrieved 
     * @return
     */
    @SuppressWarnings("unchecked")
	public static<T extends ProblemDetectionState> T getInstance(Class<T> clazz, RedHenHappeningCycle controller) {
       Object instance = INSTANCES.get(clazz);
       
       if(instance==null) {
         instance = createInstance(clazz, controller);
         INSTANCES.put(clazz, instance);
       }
       
       return (T) instance;
    }

    /**
     * Creates an instance of a reflection reasoning cycle step implemented by clazz. Uses reflection. 
     * @param clazz
     * @param controller
     * @return
     */
    private static<T extends ProblemDetectionState> T createInstance(Class<T> clazz, RedHenHappeningCycle controller) {
      try {
    	  return clazz.getDeclaredConstructor(RedHenHappeningCycle.class).newInstance(controller);
      } catch (Exception e) {
		logger.severe("Unable to instantiate " + clazz.getName());
		logger.severe(e.getMessage());
		return null;
      }
    }
	
	/**
	 * Controller that runs the reflection reasoning cycle whose states are represented by subclasses of this. Is used 
	 * by {@link #detect(EngageResult)} of subclasses to set the next reasoning step of the reflection cycle.
	 */
	protected RedHenHappeningCycle controller;
	
	/**
	 * Each detection state needs to define which ProblemDetectionState will come next in reflection reasoning cycle.
	 */
	public ProblemDetectionState nextReflectionState;

	protected ProblemDetectionState(RedHenHappeningCycle controller) {
		this.controller = controller;
	}
	
	/**
	 * Convenience method for retrieving initialized instances of ProblemDetectionState subclasses, e.g. to set them as
	 * next reasoning step in the reflection cycle.
	 * @param clazz
	 * @return
	 */
	protected<T extends ProblemDetectionState> T getInstanceFor(Class<T> clazz) {
		return ProblemDetectionState.getInstance(clazz, this.controller);
	}
	
	/**
	 * Responsible for detecting one specific problem, setting the next state of the reasoning cycle, and returning a 
	 * fix for the problem (if one was detected) or null.
	 * 
	 * @param er EngageResult containing an analyzed plotGraph
	 * @return a ProblemFixCommand or null
	 */
	public ProblemFixCommand detect(EngageResult er) {
		ProblemFixCommand fix = this.performDetect(er);
		this.controller.setNextDetectionState(nextReflectionState); // order important: `detect` sometimes sets new nextReflectionState
		return fix;
	}
	
	/**
	 * Responsible for custom code to detect one specific problem and return a fix (if a problem was detected) or null.
	 * Example see e.g. {@link DetectNarrativeEquilibrium}
	 * 
	 * @param er EngageResult containing an analyzed plotGraph
	 * @return a ProblemFixCommand or null
	 */
	protected abstract ProblemFixCommand performDetect(EngageResult er);
}
