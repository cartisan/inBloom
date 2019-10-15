package inBloom.graph.isomorphism;

import jason.asSemantics.Emotion;

import inBloom.graph.Vertex;

/**
 * Enum defining the type of a vertex with regards to functional unit identification.
 * @author Sven Wilke
 */
public enum UnitVertexType {
	NONE, INTENTION, POSITIVE, NEGATIVE, POLYEMOTIONAL, WILDCARD, ACTIVE, ACTION, SPEECH;

	public boolean hasPosEmotion = false;
	public boolean hasNegEmotion = false;

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

		boolean hasPositive = false;
		boolean hasNegative = false;
		for(String em : v.getEmotions()) {
			hasPositive |= Emotion.getEmotion(em).getP() > 0;
			hasNegative |= Emotion.getEmotion(em).getP() < 0;
		}

		if(v.getType() == Vertex.Type.ACTIVE) {
			UnitVertexType t = UnitVertexType.ACTIVE;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}

		if(v.getType() == Vertex.Type.SPEECHACT) {
			UnitVertexType t = UnitVertexType.SPEECH;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}

		if(v.getType() == Vertex.Type.ACTION) {
			UnitVertexType t = UnitVertexType.ACTION;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}

		if(hasPositive && hasNegative) {
			UnitVertexType t = UnitVertexType.POLYEMOTIONAL;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}
		if(hasPositive) {
			UnitVertexType t = UnitVertexType.POSITIVE;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}
		if(hasNegative) {
			UnitVertexType t = UnitVertexType.NEGATIVE;
			setAffect(t, hasPositive, hasNegative);
			return t;
		}

		return UnitVertexType.NONE;
	}

	private static void setAffect(UnitVertexType vertexType, boolean hasPositive, boolean hasNegative) {
		if (hasPositive) {
			vertexType.hasPosEmotion = true;
		}
		if (hasNegative) {
			vertexType.hasNegEmotion = true;
		}
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
			case ACTIVE:		return other.equals(ACTION) || other.equals(SPEECH) || other.equals(ACTIVE);
			case POLYEMOTIONAL: return other.hasPosEmotion || other.hasNegEmotion;
			case POSITIVE:		return other.hasPosEmotion;
			case NEGATIVE:		return other.hasNegEmotion;
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

	public String toString() {
		String postfix = "";
		if(this.hasPosEmotion) {
			postfix += "+";
		}
		if(this.hasNegEmotion) {
			postfix += "-";
		}
		return this.name() + postfix;
	}
}
