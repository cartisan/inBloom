package inBloom.storyworld;

import inBloom.helper.PerceptAnnotation;

public class Event {

	public PerceptAnnotation annotation;
	public String percept;
	public String patient;
	public String agent;
	
	public Event() {
		this.annotation = new PerceptAnnotation();
	}
	
	public String getEventPercept(){
		return this.percept + annotation.toString();
	}

	public PerceptAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(PerceptAnnotation annotation) {
		this.annotation = annotation;
	}

	public String getPercept() {
		return percept;
	}

	public void setPercept(String percept) {
		this.percept = percept;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

}
