package plotmas.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import plotmas.PlotLauncher.LauncherAgent;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout plotmas for saving plot-relevant
 * events. 
 * @author Leonid Berov
 */
public class PlotGraph {
    
    static Logger logger = Logger.getLogger(PlotGraph.class.getName());
	private static PlotGraph plotListener = null;
	public static Color BGCOLOR = Color.WHITE;
	private static JFrame frame;


	private PlotDirectedSparseGraph graph; 
	
	
	public static PlotGraph getPlotListener() {
		return plotListener;
	}

	public static void instantiatePlotListener(Collection<LauncherAgent> characters) {
		PlotGraph.plotListener = new PlotGraph(characters);
	}
	
	public static JFrame visualizeGraph(PlotDirectedSparseGraph g) {
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
		        	PlotGraph.frame.dispose();
		        	PlotGraph.frame = null;
		        }
		    }
		);
		
		frame.getContentPane().add(scrollPane);
		frame.pack();
		
		RefineryUtilities.positionFrameOnScreen(frame, 0.0, 0.2);
		
		frame.setVisible(true);
		
		return PlotGraph.frame;
	}
	
	public PlotGraph(Collection<LauncherAgent> characters) {
		this.graph = new PlotDirectedSparseGraph();
		
		// set up a "named" tree for each character
		for (LauncherAgent character : characters) {
			Vertex root = new Vertex(character.name, Vertex.Type.ROOT);
			graph.addRoot(root);
		}
	}
	
	public void addEvent(String character, String event) {
		this.graph.addEvent(character, event, Vertex.Type.EVENT, Edge.Type.TEMPORAL);
	}
	
	public void addEvent(String character, String event, Vertex.Type eventType) {
		this.graph.addEvent(character, event, eventType, Edge.Type.TEMPORAL);
	}
	
	public void addEvent(String character, String event, Edge.Type linkType) {
		this.graph.addEvent(character, event, Vertex.Type.EVENT, linkType);
	}
	
	public void addRequest(Message m) {
		this.graph.addRequest(m.getSender(), m.getReceiver(), m.getPropCont().toString());
	}
	
	private PlotDirectedSparseGraph postProcessThisGraph() {
		// For each subgraph, conflate action->perception->emotion vertices into one vertex
		PlotDirectedSparseGraph cleanG = this.graph.clone();
		
		for(Vertex root : this.graph.getRoots()) {
			Optional<Vertex> lastV = Optional.ofNullable(null);
			int removing = 0;
			Vertex targetEvent = null;
			
			for(Vertex v : this.graph.getCharSubgraph(root)){
				
				// if last emotion we searched was removed, patch the hole produced in cleanG by removing
				if ((removing==0) & !(targetEvent==null)) {
					cleanG.addEdge(new Edge(Edge.Type.TEMPORAL), targetEvent, v);
					targetEvent = null;
				}
				
				switch(v.getType()) {
					case PERCEPT: {
						if(removing>0) {
							throw new RuntimeException(
									"Two perceptions which report the result of actions appeared "
									+ "consequitively, not sure how to compres graph. Culprit: " + v.toString()
									);
						}
						
						//if this perception is just the result of a previous action, remove it and start removal process
						//format: relax(.*)[emotion(.+)+]
						if(lastV.isPresent() &
								(lastV.get().getType() == Vertex.Type.EVENT) &
									lastV.get().getFunctor().equals(v.getFunctor())) {
							
							// find out how many emotions are following
							int emNum = Literal.parseLiteral(v.getLabel()).getAnnots("emotion").size();
							removing = emNum;
							targetEvent = lastV.get();
							
							// remove perception
							cleanG.removeVertex(v);
							
						// otherwise keep it and continue
						} else {
							lastV = Optional.of(v);
						}
					}; break; 
					case EMOTION: {
						if(removing > 0) {
							//don't show post-percept emotion vertices in clean graph, add them to last vertex
							cleanG.removeVertex(v);
							targetEvent.addEmotion(v.getLabel());
							removing -= 1;
						} else {
							lastV = Optional.of(v);
						}
					}; break;
					default: {
						lastV = Optional.of(v);
					}
				}
			}
		}
		
		return cleanG;
	}
	
	public JFrame visualizeGraph() {
		return PlotGraph.visualizeGraph(this.postProcessThisGraph());
//		return PlotGraph.visualizeGraph(this.graph);
	}
	

	/*************************** for testing purposes ***********************************/
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
