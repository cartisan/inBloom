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

	public boolean success;
	public Map<String, PerceptAnnotation> perceptMap;
	
	public ActionReport() {
		this.success = false;
		this.perceptMap = new HashMap<>();
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