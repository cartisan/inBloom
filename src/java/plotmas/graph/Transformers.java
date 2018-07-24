package plotmas.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;

import com.google.common.base.Function;

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
	static private Font FONT_LABEL = new Font("Courier New", Font.BOLD, 15);
	static private int HEIGHT = 20;
	
	static public Function<Vertex, Integer> vertexSizeTransformer = new Function<Vertex,Integer>(){
        public Integer apply(Vertex v){
        	// arcane hack to get width of our string given our font
        	int width = (int) FONT.getStringBounds(v.toString(), 
        					new FontRenderContext(FONT.getTransform(), false, false)).getBounds().getWidth(); 
        	
        	return width + 10;
        }
    };
    
    static public Function<Vertex, Float> vertexAspectRatioTransformer = new Function<Vertex,Float>(){
    	public Float apply(Vertex v){
    		float aspectRatio = (HEIGHT / (float) vertexSizeTransformer.apply(v)); 
    		return aspectRatio;
    	}
    };
	
	static public Function<Vertex, Shape> vertexShapeTransformer = new Function<Vertex,Shape>(){
        public Shape apply(Vertex v){
        	VertexShapeFactory<Vertex> factory = new VertexShapeFactory<Vertex>(vertexSizeTransformer,
        																		vertexAspectRatioTransformer);
        	switch (v.getType()) {
        		case SPEECHACT:
        			return factory.getRoundRectangle(v);
        		case LISTEN:
        			return factory.getRoundRectangle(v);
        		case EMOTION:
        			return factory.getRectangle(v);
        		case PERCEPT:
        			return factory.getRectangle(v);
        		default:
        			return factory.getEllipse(v);
        	}	
        }
    };

    static public Function<Vertex, Font> vertexFontTransformer = new Function<Vertex,Font>(){
        public Font apply(Vertex v){
        	switch (v.getType()) {
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
	        		return PlotGraphController.BGCOLOR;
	        	case AXIS_LABEL:
	        		return PlotGraphController.BGCOLOR;
	        	case SPEECHACT:
	        		return Color.getHSBColor(Float.valueOf("0"), Float.valueOf("0"), Float.valueOf("0.95"));
	        	case LISTEN:
	        		return Color.getHSBColor(Float.valueOf("0"), Float.valueOf("0"), Float.valueOf("0.95"));
        		default:
        			return Color.LIGHT_GRAY;
        	}
        }
    };
    
    static public Function<Vertex, Paint> vertexDrawPaintTransformer = new Function<Vertex,Paint>(){
        public Paint apply(Vertex v){ 
        	switch (v.getType()) {
	        	case ROOT:
	        		return PlotGraphController.BGCOLOR;
	        	case AXIS_LABEL:
	        		return PlotGraphController.BGCOLOR;
        		default:
        			return Color.BLACK;
        	}
        }
    };
    
	static public Function<Edge, Shape> edgeShapeTransformer = new Function<Edge,Shape>(){
        public Shape apply(Edge e){
        	return new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
        }
    };

    static public Function<Edge, Stroke> edgeStrokeHighlightingTransformer = new Function<Edge,Stroke>(){
        public Stroke apply(Edge e){
            PickedState<Edge> pickedEdgeState = PlotGraphController.getPlotListener().visViewer.getPickedEdgeState();
            
            if (pickedEdgeState.isPicked(e))
            	return new BasicStroke(3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            
        	return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        }
    };
    
    static public Function<Edge, Paint> edgeDrawPaintTransformer = new Function<Edge,Paint>(){
    	public Paint apply(Edge e) {
        	switch (e.getType()) {
        	case ROOT:
        		return PlotGraphController.BGCOLOR;
        	case COMMUNICATION:
        		return Color.LIGHT_GRAY;
    		default:
    			return Color.BLACK;
        	}
    	}
    };
}
