package inBloom.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.util.VertexShapeFactory;

/**
 * Maintains a number of functions that transform the visual appearance of nodes and vertices based on their type.
 * Is employed by {@link PlotGraphController} during {@link PlotGraphController#visualizeGraph() visualizeGraph()} in order to set up its
 * {@link edu.uci.ics.jung.visualization.VisualizationViewer VisualizationViewer} instance.
 * @author Leonid Berov
 */
public class Transformers {

	static private Font FONT = new Font("Courier New", Font.PLAIN, 12);
	static private Font FONT_LABEL = new Font("Arial Black", Font.BOLD, 15);
	static private int HEIGHT = 20;

	static public Set<Vertex> HIGHLIGHT;

	static public Function<Vertex, Integer> vertexSizeTransformer = new Function<Vertex,Integer>(){
        public Integer apply(Vertex v){
        	Font currentFont = vertexFontTransformer.apply(v);

        	// arcane hack to get width of our string given our font
        	int width = (int) currentFont.getStringBounds(v.toString(),
        					new FontRenderContext(currentFont.getTransform(), false, false)).getBounds().getWidth();

        	return Math.max(width, v.minWidth * 2) + 10;
        }
    };

    static public Function<Vertex, Float> vertexAspectRatioTransformer = new Function<Vertex,Float>(){
    	public Float apply(Vertex v){
    		float aspectRatio = HEIGHT / (float) vertexSizeTransformer.apply(v);
    		return aspectRatio;
    	}
    };

	static public Function<Vertex, Shape> vertexShapeTransformer = new Function<Vertex,Shape>(){
        public Shape apply(Vertex v){
        	VertexShapeFactory<Vertex> factory = new VertexShapeFactory<>(vertexSizeTransformer,
        																		vertexAspectRatioTransformer);
        	switch (v.getType()) {
	        	case SPEECHACT:
        		case INTENTION:
        		case LISTEN:
        		case PERCEPT:
        			return factory.getRoundRectangle(v);
        		case ACTION:
        			return factory.getRectangle(v);
        		default:
        			return factory.getEllipse(v);
        	}
        }
    };

    static public Function<Vertex, Font> vertexFontTransformer = new Function<Vertex,Font>(){
        public Font apply(Vertex v){
        	switch (v.getType()) {
        		case ROOT:
	        	case AXIS_LABEL:
	        		return FONT_LABEL;
	        	default:
	        		return FONT;
	        	}
        }
    };

    static public Function<Vertex, Paint> vertexFillPaintTransformer = new Function<Vertex,Paint>(){
        public Paint apply(Vertex v){
        	switch (v.getType()) {
	        	case ROOT:
	        	case AXIS_LABEL:
	        		return PlotGraphController.BGCOLOR;
	        	case ACTION:
	        		return Color.getHSBColor(Float.valueOf("0"), Float.valueOf("0"), Float.valueOf("0.55"));
	        	case PERCEPT: {
	        		if (v.getLabel().contains("wish") | v.getLabel().contains("obligation") | v.getLabel().contains("mood")) {
						return PlotGraphController.BGCOLOR;
					}
	        		return Color.LIGHT_GRAY;
	        	}
        		default:
        			return Color.LIGHT_GRAY;
        	}
        }
    };

    static public Function<Vertex, Paint> vertexDrawPaintTransformer = new Function<Vertex,Paint>(){
        public Paint apply(Vertex v){
        	if(HIGHLIGHT != null) {
        		if(HIGHLIGHT.contains(v)) {
        			return Color.GREEN;
        		}
        	}
        	switch (v.getType()) {
	        	case ROOT:
	        	case AXIS_LABEL:
	        		return PlotGraphController.BGCOLOR;
	        	case PERCEPT: {
	        		if (v.getLabel().contains("wish") | v.getLabel().contains("obligation") | v.getLabel().contains("mood")) {
						return PlotGraphController.BGCOLOR;
					}
	        	}
        		default:
        			return Color.BLACK;
        	}
        }
    };

	public static Function<? super Vertex, Stroke> vertexStrokeTransformer =  new Function<Vertex,Stroke>() {
		public Stroke apply(Vertex v) {
        	switch (v.getType()) {
	        	case PERCEPT: {
	        		if (v.getLabel().contains("wish") | v.getLabel().contains("obligation") | v.getLabel().contains("mood")) {
						return new BasicStroke(0f);
					}
	        		}
	    		default:
	    			return new BasicStroke(1f);
        	}
		}
	};

	static public Function<Edge, Shape> edgeShapeTransformer = new Function<Edge,Shape>(){
        public Shape apply(Edge e){
        	switch(e.getType()) {
        	case TEMPORAL:
        		return new Line2D.Float(0f, 0f, 0f, 0f);
        	default:
        		return new Line2D.Float(0f, e.getOffset(), 1f, e.getOffset());
        	}
        }
    };

    static public Function<Edge, Stroke> edgeStrokeHighlightingTransformer = new Function<Edge,Stroke>(){
        public Stroke apply(Edge e){
        	// set width according to whether the edges is picked
        	float width;
    		PickedState<Edge> pickedEdgeState = PlotGraphController.getPlotListener().visViewer.getPickedEdgeState();
            if (pickedEdgeState.isPicked(e)) {
            	width = 3.5f;
            } else {
            	width = 1f;
            }

            // E edges and Wildcards are stroked
        	if (e.getType().equals(Edge.Type.EQUIVALENCE)) {
        		float[] dash = { 7 };
        		return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 10f);
        	}

        	return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        }
    };

    static public Function<Edge, Paint> edgeDrawPaintTransformer = new Function<Edge,Paint>(){
    	public Paint apply(Edge e) {
        	switch (e.getType()) {
        	case ROOT:
        		return PlotGraphController.BGCOLOR;
        	case ACTUALIZATION:
        		return Color.CYAN.darker();
        	case TERMINATION:
        		return Color.getHSBColor(0f, .65f, .85f);
        	case CAUSALITY:
        		return Color.getHSBColor(.6f, .73f, .95f);
        	case WILDCARD:
        		return Color.GREEN;
    		default:
    			return Color.LIGHT_GRAY;
        	}
    	}
    };

	public static Predicate<Context<Graph<Vertex, Edge>, Edge>> edgeArrowPredicate = new Predicate<Context<Graph<Vertex, Edge>, Edge>>() {
		@Override
		public boolean apply(Context<Graph<Vertex, Edge>, Edge> input) {
			switch (input.element.getType()) {
        	case EQUIVALENCE:
        		return true;										// Equivalence edges have no arrows
    		default:
    			return true;
			}
		}

	};
}
