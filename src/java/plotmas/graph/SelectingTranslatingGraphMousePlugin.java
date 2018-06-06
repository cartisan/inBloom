package plotmas.graph;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * Plugin that highlights edges when mouseEvent is executed close by, and is also capable of moving canvas when
 * the mouse is dragged.
 * 
 * @author Leonid Berov
 */
//public class SelectingTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin implements MouseListener, MouseMotionListener {
public class SelectingTranslatingGraphMousePlugin extends AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {
    
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
    	
    	if (e.getModifiers() == modifiers) {
    		down = e.getPoint();
    		
			VisualizationViewer<Vertex,Edge> vv = PlotGraphController.getPlotListener().visViewer;
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
		if (e.getModifiers() == modifiers) {
			down = null;
			
			VisualizationViewer<Vertex,Edge> vv = PlotGraphController.getPlotListener().visViewer;
			
			PickedState<Edge> pickedEdgeState = vv.getPickedEdgeState();
			
			// remove edge that was picked on mouse press
			pickedEdgeState.clear();
			
			vv.repaint();
			
	        vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	        
            e.consume();
		}
    }

    /**
     * chack the modifiers. If accepted, translate the graph according
     * to the dragging of the mouse pointer
     * @param e the event
	 */
    public void mouseDragged(MouseEvent e) {
        VisualizationViewer<Vertex,Edge> vv = PlotGraphController.getPlotListener().visViewer;
        boolean accepted = checkModifiers(e);
        if(accepted) {
            MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
            vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            try {
                Point2D q = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
                Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
                float dx = (float) (p.getX()-q.getX());
                float dy = (float) (p.getY()-q.getY());
                
                modelTransformer.translate(dx, dy);
                down.x = e.getX();
                down.y = e.getY();
            } catch(RuntimeException ex) {
                System.err.println("down = "+down+", e = "+e);
                throw ex;
            }
        
            e.consume();
            vv.repaint();
        }
    }

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
