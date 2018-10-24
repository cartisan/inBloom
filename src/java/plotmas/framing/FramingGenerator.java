package plotmas.framing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.helper.Tellability;

/**
 * Class used to generate a piece of natural language framing information (summary)
 * from a given tellability analysis.
 * @author Sven
 */
public class FramingGenerator {
	
	private static HashMap<FunctionalUnit, String> nlSnippets = new HashMap<>();
	
	/**
	 * %1$s is the first agent in the unit (the agent which owns the beginning vertex).
	 * %2$s is the other agent of the unit (if it is a cross-character unit).
	 * %3$s is the subject of the unit in base form.
	 * %4$s is the subject of the unit in -ing form.
	 * %5$s is the subject of the unit in -s form.
	 */
	static {
		nlSnippets.put(FunctionalUnits.NESTED_GOAL, "%1$s takes up a complex plan to %3$s");
		nlSnippets.put(FunctionalUnits.RETALIATION, "%2$s retaliates against %1$s by %4$s them");
		nlSnippets.put(FunctionalUnits.DENIED_REQUEST, "%2$s deny %1$s's request for %3$s");
		nlSnippets.put(FunctionalUnits.HONORED_REQUEST, "%2$s honor %1$s's request for %3$s");
	}
	
	public static String generateFraming(Tellability tellability) {
		
		// Sort instances by polyvalence (descending)
		FunctionalUnit.Instance[] units = new FunctionalUnit.Instance[tellability.connectivityGraph.getVertices().size()];
		units = tellability.connectivityGraph.getVertices().toArray(units);
		Arrays.sort(units, new Comparator<FunctionalUnit.Instance>() {
			@Override
			public int compare(FunctionalUnit.Instance o1, FunctionalUnit.Instance o2) {
				return tellability.connectivityGraph.getNeighborCount(o2) - tellability.connectivityGraph.getNeighborCount(o1);
			}
		});
		
		// Retrieve the instance of each unique functional unit type which has the highest polyvalence
		Map<FunctionalUnit, FunctionalUnit.Instance> uniqueUnits = new HashMap<FunctionalUnit, FunctionalUnit.Instance>();
		for(int i = 0; i < units.length; i++) {
			if(!units[i].getUnit().isPrimitive()) {
				if(!uniqueUnits.containsKey(units[i].getUnit())) {
					uniqueUnits.put(units[i].getUnit(), units[i]);
				}
			}
		}
		
		// Order the instances temporally
		FunctionalUnit.Instance[] instances = new FunctionalUnit.Instance[uniqueUnits.size()];
		instances = uniqueUnits.values().toArray(instances);
		Arrays.sort(instances, new Comparator<FunctionalUnit.Instance>() {
			@Override
			public int compare(FunctionalUnit.Instance o1, FunctionalUnit.Instance o2) {
				if(o1 == o2) {
					return 0;
				}
				return tellability.connectivityGraph.isPredecessor(o1, o2) ? -1 : 1;
			}
		});
		
		// Only generate some framing if there were any complex units.
		if(instances.length == 0) {
			return "This is not a story, but I wrote it anyway as an example of how not to be a good author.";
		}
		
		// Construct natural language snippets
		String desc = "";
		for(int i = 0; i < instances.length; i++) {
			if(i > 0) {
				desc += ";";
			}
			desc += getNL(instances[i]);
		}
		
		// combine the natural language snippets into
		// a complete natural language sentence.
		String[] parts = desc.split(";");
		desc = "I wanted to write a story in which ";
		for(int i = 0; i < parts.length; i++) {
			if(i > 0) {
				if(i == parts.length - 1) {
					desc += " and ";
				} else {
					desc += ", ";
				}
			}
			desc += parts[i];
		}
		desc += ".";
		
		return desc;
	}
	
	private static String getNL(FunctionalUnit.Instance instance) {
		String name = "";
		if(!nlSnippets.containsKey(instance.getUnit())) {
			name = instance.getUnit().getName().toLowerCase() + "s";
		} else {
			name = nlSnippets.get(instance.getUnit());
		}
		return String.format(name, instance.getFirstAgent(), instance.getSecondAgent(),
				Translator.translate(instance.getSubject(), Translator.Form.BASE),
				Translator.translate(instance.getSubject(), Translator.Form.ING),
				Translator.translate(instance.getSubject(), Translator.Form.S));
	}
}
