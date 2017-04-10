package little_red_hen.graph;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

public class PlotGraphLayout<V,E> extends AbstractLayout<V, E> {

	public PlotGraphLayout(Forest<V, E> graph, Dimension size) {
        super(graph, new RandomLocationTransformer<V>(size), size);
    }
	
    public PlotGraphLayout(Forest<V, E> graph)  {
        super(graph);
    }


	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
