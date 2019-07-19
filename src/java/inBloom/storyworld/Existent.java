package inBloom.storyworld;

import jason.asSyntax.Literal;
import jason.asSyntax.Pred;

public abstract class Existent {

	/**
	 * Returns an AgentSpeak {@link jason.asSyntax.Literal literal} denoting the existent and potentially its
	 * current state using annotations.
	 * @return A literal denoting this existent and any relevant information about it
	 * @see inBloom.stories.little_red_hen.FarmModel.Wheat
	 */
	public Literal literal() {
			return new Pred(this.toString());
	};

	@Override
	public String toString() {
		throw new RuntimeException("Each subclass is responsible for defining an ASL compliant toString method");
	}
}
