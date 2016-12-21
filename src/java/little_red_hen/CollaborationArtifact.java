package little_red_hen;

import cartago.Artifact;
import cartago.GUARD;
import cartago.OPERATION;

public class CollaborationArtifact extends Artifact {
	
	int collaborators;

	void init(){
		collaborators = 0;
	}
	
	@OPERATION 
	void startCollaboration(){
		collaborators+=1;
		log("Number of collaborators is " + String.valueOf(collaborators));
	}
	
	@OPERATION(guard="collaboratorsLeft")
	void stopCollaboration(){
		collaborators-=1;
		log("Number of collaborators is " + String.valueOf(collaborators));
	}
	
	@GUARD boolean collaboratorsLeft(){
		// TODO: Actually we should ensure that agent started a collab before ending
		return collaborators >= 1;
	}
}
