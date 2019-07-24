package inBloom.stories.oedipus;

import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.storyworld.Character;

public class OedipusEnvironment extends PlotEnvironment<OedipusModel> {

	static Logger logger = Logger.getLogger(OedipusEnvironment.class.getName());

	@Override
    public void initialize(List<LauncherAgent> agents) {
		super.initialize(agents);
	}

	public ActionReport doExecuteAction(String agentName, Structure action) {
		ActionReport result = null;
		Character agent = this.getModel().getCharacter(agentName);

		if (action.getFunctor().equals("chilling")) {
			result = this.getModel().chilling(agent);
		}

		if (action.getFunctor().equals("working")) {
			result = this.getModel().working(agent);
		}

		if (action.getFunctor().equals("answer_question")) {
			Term receiverTerm = action.getTerm(0);

			Character patient = this.getModel().getCharacter(receiverTerm.toString());
			result = this.getModel().answer_question(agent, patient);
		}

		if (action.getFunctor().equals("ask")) {
			Term receiverTerm = action.getTerm(0);

			Character patient = this.getModel().getCharacter(receiverTerm.toString());
			result = this.getModel().ask(agent, patient);
		}

		if (action.getFunctor().equals("getChild")) {
			result = this.getModel().getChild(agent);
		}

		if (action.getFunctor().equals("giveChildTo")) {
			Term receiverTerm = action.getTerm(0);

			Character patient = this.getModel().getCharacter(receiverTerm.toString());
			result = this.getModel().giveChildTo(agent, patient);

		}

		if (action.getFunctor().equals("adopt")) {
			Term receiverTerm = action.getTerm(0);

			Character patient = this.getModel().getCharacter(receiverTerm.toString());
			result = this.getModel().adopt(agent, patient);

		}

		return result;


	}
}
