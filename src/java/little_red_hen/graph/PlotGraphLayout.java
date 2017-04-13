package little_red_hen.graph;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

public class PlotGraphLayout<V,E> extends TreeLayout<V, E> {

    public PlotGraphLayout(Forest<V, E> graph)  {
        super(graph);
    }
    

    public PlotGraphLayout(Forest<V, E> graph, int distx)  {
        super(graph, distx);
    }


    @Override
    protected void buildTree() {
    	
    }

}
