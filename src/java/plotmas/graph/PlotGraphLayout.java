package plotmas.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;

/**
 * Responsible for building the visual representation of the plot graph, especially maintaining correct spatial
 * relations between vertices.
 * @author Leonid Berov
 */
public class PlotGraphLayout extends AbstractLayout<Vertex, Edge> {

	static Logger logger = Logger.getLogger(PlotGraphLayout.class.getName());
    
    // The horizontal/vertical vertex spacing.
    public static final int PAD_X = 50;
    public static final int PAD_Y = 50;
    
    // start point of layout
    public static final int START_X = 200;
    public static final int START_Y = 0;

    public static final int STEP_OFFSET = 25;
	
    protected transient Point m_currentPoint = new Point();
    protected transient Vertex currentRoot;
    
    protected HashMap<Vertex, Integer> columnWidths = new HashMap<>();		// maps: root     -> width of column
    protected HashMap<Vertex, Integer> columnStartAtX = new HashMap<>();	// maps: root     -> x value of all vertices in column
    protected HashMap<Integer, Integer> stepStartAtY = new HashMap<>();		// maps: step_num -> y value of first occ of this step
    protected HashMap<Integer, Integer> stepEndAtY = new HashMap<>();		// maps: step_num -> y value of last occ of this step
    
    public PlotGraphLayout(PlotDirectedSparseGraph graph)  {
        super(graph);
        this.size = new Dimension(600,600);
        this.buildGraph();
    }
    
	public void initialize() {
		// Intentionally empty
	}

	public void reset() {
		this.columnStartAtX = new HashMap<>();
		this.stepStartAtY = new HashMap<>();
		this.stepEndAtY = new HashMap<>();
	}
    
    protected void buildGraph() {
    	this.computeLayout();
    	this.buildLayout();
    }


	private void computeLayout() {
		logger.info("Computing time-step oriented layout");
        this.m_currentPoint = new Point(START_X, START_Y);
        Collection<Vertex> roots = ((PlotDirectedSparseGraph) this.graph).getRoots();
        
        if (roots.size() > 0 && this.graph != null) {
        	// analyze each column
       		for(Vertex root : roots) {
       			logger.fine("Column " + root.getLabel());
       			this.currentRoot = root;
       			this.columnWidths.put(root, 0);
       			
       			// persist x pos of column based on current position of pointer 
       			this.columnStartAtX.put(this.currentRoot, this.m_currentPoint.x);
       			
       			// analyze y pos of steps in this column 
       			this.m_currentPoint.y = START_Y;
       			List<Integer> encounteredSteps = new LinkedList<>();
       			
       			int colWidth = this.analyzeColumn(root, encounteredSteps, this.columnWidths.get(root));
       			this.columnWidths.put(root, colWidth);
       			
            	this.updateCanvasSize();
        		
        		// the next root should start after longest vertex in this column
        		this.m_currentPoint.x += colWidth + PAD_X;
        	}
        }
	}

