package little_red_hen.jason;

import jason.architecture.AgArch;
import jason.asSemantics.Message;
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
}
