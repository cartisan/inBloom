package plotmas.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.ui.RefineryUtilities;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import jason.asSemantics.Message;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.graph.isomorphism.UnitFinder;
import plotmas.graph.visitor.EdgeLayoutVisitor;
import plotmas.graph.visitor.PostProcessVisitor;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout plotmas for saving plot-relevant
 * events. In order to open a JFrame with the graph call the  non-static 
 * {@link #visualizeGraph(boolean) visualizeGraph} method.
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotGraphController extends JFrame{
    
	private static PlotGraphController plotListener = null;
	/**
	 * Types of plot graph that can be drawn: [0] full graph, [1] analyzed graph
	 */
	private static final String[] GRAPH_TYPES = new String[] {"full plot graph", "analyzed plot graph"};
	protected static Logger logger = Logger.getLogger(PlotGraphController.class.getName());
	public static Color BGCOLOR = Color.WHITE;
	
	private PlotDirectedSparseGraph graph = null;			// graph that gets populated by this listener
	private PlotDirectedSparseGraph analyzedGraph = null;	// analyzed (post-processed) version of graph
	protected PlotDirectedSparseGraph drawnGraph = null;	// graph that is currently being drawn
	private JComboBox<String> graphTypeList = new JComboBox<>(GRAPH_TYPES);			// ComboBox that is displayed on the graph to change display type
	private String selectedGraphType = GRAPH_TYPES[0];
	public VisualizationViewer<Vertex, Edge> visViewer = null;
	private JPanel infoPanel = new JPanel(); // parent of information JLabels
	
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
		        	PlotGraphController.getPlotListener().dispose();
		        }
		    }
		);
		
		// create and initialize the plot graph the will be created by this listener
		this.graph = new PlotDirectedSparseGraph();
		// set up a "named" tree for each character
		for (LauncherAgent character : characters) {
			Vertex root = new Vertex(character.name, Vertex.Type.ROOT);
			graph.addRoot(root);
		}
		
		addInformation("Agents: " + characters.size());
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
	
	public void addInformation(String info) {
		infoPanel.add(new JLabel(info));
		infoPanel.validate();
		infoPanel.repaint();
	}
	
	public void analyze() {
		PlotDirectedSparseGraph g = new PostProcessVisitor().apply(this.graph);

		EdgeLayoutVisitor elv = new EdgeLayoutVisitor(g, 9);
		g.accept(elv);
		
		Map<Vertex, Integer> vertexUnitCount = new HashMap<>();
		
		long start = System.currentTimeMillis();
		UnitFinder finder = new UnitFinder();
		int polyvalentVertices = 0;
		int unitInstances = 0;
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			Set<Map<Vertex, Vertex>> mappings = finder.findUnits(g, unit.getGraph());
			unitInstances += mappings.size();
			for(Map<Vertex, Vertex> map : mappings) {
				for(Vertex v : map.keySet()) {
					if(!vertexUnitCount.containsKey(v)) {
						vertexUnitCount.put(v, 1);
					} else {
						int count = vertexUnitCount.get(v);
						count++;
						if(count == 2) {
							polyvalentVertices++;
						}
						vertexUnitCount.put(v, count);
					}
				}
			}
		}
		long time = System.currentTimeMillis() - start;
		addInformation("Time taken: " + time + "ms");
		addInformation("Units found: " + unitInstances);
		addInformation("Polyvalence: " + polyvalentVertices);
		addInformation("Tellability: " + ((float)polyvalentVertices / (float)g.getVertexCount()));
		this.analyzedGraph = g;
	}
	
	/**
	 * Draws the current state of the plot graph in a JFrame. 
	 * @param compress true if conflated view (more compact by applying {@link #postProcessThisGraph()}) is to be 
	 * 	displayed
	 * @return the displayed JFrame
	 */
	public JFrame visualizeGraph(boolean compress) {
		if(this.analyzedGraph != null) {
			this.drawnGraph = this.analyzedGraph;
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
	 * @return the displayed JFrame
	 */
	protected JFrame visualizeGraph() {
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
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visViewer);

		// c information panel
		infoPanel.setLayout(new FlowLayout(SwingConstants.LEADING, 15, 5));
		
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
					Layout<Vertex, Edge> layout = new PlotGraphLayout(PlotGraphController.getPlotListener().analyzedGraph != null ? PlotGraphController.getPlotListener().analyzedGraph : PlotGraphController.getPlotListener().graph);
					PlotGraphController.getPlotListener().visViewer.setGraphLayout(layout);
					PlotGraphController.getPlotListener().visViewer.repaint();

				}
			}
		});
		
		this.add(graphTypeList, BorderLayout.NORTH);	
		this.add(infoPanel, BorderLayout.SOUTH);
		
		this.getContentPane().add(scrollPane);
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
		Vertex v3 = new Vertex("!achieve(help_with(plant(wheat)))", Vertex.Type.SPEECHACT); Vertex v4 = new Vertex("!help_with(plant(wheat))[(-)]", Vertex.Type.LISTEN); 
		Vertex v5 = new Vertex("!help_with(plant(wheat))", Vertex.Type.INTENTION); Vertex v6 = new Vertex("!tell(reject_request(help_with(plant(wheat))))", Vertex.Type.SPEECHACT);
		Vertex v7 = new Vertex("reject_request(help_with(plant(wheat)))[(-)]", Vertex.Type.LISTEN);
		
		graph.addRoot(v1);
		graph.addRoot(v2);
		
		// simulate adding vertices later
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v3);
		graph.addEdge(new Edge(Edge.Type.ROOT), v2, v4);
		graph.addEdge(new Edge(Edge.Type.TEMPORAL), v3, v7);
		graph.addEdge(new Edge(Edge.Type.COMMUNICATION), v3, v4);
		graph.addEdge(new Edge(Edge.Type.MOTIVATION), v4, v5);
		graph.addEdge(new Edge(Edge.Type.TEMPORAL), v4, v5);
		graph.addEdge(new Edge(Edge.Type.MOTIVATION), v5, v6);
		graph.addEdge(new Edge(Edge.Type.TEMPORAL), v5, v6);
		graph.addEdge(new Edge(Edge.Type.COMMUNICATION), v6, v7);
		graph.addEdge(new Edge(Edge.Type.TERMINATION), v7, v3);
		
		return graph;
	}
	
	public static void main(String[] args) {
		PlotDirectedSparseGraph forest = createTestGraph();

		EdgeLayoutVisitor elv = new EdgeLayoutVisitor(forest, 9);
		forest.accept(elv);

		PlotGraphController.instantiatePlotListener(new ArrayList<LauncherAgent>());
		PlotGraphController controller = PlotGraphController.getPlotListener();
		controller.drawnGraph = forest;
		controller.visualizeGraph();
	}
}