	protected int analyzeColumn(Vertex vertex, List<Integer> encounteredSteps, int columnWidth) {
    	logger.fine("  step: " + vertex.getStep() +"    vertex: " + vertex.getLabel());
		// update pointer
        this.m_currentPoint.y += PlotGraphLayout.PAD_Y;
    	logger.fine("    initial pointer position: " + this.m_currentPoint.y);        
        
        // check if this vertex needs more space than biggest previous vertex in this column
    	int width = Transformers.vertexSizeTransformer.apply(vertex);
    	if (width > columnWidth) {
    		columnWidth = width;
    	}
    	
    	// check if step position along the y axis needs to be updated
    	if (!encounteredSteps.contains(vertex.getStep())) {
    		// this is the first time this step appears in this column, its position is relevant
    		// add small vertical offset to indicate new step start
            this.m_currentPoint.y += PlotGraphLayout.STEP_OFFSET;
            
            logger.fine("    step relevant for stepStart position in this column");
            
    		if (this.stepStartAtY.containsKey(vertex.getStep())) {
    			// step was layouted by previous columns
    			updateStepLocation(vertex, this.stepStartAtY); 
    		} else { 
    			// found a step that was not layouted by previous columns
    			addStepLocation(vertex, this.stepStartAtY, true);
    		}

    		// shift current pointer to reflect y position according to layout, no matter which column is responsible for the layout
			this.m_currentPoint.y = this.stepStartAtY.get(vertex.getStep());
			
    		// make sure following events in this time step do not change it's starting position
    		encounteredSteps.add(vertex.getStep());
    	}
    	
    	// continue with traversal of column
        Vertex child = ((PlotDirectedSparseGraph) this.graph).getCharSuccessor(vertex);
        if(!(child == null)) {
        	// successor exists
        	
        	// check if next vertex has different step
        	if(child.getStep() != vertex.getStep()) {
        		// this is the last vertex of current step, update layout data
        		logger.fine("    step relevant for stepEnd position in this column");
        		if (this.stepEndAtY.containsKey(vertex.getStep())) {
        			// step was layouted by previous columns
        			updateStepLocation(vertex, this.stepEndAtY); 
        		} else { 
        			// found a step that was not layouted by previous columns
        			addStepLocation(vertex, this.stepEndAtY, false);
        		}
        	}
        	
        	logger.fine("    new pointer position: " + this.m_currentPoint.y);
        	
        	// compute position for children
        	columnWidth = this.analyzeColumn(child, encounteredSteps, columnWidth);
        } else {
        	logger.fine("  new pointer position: " + this.m_currentPoint.y);
        	
        	// found last vertex in column --> adopt canvas size
        	logger.fine("Column " + this.currentRoot.getLabel() + " steps start at y, dict: " + Arrays.toString(this.stepStartAtY.entrySet().toArray()));
        	logger.fine("Column " + this.currentRoot.getLabel() + " steps end at y, dict: " + Arrays.toString(this.stepEndAtY.entrySet().toArray()));
        }
        
        return columnWidth;
    }

	/**
	 * @param vertex
	 * @param stepLocationMap
	 * @param start
	 */
	private void addStepLocation(Vertex vertex, HashMap<Integer, Integer> stepLocationMap, boolean start) {
		// check if it was just skipped by previous columns 
		logger.fine("     adding step position");
		if (stepLocationMap.keySet().stream().anyMatch(key -> key > vertex.getStep())) {
			if (start) {
				// put the skipped step in the place where previous columns located its successor (or itself, if bigger)
				int succYKey = stepLocationMap.keySet().stream().filter(key -> key > vertex.getStep())
															    .mapToInt(x -> x)
															    .min().getAsInt();
				logger.fine("      based on successor step " + succYKey + " present at: " + stepLocationMap.get(succYKey));
				this.m_currentPoint.y = Integer.max(stepLocationMap.get(succYKey), this.m_currentPoint.y);
			} else {
				// finishing adding new steps, delta-y to shift all following steps based on length of new step
				int diff = this.m_currentPoint.y - this.stepStartAtY.get(vertex.getStep());
				// starts as well as end of all lower steps are affected
				updateSucceedingSteps(vertex.getStep(), diff, this.stepStartAtY);
				updateSucceedingSteps(vertex.getStep(), diff, this.stepEndAtY);				
			}
		} 
		
		// safe the steps location in the layout
		stepLocationMap.put(vertex.getStep(), this.m_currentPoint.y);
	}

	/**
	 * @param vertex
	 * @param stepLocationMap
	 */
	private void updateStepLocation(Vertex vertex, HashMap<Integer, Integer> stepLocationMap) {
		// check if new step y position is bigger then the one we previously found
		int prevY = stepLocationMap.get(vertex.getStep());
		if (prevY < this.m_currentPoint.y) {
			// save new position of step in layout
			stepLocationMap.put(vertex.getStep(), this.m_currentPoint.y);
			logger.fine("     new y-position for step " + vertex.getStep() + " is: " + this.m_currentPoint.y);
			
			// move all (previously positioned) steps below this step according to newly found delta-y
			int diff = this.m_currentPoint.y - prevY;
			
			// starts as well as end of all lower steps are affected
			updateSucceedingSteps(vertex.getStep(), diff, this.stepStartAtY);
			updateSucceedingSteps(vertex.getStep(), diff, this.stepEndAtY);
			logger.fine("     shifting succeeding steps (start/end) down by " + diff);
		} else {
			logger.fine("     no new y-position necessary");
		}
	}

