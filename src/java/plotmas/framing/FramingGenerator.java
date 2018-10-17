package plotmas.framing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import plotmas.graph.isomorphism.FunctionalUnit;
import plotmas.graph.isomorphism.FunctionalUnits;
import plotmas.helper.Tellability;

public class FramingGenerator {
	
	private static HashMap<FunctionalUnit, String> nlSnippets = new HashMap<>();
	
	static {
		nlSnippets.put(FunctionalUnits.NESTED_GOAL, "complex plan");
		nlSnippets.put(FunctionalUnits.RETALIATION, "revenge act");
		nlSnippets.put(FunctionalUnits.DENIED_REQUEST, "denied help request");
	}
	
	public static String generateFraming(Tellability tellability) {
		FunctionalUnit.Instance[] units = new FunctionalUnit.Instance[tellability.connectivityGraph.getVertices().size()];
		units = tellability.connectivityGraph.getVertices().toArray(units);
		Arrays.sort(units, new Comparator<FunctionalUnit.Instance>() {
			@Override
			public int compare(FunctionalUnit.Instance o1, FunctionalUnit.Instance o2) {
				return tellability.connectivityGraph.getNeighborCount(o2) - tellability.connectivityGraph.getNeighborCount(o1);
			}
		});
		Set<FunctionalUnit> uniqueUnits = new HashSet<FunctionalUnit>();
		String desc = "";
		int c = 0;
		for(int i = 0; i < units.length; i++) {
			if(!units[i].getUnit().isPrimitive()) {
				if(!uniqueUnits.contains(units[i].getUnit())) {
					if(c > 0) {
						desc += ",";
					}
					desc += getNL(units[i].getUnit(), true);
					c++;
				}
				uniqueUnits.add(units[i].getUnit());
			}
		}
		String[] parts = desc.split(",");
		desc = "This story is about ";
		for(int i = 0; i < parts.length; i++) {
			if(i == parts.length - 1) {
				desc += " and ";
			} else
			if(i > 0) {
				desc += ", ";
			}
			desc += parts[i];
		}
		desc += ".";
		return desc;
		
	}
	
	private static String getNL(FunctionalUnit unit, boolean plural) {
		String name = "";
		if(!nlSnippets.containsKey(unit)) {
			name = unit.getName().toLowerCase();
		} else {
			name = nlSnippets.get(unit);
		}
		return (!plural ? "a " : "") + name + (plural ? "s" : "");
	}
}
