package plotmas.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import jason.asSemantics.Emotion;
import jason.asSemantics.Message;
import jason.asSyntax.parser.ParseException;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher.LauncherAgent;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout plotmas for saving plot-relevant
 * events. In order to open a JFrame with the graph call the  non-static 
 * {@link #visualizeGraph(boolean) visualizeGraph} method.
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotGraphController extends JFrame implements PlotmasGraph {
    
	private static PlotGraphController plotListener = null;
	/**
	 * Types of plot graph that can be drawn: [0] full graph, [1] analyzed graph
	 */
	private static final String[] GRAPH_TYPES = new String[] {"full plot graph", "analyzed plot graph"};
	protected static Logger logger = Logger.getLogger(PlotGraphController.class.getName());
	public static Color BGCOLOR = Color.WHITE;


	private PlotDirectedSparseGraph graph = null;			// graph that gets populated by this listener
	protected PlotDirectedSparseGraph drawnGraph = null;	// graph that is currently being drawn
	private JComboBox<String> graphTypeList = new JComboBox<>(GRAPH_TYPES);			// ComboBox that is displayed on the graph to change display type
	private String selectedGraphType = GRAPH_TYPES[0];
	public VisualizationViewer<Vertex, Edge> visViewer = null;
	private GraphZoomScrollPane scrollPane = null; //panel used to display scrolling bars
	
	
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
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to capture new events.
	 * Sets up a subgraphs for each character agent.
	 * @param characters a collection of all acting character agents
	 */
	public PlotGraphController(Collection<LauncherAgent> characters) {
		super("Plot Graph");

		// Set up controls of plot graph
		// Closing this window doesn't stop simulation
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					PlotGraphController.getPlotListener().closeGraph();
		        }
		    }
		);
		
		// create and initialize the plot graph the will be created by this listener
		this.graph = new PlotDirectedSparseGraph();
		// set up a "named" tree for each character
		for (LauncherAgent character : characters) {
			this.addCharacter(character.name);
		}
	}

	public void closeGraph() {
		this.getContentPane().remove(scrollPane);
    	this.dispose();
    	
    	PlotControlsLauncher gui = (PlotControlsLauncher) PlotControlsLauncher.getRunner();
    	gui.graphClosed(this);
	}
	
	public void addCharacter(String agName) {
		Vertex root = new Vertex(agName, Vertex.Type.ROOT);
		graph.addRoot(root);		
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
	
	public Vertex addMsgSend(Message m) {
		Vertex senderV = this.graph.addMsgSend(m.getSender(), m.getPropCont().toString());
		return senderV;
	}

	public Vertex addMsgReceive(Message m, Vertex senderV) {
		Vertex recV = this.graph.addMsgReceive(m.getReceiver(), m.getPropCont().toString(), senderV);
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
		logger.info("Start analzing and compressing the plot graph");
		// For each subgraph, conflate action->perception->emotion vertices into one vertex
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
								if((targetEvent.getWithoutAnnotation().equals(em.getCause())) & !(targetEvent.hasEmotion(em.getName()))) {
									// safe emotion in corresponding action
									targetEvent.addEmotion(em.getName());
									
									// remove emotion vertex
									Vertex lastV = eventList.isEmpty() ? root : eventList.getFirst();
									cleanG.removeVertexAndPatchGraph(v, lastV);
									break;
								}
							}
					}; break;
					default: {
						eventList.addFirst(v);;
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
	public PlotGraphController visualizeGraph(boolean compress) {
		if(compress) {
			this.drawnGraph = this.postProcessThisGraph();
			this.selectedGraphType  = GRAPH_TYPES[1];
		}
		else { 
			this.drawnGraph = this.graph;
			this.selectedGraphType = GRAPH_TYPES[0];
		}
		return this.visualizeGraph();
	}
	
	/**
	 * Plots and displays the graph that is selected by {@code this.drawnGraph}.
	 * @return the displayed PlotGraphController
	 */
	public PlotGraphController visualizeGraph() {
		// Maybe just implement custom renderer instead of all the transformers?
		// https://www.vainolo.com/2011/02/15/learning-jung-3-changing-the-vertexs-shape/
		
		// Tutorial:
		// http://www.grotto-networking.com/JUNG/JUNG2-Tutorial.pdf
		
		Layout<Vertex, Edge> layout = new PlotGraphLayout(this.drawnGraph);
		
		// Create a viewing server
		this.visViewer = new VisualizationViewer<Vertex, Edge>(layout);
		this.visViewer.setPreferredSize(new Dimension(1500, 600)); // Sets the viewing area
		this.visViewer.setBackground(BGCOLOR);
		
		// Add a mouse to translate the graph.
		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new SelectingTranslatingGraphMousePlugin());
		this.visViewer.setGraphMouse(gm);
		
		// modify vertices
		this.visViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		this.visViewer.getRenderContext().setVertexFontTransformer(Transformers.vertexFontTransformer);
		this.visViewer.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		this.visViewer.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexFillPaintTransformer);
		this.visViewer.getRenderContext().setVertexDrawPaintTransformer(Transformers.vertexDrawPaintTransformer);
		this.visViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		// modify edges
		this.visViewer.getRenderContext().setEdgeShapeTransformer(Transformers.edgeShapeTransformer);
		this.visViewer.getRenderContext().setEdgeDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		this.visViewer.getRenderContext().setArrowDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		this.visViewer.getRenderContext().setArrowFillPaintTransformer(Transformers.edgeDrawPaintTransformer);
		this.visViewer.getRenderContext().setEdgeStrokeTransformer(Transformers.edgeStrokeHighlightingTransformer);

		// enable scrolling control bar
		this.scrollPane = new GraphZoomScrollPane(visViewer);

		// set up the combo-box for changing displayed plot graphs: first select the currently shown graph type
		this.graphTypeList.setSelectedItem(this.selectedGraphType);
		
		// second: activate a listener that redraws the plot when selection changes. Careful here: order with 1. matters
		this.graphTypeList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				@SuppressWarnings("unchecked")
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
				String selectedType = (String) combo.getSelectedItem();
				
				if(selectedType.equals(GRAPH_TYPES[0])) {
					Layout<Vertex, Edge> layout = new PlotGraphLayout(PlotGraphController.getPlotListener().graph);
					PlotGraphController.getPlotListener().visViewer.setGraphLayout(layout);
					PlotGraphController.getPlotListener().visViewer.repaint();
				}
				else {
					Layout<Vertex, Edge> layout = new PlotGraphLayout(PlotGraphController.getPlotListener().postProcessThisGraph());
					PlotGraphController.getPlotListener().visViewer.setGraphLayout(layout);
					PlotGraphController.getPlotListener().visViewer.repaint();

				}
			}
		});
		
		this.add(graphTypeList, BorderLayout.NORTH);
		
		this.getContentPane().add(this.scrollPane);
		this.pack();
		
		RefineryUtilities.positionFrameOnScreen(this, 0.0, 0.2);
		
		this.setVisible(true);
		
		return this;
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
		PlotGraphController.instantiatePlotListener(new ArrayList<LauncherAgent>());
		PlotGraphController controller = PlotGraphController.getPlotListener();
		controller.drawnGraph = forest;
		controller.visualizeGraph();
	}
}
