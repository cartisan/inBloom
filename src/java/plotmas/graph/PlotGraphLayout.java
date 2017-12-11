package plotmas.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;

/**
 * Responsible for building the visual representation of the plot graph, especially maintaining correct spatial
 * relations between vertices.
 * @author Leonid Berov
 */
public class PlotGraphLayout extends AbstractLayout<Vertex, Edge> {

    // The horizontal/vertical vertex spacing.
    protected int distX = 50;
    protected int distY = 50;
	
    protected transient Point m_currentPoint = new Point();
    
    public PlotGraphLayout(PlotDirectedSparseGraph graph)  {
        super(graph);
        this.size = new Dimension(600,600);
        this.buildGraph();
    }
    
	public void initialize() {
		// Intentionally empty
	}

	public void reset() {
		// Intentionally empty
		
	}
    
    protected void buildGraph() {
        this.m_currentPoint = new Point(200, 0);
        Collection<Vertex> roots = ((PlotDirectedSparseGraph) this.graph).getRoots();
        
        if (roots.size() > 0 && this.graph != null) {
        	// build each column
       		for(Vertex v : roots) {
        		buildGraph(v);
        		
        		// the next root should start after longest vertex in this column
        		this.distX = calculateDimensionX(v);
        		m_currentPoint.x += this.distX + 50;
        	}
        }
    }

    protected void buildGraph(Vertex v) {
        //go one level further down
        this.m_currentPoint.y += this.distY;
    	
        // adopt size if current point outgrows it
    	if(this.m_currentPoint.x > size.width - distX) 
    		this.size.width = this.m_currentPoint.x + distX;
    	
    	if(this.m_currentPoint.y > size.height - distY) 
    		this.size.height = this.m_currentPoint.y + distY;

    	// save vertex location
    	locations.getUnchecked(v).setLocation(m_currentPoint);

    	// compute position for child
        for (Vertex child : ((PlotDirectedSparseGraph) this.graph).getCharSuccessors(v)) {
            buildGraph(child);
        }
        
        this.m_currentPoint.y -= this.distY;
    }
    
    private int calculateDimensionX(Vertex vertex) {
    	int width = Transformers.vertexSizeTransformer.apply(vertex);

    	for (Vertex v : ((PlotDirectedSparseGraph) this.graph).getCharSuccessors(vertex)) {
    		int new_width = calculateDimensionX(v);
    		if (new_width > width) {
    			width = new_width;
    		}
    	}
    	
    	if (distX > width) {
    		return distX;
    	}
    	return width;
    }
}
