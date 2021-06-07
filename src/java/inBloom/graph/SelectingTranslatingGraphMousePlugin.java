package inBloom.graph;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.swing.JPopupMenu;

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

	/* If picked vertex is part of currently highlighted FU, note which instances it is part of so they can be visually marked */
	static public Set<Integer> PICKED_INSTANCES = null;

	private static int PROXIMITY_DIST = 8;

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

    	if (e.getModifiers() == this.modifiers) {
			VisualizationViewer<Vertex,Edge> vv = (VisualizationViewer<Vertex,Edge>) e.getSource();
			GraphElementAccessor<Vertex,Edge> pickSupport = vv.getPickSupport();
			((ShapePickSupport<Vertex,Edge>) pickSupport).setPickSize(PROXIMITY_DIST);

			PickedState<Edge> pickedEdgeState = vv.getPickedEdgeState();
			PickedState<Vertex> pickedVertexState = vv.getPickedVertexState();

			if (pickedEdgeState != null & pickedVertexState != null) {
				// p is the screen point for the mouse event
				Point2D p = e.getPoint();

				// first try to do vertex picking
				Vertex selectedVertex = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
				if (selectedVertex != null) {
					vv.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					if (pickedVertexState.isPicked(selectedVertex) == false) {
						// if it wasn't picked before, pick it and all its edges, clear all previously picked edges
						pickedVertexState.clear();
						pickedEdgeState.clear();
						PICKED_INSTANCES = null;

						pickedVertexState.pick(selectedVertex, true);

						// if we are in highlight mode, and picked vertex is part of highlighted FUs, note the instances it is part of
						if (PlotGraphController.HIGHLIGHTED_VERTICES.containsKey(selectedVertex)) {
							PICKED_INSTANCES = PlotGraphController.HIGHLIGHTED_VERTICES.get(selectedVertex);
						} else {
							for (Edge incidentEdge : selectedVertex.getIncidentEdges()) {
								pickedEdgeState.pick(incidentEdge, true);
							}
						}

					} else {
						// if it was picked before, just unpick it
						pickedVertexState.clear();
						pickedEdgeState.clear();
						PICKED_INSTANCES = null;
					}

					vv.repaint();
		            e.consume();
		            return;
				}

				// els edge if edge picking worked
				Edge selectedEdge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
				if (selectedEdge != null) {
					vv.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					if (pickedEdgeState.isPicked(selectedEdge) == false) {
						// if it wasn't picked before, pick it and clear all previously picked edges
						pickedEdgeState.clear();
						pickedEdgeState.pick(selectedEdge, true);
					} else {
						// if it was picked before, just unpick it
						pickedEdgeState.clear();
					}
				}

				vv.repaint();
	            e.consume();
	            return;
			}
		}

    	if (e.isPopupTrigger()) {
    		JPopupMenu popup = PlotGraphController.getPlotListener().getPopup();
            if (popup != null) {
            	popup.show(PlotGraphController.getPlotListener(), e.getX(), e.getY());
            }
        }
	}


	/**
	 * Remove all picked edges and redraw, delegate to super class.
	 *
	 * @param e the event
	 */
    public void mouseReleased(MouseEvent e) {
    	super.mouseReleased(e);

    	if (e.isPopupTrigger()) {
    		JPopupMenu popup = PlotGraphController.getPlotListener().getPopup();
            if (popup != null) {
            	popup.show(PlotGraphController.getPlotListener(), e.getX(), e.getY());
            }
        }
    }
}