package plotmas.graph.isomorphism;

import plotmas.graph.Vertex;

enum UnitVertexType {
	NONE, INTENTION, POSITIVE, NEGATIVE, WILDCARD;
	
	static UnitVertexType typeOf(Vertex v) {
		if(!v.getIntention().isEmpty()) {
			return UnitVertexType.INTENTION;
		}
		String vs = v.toString();
		char valence = vs.charAt(vs.length() - 3);
		if(valence == '+') {
			return UnitVertexType.POSITIVE;
		}
		if(valence == '-') {
			return UnitVertexType.NEGATIVE;
		}
		if(valence == '*') {
			return UnitVertexType.WILDCARD;
		}
		return UnitVertexType.NONE;
	}
}
