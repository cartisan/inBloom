package oedipusnew;

import java.util.List;

import jason.asSemantics.Personality;
import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
//import plotmas.storyworld.Item;
import plotmas.storyworld.Model;
import plotmas.storyworld.StoryworldAgent;
//import jason.asSemantics.DefaultInternalAction;


public class OedipusModelNew extends Model {
	
	
	public int actionCount;
	public String location; 
	public int pregnancytimeCount;
	
	public OedipusModelNew(List<LauncherAgent> agentList, PlotEnvironment<OedipusModelNew> env) {
		super(agentList, env);
		this.actionCount = 0;
		this.pregnancytimeCount = 0;
	}
	
	
	public boolean chilling(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was chilling");
		
		if(this.actionCount == 20) {
			this.environment.addEventPerception("jocaste", "pregnant");
			this.environment.addEventPerception("laios", "wifePregnant");
			pregnancytimeCount ++;
			
			
			logger.info("jocaste is pregnant");
		}
		
		return agent.relax(); 
		
		
	}
	
	public boolean working(StoryworldAgent agent){
		this.actionCount +=1;
		logger.info("Someone was working");
		
		if(this.actionCount == 20) {
			this.environment.addEventPerception("jocaste", "pregnant");
			this.environment.addEventPerception("laios", "wifePregnant");
			pregnancytimeCount ++;
			
			
			logger.info("jocaste is pregnant");
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
	
	
	public boolean getChild(StoryworldAgent agent) {
		
		if(agent.name == "jocaste") {
			this.environment.addEventPerception(agent.name, "gotChild");
			this.actionCount +=1;
			
			this.environment.createAgent("Oedipus", "agent_oedipusNew.asl", new Personality(0,0,0,0,0));
			logger.info(agent.name + "gave birth to Oedipus");
			
		}
		
		return true; 
		
	}
	
	public boolean giveChildTo(StoryworldAgent giver, StoryworldAgent receiver){
		this.actionCount +=1;
		logger.info(giver.name +" gave "+ "Oedipus " + "to " + receiver.name);
		this.environment.addEventPerception(receiver.name, "guardianOfOedipus");
		logger.info(receiver.name + " is guardian of Oedipus");
		
		return true;
	}
	
	
	public boolean adopt(StoryworldAgent adopter, StoryworldAgent adopted){
		this.actionCount +=1;
		logger.info(adopter.name +" adopted" + adopted.name);
		this.environment.addEventPerception(adopter.name, "parentsOfOedipus");
		this.environment.addEventPerception(adopted.name, "childOfPolybosAndMerope");
		
		return true;
	}


	
	

}
