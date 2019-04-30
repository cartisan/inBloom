package inBloom.framing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.graph.isomorphism.FunctionalUnit.Instance;

public class Transformers {
	static protected Font FONT = new Font("Courier New", Font.PLAIN, 12);
	static protected Font FONT_LABEL = new Font("Courier New", Font.BOLD, 15);
	
	static public Function<FunctionalUnit.Instance, Integer> vertexSizeTransformer = new Function<FunctionalUnit.Instance, Integer>() {
		@Override
		public Integer apply(Instance input) {
			return 10 + (int) FONT.getStringBounds(input.toString(), 
					new FontRenderContext(FONT.getTransform(), false, false)).getBounds().getWidth();
		}
     };
     
     static public Function<FunctionalUnit.Instance, Float> vertexAspectRatioTransformer = new Function<FunctionalUnit.Instance, Float>(){
     	public Float apply(FunctionalUnit.Instance v){
     		float aspectRatio = (20 / (float) vertexSizeTransformer.apply(v)); 
     		return aspectRatio;
     	}
     };
     
     static public Function<FunctionalUnit.Instance, Shape> vertexShapeTransformer = new Function<FunctionalUnit.Instance,Shape>(){
         public Shape apply(FunctionalUnit.Instance v){
         	VertexShapeFactory<FunctionalUnit.Instance> factory = new VertexShapeFactory<FunctionalUnit.Instance>(vertexSizeTransformer,
         																		vertexAspectRatioTransformer);
         	return factory.getRoundRectangle(v);
         }
     };
     
     static public Function<FunctionalUnit.Instance, Font> vertexFontTransformer = new Function<FunctionalUnit.Instance,Font>(){
         public Font apply(FunctionalUnit.Instance v){
         	return FONT;
         }
     };
     
     static public Function<FunctionalUnit.Instance, Paint> vertexFillPaintTransformer = new Function<FunctionalUnit.Instance,Paint>(){
         public Paint apply(FunctionalUnit.Instance v){ 
         	FunctionalUnit unit = v.getUnit();
         	if(unit == FunctionalUnits.NESTED_GOAL) {
         		return Color.LIGHT_GRAY;
         	}
         	if(unit == FunctionalUnits.RETALIATION) {
         		return Color.ORANGE;
         	}
         	if(unit == FunctionalUnits.DENIED_REQUEST) {
         		return Color.CYAN;
         	}
         	if(unit.isPrimitive()) {
         		return Color.WHITE;
         	}
         	return Color.MAGENTA;
         }
     };
}
