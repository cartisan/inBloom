package inBloom.stories.oedipus;

import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.storyworld.Character;
import inBloom.storyworld.HappeningDirector;
import jason.asSemantics.Personality;


public class OedipusModel extends PlotModel<OedipusEnvironment> {
	
	
	public int actionCount;
	public String location; 
	public int pregnancytimeCount;
	
	public OedipusModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		this.actionCount = 0;
		this.pregnancytimeCount = 0;
	}
	
	
	public ActionReport chilling(Character agent){
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
	
	public ActionReport working(Character agent){
		this.actionCount +=1;
		logger.info("Someone was working");
		
		if(this.actionCount == 20) {
			this.environment.addEventPercept("jocaste", "pregnant");
			this.environment.addEventPercept("laios", "wifePregnant");
			pregnancytimeCount ++;
			
			
			logger.info("jocaste is pregnant");
		}
		
		return new ActionReport(true); 
	}
	
	public ActionReport ask(Character asker, Character asked){
		this.actionCount +=1;
		logger.info(asker.name +" asked "+ asked.name);
		this.environment.addEventPercept(asked.name, "wasAsked");
		
		return new ActionReport(true);
	}
	
	public ActionReport answer_question(Character answerer, Character answered){
		this.actionCount +=1;
		logger.info(answerer.name +" answered "+ answered.name);
		if (answered.name == "laios") {
			this.environment.addEventPercept(answered.name, "sonKillsMe");
			logger.info(answered.name +"getsAnswer sonKillsMe");
		}
		return new ActionReport(true);
	}
	
	
	public ActionReport getChild(Character agent) {
		if(agent.name == "jocaste") {
			this.environment.addEventPercept(agent.name, "gotChild");
			this.actionCount +=1;
			
			this.environment.createAgent("Oedipus", "agent_oedipus.asl", new Personality(0,0,0,0,0));
			logger.info(agent.name + "gave birth to Oedipus");
			
		}
		
		return new ActionReport(true); 
		
	}
	
	public ActionReport giveChildTo(Character giver, Character receiver){
		this.actionCount +=1;
		logger.info(giver.name +" gave "+ "Oedipus " + "to " + receiver.name);
		this.environment.addEventPercept(receiver.name, "guardianOfOedipus");
		logger.info(receiver.name + " is guardian of Oedipus");
		
		return new ActionReport(true);
	}
	
	
	public ActionReport adopt(Character adopter, Character adopted){
		this.actionCount +=1;
		logger.info(adopter.name +" adopted" + adopted.name);
		this.environment.addEventPercept(adopter.name, "parentsOfOedipus");
		this.environment.addEventPercept(adopted.name, "childOfPolybosAndMerope");
		
		return new ActionReport(true);
	}
}
