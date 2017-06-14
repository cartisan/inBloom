package little_red_hen.jason;

import java.util.Iterator;

import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.bb.BeliefBase;
import little_red_hen.PlotGraph;

public class PlotAwareAgArch extends AgArch {

	static int DECAY_RATE = 1;
	
	@Override
    public void sendMsg(Message m) throws Exception {
        // actually send the message
		getNextAgArch().sendMsg(m);
		
		// plot it in the graph
		PlotGraph.getPlotListener().addRequest(m.getSender(), m.getReceiver(), m.getPropCont().toString());
	}
//	
//	@Override
//	public void reasoningCycleStarting() {
//		BeliefBase belief_base = getTS().getAg().getBB();
//		
//		// get names of emotion scales
//		Iterator<Literal> em_scale_it = belief_base.getCandidateBeliefs(Literal.parseLiteral("emotion_scale(X)"),
//															   new Unifier());
//		
//		//for each scale extract literal of form:
//		//   em_scale(Value)[target(_), source(_)]
//		while (em_scale_it.hasNext()) {
//			Literal em_scale_general = em_scale_it.next();			//has no value or annotations attached
//			Literal emotion_term = Literal.parseLiteral(em_scale_general.getTerm(0).toString());
//			
//			Literal em_scale_concr = belief_base.getCandidateBeliefs(emotion_term, new Unifier()).next();
//			int value = Integer.valueOf(em_scale_concr.getTerm(0).toString());
//			
//			if (value > 0) {
//				int new_val = Integer.min(value-DECAY_RATE, 0);
//				em_scale_concr.setTerm(0, ASSyntax.parseNumber(String.valueOf(new_val)));
////				belief_base.remove(em_scale_general);
////				belief_base.add(em_scale_concr);
//			}
//		}
//		
//		super.reasoningCycleStarting();
//	}

}