	/**
	 * @param currentStep the step number of the vertex currently analysed
	 * @param diff the delta_y by which following steps need to be moved
	 * @param stepLocationMap the map in which the update takes place ({@linkplain PlotGraphLayout#stepStartAtY} or {@linkplain  PlotGraphLayout#stepEndAtY}) 
	 */
	private void updateSucceedingSteps(Integer currentStep, int diff, HashMap<Integer, Integer> stepLocationMap) {
		Integer[] steps = stepLocationMap.keySet().stream()
										 .filter(step -> step > currentStep)
								         .toArray(Integer[]::new);
		
		for(int step : steps) {
			stepLocationMap.put(step, stepLocationMap.get(step) + diff);
		}
	}

	private void buildLayout() {
		logger.info("Building time-step oriented layout");
        ArrayList<Vertex> roots = ((PlotDirectedSparseGraph) this.graph).getRoots();
        
        if (roots.size() > 0 && this.graph != null) {
        	// build step-axis before first col
        	this.buildYAxis();
        	
        	// build each column
       		for(Vertex root : roots) {
       			this.currentRoot = root;
       			this.buildColumn(root, 0, Integer.MIN_VALUE);
       		}
        }
    }

	public void buildYAxis() {
    	Vertex firstColRoot = ((PlotDirectedSparseGraph) this.graph).getRoots().get(0);
    	int x_pos = this.columnStartAtX.get(firstColRoot) - (this.columnWidths.get(firstColRoot) / 2 + STEP_OFFSET);
    	
    	// Update x positions of all columns to make place for axis
    	for (Vertex root : this.columnStartAtX.keySet()) {
    		this.columnStartAtX.put(root,
    							    this.columnStartAtX.get(root) - x_pos + PAD_X);
    	}
    	this.size.width = this.size.width - x_pos + PAD_X;
    	x_pos = PAD_X;

    	for (Vertex vertex : ((PlotDirectedSparseGraph) this.graph).getAxisVertices()) {
    		int y_pos = 0;
    		
    		if(this.stepStartAtY.containsKey(vertex.getStep())) {
    			y_pos = this.stepStartAtY.get(vertex.getStep());
    		} else {
    			this.graph.removeVertex(vertex);
    		}

			locations.getUnchecked(vertex).setLocation(new Point(x_pos, y_pos));	
    	}
	}
	
	private void buildColumn(Vertex vertex, int stepRepeated, int lastStep) {
		// compute offset for several vertices in one step
		if (lastStep != vertex.getStep()) {
			stepRepeated = 0;
		} else {
			stepRepeated += 1;
		}
		// save vertex location
		int x_pos = this.columnStartAtX.get(this.currentRoot);
		int y_pos = this.stepStartAtY.get(vertex.getStep()) + PAD_Y * stepRepeated;
		locations.getUnchecked(vertex).setLocation(new Point(x_pos, y_pos));

		// continue with next vertex
		Vertex child = ((PlotDirectedSparseGraph) this.graph).getCharSuccessor(vertex);
        if(!(child == null)) {
        	lastStep = vertex.getStep();
        	this.buildColumn(child, stepRepeated, lastStep);
        }
	}

	/**
	 * Checks if the current pointer would be outside the canvas size, if yes adopts it accordingly
	 */
	private void updateCanvasSize() {
		// adopt size if current point outgrows it
    	if(this.columnStartAtX.get(currentRoot) + this.columnWidths.get(currentRoot)/2 > size.width - PAD_X) { 
    		this.size.width = this.m_currentPoint.x + this.columnWidths.get(currentRoot)/2 + PAD_X;
    	}
    	
    	if(this.m_currentPoint.y > size.height - PAD_Y) 
    		this.size.height = this.m_currentPoint.y + PAD_Y;
	}
}
