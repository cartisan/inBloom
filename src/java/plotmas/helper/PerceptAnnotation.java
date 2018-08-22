package plotmas.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import jason.asSemantics.Emotion;

public class PerceptAnnotation {
	   static Logger logger = Logger.getLogger(PerceptAnnotation.class.getName());
	   
	private String cause;
	private List<String> emotions;
	
	public static PerceptAnnotation fromEmotion(String emotion) {
		PerceptAnnotation annot = new PerceptAnnotation();
		
		if (Emotion.getAllEmotions().contains(emotion)) {
			annot.emotions.add(emotion);
		}
		else{
			logger.warning("Error: Trying to add an invalid emotion to a percept: " + emotion);
			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + emotion);
		}
		
		
		return annot;
	}
	
	public static PerceptAnnotation fromCause(String cause) {
		PerceptAnnotation annot = new PerceptAnnotation();
		annot.cause = cause;
		return annot;
	}
	
	public PerceptAnnotation() {
		emotions = new ArrayList<>();
		cause = "";
	}
	
	public PerceptAnnotation(String... ems) {
		// check for validity first
		for (String em : ems) {
    		if (!Emotion.getAllEmotions().contains(em)) {
    			logger.warning("Error: Trying to add an invalid emotion to a percept: " + em);
    			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + em);
    		}
		}
		
		this.emotions = Arrays.asList(ems);
	}
	
	@Override
	public String toString(){
		String cau = this.causalityAnnotation();
		String em = this.emotionsAnnotation();
		String sep = this.separator(cau, em);
		
		String result = "[" + cau + sep + em + "]";
				
		return result;
	}
	
	public String separator(String literal1, String literal2) {
		if ((literal1 != "") & (literal2 != ""))
			return ",";
		return "";
	}
	
	public String causalityAnnotation() {
		if((null == this.cause) || (this.cause.equals(""))) {
			return "";
		}
		
		return "cause(" + this.cause + ")";
	}
	
	public String emotionsAnnotation() {
		String result = "";
		
    	for(String em: this.emotions) {
			result += Emotion.ANNOTATION_FUNCTOR + "(" + em + "),";
    	}
    	
    	// remove comma after last emotion
    	result = result.substring(0, result.length() - 1);
    	
    	return result;
    }
	
	public PerceptAnnotation addTargetedEmotion(String emotion, String target) {
    	if (Emotion.getAllEmotions().contains(emotion)) {
			this.emotions.add(emotion + "," + target);
		}
		else {
			logger.warning("Error: Trying to add an invalid emotion to a percept: " + emotion);
			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + emotion);
		}
    	
    	return this;
	}
	
	public void setCause(String cause) {
		this.cause = cause;
	}
}
