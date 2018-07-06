package plotmas.little_red_hen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jason.asSemantics.Personality;
import plotmas.PlotCycle;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.graph.visitor.EdgeLayoutVisitor;

public class RedHenCycle extends PlotCycle {
	
	int cycle = 0;
	
	double[] scale = new double[] { -0.9, 0.0, 0.9 };
	
	private PlotDirectedSparseGraph bestGraph = null;
	private float bestTellability = -1f;
	private Personality bestPersonality = null;
	
	private List<int[]> valueList;

	protected RedHenCycle() {
		// Create PlotCycle with needed agents.
		super(new String[] { "hen", "dog", "cow", "pig" }, "agent");

		// Generate all possible personalities.
		int[] values = new int[5];
		boolean hasAll = false;
		HashSet<int[]> valueSet = new HashSet<>();
		while(!hasAll) {
			for(int i = 0; i < values.length; i++) {
				values[i]++;
				if(values[i] < scale.length) {
					break;
				} else {
					if(i == values.length - 1) {
						hasAll = true;
					}
					values[i] = 0;
				}
			}
			valueSet.add(values.clone());
		}
		valueList = new ArrayList<>(valueSet.size());
		for(int[] v : valueSet) {
			valueList.add(v);
		}
		
		// Log how many personalities there are.
		log("Running " + valueList.size() + " cycles...");
	}
	
	public static void main(String[] args) {
		RedHenCycle cycle = new RedHenCycle();
		cycle.run();
	}

	@Override
	protected ReflectResult reflect(EngageResult er) {
		if(cycle >= valueList.size()) {
			return new ReflectResult(null, null, false);
		}
		
		// Get personality values of last simulation (to save them if the simulation was good)
		int[] values = valueList.get(cycle - 1);
		
		// Save tellability, graph and hen personality if it was better than the best before
		if(er.getTellability() > bestTellability) {
			bestTellability = er.getTellability();
			log("New best: " + bestTellability);
			bestGraph = er.getPlotGraph();
			bestPersonality = createPersonalitiesFromValues(values)[0];
		}
		
		// Retrieve next personality values
		values = valueList.get(cycle);
		
		log("Cycle " + cycle++);
		
		// Create new personalities
		Personality[] p = createPersonalitiesFromValues(values);
		
		return new ReflectResult(new RedHenLauncher(), p);
	}
	
	@Override
	protected void finish() {
		// Print results
		log("Best tellability: " + bestTellability);
		log("Hen personality: " + bestPersonality.toString());
		
		// Add best graph to PlotGraphController
		bestGraph.setName("Plot graph with highest tellability");
		bestGraph.accept(new EdgeLayoutVisitor(bestGraph, 9));
		PlotGraphController.getPlotListener().addGraph(bestGraph);
	}
	
	private Personality[] createPersonalitiesFromValues(int[] values) {
		return new Personality[] {
			new Personality(scale[values[0]], scale[values[1]], scale[values[2]], scale[values[3]], scale[values[4]]),
			new Personality(0, -1, 0, -0.7, -0.8),
			new Personality(0, -1, 0, -0.7, -0.8),
			new Personality(0, -1, 0, -0.7, -0.8)
			//new Personality(scale[values[5]], scale[values[6]], scale[values[7]], scale[values[8]], scale[values[9]]),
			//new Personality(scale[values[10]], scale[values[11]], scale[values[12]], scale[values[13]], scale[values[14]]),
			//new Personality(scale[values[15]], scale[values[16]], scale[values[17]], scale[values[18]], scale[values[19]])
		};
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		ReflectResult rr = new ReflectResult(new RedHenLauncher(), createPersonalitiesFromValues(valueList.get(cycle)));
		log("Cycle " + cycle++);
		return rr;
	}

}
