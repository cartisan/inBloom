package little_red_hen;

import java.awt.Dimension;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

public class PlotGraph {
	
	public static Graph<String, String> graph = new SparseMultigraph<String, String>();
	
	// TODO: find way of automatically numbering nodes and edges on adding them
	
	public static void main(String[] args) {
		// Add some vertices
		 graph.addVertex("hen");
		 graph.addVertex("dog");
		 graph.addVertex("randomFarming");
		 
		 // Add some edges
		 graph.addEdge("1", "hen", "randomFarming", EdgeType.DIRECTED);
		 
		 System.out.println("The graph g= " + graph.toString()); 
		 
		 // visualize stuff 
		 Layout<String, String> layout = new DAGLayout<String, String>(graph);
		 layout.setSize(new Dimension(300,300));
		 
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 BasicVisualizationServer<String,String> vv = new BasicVisualizationServer<String,String>(layout);
		 vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
		 vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		 
		 vv.getRenderer().getVertexLabelRenderer().setPositioner(new BasicVertexLabelRenderer.OutsidePositioner());
		 vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));

		 JFrame frame = new JFrame("Plot Graph");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true); 
	}
}
