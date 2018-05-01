package plotmas.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import jason.asSemantics.Emotion;
import jason.asSemantics.Message;
import jason.asSyntax.parser.ParseException;
import plotmas.PlotLauncher.LauncherAgent;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout plotmas for saving plot-relevant
 * events. In order to open a JFrame with the graph call the  non-static 
 * {@link #visualizeGraph(boolean) visualizeGraph} method.
 * @author Leonid Berov
 */
public class PlotGraphController {
    
    static Logger logger = Logger.getLogger(PlotGraphController.class.getName());
	private static PlotGraphController plotListener = null;
	public static Color BGCOLOR = Color.WHITE;
	private static JFrame frame;


	private PlotDirectedSparseGraph graph; 
	
	
	/**
	 * System-wide method for getting access to the active PlotGraph instance that collects events
	 * and is used for drawing the graph.
	 * @return an instance of PlotGraph
	 */
	public static PlotGraphController getPlotListener() {
		return plotListener;
	}

	/**
	 * Initializes the mapping of plot events using this class by creating an instance and setting up
	 * up a graph with subgraphs for each character.
	 * @param characters a collection of all acting character agents
	 */
	public static void instantiatePlotListener(Collection<LauncherAgent> characters) {
		PlotGraphController.plotListener = new PlotGraphController(characters);
	}
	
	/**
	 * Helper method that allows the plotting of arbitrary instances of plot graphs.
	 * @param g an instance of {@link PlotDirectedSparseGraph} to be drawn and opened in a JFrame
	 * @return
	 */
	private static JFrame visualizeGraph(PlotDirectedSparseGraph g) {
		// Maybe just implement custom renderer instead of all the transformers?
		// https://www.vainolo.com/2011/02/15/learning-jung-3-changing-the-vertexs-shape/
		
		// Tutorial:
		// http://www.grotto-networking.com/JUNG/JUNG2-Tutorial.pdf
		
		Layout<Vertex, Edge> layout = new PlotGraphLayout(g);
		
		
		
		// Create a viewing server
		VisualizationViewer<Vertex, Edge> vv = new VisualizationViewer<Vertex, Edge>(layout);
		vv.setPreferredSize(new Dimension(600, 600)); // Sets the viewing area
		vv.setBackground(BGCOLOR);
		
		// Add a mouse to translate the graph.
		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK));
		vv.setGraphMouse(gm);

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
		PlotGraphController.frame = new JFrame(name[name.length-1]);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	PlotGraphController.frame.dispose();
		        	PlotGraphController.frame = null;
		        }
		    }
		);
		
		frame.getContentPane().add(scrollPane);
		frame.pack();
		
		RefineryUtilities.positionFrameOnScreen(frame, 0.0, 0.2);
		
		frame.setVisible(true);
		
		return PlotGraphController.frame;
	}
	
	/**
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to capture new events.
	 * Sets up a subgraphs for each character agent.
	 * @param characters a collection of all acting character agents
	 */
	public PlotGraphController(Collection<LauncherAgent> characters) {
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
	
	public Vertex addMsgSend(Message m, String motivation) {
		// Format message to intention format, i.e. "!performative(content)"
		Vertex senderV = this.graph.addMsgSend(m.getSender(), "!" + m.getIlForce() + "(" + m.getPropCont().toString() + ")" + motivation);
		return senderV;
	}

	public Vertex addMsgReceive(Message m, Vertex senderV) {
		// Add an "!" to the content if message was an achieve performative
		// "+", to have the percept format, is added in Vertex#toString
		Vertex recV = this.graph.addMsgReceive(m.getReceiver(), (m.getIlForce().startsWith("achieve") ? "!" : "") + m.getPropCont().toString(), senderV);
		return recV;
	}
	
	/**
	 * Clones this.graph and conflates the copy to reduce redundant information. 
	 * Conflation removes from the graph: 
	 *   - perceptions that are reporting the results of an agent action
 	 *   - emotions that are caused by actions or perceptions
 	 * The removed emotions are incorporated into the vertex of causing
 	 * 
	 * @return a clone of this.graph with removed redundant vertices
	 */
	private PlotDirectedSparseGraph postProcessThisGraph() {
		// For each subgraph, conflate action->perception->emotion vertices into one vertex
		// Additionally, create motivation edges between intentions and their motivations
		PlotDirectedSparseGraph cleanG = this.graph.clone();
		
		for(Vertex root : cleanG.getRoots()) {
			LinkedList<Vertex> eventList = new LinkedList<>();

			for(Vertex v : cleanG.getCharSubgraph(root)){
				switch(v.getType()) {
					case PERCEPT: {
						//if this perception is just the result of a previous action, remove it and start removal process
						//format: relax(.*)[emotion(.+)+]
						if(!eventList.isEmpty() &&
								(eventList.getFirst().getType() == Vertex.Type.EVENT) &&
									eventList.getFirst().getFunctor().equals(v.getFunctor())) {
							
							// remove perception from graph
							Vertex lastV = eventList.isEmpty() ? root : eventList.getFirst();
							cleanG.removeVertexAndPatchGraph(v, lastV);
							
						// otherwise keep it and continue
						} else {
							eventList.addFirst(v);
						}
					}; break; 
					case EMOTION: {
							//don't show emotion vertices in clean graph, add them to last causing vertex
							Emotion em;
							try {
								em = Emotion.parseString(v.getLabel());
							} catch (ParseException e) {
								break;
							}
							
							for(Vertex targetEvent:eventList) {
								if((targetEvent.getFunctor().equals(em.getCause())) & !(targetEvent.hasEmotion(em.getName()))) {
									// safe emotion in corresponding action
									targetEvent.addEmotion(em.getName());
									
									// remove emotion vertex
									Vertex lastV = eventList.isEmpty() ? root : eventList.getFirst();
									cleanG.removeVertexAndPatchGraph(v, lastV);
									break;
								}
							}
					}; break;
					case INTENTION:
					case SPEECHACT: {
						// If Source != Self -> Motivated by previous Listen
							// Find last listen with same term
							// Add source to that listen vertex
							// remove this intention vertex and patch graph
						// Else -> Motivated by previous intention / percept
							// Find last vertex with same term
							// add an edge from that vertex to this intention vertex
						
						String label = v.getLabel();
						String[] parts = label.split("\\[motivation\\(");
						
						boolean isVertexRemoved = false;
						
						if(parts.length > 1) {
							String motivation = parts[1].substring(0, parts[1].length() - 2).split("\\[")[0];
							String resultingLabel = parts[0];
							for(Vertex target : eventList) {
								String intention = target.getIntention();
								if(intention.equals(motivation)) {
									cleanG.addEdge(new Edge(Edge.Type.MOTIVATION), target, v);
									v.setMotivation(target);
									v.setLabel(resultingLabel);
								}
							}
						} else {
							// has no motivation, meaning it is the result of a recursion (default_activity -> relax)
							// or it is the result of a listen.
							String[] sourceSplit = label.split("\\[source\\(");
							String source = "";
							if(sourceSplit.length > 1) {
								source = sourceSplit[1].substring(0, sourceSplit[1].length() - 2);
							}
							// look for a previous listen of the same intention and collapse if found
							for(Vertex target : eventList) {
								if(target.getType() == Vertex.Type.LISTEN) {
									if(target.getIntention().equals(v.getIntention())) {
										
										// Update the source on the label.
										if(!source.isEmpty())
											target.setLabel(target.getLabel() + "[source(" + source + ")]");
										
										if(v.toString().contains("disappointment")) {
											target.setLabel(target.getLabel() + "---");
										}
										
										// Remove this intention.
										Vertex lastV = eventList.isEmpty() ? root : eventList.getFirst();
										cleanG.removeVertexAndPatchGraph(v, lastV);
										isVertexRemoved = true;
										break;
									}
								}
							}
						}
						if(!isVertexRemoved) {
							eventList.addFirst(v);
						}
					}; break;
					default: {
						eventList.addFirst(v);
					}
				}
			}
		}
		
		return cleanG;
	}
	
	/**
	 * Draws the current state of the plot graph in a JFrame. 
	 * @param compress true if conflated view (more compact by applying {@link #postProcessThisGraph()}) is to be 
	 * 	displayed
	 * @return the displayed JFrame
	 */
	public JFrame visualizeGraph(boolean compress) {
		if(compress)
			return PlotGraphController.visualizeGraph(this.postProcessThisGraph());
		else 
			return PlotGraphController.visualizeGraph(this.graph);
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
