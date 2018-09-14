package plotmas.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freehep.graphicsbase.util.export.ExportDialog;
import org.jfree.ui.RefineryUtilities;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import jason.asSemantics.Message;
import plotmas.LauncherAgent;
import plotmas.PlotControlsLauncher;
import plotmas.PlotLauncher;
import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.graph.visitor.EdgeLayoutVisitor;
import plotmas.helper.Tellability;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout plotmas for saving plot-relevant
 * events. In order to open a JFrame with the graph call the  non-static 
 * {@link #visualizeGraph(boolean) visualizeGraph} method.
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotGraphController extends JFrame implements PlotmasGraph, ActionListener {
    
	protected static Logger logger = Logger.getLogger(PlotGraphController.class.getName());
    
	/** Singleton instance used to collect the plot */
	private static PlotGraphController plotListener = null;

	public static Color BGCOLOR = Color.WHITE;

	/** Save action command. */
	public static final String SAVE_COMMAND = "SAVE";
	/** Change plot view action command. */
    public static final String CHANGE_VIEW_COMMAND = "CHANGE_VIEW";
	
	private PlotDirectedSparseGraph graph = null;			// graph that gets populated by this listener
	private JComboBox<PlotDirectedSparseGraph> graphTypeList = new JComboBox<>();	// ComboBox that is displayed on the graph to change display type
	public VisualizationViewer<Vertex, Edge> visViewer = null;
	private JPanel infoPanel = new JPanel(); // parent of information JLabels
	private GraphZoomScrollPane scrollPane = null; //panel used to display scrolling bars
	private JPopupMenu popup = null;	
	private Tellability analysisResult = null;
	private JComboBox<FunctionalUnit> unitComboBox = null;

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
	 * Creates a PlotGraphController instance that can be used to display graph.
	 * <b>Attention</b>: Overwrites the plotListener singleton, so that all future plot events
	 * will be directed to this very graph.
	 * @param graph Graph to be displayed
	 * @return the new PlotGraphListener instance
	 */
	public static PlotGraphController fromGraph(PlotDirectedSparseGraph graph) {
		PlotGraphController.plotListener = new PlotGraphController(graph);
		return PlotGraphController.plotListener;
	}
	
	/**
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to capture new events.
	 * Sets up a subgraphs for each character agent.
	 * @param characters a collection of all acting character agents
	 */
	public PlotGraphController(Collection<LauncherAgent> characters) {
		super("Plot Graph");
		
		// create and initialize the plot graph the will be created by this listener
		this.graph = new PlotDirectedSparseGraph();
		this.graph.setName("Full Plot Graph");
		
		// set up a "named" tree for each character
		for (LauncherAgent character : characters) {
			this.addCharacter(character.name);
		}

		setUp();
		this.addGraph(this.graph);
		this.graphTypeList.setSelectedItem(this.graph);
	}

	/**
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to display a graph.
	 * @param graph Graph to be displayed
	 */
	public PlotGraphController(PlotDirectedSparseGraph graph) {
		super("Plot Graph");

		// create and initialize the plot graph the will be created by this listener
		this.graph = graph;
		
		setUp();
		addGraph(this.graph);
		graphTypeList.setSelectedItem(this.graph);
	}

	/**
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to aggregate a set of graphs
	 * which can be displayed later.
	 */
	public PlotGraphController() {
		super("Plot Graph");
		setUp();
		PlotGraphController.plotListener = this;
	}
	
	/**
	 * Sets up an instance of this class, after {@code this.graph} has been set in the constructor.
	 */
	private void setUp() {
		// Set up controls of plot graph
		// Closing this window doesn't stop simulation
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				PlotGraphController.getPlotListener().closeGraph();
			}
		}
				);
		
		this.createPopupMenu();
		
		// Initialize functional unit combo box
		this.unitComboBox = new JComboBox<FunctionalUnit>();
		this.unitComboBox.addItem(null);
		this.unitComboBox.setSelectedItem(null);
		this.unitComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				@SuppressWarnings("unchecked")
				JComboBox<FunctionalUnit> combo = (JComboBox<FunctionalUnit>) event.getSource();
				FunctionalUnit selectedUnit = (FunctionalUnit) combo.getSelectedItem();
				if(selectedUnit == null) {
					Transformers.HIGHLIGHT = null;
				} else {
					Transformers.HIGHLIGHT = ((PlotDirectedSparseGraph)graphTypeList.getSelectedItem()).getUnitVertices(selectedUnit);
				}
				PlotGraphController.getPlotListener().visViewer.repaint();
			}
		});
		
		addGraph(FunctionalUnits.ALL_UNITS_GRAPH);
	}
	
    /**
     * Initializes the a popup menu that will appear on left-click.
     */
    protected void createPopupMenu() {
        JPopupMenu result = new JPopupMenu("PlotGraph");

        JMenuItem pngItem = new JMenuItem("Save Graph");
        pngItem.setActionCommand(SAVE_COMMAND);
        pngItem.addActionListener(this);

        result.add(pngItem);

        this.popup = result;
    }
    
	/**
	 * Method which allows this to registered as an ActionListener. Performs the handling of all events
	 * that result from interactions with UI elements of this JFrame. 
	 * @param event Event that specifies how to react
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        
        if (command.equals(SAVE_COMMAND)) {
            try {
                doSaveAs();
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "I/O error occurred.", 
                        "Save Graph", JOptionPane.WARNING_MESSAGE);
            }
        } else if (command.equals(CHANGE_VIEW_COMMAND)) {
			@SuppressWarnings("unchecked")
			JComboBox<PlotDirectedSparseGraph> combo = (JComboBox<PlotDirectedSparseGraph>) event.getSource();
			PlotDirectedSparseGraph selectedGraph = (PlotDirectedSparseGraph) combo.getSelectedItem();
			
			Layout<Vertex, Edge> layout = new PlotGraphLayout(selectedGraph);
			PlotGraphController.getPlotListener().visViewer.setGraphLayout(layout);
			PlotGraphController.getPlotListener().visViewer.repaint();
        }
	}
	
	/**
	 * Saves currently displayed plot graph as PNG image. Displays a FileChoose to select name and target dir.
	 * @throws IOException
	 */
	private void doSaveAs() throws IOException {
        // instantiate and configure image-able visualization viewer
        VisualizationImageServer<Vertex, Edge> vis =
        	    new VisualizationImageServer<Vertex, Edge>(this.visViewer.getGraphLayout(),
        	    										   this.visViewer.getGraphLayout().getSize());

        setUpAppearance(vis);

        ExportDialog export = new ExportDialog();
        export.showExportDialog(vis, "Export view as ...", vis, "export");
	}

	public JPopupMenu getPopup() {
		return this.popup;
	}
	
	public JComboBox<FunctionalUnit> getUnitComboBox() {
		return unitComboBox;
	}

	public void closeGraph() {
		logger.info("Closing and reseting plot graph view");
		
		this.getContentPane().remove(scrollPane);
    	this.dispose();
    	
    	PlotControlsLauncher gui = PlotLauncher.getRunner();
    	gui.graphClosed(this);
	}
	
	public void addCharacter(String agName) {
		this.graph.addRoot(agName);		
	}	
	
	public void addEvent(String character, String event, Vertex.Type eventType, int step) {
		this.graph.addEvent(character, event, step, eventType, Edge.Type.TEMPORAL);
	}
	
	public Vertex addMsgSend(Message m, String motivation, int step) {
		// Format message to intention format, i.e. "!performative(content)"
		Vertex senderV = this.graph.addMsgSend(m.getSender(), "!" + m.getIlForce() + "(" + m.getPropCont().toString() + ")" + motivation, step);
		return senderV;
	}

	public Vertex addMsgReceive(Message m, Vertex senderV, int step) {
		// Add an "!" to the content if message was an achieve performative
		// "+", to have the percept format, is added in Vertex#toString
		Vertex recV = this.graph.addMsgReceive(m.getReceiver(), (m.getIlForce().startsWith("achieve") ? "!" : "") + m.getPropCont().toString(), senderV, step);
		return recV;
	}
	
	/**
	 * Adds an information label to the bottom of the graph window.
	 * @param info Information string to display
	 */
	public void addInformation(String info) {
		infoPanel.add(new JLabel(info));
		infoPanel.validate();
		infoPanel.repaint();
	}
	
	public void addInformation(String label, JComponent component) {
		infoPanel.add(new JLabel(label));
		infoPanel.add(component);
		infoPanel.validate();
		infoPanel.repaint();
	}
	
	/**
	 * Adds a graph to the graph type list.
	 * If a graph with the same name is already in the list,
	 * the new one will replace it.
	 * @param g Graph to add
	 */
	public void addGraph(PlotDirectedSparseGraph g) {
		for(int i = 0; i < graphTypeList.getItemCount(); i++) {
			String n = graphTypeList.getItemAt(i).toString();
			if(n.equals(g.toString())) {
				graphTypeList.removeItemAt(i);
				graphTypeList.addItem(g);
				return;
			}
		}
		graphTypeList.addItem(g);
		graphTypeList.repaint();
	}
	
	/**
	 * Adds a functional unit to the drop down menu for highlighting units in plot graph
	 * @param unit
	 */
	public void addDetectedPlotUnitType(FunctionalUnit unit) {
		this.unitComboBox.addItem(unit);
	}
	
	/**
	 * Uses the combobox graphTypeList to select graph g. Results in {@linkplain #visualizeGraph} showing this graph.
	 * @param g
	 */
	public void setSelectedGraph(PlotDirectedSparseGraph g) { 
		graphTypeList.setSelectedItem(g);
	}
	
	/**
	 * Analyzes the plot graph, computes the plots tellability and returns it.
	 * Unlike {@link analyze(PlotDirectedSparseGraph) analyze}, does not store the
	 * analyzed version of the graph for further processing.
	 */
	public Tellability analyze() {
		return analyze(null);
	}
	
	/**
	 * Analyzes the plot graph, computes the plots tellability and returns it.
	 * <ul>
	 *  <li> Analyzing a plot graph includes merging related vertices and specifying the edge types from mere temporal to
	 * ones with more appropriate semantics so all primitive plot units can be represented. The resulting <b> new plot
	 * graph is stored in analyzedGraphContainer </b> for displaying and further analyzes e.g. by the ER cycle.</li>
	 *  <li> Computing the tellability atm includes just computing functional polyvalence and dispalying the results
	 *  in the info panel. </li>
	 *  </ul>
	 * @param analyzedGraphContainer an (empty) plot graph that will be used to store the analyzed graph, or null
	 * @return
	 */
	public Tellability analyze(PlotDirectedSparseGraph analyzedGraphContainer) {
		if(analysisResult != null) {
			return analysisResult;
		}
		
		// Create analysed graph with semantically interpretable edges and collapsed vertices
		PlotDirectedSparseGraph g = new FullGraphPPVisitor().apply(this.graph);
		g.setName("Analyzed Plot Graph");
		g.accept(new CompactGraphPPVisitor(g));

		// compute all necessary statictics for tellability
		analysisResult = new Tellability(g);
		
		// Create GUI representation of tellability analysis
		addInformation("#Functional Units: " + this.analysisResult.numFunctionalUnits);
		addInformation("Highlight Units:");
		this.infoPanel.add(unitComboBox);
		addInformation("#Polyvalent Vertices: " + this.analysisResult.numPolyvalentVertices);
		addInformation("Suspense: " + this.analysisResult.suspense);
		addInformation("Tellability: " + this.analysisResult.compute());
		
		// Insert spacing between motivation edges
		g.accept(new EdgeLayoutVisitor(g, 9));
		
		this.addGraph(g);
		this.graphTypeList.setSelectedItem(g);
		
		if(analyzedGraphContainer != null) {
			g.cloneInto(analyzedGraphContainer);
		}
		
		return analysisResult;
	}
	
	/**
	 * Plots and displays the graph that is selected by {@code this.graphTypeList}.
	 * @return the displayed JFrame
	 */
	public PlotGraphController visualizeGraph() {
		Layout<Vertex, Edge> layout = new PlotGraphLayout((PlotDirectedSparseGraph)this.graphTypeList.getSelectedItem());
		
		// Create a viewing server
		this.visViewer = new VisualizationViewer<Vertex, Edge>(layout);
		this.setUpAppearance(visViewer);
		
		// Add a mouse to translate the graph.
		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new SelectingTranslatingGraphMousePlugin());
		this.visViewer.setGraphMouse(gm);

		// enable scrolling control bar
		this.scrollPane = new GraphZoomScrollPane(visViewer);

		// c information panel
		infoPanel.setLayout(new FlowLayout(SwingConstants.LEADING, 15, 5));
		
		// second: register a listener that redraws the plot when selection changes. Careful here: order with last command matters
		this.graphTypeList.setActionCommand(CHANGE_VIEW_COMMAND);
		this.graphTypeList.addActionListener(this);
		this.add(graphTypeList, BorderLayout.NORTH);
		
		this.add(graphTypeList, BorderLayout.NORTH);	
		this.add(infoPanel, BorderLayout.SOUTH);
		
		this.getContentPane().add(this.scrollPane);
		this.pack();
		
		RefineryUtilities.positionFrameOnScreen(this, 0.0, 0.2);
		
		this.setVisible(true);
		
		return this;
	}
	

	/**
	 * Sets up an VisualizationServer instance with all the details and renders defining the graphs appearance. 
	 * @param vis
	 */
	private void setUpAppearance(BasicVisualizationServer<Vertex, Edge> vis) {
		vis.setBackground(BGCOLOR);
		vis.setPreferredSize(new Dimension(1500, 600)); // Sets the viewing area
		
		// modify vertices
		vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vis.getRenderContext().setVertexFontTransformer(Transformers.vertexFontTransformer);
		vis.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		vis.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexFillPaintTransformer);
		vis.getRenderContext().setVertexDrawPaintTransformer(Transformers.vertexDrawPaintTransformer);
		vis.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		// modify edges
		vis.getRenderContext().setEdgeShapeTransformer(Transformers.edgeShapeTransformer);
		vis.getRenderContext().setEdgeDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		vis.getRenderContext().setArrowDrawPaintTransformer(Transformers.edgeDrawPaintTransformer);
		vis.getRenderContext().setArrowFillPaintTransformer(Transformers.edgeDrawPaintTransformer);
		vis.getRenderContext().setEdgeArrowPredicate(Transformers.edgeArrowPredicate);
		vis.getRenderContext().setEdgeStrokeTransformer(Transformers.edgeStrokeHighlightingTransformer);
	}
}
