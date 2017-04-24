package little_red_hen.graph;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.util.VertexShapeFactory;
import little_red_hen.PlotGraph;

public class Transformers {
	
	static private Font FONT = new Font("Courier New", Font.PLAIN, 12);
	static private int HEIGHT = 20;
	
	static private Function<Vertex, Integer> vertexSizeTransformer = new Function<Vertex,Integer>(){
        public Integer apply(Vertex v){
        	// arcane hack to get width of our string given our font
        	int width = (int) FONT.getStringBounds(v.toString(), 
        					new FontRenderContext(FONT.getTransform(), false, false)).getBounds().getWidth(); 
        	
        	return width + 10;
        }
    };
    
    static private Function<Vertex, Float> vertexAspectRatioTransformer = new Function<Vertex,Float>(){
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
        		case EMOTION:
        			return factory.getEllipse(v);
        		default:
        			return factory.getRectangle(v); 
        	}	
        }
    };

    static public Function<Vertex, Font> vertexFontTransformer = new Function<Vertex,Font>(){
        public Font apply(Vertex v){    
        	return FONT;
        }
    };
    
    static public Function<Vertex, Paint> vertexFillPaintTransformer = new Function<Vertex,Paint>(){
        public Paint apply(Vertex v){ 
        	switch (v.getType()) {
	        	case ROOT:
	        		return PlotGraph.BGCOLOR;
        		default:
        			return Color.LIGHT_GRAY;
        	}
        }
    };
    
    static public Function<Vertex, Paint> vertexDrawPaintTransformer = new Function<Vertex,Paint>(){
        public Paint apply(Vertex v){ 
        	switch (v.getType()) {
	        	case ROOT:
	        		return PlotGraph.BGCOLOR;
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
    
    static public Function<Edge, Paint> edgeDrawPaintTransformer = new Function<Edge,Paint>(){
    	public Paint apply(Edge e) {
        	switch (e.getType()) {
        	case ROOT:
        		return PlotGraph.BGCOLOR;
        	case COMMUNICATION:
        		return Color.LIGHT_GRAY;
    		default:
    			return Color.BLACK;
        	}
    	}
    };
}
