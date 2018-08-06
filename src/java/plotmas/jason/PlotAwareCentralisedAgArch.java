package plotmas.jason;

import java.util.List;

import jason.ReceiverNotFoundException;
import jason.asSemantics.Message;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.MsgListener;
import plotmas.PlotLauncher;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;

/**
 * A type of centralised agent architecture that is responsible for maintaining the data that is relevant for plotmas. 
 * It relays the speech acts of the agents to the plot graph to provide inter-character edges. 
 * @author Leonid Berov
 */
public class PlotAwareCentralisedAgArch extends CentralisedAgArch {

    @Override
    public void sendMsg(Message m) throws ReceiverNotFoundException {
        // insert message send into plot graph before message is actually send, 
    	// necessary because super.sendMsd calls receiveMsg and sometimes results in race conditions in plot graph
        int step = PlotLauncher.runner.getUserEnvironment().getStep();
    	Vertex senderV = PlotGraphController.getPlotListener().addMsgSend(m, step);
    	
    	// actually send the message
        if (m.getSender() == null)  m.setSender(getAgName());
        PlotAwareCentralisedAgArch rec = (PlotAwareCentralisedAgArch) PlotLauncher.getRunner().getAg(m.getReceiver());
            
        if (rec == null) {
            if (isRunning())
                throw new ReceiverNotFoundException("Receiver '" + m.getReceiver() + "' does not exist! Could not send " + m);
            else
                return;
        }
        rec.receiveMsg(m.clone(), senderV); // send a cloned message
    
        // notify listeners
        List<MsgListener> listeners = getMsgListeners();
        if (listeners != null) 
            for (MsgListener l: listeners) 
                l.msgSent(m);
    }
    
    public void receiveMsg(Message m, Vertex senderV) {
        int step = PlotLauncher.runner.getUserEnvironment().getStep();
    	PlotGraphController.getPlotListener().addMsgReceive(m, senderV, step);

    	//actually receive the message
        super.receiveMsg(m);
    }
	
}
