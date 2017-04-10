package little_red_hen.graph;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;

public class Transformers {
	
	static private Font FONT = new Font("Courier New", Font.PLAIN, 12);
	static private int HEIGHT = 20;
	
	static private Function<String, Integer> vertexSizeTransformer = new Function<String,Integer>(){
        public Integer apply(String v){
        	// arcane hack to get width of our string given our font
        	int width = (int) FONT.getStringBounds(v, 
        					new FontRenderContext(FONT.getTransform(), false, false)).getBounds().getWidth(); 
        	
        	return width + 10;
        }
    };
    
    static private Function<String, Float> vertexAspectRatioTransformer = new Function<String,Float>(){
    	public Float apply(String v){
    		float aspectRatio = (HEIGHT / (float) vertexSizeTransformer.apply(v)); 
    		return aspectRatio;
    	}
    };
	
	static public Function<String, Shape> vertexShapeTransformer = new Function<String,Shape>(){
        public Shape apply(String v){
        	VertexShapeFactory<String> factory = new VertexShapeFactory<String>(vertexSizeTransformer,
        																		vertexAspectRatioTransformer);
        	
        	Rectangle2D rectangle = factory.getRectangle(v); 
            return rectangle;
        }
    };

    static public Function<String, Font> vertexFontTransformer = new Function<String,Font>(){
        public Font apply(String v){    
        	return FONT;
        }
    };
    
}
