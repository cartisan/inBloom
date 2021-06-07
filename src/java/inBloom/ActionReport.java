package inBloom;

import java.util.HashMap;
import java.util.Map;

import inBloom.helper.PerceptAnnotation;

/**
 * Data class used by story-world classes that implement methods which enable agent actions. This class stores the
 * model-side outcomes of action execution, that is, its success and potentially the emotions it elicits in the
 * participating parties.
 * Processed by {@linkplain PlotEnvironment#executeAction(String, jason.asSyntax.Structure)} to generate the ASL-side
 * perception as a literal that is equivalent to the action-literal, and personalized reactions in annotations.
 *
 * <p>Success defaults to false</p>.
 *
 * @author Leonid Berov
 */
public class ActionReport {

	/** Notes whether the action was successfully executed or failed. */
	public Boolean success;
	/** Map from character name to individual percept-annotations of this action for the respective character- */
	public Map<String, PerceptAnnotation> perceptMap;
	/** Plot time when this action report was generated, can e.g. be used to generate unique IDs so that x-character
	 * edges between different char's perception of this action can be connected. */
	public long eventTime;

	public ActionReport() {
		this.success = false;
		this.perceptMap = new HashMap<>();
		this.eventTime = System.nanoTime();
	}

	public ActionReport(boolean result) {
		this();
		this.success = result;
	}

	public void addPerception(String agName, PerceptAnnotation annot) {
		this.perceptMap.put(agName, annot);
	}

	public PerceptAnnotation getAnnotation(String agName) {
		if(!this.perceptMap.containsKey(agName)) {
			 this.perceptMap.put(agName, new PerceptAnnotation());
		}
		return this.perceptMap.get(agName);
	}
}