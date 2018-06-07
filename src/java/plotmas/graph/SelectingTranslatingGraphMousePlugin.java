package plotmas.graph;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;

/**
 * Plugin that highlights edges when mouseEvent is executed close by, and is also capable of moving canvas when
 * the mouse is dragged.
 * 
 * @author Leonid Berov
 */
public class SelectingTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin implements MouseListener, MouseMotionListener {
    
	private static int PROXIMITY_DIST = 25;
    /**
	 * Creates an instance with default modifiers of BUTTON1_MASK
	 */
	public SelectingTranslatingGraphMousePlugin() {
	    this(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Creates an instance with the specified mouse event modifiers.
	 * @param selectionModifiers the mouse event modifiers to use.
	 */
    public SelectingTranslatingGraphMousePlugin(int selectionModifiers) {
        super(selectionModifiers);
    }

	/**
	 * If the event occurs on an Edge, pick that single Edge and redraw,
	 * delegate event to super class.
	 * @param e the event
	 */
    @SuppressWarnings("unchecked")
    public void mousePressed(MouseEvent e) {
    	super.mousePressed(e);
    	
    	if (e.getModifiers() == modifiers) {
			VisualizationViewer<Vertex,Edge> vv = (VisualizationViewer<Vertex,Edge>) e.getSource();
			GraphElementAccessor<Vertex,Edge> pickSupport = vv.getPickSupport();
			((ShapePickSupport<Vertex,Edge>) pickSupport).setPickSize(PROXIMITY_DIST);
			
			PickedState<Edge> pickedEdgeState = vv.getPickedEdgeState();
            
			if (pickSupport != null && pickedEdgeState != null) {
				// p is the screen point for the mouse event
				Point2D p = e.getPoint();
				Edge selectedEdge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
				
				if (selectedEdge != null) {
					vv.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
//					System.out.println("selectecd edge: " + selectedEdge.toString());
					if (pickedEdgeState.isPicked(selectedEdge) == false) {
						pickedEdgeState.clear();
						pickedEdgeState.pick(selectedEdge, true);
					}
				}
			}
			
			vv.repaint();
			
            e.consume();
		}
	}


	/**
	 * Remove all picked edges and redraw, delegate to super class.
	 * 
	 * @param e the event
	 */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
    	super.mouseReleased(e);
    	
		if (e.getModifiers() == modifiers) {
			VisualizationViewer<Vertex,Edge> vv = (VisualizationViewer<Vertex,Edge>) e.getSource();
			PickedState<Edge> pickedEdgeState = vv.getPickedEdgeState();
			
			// remove edge that was picked on mouse press
			pickedEdgeState.clear();
			
			vv.repaint();
			
            e.consume();
		}
    }
}