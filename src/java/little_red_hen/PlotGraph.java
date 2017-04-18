package little_red_hen;

import java.awt.Color;
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
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import little_red_hen.graph.Edge;
import little_red_hen.graph.PlotDirectedSparseMultigraph;
import little_red_hen.graph.Transformers;
import little_red_hen.graph.Vertex;

public class PlotGraph {

	public static Color BGCOLOR = Color.WHITE;
	
	public static Forest<Vertex, Edge> createForest() {
		// Create Trees for each agent and add the roots
		Vertex v1 = new Vertex("hen", Vertex.Type.ROOT); Vertex v2 = new Vertex("dog", Vertex.Type.ROOT); 
		Vertex v3 = new Vertex("cow", Vertex.Type.ROOT); Vertex v4 = new Vertex("cazzegiare"); 
		Vertex v5 = new Vertex("cazzegiare"); Vertex v6 = new Vertex("askHelp(plant(wheat))");
		Vertex v7 = new Vertex("plant(wheat)");
		
		DelegateTree<Vertex, Edge> tree1 = new DelegateTree<Vertex, Edge>();
		tree1.addVertex(v1);

		DelegateTree<Vertex, Edge> tree2 = new DelegateTree<Vertex, Edge>();
		tree2.addVertex(v2);
		
		DelegateTree<Vertex, Edge> tree3 = new DelegateTree<Vertex, Edge>();
		tree3.addVertex(v3);

		// simulate adding vertices later
		
		tree1.addChild(new Edge(Edge.Type.ROOT), v1, v6);
		tree1.addChild(new Edge(), v6, v7);
		tree2.addChild(new Edge(Edge.Type.ROOT), v2, v4);
		tree3.addChild(new Edge(Edge.Type.ROOT), v3, v5);
		tree1.addEdge(new Edge(Edge.Type.COMMUNICATION), v6, v5);
		
		// Set up the forest
		DelegateForest<Vertex, Edge> graphForrest = new DelegateForest<Vertex, Edge>(new PlotDirectedSparseMultigraph());
		graphForrest.addTree(tree1);
		graphForrest.addTree(tree2);
		graphForrest.addTree(tree3);

		System.out.println("The graphForrest gf= " + graphForrest.toString());
		
		return graphForrest;
	}
	
	public static void visualizeGraph(Forest<Vertex, Edge> g) {
		// TODO: Maybe just implement custom renderer instead of all the transformers?
		// https://www.vainolo.com/2011/02/15/learning-jung-3-changing-the-vertexs-shape/
		
		// Tutorial:
		// http://www.grotto-networking.com/JUNG/JUNG2-Tutorial.pdf
		
		Layout<Vertex, Edge> layout = new TreeLayout<Vertex, Edge>(g, 150);
		
		// Create a viewing server
		VisualizationViewer<Vertex, Edge> vv = new VisualizationViewer<Vertex, Edge>(layout);
		vv.setPreferredSize(new Dimension(500, 500)); // Sets the viewing area
//		vv.setOpaque(false);
		vv.setBackground(BGCOLOR);

		// modify vertices
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setVertexFontTransformer(Transformers.vertexFontTransformer);
		vv.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		vv.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexFillPaintTransformer);
		vv.getRenderContext().setVertexDrawPaintTransformer(Transformers.vertexDrawPaintTransformer);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		// modify edges
		vv.getRenderContext().setEdgeShapeTransformer(Transformers.edgeShapeTransformer);
		vv.getRenderContext().setEdgeDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		vv.getRenderContext().setArrowDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		vv.getRenderContext().setArrowFillPaintTransformer(Transformers.edgeDrawPaintTransformer);

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
		Forest<Vertex, Edge> forest = createForest();
		visualizeGraph(forest);
	}
}
