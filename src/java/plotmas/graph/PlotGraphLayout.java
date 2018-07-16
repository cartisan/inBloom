package plotmas.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;

/**
 * Responsible for building the visual representation of the plot graph, especially maintaining correct spatial
 * relations between vertices.
 * @author Leonid Berov
 */
public class PlotGraphLayout extends AbstractLayout<Vertex, Edge> {

    // The horizontal/vertical vertex spacing.
    public static int PAD_X = 50;
    public static int PAD_Y = 50;
    
    // start point of layout
    public static int START_X = 200;
    public static int START_Y = 0;
	
    protected transient Point m_currentPoint = new Point();
    protected transient Vertex currentRoot;
    
//    protected HashMap<Vertex, Integer> columnWidth = new HashMap<>();		// maps: root     -> width of column
    protected HashMap<Vertex, Integer> columnStartAtX = new HashMap<>();	// maps: root     -> x value of all vertices in column
    protected HashMap<Integer, Integer> stepStartAtY = new HashMap<>();		// maps: step_num -> y value of first occ of this step
    
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
	}
    
    protected void buildGraph() {
    	this.computeLayout();
    	this.buildLayout();
    }


	private void computeLayout() {
        this.m_currentPoint = new Point(START_X, START_Y);
        Collection<Vertex> roots = ((PlotDirectedSparseGraph) this.graph).getRoots();
        
        if (roots.size() > 0 && this.graph != null) {
        	// analyze each column
       		for(Vertex root : roots) {
       			this.currentRoot = root;
       			
       			// persist x pos of column based on current position of pointer 
       			this.columnStartAtX.put(this.currentRoot, this.m_currentPoint.x);
       			
       			// analyze y pos of steps in this column 
       			this.m_currentPoint.y = START_Y;
       			int columnWidth = 0;
       			List<Integer> encounteredSteps = new LinkedList<>();
       			
       			columnWidth = this.analyzeColumn(root, encounteredSteps, columnWidth);
        		
        		// the next root should start after longest vertex in this column
        		this.m_currentPoint.x += columnWidth + PAD_X;
        	}
        }
	}

	protected int analyzeColumn(Vertex vertex, List<Integer> encounteredSteps, int columnWidth) {
        // update pointer
        this.m_currentPoint.y += PlotGraphLayout.PAD_Y;
        
        // check if this vertex needs more space than biggest previous vertex in this column
    	int width = Transformers.vertexSizeTransformer.apply(vertex);
    	if (width > columnWidth) {
    		columnWidth = width;
    	}
    	
    	// check if step position along the  y axis needs to be updated
    	if (!encounteredSteps.contains(vertex.getStep())) {
    		// this is the first time this step appears in this column
    		// check if its y position is bigger then the one we found in previous columns
    		int prevY = this.stepStartAtY.getOrDefault(vertex.getStep(), Integer.MIN_VALUE);
    		if (prevY < this.m_currentPoint.y) {
    			this.stepStartAtY.put(vertex.getStep(), this.m_currentPoint.y);
    		}
    		
    		// make sure following events in this time step do not change it's starting position
    		encounteredSteps.add(vertex.getStep());
    	}
    	
    	// compute position for child
        Vertex child = ((PlotDirectedSparseGraph) this.graph).getCharSuccessor(vertex);
        if(!(child == null)) {
        	columnWidth = this.analyzeColumn(child, encounteredSteps, columnWidth);
        } else {
        	// found last vertex in column --> adopt canvas size
        	this.updateCanvasSize();
        }
        
        return columnWidth;
    }

	private void buildLayout() {
        Collection<Vertex> roots = ((PlotDirectedSparseGraph) this.graph).getRoots();
        
        if (roots.size() > 0 && this.graph != null) {
        	// build each column
       		for(Vertex root : roots) {
       			this.currentRoot = root;
       			this.buildColumn(root, 0, Integer.MIN_VALUE);
       		}
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
    	if(this.m_currentPoint.x > size.width - PAD_X) 
    		this.size.width = this.m_currentPoint.x + PAD_X;
    	
    	if(this.m_currentPoint.y > size.height - PAD_Y) 
    		this.size.height = this.m_currentPoint.y + PAD_Y;
	}
	
//    private int calculateWidthForColumn(Vertex vertex) {
//    	int width = Transformers.vertexSizeTransformer.apply(vertex);
//
//    	Vertex successor = ((PlotDirectedSparseGraph) this.graph).getCharSuccessor(vertex); 
//		if(successor == null) {
//			return (this.PAD_X > width ? PAD_X : width);
//		}
//		
//    	int max_width = calculateWidthForColumn(successor);
//    	return (width > max_width ? width : max_width);
//    }
}
