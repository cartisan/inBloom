package plotmas.stories.oedipus;

import java.util.List;

import jason.asSemantics.Personality;
import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.Character;
//import jason.asSemantics.DefaultInternalAction;
import plotmas.storyworld.HappeningDirector;


public class OedipusModel extends PlotModel<OedipusEnvironment> {
	
	
	public int actionCount;
	public String location; 
	public int pregnancytimeCount;
	
	public OedipusModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		this.actionCount = 0;
		this.pregnancytimeCount = 0;
	}
	
	
	public boolean chilling(Character agent){
		this.actionCount +=1;
		logger.info("Someone was chilling");
		
		if(this.actionCount == 20) {
			this.environment.addEventPercept("jocaste", "pregnant");
			this.environment.addEventPercept("laios", "wifePregnant");
			pregnancytimeCount ++;
			
			
			logger.info("jocaste is pregnant");
		}
		
		return agent.relax(); 
		
		
	}
	
	public boolean working(Character agent){
		this.actionCount +=1;
		logger.info("Someone was working");
		
		if(this.actionCount == 20) {
			this.environment.addEventPercept("jocaste", "pregnant");
			this.environment.addEventPercept("laios", "wifePregnant");
			pregnancytimeCount ++;
			
			
			logger.info("jocaste is pregnant");
		}
		
		return true; 
	}
	
	public boolean ask(Character asker, Character asked){
		this.actionCount +=1;
		logger.info(asker.name +" asked "+ asked.name);
		this.environment.addEventPercept(asked.name, "wasAsked");
		
		return true;
	}
	
	public boolean answer_question(Character answerer, Character answered){
		this.actionCount +=1;
		logger.info(answerer.name +" answered "+ answered.name);
		if (answered.name == "laios") {
			this.environment.addEventPercept(answered.name, "sonKillsMe");
			logger.info(answered.name +"getsAnswer sonKillsMe");
		}
		return true;
	}
	
	
	public boolean getChild(Character agent) {
		
		if(agent.name == "jocaste") {
			this.environment.addEventPercept(agent.name, "gotChild");
			this.actionCount +=1;
			
			this.environment.createAgent("Oedipus", "agent_oedipus.asl", new Personality(0,0,0,0,0));
			logger.info(agent.name + "gave birth to Oedipus");
			
		}
		
		return true; 
		
	}
	
	public boolean giveChildTo(Character giver, Character receiver){
		this.actionCount +=1;
		logger.info(giver.name +" gave "+ "Oedipus " + "to " + receiver.name);
		this.environment.addEventPercept(receiver.name, "guardianOfOedipus");
		logger.info(receiver.name + " is guardian of Oedipus");
		
		return true;
	}
	
	
	public boolean adopt(Character adopter, Character adopted){
		this.actionCount +=1;
		logger.info(adopter.name +" adopted" + adopted.name);
		this.environment.addEventPercept(adopter.name, "parentsOfOedipus");
		this.environment.addEventPercept(adopted.name, "childOfPolybosAndMerope");
		
		return true;
	}


	
	

}
