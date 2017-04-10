package little_red_hen;

import java.awt.Dimension;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import little_red_hen.graph.Transformers;

public class PlotGraph {

	// TODO: find way of automatically numbering nodes and edges on adding them

	public static Graph<String,String> createForest() {
		// Create Trees for each agent and add the roots
		DelegateTree<String, String> tree1 = new DelegateTree<String, String>();
		tree1.addVertex("hen");

		DelegateTree<String, String> tree2 = new DelegateTree<String, String>();
		tree2.addVertex("dog");
		
		DelegateTree<String, String> tree3 = new DelegateTree<String, String>();
		tree3.addVertex("cow");

		// simulate adding vertices later
		tree1.addChild("1", "hen", "randomFarming");
		tree2.addChild("2", "dog", "cazzegiare");
		tree3.addChild("3", "cow", "cazzegiare2");
		tree1.addEdge("4", "randomFarming", "cazzegiare");
		
		// Set up the forest
		DelegateForest<String, String> graphForrest = new DelegateForest<String, String>();
		graphForrest.addTree(tree1);
		graphForrest.addTree(tree2);
		graphForrest.addTree(tree3);

		System.out.println("The graphForrest gf= " + graphForrest.toString());
		
		return graphForrest;
	}

	public static Graph<String,String> createGraph() {
		DirectedSparseMultigraph<String, String> graph = new DirectedSparseMultigraph<String, String>();
		
		graph.addVertex("hen");
		graph.addVertex("dog");
		graph.addVertex("cow");
		
		// simulate first round of action
		graph.addVertex("randomFarming");
		graph.addEdge("1", "hen", "randomFarming");
		
		graph.addVertex("cazzegiare");
		graph.addEdge("2", "dog", "cazzegiare");
		graph.addEdge("3", "randomFarming", "cazzegiare");
		
		graph.addVertex("cazzegiare2");
		graph.addEdge("4", "cow", "cazzegiare2");
		
		System.out.println("The graph g= " + graph.toString());
		
		return graph;
	}
	
	public static void visualizeGraph(Graph<String, String> g) {
		// TODO: Change color and starting edge for "root" nodes:
		// http://stackoverflow.com/questions/21190701/how-change-color-of-specifics-vertex-in-jung
		
		// TODO: Maybe just implement custom renderer instead of all the transformers?
		// https://www.vainolo.com/2011/02/15/learning-jung-3-changing-the-vertexs-shape/
		
		
		// Initialize Layout
		Layout<String, String> layout = new TreeLayout<String, String>((Forest<String, String>) g, 150);
		
		// Create a viewing server
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
		vv.setPreferredSize(new Dimension(500, 500)); // Sets the viewing area

		// modify vertices
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setVertexFontTransformer(Transformers.vertexFontTransformer);
		vv.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		
		// modify edges
		vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(g));
		

		// Start visualization components
		GraphZoomScrollPane scrollPane= new GraphZoomScrollPane(vv);

		String[] name = g.getClass().toString().split("\\.");
		JFrame frame = new JFrame(name[name.length-1]);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scrollPane);
		frame.pack();
		frame.setVisible(true);	
	}
	
	public static void main(String[] args) {
		Graph<String, String> forest = createForest();
//		Graph<String, String> graph = createGraph();
		
		visualizeGraph(forest);
//		visualizeGraph(graph);
	}
}
