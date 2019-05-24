package plotmas.graph.isomorphism;

import jason.asSemantics.Emotion;
import plotmas.graph.Vertex;

/**
 * Enum defining the type of a vertex with regards to functional unit identification.
 * @author Sven Wilke
 */
enum UnitVertexType {
	NONE, INTENTION, POSITIVE, NEGATIVE, POLYEMOTIONAL, WILDCARD;
	
	/**
	 * Returns the type of a given vertex.
	 * @param v Vertex to return the type of.
	 * @return The UnitVertexType of <i>v</i>.
	 */
	static UnitVertexType typeOf(Vertex v) {
		if(v.getType() == Vertex.Type.WILDCARD) {
			return UnitVertexType.WILDCARD;
		}
		if(!v.getIntention().isEmpty()) {
			return UnitVertexType.INTENTION;
		}
		boolean hasPositive = false;
		boolean hasNegative = false;
		for(String emotion : Emotion.getAllEmotions()) {
			if(v.hasEmotion(emotion)) {
				hasPositive |= Emotion.getEmotion(emotion).getP() > 0;
				hasNegative |= Emotion.getEmotion(emotion).getP() < 0;
			}
		}
		if(hasPositive && hasNegative) {
			return UnitVertexType.POLYEMOTIONAL;
		}
		if(hasPositive) {
			return UnitVertexType.POSITIVE;
		}
		if(hasNegative) {
			return UnitVertexType.NEGATIVE;
		}
		return UnitVertexType.NONE;
	}
}
