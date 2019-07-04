package inBloom.helper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import inBloom.graph.Edge;
import jason.asSemantics.Emotion;

public class PerceptAnnotation {
    static Logger logger = Logger.getLogger(PerceptAnnotation.class.getName());
	   
	private List<String> annots;
	
	public static PerceptAnnotation fromEmotion(String emotion) {
		PerceptAnnotation annot = new PerceptAnnotation();
		
		if (Emotion.getAllEmotions().contains(emotion)) {
			annot.addAnnotation(Emotion.ANNOTATION_FUNCTOR, emotion);
		}
		else{
			logger.warning("Error: Trying to add an invalid emotion to a percept: " + emotion);
			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + emotion);
		}
		
		
		return annot;
	}
	
	public static PerceptAnnotation fromCause(String cause) {
		PerceptAnnotation annot = new PerceptAnnotation();
		annot.setCause(cause);
		return annot;
	}
	
	public PerceptAnnotation() {
		this.annots = new LinkedList<>();
	}
	
	/**
	 * Creates a PerceptAnnotation from a number of emotion strings.
	 * @param ems
	 */
	public PerceptAnnotation(String... ems) {
		this();
		
		for (String em : Arrays.asList(ems)) {
    		if (Emotion.getAllEmotions().contains(em)) {
				this.addAnnotation(Emotion.ANNOTATION_FUNCTOR, em);
    		} else {
				logger.warning("Error: Trying to add an invalid emotion to a percept: " + em);
				throw new RuntimeException("Trying to add an invalid emotion to a percept: " + em);
			}
		}
	}
	
	@Override
	public String toString(){
		String result = "";
		
		if (!this.annots.isEmpty()) {
			result += this.annots.stream().collect(Collectors.joining( ",", "[", "]") );
		}
				
		return result;
	}
	
	public PerceptAnnotation addAnnotation(String functor, String... args) {
		String annot = functor;
		if (args.length > 0 ) {
			annot += "(";
			for (String arg: args) {
				annot += arg;
				annot += ",";
			}
			// delete trailing comma
			annot = annot.substring(0, annot.lastIndexOf(","));
			annot += ")";
		}
		
		this.annots.add(annot);
		return this;
	}
	
	public String separator(String literal1, String literal2) {
		if ((literal1.equals("")) || (literal2.equals("")))
			return "";
		return ",";
	}
	
	public PerceptAnnotation addTargetedEmotion(String emotion, String target) {
    	if (Emotion.getAllEmotions().contains(emotion)) {
			this.addAnnotation(Emotion.ANNOTATION_FUNCTOR, emotion);
			this.addAnnotation("target", target);
		}
		else {
			logger.warning("Error: Trying to add an invalid emotion to a percept: " + emotion);
			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + emotion);
		}
    	
    	return this;
	}
	
	public void setCause(String cause) {
		this.addAnnotation(Edge.Type.CAUSALITY.toString(), TermParser.removeAnnots(cause));
	}
	
	public void addCrossCharAnnotation(String eventName, long eventTime) {
		Integer hashCode = Math.abs((eventName + eventTime).hashCode());	// need to take abs value because toString() puts negative numbers in brackets like "(-5)"
		this.addAnnotation(Edge.Type.CROSSCHARACTER.toString(), hashCode.toString());
	}
}
