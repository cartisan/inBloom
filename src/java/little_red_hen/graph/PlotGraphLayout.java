package little_red_hen.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;

@Deprecated
public class PlotGraphLayout<V,E> extends TreeLayout<V, E> {

    public PlotGraphLayout(Forest<V, E> graph)  {
        super(graph);
    }
    

    public PlotGraphLayout(Forest<V, E> graph, int distx)  {
        super(graph, distx);
    }


    @Override
    protected void buildTree() {
        this.m_currentPoint = new Point(20, 20);
        Collection<V> roots = TreeUtils.getRoots(graph);
        if (roots.size() > 0 && graph != null) {
        	this.m_currentPoint.x += this.distX;
        	this.m_currentPoint.y += this.distY;
        	
        	
        	
       		calculateDimensionX(roots);
       		for(V v : roots) {
        		m_currentPoint.x += this.basePositions.get(v)/2 + this.distX;
        		buildTree(v, this.m_currentPoint.x);
        	}
        }
    }

    @Override
    protected void buildTree(V v, int x) {

        if (alreadyDone.add(v)) {
            //go one level further down
            this.m_currentPoint.y += this.distY;
            this.m_currentPoint.x = x;

            this.setCurrentPositionFor(v);

            int sizeXofCurrent = basePositions.get(v);

            int lastX = x - sizeXofCurrent / 2;

            int sizeXofChild;
            int startXofChild;

            for (V element : graph.getSuccessors(v)) {
                sizeXofChild = this.basePositions.get(element);
                startXofChild = lastX + sizeXofChild / 2;
                buildTree(element, startXofChild);
                lastX = lastX + sizeXofChild + distX;
            }
            this.m_currentPoint.y -= this.distY;
        }
    }
    
    private int calculateDimensionX(V v) {

        int size = 0;
        int childrenNum = graph.getSuccessors(v).size();

        if (childrenNum != 0) {
            for (V element : graph.getSuccessors(v)) {
                size += calculateDimensionX(element) + distX;
            }
        }
        size = Math.max(0, size - distX);
        basePositions.put(v, size);

        return size;
    }

    private int calculateDimensionX(Collection<V> roots) {

    	int size = 0;
    	for(V v : roots) {
    		int childrenNum = graph.getSuccessors(v).size();

    		if (childrenNum != 0) {
    			for (V element : graph.getSuccessors(v)) {
    				size += calculateDimensionX(element) + distX;
    			}
    		}
    		size = Math.max(0, size - distX);
    		basePositions.put(v, size);
    	}

    	return size;
    }

}
