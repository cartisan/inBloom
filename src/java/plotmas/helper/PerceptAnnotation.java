package plotmas.helper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Emotion;
import plotmas.graph.Edge;

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
		annot.addAnnotation(Edge.Type.CAUSALITY.toString(), cause);
		return annot;
	}
	
	public PerceptAnnotation() {
		this.annots = new LinkedList<>();
	}
	
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
			result += "[";
			result += this.annots.stream().collect( Collectors.joining( "," ) );
			result.substring(0, result.length());
			result += "]";
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
		this.addAnnotation(Edge.Type.CAUSALITY.toString(), cause);
	}
}
