package oedipusnew;

import java.util.List;

import jason.asSemantics.Personality;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Item;
import plotmas.storyworld.Model;
import plotmas.storyworld.StoryworldAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.stdlib.create_agent;

public class OedipusModelNew extends Model {
	
	
	public int actionCount;
	public String location; 
	
	public OedipusModelNew(List<LauncherAgent> agentList, PlotEnvironment<OedipusModelNew> env) {
		super(agentList, env);
		this.actionCount = 0;
	}
	
	
	public boolean chilling(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was chilling");
		
		
		
		return agent.relax(); 
		
		
	}
	
	public boolean working(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was working");
		
		if(this.actionCount == 20){
			this.environment.addEventPerception("jocaste", "pregnant");
			this.environment.addEventPerception("laios", "wifePregnant");
			
			
			logger.info(agent.name + " is pregnant");
		}
		
		return true; 
	}
	
	public boolean ask(StoryworldAgent asker, StoryworldAgent asked){
		this.actionCount +=1;
		logger.info(asker.name +" asked "+ asked.name);
		this.environment.addEventPerception(asked.name, "wasAsked");
		
		return true;
	}
	
	public boolean answer_question(StoryworldAgent answerer, StoryworldAgent answered){
		this.actionCount +=1;
		logger.info(answerer.name +" answered "+ answered.name);
		if (answered.name == "laios") {
			this.environment.addEventPerception(answered.name, "sonKillsMe");
			logger.info(answered.name +"getsAnswer sonKillsMe");
		}
		return true;
	}
	
	public boolean goToPlace(StoryworldAgent agent, String new_location ) {
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
