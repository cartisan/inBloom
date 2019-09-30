package inBloom.graph.isomorphism;

import jason.asSemantics.Emotion;

import inBloom.graph.Vertex;

/**
 * Enum defining the type of a vertex with regards to functional unit identification.
 * @author Sven Wilke
 */
public enum UnitVertexType {
	NONE, INTENTION, POSITIVE, NEGATIVE, POLYEMOTIONAL, WILDCARD, ACTIVE, ACTION, SPEECH;

	/**
	 * Returns the FU vertex type of a given graph vertex.
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

		if(v.getType() == Vertex.Type.ACTIVE) {
			return UnitVertexType.ACTIVE;
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

	/**
	 * Tests whether this UnitVertexType matches other, that is, whether 'other' (type of a graph vertex) is an 
	 * instance or specialization of this type (in an FU). 
	 * @param other
	 * @return
	 */
	public boolean matches(UnitVertexType other) {
		switch(this) {
			case NONE: 			return false;
			case WILDCARD: 		return true;
			case POLYEMOTIONAL: return other.equals(POSITIVE) || other.equals(NEGATIVE) || other.equals(POLYEMOTIONAL);
			case ACTIVE:		return other.equals(ACTION) || other.equals(SPEECH) || other.equals(ACTIVE);
			default: 			return this.equals(other);
		}
	}

	/**
	 * If a vertex in a FU is defined not using a specific type corresponding top one vertex type
	 * but a type that can match several different vertex types, then its adjacent edges in the FU will need to be
	 * interpreted as wildcard edges without a specific type.
	 * @see State#checkEdgeCompatibility()
	 * @return
	 */
	public boolean needsWildcardEdge() {
		switch(this) {
			case WILDCARD:
			case ACTIVE:	return true;
			default:		return false;
		}
	}
}
