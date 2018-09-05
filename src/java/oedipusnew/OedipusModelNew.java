package oedipusnew;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Character;

public class OedipusModelNew extends PlotModel<OedipusEnvironmentNew> {
	
	
	public int actionCount;
	public String location; 
	
	public OedipusModelNew(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		this.actionCount = 0;
	}
	
	
	public boolean chilling(Character agent){
		this.actionCount +=1;
		logger.info("Someone was chilling");
		
		
		
		return agent.relax(); 
		
		
	}
	
	public boolean working(Character agent){
		this.actionCount +=1;
		logger.info("Someone was working");
		
		if(this.actionCount == 20){
			this.environment.addEventPerception("jocaste", "pregnant");
			this.environment.addEventPerception("laios", "wifePregnant");
			
			
			logger.info(agent.name + " is pregnant");
		}
		
		return true; 
	}
	
	public boolean ask(Character asker, Character asked){
		this.actionCount +=1;
		logger.info(asker.name +" asked "+ asked.name);
		this.environment.addEventPerception(asked.name, "wasAsked");
		
		return true;
	}
	
	public boolean answer_question(Character answerer, Character answered){
		this.actionCount +=1;
		logger.info(answerer.name +" answered "+ answered.name);
		if (answered.name == "laios") {
			this.environment.addEventPerception(answered.name, "sonKillsMe");
			logger.info(answered.name +"getsAnswer sonKillsMe");
		}
		return true;
	}
	
	public boolean goToPlace(Character agent, String new_location ) {
		this.actionCount +=1;
		location = new_location;
		logger.info(agent.name + " went to" + location);
		//this.environment.updateStatePercepts(agent.name, "wentTo [position(location)]");
		return true;
	}
	
	//public boolean getChild(StoryworldAgent agent, StoryworldAgent patient) {
		//this.environment.addEventPerception(agent.name, "gotChild");
		//this.actionCount +=1;

		//((OedipusEnvironmentNew) this.environment).createAgent();
		//return true; 
		
	//}
	



	
	

}
/**		if	(agent.name == "jocaste"){

create_agent(oedipus,"agent_oedipusNew.asl");
logger.info(agent.name + "gave birth to Oedipus");} **/
/** Agenten haben ein marriedTo, kann null sein oder ein anderer Agent. Muss durch Perceptions an Agents zurück gegeben werden.**/