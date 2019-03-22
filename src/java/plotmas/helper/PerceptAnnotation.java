package plotmas.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;
import plotmas.graph.Edge;

public class PerceptAnnotation {
	// TODO: Finish test, refactor to unified annotation storage
    static Logger logger = Logger.getLogger(PerceptAnnotation.class.getName());
	   
	private String cause;
	private List<String> emotions;
	private List<String> furtherAnnots;
	
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
		this.furtherAnnots = new LinkedList<>();
		this.emotions = new ArrayList<>();
		this.cause = "";
	}
	
	public PerceptAnnotation(String... ems) {
		this();
		
		for (String em : Arrays.asList(ems)) {
    		if (Emotion.getAllEmotions().contains(em)) {
				this.emotions.add(em);
    		} else {
				logger.warning("Error: Trying to add an invalid emotion to a percept: " + em);
				throw new RuntimeException("Trying to add an invalid emotion to a percept: " + em);
			}
		}
	}
	
	@Override
	public String toString(){
		// TODO: Test that this works
		// TODO: Use it in FarmModel sing and so on
		String cau = this.causalityAnnotation();
		String em = this.emotionsAnnotation();
		String more = ""; 
		if (!this.furtherAnnots.isEmpty()) {
			more = this.furtherAnnots.stream().collect( Collectors.joining( "," ) );
			more.substring(0, more.length());
		}
		
		String result = cau + this.separator(cau, em) + em;
		result = result + this.separator(result, more) + more;
		result = "[" +result  + "]";
				
		return result;
	}
	
	public void addAnnotation(String annot) {
		this.furtherAnnots.add(annot);
	}
	
	public String separator(String literal1, String literal2) {
		if ((literal1.equals("")) || (literal2.equals("")))
			return "";
		return ",";
	}
	
	public String causalityAnnotation() {
		if((null == this.cause) || (this.cause.equals(""))) {
			return "";
		}
		
		return Edge.Type.CAUSALITY.toString() + "(" + this.cause + ")";
	}
	
	public String emotionsAnnotation() {
		String result = "";
		
    	for(String em: this.emotions) {
    			result += Emotion.ANNOTATION_FUNCTOR + "(" + em + "),";
    	}
    	
    	// remove comma after last emotion
    	if (result.length() > 0) {
    		result = result.substring(0, result.length() - 1);
    	}
    	
    	return result;
    }
	
	public PerceptAnnotation addTargetedEmotion(String emotion, String target) {
    	if (Emotion.getAllEmotions().contains(emotion)) {
			this.emotions.add(emotion);
			this.addAnnotation("target(" + target + ")");
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
