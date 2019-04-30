package inBloom.storyworld;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import inBloom.PlotModel;

/**
 * Can be used to annotate fields of storyworld classes (Models, Locations, Characters, ...) if they represent the
 * state of the storyworld. Is used by {@linkplain PlotModel} to detect causality relations, by tracking the values of
 * ModelState fields and noting which events caused their change.
 *
 * @author Leonid Berov
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)	// allows reflections to access annotations
public @interface ModelState {
	public static enum DEFAULT_STATES {NOVAL};
}
