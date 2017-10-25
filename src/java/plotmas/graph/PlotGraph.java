package plotmas.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import plotmas.storyworld.StoryworldAgent;

public class PlotGraph {
    
    static Logger logger = Logger.getLogger(PlotGraph.class.getName());
	private static PlotGraph plotListener = null;
	public static boolean isDisplayed = false;
	
	public static PlotGraph getPlotListener() {
		return plotListener;
	}

	public static void instantiatePlotListener(Collection<StoryworldAgent> characters) {
		PlotGraph.plotListener = new PlotGraph(characters);
	}

	public static Color BGCOLOR = Color.WHITE;
	private static JFrame frame;
	
	private HashMap<String, Vertex> lastVertexMap;
	private PlotDirectedSparseGraph graph; 
	
	public PlotGraph(Collection<StoryworldAgent> characters) {
		this.graph = new PlotDirectedSparseGraph();
	    this.lastVertexMap = new HashMap<String, Vertex>();
		
	    // set up a "named" tree for each character
		for (StoryworldAgent character : characters) {
			Vertex root = new Vertex(character.name, Vertex.Type.ROOT);
			graph.addRoot(root);
			
			this.lastVertexMap.put(character.name, root);
		}
	}
	
	public void addEvent(String character, String event) {
		this.addEvent(character, event, Vertex.Type.EVENT, Edge.Type.TEMPORAL);
	}

	public void addEvent(String character, String event, Vertex.Type eventType) {
		this.addEvent(character, event, eventType, Edge.Type.TEMPORAL);
	}
	
	public void addEvent(String character, String event, Edge.Type linkType) {
		this.addEvent(character, event, Vertex.Type.EVENT, linkType);
	}
	
	public void addEvent(String character, String event, Vertex.Type eventType, Edge.Type linkType) {
		Vertex newVertex = new Vertex(event, eventType);
		Vertex parent = lastVertexMap.get(character);
		
		if (parent.getType() == Vertex.Type.ROOT) {
			linkType = Edge.Type.ROOT;
		}
		
		graph.addEdge(new Edge(linkType), parent, newVertex);
		lastVertexMap.put(character, newVertex);
	}
	
	public void addRequest(String sender, String receiver, String message) {
		message = "SPEECH:" + message;
		if (!(lastVertexMap.get(sender).getLabel().equals(message))) {
			// this message is different from content of last event,
			// means was not send to another receiver, too
			addEvent(sender, message, Vertex.Type.SPEECHACT);
		}
		// same message was send before to another recipient 
		// no need to add a new vertex, just reuse the last one
		
		// add receiver vertex linking to last top, same procedure as with sender
		if (!(lastVertexMap.get(receiver).getLabel().equals(""))) {
			addEvent(receiver, "", Vertex.Type.SPEECHACT);
		}
		
		Vertex senderVertex = lastVertexMap.get(sender);
		Vertex receiverVertex = lastVertexMap.get(receiver);
		graph.addEdge(new Edge(Edge.Type.COMMUNICATION), senderVertex, receiverVertex);
	}
	
	public void visualizeGraph() {
		PlotGraph.visualizeGraph(this.graph);
	}
	
	public static void visualizeGraph(PlotDirectedSparseGraph g) {
		// Maybe just implement custom renderer instead of all the transformers?
		// https://www.vainolo.com/2011/02/15/learning-jung-3-changing-the-vertexs-shape/
		
		// Tutorial:
		// http://www.grotto-networking.com/JUNG/JUNG2-Tutorial.pdf
		
		Layout<Vertex, Edge> layout = new PlotGraphLayout(g);
		
		// Create a viewing server
		VisualizationViewer<Vertex, Edge> vv = new VisualizationViewer<Vertex, Edge>(layout);
		vv.setPreferredSize(new Dimension(600, 600)); // Sets the viewing area
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
		PlotGraph.frame = new JFrame(name[name.length-1]);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	PlotGraph.isDisplayed = false;
		        	PlotGraph.frame.dispose();
		        	PlotGraph.frame = null;
		        }
		    }
		);
		
		frame.getContentPane().add(scrollPane);
		frame.pack();
		frame.setVisible(true);
		PlotGraph.isDisplayed = true;
	}
	
	private static PlotDirectedSparseGraph createTestGraph() {
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		
		// Create Trees for each agent and add the roots
		Vertex v1 = new Vertex("hen", Vertex.Type.ROOT); Vertex v2 = new Vertex("dog", Vertex.Type.ROOT); 
		Vertex v3 = new Vertex("cow", Vertex.Type.ROOT); Vertex v4 = new Vertex("cazzegiare"); 
		Vertex v5 = new Vertex("cazzegiare"); Vertex v6 = new Vertex("askHelp(plant(wheat))");
		Vertex v7 = new Vertex("plant(wheat)");
		
		graph.addRoot(v1);
		graph.addRoot(v2);
		graph.addRoot(v3);
		
		// simulate adding vertices later
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v6);
		graph.addEdge(new Edge(), v6, v7);
		graph.addEdge(new Edge(Edge.Type.ROOT), v2, v4);
		graph.addEdge(new Edge(Edge.Type.ROOT), v3, v5);
		graph.addEdge(new Edge(Edge.Type.COMMUNICATION), v6, v5);
		
		return graph;
	}
	
	public static void main(String[] args) {
		PlotDirectedSparseGraph forest = createTestGraph();
		visualizeGraph(forest);
	}
}
