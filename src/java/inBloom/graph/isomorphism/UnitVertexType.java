package inBloom.graph.isomorphism;

import jason.asSemantics.Emotion;

import inBloom.graph.Vertex;

/**
 * Enum defining the type of a vertex with regards to functional unit identification.
 * @author Sven Wilke
 */
public enum UnitVertexType {
	NONE, INTENTION, POSITIVE, NEGATIVE, POLYEMOTIONAL, WILDCARD, ACTION, SPEECH;

	/**
	 * Returns the type of a given vertex.
	 * @param v Vertex to return the type of.
	 * @return The UnitVertexType of <i>v</i>.
	 */
	public static UnitVertexType typeOf(Vertex v) {
		if(v.getType() == Vertex.Type.WILDCARD) {
			return UnitVertexType.WILDCARD;
		}

		if(v.getType() == Vertex.Type.INTENTION) {
			return UnitVertexType.INTENTION;
		}

		if(v.getType() == Vertex.Type.SPEECHACT) {
			return UnitVertexType.SPEECH;
		}

		boolean hasPositive = false;
		boolean hasNegative = false;
		for(String em : v.getEmotions()) {
				hasPositive |= Emotion.getEmotion(em).getP() > 0;
				hasNegative |= Emotion.getEmotion(em).getP() < 0;
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

	public boolean matches(UnitVertexType other) {
		switch(this) {
			case WILDCARD: 		return true;
			case POLYEMOTIONAL: return other.equals(POSITIVE) || other.equals(NEGATIVE) || other.equals(POLYEMOTIONAL);
			case NONE: 			return false;
			default: 			return this.equals(other);
		}
	}
}
