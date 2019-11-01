package inBloom.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JButton;
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

import com.google.common.collect.HashMultimap;

import jason.asSemantics.Message;

import inBloom.LauncherAgent;
import inBloom.PlotControlsLauncher;
import inBloom.PlotLauncher;
import inBloom.ERcycle.CounterfactualityCycle;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnit.Instance;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.visitor.EdgeLayoutVisitor;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Responsible for maintaining and visualizing the graph that represents the emergent plot of the narrative universe.
 * Class provides an instance: <i>plotListener</i>, which is accessible throughout inBloom for saving plot-relevant
 * events. In order to open a JFrame with the graph call the  non-static
 * {@link #visualizeGraph(boolean) visualizeGraph} method.
 * @author Leonid Berov
 */
@SuppressWarnings("serial")
public class PlotGraphController extends JFrame implements PlotmasGraph, ActionListener {

	protected static Logger logger = Logger.getLogger(PlotGraphController.class.getName());

	/** Save action command. */
	public static final String SAVE_COMMAND = "SAVE";
	/** Change plot view action command. */
	public static final String CHANGE_VIEW_COMMAND = "CHANGE_VIEW";

	/** Singleton instance used to collect the plot */
	private static PlotGraphController plotListener = null;

	public static Color BGCOLOR = Color.WHITE;

	/** Saves which vertices are to be highlighted, because they belong to the same FU type
	 * maps from vertex to set of FU Instances of which this vertex is part of */
	static public HashMultimap<Vertex, Integer> HIGHLIGHTED_VERTICES = HashMultimap.create();

	private PlotDirectedSparseGraph graph = null;			// graph that gets populated by this listener
	private JComboBox<PlotDirectedSparseGraph> graphTypeList = new JComboBox<>();	// ComboBox that is displayed on the graph to change display type
	public VisualizationViewer<Vertex, Edge> visViewer = null;
	private JButton counterfactButton;
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
	 * Opens a window visualizing graph, creates roots and steps if necessary. For debugging purposes.
	 * @param graph graph to be displayed
	 * @param plotLayout whether the inBloom plot layout should be used, or a jung default-layout
	 * @param the step numbers of the vertices that are to be used as first vertex in each char subgraph, i.e. to be connected with the roots
	 */
	public static void visualize(PlotDirectedSparseGraph graph, boolean plotLayout, Integer... startSteps) {
		FunctionalUnit display = new FunctionalUnit(graph.getName(), graph, startSteps);
		PlotGraphController vis =  PlotGraphController.fromGraph(display.getDisplayGraph());
		if (plotLayout) {
			vis.visualizeGraph();
		} else {
			vis.visualizeGraph(new CircleLayout<>(graph));
		}
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

		this.setUp();
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

		this.setUp();
		this.addGraph(this.graph);
		this.graphTypeList.setSelectedItem(this.graph);
	}

	/**
	 * Creates a new instance of {@link PlotDirectedSparseGraph}, which is used to aggregate a set of graphs
	 * which can be displayed later.
	 */
	public PlotGraphController() {
		super("Plot Graph");
		this.setUp();
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
		this.unitComboBox = new JComboBox<>();
		this.unitComboBox.addItem(null);
		this.unitComboBox.setSelectedItem(null);
		this.unitComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				@SuppressWarnings("unchecked")
				JComboBox<FunctionalUnit> combo = (JComboBox<FunctionalUnit>) event.getSource();
				PlotGraphController.HIGHLIGHTED_VERTICES.clear();
				SelectingTranslatingGraphMousePlugin.PICKED_INSTANCES = null;

				FunctionalUnit selectedUnit = (FunctionalUnit) combo.getSelectedItem();
				int instanceNum = 0;
				for (Instance i : ((PlotDirectedSparseGraph) PlotGraphController.this.graphTypeList.getSelectedItem()).getFUInstances(selectedUnit)) {
					++instanceNum;
					for (Vertex v : i.getVertices()) {
						PlotGraphController.HIGHLIGHTED_VERTICES.put(v, instanceNum);
					}
				}
				PlotGraphController.getPlotListener().visViewer.repaint();
			}
		});

		this.addGraph(FunctionalUnits.ALL_UNITS_GRAPH);
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
     * Adds a Button to the menu that will switch from Counterfactuality
     * to originial and the other way around
     *
     * Can be extended by different buttons and a createButtons method
     */

    protected void createCounterfactButton() {
    	if (this.counterfactButton == null) {
	    	JButton btCounterfact = new JButton("Create counterfactual");
	    	btCounterfact.addActionListener(new ActionListener() {
				//boolean for first time -> calculating
	    		private boolean firstClick = true;
	   			//distinguishing: counterfactual click or original click?
	    		private boolean counterfact = true;
	    		public void actionPerformed(ActionEvent click) {

	    			PlotDirectedSparseGraph originalGraph = new PlotDirectedSparseGraph();
	    			if(this.firstClick) {

	    				//getting the current graph and give it to the CounterfactualityLauncher
	        			originalGraph = PlotGraphController.getPlotListener().getGraph();
	        			MoodMapper moodData = PlotControlsLauncher.runner.getUserModel().moodMapper;

	        			// get counterfactuality class
	        			CounterfactualityCycle counterfact;
	        			try {
	        				counterfact = (CounterfactualityCycle) PlotLauncher.getRunner().COUNTERFACT_CLASS.getConstructors()[0].newInstance(originalGraph, moodData);
	        				counterfact.run();
	        			} catch (Exception e) {
							System.err.println("Error instantiating counterfactuality class");
							System.exit(0);
						}

	          			//set firstClick false
	        			this.firstClick = false;
	        			logger.info("The first click was done!");
	    			}

	    			this.counterfact = !this.counterfact;
	    		}
	    	});
	    	this.counterfactButton = btCounterfact;
    	}
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
                this.doSaveAs();
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
        	    new VisualizationImageServer<>(this.visViewer.getGraphLayout(),
        	    										   this.visViewer.getGraphLayout().getSize());

        this.setUpAppearance(vis);

        ExportDialog export = new ExportDialog();
        export.showExportDialog(vis, "Export view as ...", vis, "export");
	}

	public JPopupMenu getPopup() {
		return this.popup;
	}

	public JComboBox<FunctionalUnit> getUnitComboBox() {
		return this.unitComboBox;
	}

	public Tellability getAnalysisResult() {
		return this.analysisResult;
	}

	public PlotDirectedSparseGraph getGraph() {
		return this.graph;
	}

	public void closeGraph() {
		logger.info("Closing and reseting plot graph view");

		this.getContentPane().remove(this.scrollPane);
    	this.dispose();
    	this.visViewer = null;

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
		this.infoPanel.add(new JLabel(info));
		this.infoPanel.validate();
		this.infoPanel.repaint();
	}

	public void addInformation(String label, JComponent component) {
		this.infoPanel.add(new JLabel(label));
		this.infoPanel.add(component);
		this.infoPanel.validate();
		this.infoPanel.repaint();
	}

	/**
	 * Adds a graph to the graph type list.
	 * If a graph with the same name is already in the list,
	 * the new one will replace it.
	 * @param g Graph to add
	 */
	public void addGraph(PlotDirectedSparseGraph g) {
		for(int i = 0; i < this.graphTypeList.getItemCount(); i++) {
			String n = this.graphTypeList.getItemAt(i).toString();
			if(n.equals(g.toString())) {
				this.graphTypeList.removeItemAt(i);
				this.graphTypeList.addItem(g);
				return;
			}
		}
		this.graphTypeList.addItem(g);
		this.graphTypeList.repaint();
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
		this.graphTypeList.setSelectedItem(g);
	}

	/**
	 * Analyzes the plot graph, computes the plots tellability and returns it.
	 * Unlike {@link analyze(PlotDirectedSparseGraph) analyze}, does not store the
	 * analyzed version of the graph for further processing.
	 */
	public Tellability analyze() {
		return this.analyze(null);
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
		Instant start = Instant.now();

		if(this.analysisResult != null) {
			return this.analysisResult;
		}

		// Create analysed graph with semantically interpretable edges and collapsed vertices
		PlotDirectedSparseGraph g = new VertexMergingPPVisitor().apply(this.graph);
		g = new EdgeGenerationPPVisitor().apply(g);
		g = new VisualizationFilterPPVisitor().apply(g);
		g.setName("Analyzed Plot Graph");

		// compute all necessary statictics for tellability
		this.analysisResult = new Tellability(g);

		// Create GUI representation of tellability analysis
		this.addInformation("#Functional Units: " + this.analysisResult.numFunctionalUnits);
		this.addInformation("Highlight Units:");
		this.infoPanel.add(this.unitComboBox);
		this.addInformation("#Polyvalent Vertices: " + this.analysisResult.numPolyvalentVertices);
		this.addInformation("Suspense: " + this.analysisResult.suspense);
		this.addInformation("Tellability: " + this.analysisResult.compute());

		//counterfactuality Button
		this.createCounterfactButton();
		//add Button to infopanel
		this.infoPanel.add(this.counterfactButton);

		// Insert spacing between motivation edges
		g = new EdgeLayoutVisitor(9).apply(g);

		this.addGraph(g);
		this.graphTypeList.setSelectedItem(g);

		if(analyzedGraphContainer != null) {
			g.cloneInto(analyzedGraphContainer);
		}

		logger.info( "Analyze time in ms:" + Duration.between(start, Instant.now()).toMillis());
		return this.analysisResult;
	}

	/**
	 * Displays the graph that is selected by {@code this.graphTypeList} using the inBloom PlotGraphLayout.
	 * @return the displayed JFrame
	 */
	public PlotGraphController visualizeGraph() {
		return this.visualizeGraph(new PlotGraphLayout((PlotDirectedSparseGraph)this.graphTypeList.getSelectedItem()));
	}

	/**
	 * Displaying method that for debugging purposes can use another layout to represent the graph that is selected by
	 * {@link this.graphTypeList}.
	 * @return the displayed JFrame
	 */
	private PlotGraphController visualizeGraph(Layout<Vertex, Edge> layout) {
		// Create a viewing server
		this.visViewer = new VisualizationViewer<>(layout);
		this.setUpAppearance(this.visViewer);

		// Add a mouse to translate the graph.
		PluggableGraphMouse gm = new PluggableGraphMouse();
		gm.add(new SelectingTranslatingGraphMousePlugin());
		this.visViewer.setGraphMouse(gm);

		// enable scrolling control bar
		this.scrollPane = new GraphZoomScrollPane(this.visViewer);

		// c information panel
		this.infoPanel.setLayout(new FlowLayout(SwingConstants.LEADING, 15, 5));

		// second: register a listener that redraws the plot when selection changes. Careful here: order with last command matters
		this.graphTypeList.setActionCommand(CHANGE_VIEW_COMMAND);
		this.graphTypeList.addActionListener(this);
		this.add(this.graphTypeList, BorderLayout.NORTH);

		this.add(this.graphTypeList, BorderLayout.NORTH);
		this.add(this.infoPanel, BorderLayout.SOUTH);

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
		vis.getRenderContext().setVertexStrokeTransformer(Transformers.vertexStrokeTransformer);
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