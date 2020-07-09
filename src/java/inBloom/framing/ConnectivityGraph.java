package inBloom.framing;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.google.common.base.Function;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.Vertex;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnit.Instance;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Used to create and generate the connectivity graph of the functional units
 * of a plot.
 * @author Sven
 */
public class ConnectivityGraph extends DirectedSparseGraph<FunctionalUnit.Instance, Edge> {

	private static final long serialVersionUID = 0L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ConnectivityGraph.class.getSimpleName());

	@SuppressWarnings("unused")
	private PlotDirectedSparseGraph plotGraph;

	public ConnectivityGraph(PlotDirectedSparseGraph plotGraph) {
		this.plotGraph = plotGraph;
	}

	/**
	 * Retrieves all functional unit instances which contain a given
	 * plot vertex.
	 * Used when adding new instances to identify polyvalence and establish edges.
	 * @param vertex to look for
	 * @return Set of functional unit instances
	 */
	public Set<FunctionalUnit.Instance> getUnitsContaining(Vertex vertex) {
		Set<FunctionalUnit.Instance> resultSet = new HashSet<>();
		for (FunctionalUnit.Instance inst : this.getVertices()) {
			if (inst.contains(vertex)) {
				resultSet.add(inst);
			}
		}
		return resultSet;
	}

	/**
	 * Removes all functional units which are entailed by another unit (i.e. removes all units X
	 * for which there is a unit Y such that every vertex of X is contained in Y).
	 * The graph is then left with only "top-level units" (see Lehnert, 1981)
	 */
	public void removeEntailed() {
		List<FunctionalUnit.Instance> entailedUnits = new LinkedList<>();
		for (FunctionalUnit.Instance inside : this.getVertices()) {
			for (FunctionalUnit.Instance outside : this.getVertices()) {
				if (inside == outside) {
					continue;
				}
				boolean isEntailed = true;
				for (Vertex v : inside.getVertices()) {
					if (!outside.contains(v)) {
						isEntailed = false;
						break;
					}
				}
				if (isEntailed) {
					entailedUnits.add(inside);
					break;
				}
			}
		}
		for (FunctionalUnit.Instance inst : entailedUnits) {
			this.removeVertex(inst);
		}
	}

	/**
	 * Removes all primitive functional unit instances which are
	 * not connected to at least one complex functional unit.
	 * In our story, this has the effect of removing groups of
	 * primitive units which are not connected to the main connectivity
	 * graph of the plot.
	 */
	public void prunePrimitives() {
		List<FunctionalUnit.Instance> primitivesToPrune = new LinkedList<>();
		for (FunctionalUnit.Instance inst : this.getVertices()) {
			if (inst.getUnit().isPrimitive()) {
				boolean allPrimitives = true;
				for (FunctionalUnit.Instance related : this.getNeighbors(inst)) {
					if (!related.getUnit().isPrimitive()) {
						allPrimitives = false;
						break;
					}
				}
				if (allPrimitives) {
					primitivesToPrune.add(inst);
				}
			}
		}
		for (FunctionalUnit.Instance inst : primitivesToPrune) {
			this.removeVertex(inst);
		}
	}

	/**
	 * Merges groups of vertices which are connected bidirectionally
	 * and are the same unit into a single vertex.
	 */
	public void mergeTimeEquivalents() {
		List<Set<FunctionalUnit.Instance>> merges = new LinkedList<>();
		for(FunctionalUnit.Instance a : this.getVertices()) {
			Collection<FunctionalUnit.Instance> equivalents = this.intersect(this.getSuccessors(a), this.getPredecessors(a));
			for(FunctionalUnit.Instance b : equivalents) {
				if(a.getUnit() == b.getUnit()) {
					Set<FunctionalUnit.Instance> mergeSet = new HashSet<>();
					boolean isNew = true;
					for(Set<FunctionalUnit.Instance> existingMerge : merges) {
						if(existingMerge.contains(a) || existingMerge.contains(b)) {
							mergeSet = existingMerge;
							isNew = false;
							break;
						}
					}
					mergeSet.add(a);
					mergeSet.add(b);
					if(isNew) {
						merges.add(mergeSet);
					}
				}
			}
		}
		for(Set<FunctionalUnit.Instance> m : merges) {
			this.merge(m);
		}
	}

	/**
	 * Merges a set of functional unit instances into a
	 * single vertex by combining the set of plot vertices they contain,
	 * the agents of the unit, as well as the subjects. (Subjects are
	 * usually the same if this is called by {@link #mergeTimeEquivalents()}).
	 * @param set of instances to merge
	 */
	private void merge(Set<FunctionalUnit.Instance> set) {
		FunctionalUnit unit = null;
		Set<FunctionalUnit.Instance> successors = new HashSet<>();
		Set<FunctionalUnit.Instance> predecessors = new HashSet<>();
		Set<Vertex> vertices = new HashSet<>();
		Set<String> firstAgents = new HashSet<>();
		Set<String> secondAgents = new HashSet<>();
		Set<String> subjects = new HashSet<>();
		boolean firstPlural = false;
		boolean secondPlural = false;

		// Extract individual instance information and remove from graph
		for(FunctionalUnit.Instance inst : set) {
			if(unit == null) {
				unit = inst.getUnit();
			}
			if(inst.getFirstAgent() != null) {
				firstAgents.add(inst.getFirstAgent());
			}
			if(inst.getSecondAgent() != null) {
				secondAgents.add(inst.getSecondAgent());
			}
			if(inst.isFirstPlural()) {
				firstPlural = true;
			}
			if(inst.isSecondPlural()) {
				secondPlural = true;
			}
			if(inst.getSubject() != null) {
				subjects.add(inst.getSubject());
			}
			for(FunctionalUnit.Instance succ : this.getSuccessors(inst)) {
				if(!set.contains(succ)) {
					successors.add(succ);
				}
			}
			for(FunctionalUnit.Instance pred : this.getPredecessors(inst)) {
				if(!set.contains(pred)) {
					predecessors.add(pred);
				}
			}
			vertices.addAll(inst.getVertices());
			this.removeVertex(inst);
		}

		FunctionalUnit.Instance mergedInstance = unit.new Instance(null, vertices, null);

		// Build agents
		String firstAgent = "";
		int c = 0;
		for(String ag : firstAgents) {
			if(c == 0) {
				firstAgent += ag;
			} else
			if(c == firstAgents.size() - 1) {
				firstAgent += " and " + ag;
			} else {
				firstAgent += ", " + ag;
			}
			c++;
		}
		if(!firstAgent.isEmpty()) {
			mergedInstance.setFirstAgent(firstAgent);
		}
		String secondAgent = "";
		c = 0;
		for(String ag : secondAgents) {
			if(c == 0) {
				secondAgent += ag;
			} else
			if(c == secondAgents.size() - 1) {
				secondAgent += " and " + ag;
			} else {
				secondAgent += ", " + ag;
			}
			c++;
		}
		if(!secondAgent.isEmpty()) {
			mergedInstance.setSecondAgent(secondAgent);
		}

		firstPlural = firstPlural || firstAgents.size() > 1;
		secondPlural = secondPlural || secondAgents.size() > 1;

		if(firstPlural) {
			mergedInstance.setFirstPlural();
		}
		if(secondPlural) {
			mergedInstance.setSecondPlural();
		}

		String subject = "";
		c = 0;
		for(String s : subjects) {
			if(c > 0) {
				subject += ";";
			}
			subject += s;
		}
		if(!subject.isEmpty()) {
			mergedInstance.setSubject(subject);
		}
		// Rebuild graph connectivity
		super.addVertex(mergedInstance);
		for(FunctionalUnit.Instance succ : successors) {
			this.addEdge(new Edge(), mergedInstance, succ);
		}
		for(FunctionalUnit.Instance pred : predecessors) {
			this.addEdge(new Edge(), pred, mergedInstance);
		}
	}

	/**
	 * Generates the intersection of two sets of functional unit instances.
	 * @param a
	 * @param b
	 * @return set containing all instances which are contained in both a and b.
	 */
	private Set<FunctionalUnit.Instance> intersect(Collection<FunctionalUnit.Instance> a, Collection<FunctionalUnit.Instance> b) {
		Set<FunctionalUnit.Instance> c = new HashSet<>();
		for(FunctionalUnit.Instance inst : a) {
			if(b.contains(inst)) {
				c.add(inst);
			}
		}
		return c;
	}

	/**
	 * Holds units which identify as pivotal units as per Lehnert.
	 */
	private Set<FunctionalUnit.Instance> pivotalUnits;
	/**
	 * Finds all pivotal units in the connectivity graph and stores
	 * them in the pivotalUnits set.
	 * These are the units with the highest amount of overlaps (polyvalence).
	 */
	public void identifyPivot() {
		Set<FunctionalUnit.Instance> currentMax = new HashSet<>();
		int maxRelatedCount = 0;
		for (FunctionalUnit.Instance inst : this.getVertices()) {
			int count = this.getNeighborCount(inst);
			if (count > maxRelatedCount) {
				currentMax.clear();
				currentMax.add(inst);
				maxRelatedCount = count;
			} else if (count == maxRelatedCount) {
				currentMax.add(inst);
			}
		}
		this.pivotalUnits = currentMax;
	}

	@Override
	public boolean addVertex(FunctionalUnit.Instance vertex) {
		boolean result = super.addVertex(vertex);
		if (!result) {
			return false;
		}
		for (Vertex v : vertex.getVertices()) {
			for (FunctionalUnit.Instance others : this.getUnitsContaining(v)) {
				if (others != vertex) {
					int stepDelta = others.compareTo(vertex);
					if(stepDelta == 0) {
						this.addEdge(new Edge(), others, vertex);
						this.addEdge(new Edge(), vertex, others);
					} else
					if(stepDelta > 0) {
						this.addEdge(new Edge(), vertex, others);
					} else {
						this.addEdge(new Edge(), others, vertex);
					}
				}
			}
		}
		return true;
	}

	public Collection<Instance> getInstances() {
		return this.getVertices();
	}

	/**
	 * Returns whether instance a is a predecessor of b in any distance.
	 * I.e. this returns true whenever there is a set of instances X, such that
	 * getPredecessors(b).contains(x1 element of X) and getPredecessors(x1).contains(x2 element of X)
	 * and ... and getPredecessors(xn).contains(a).
	 */
	public boolean isPredecessor(FunctionalUnit.Instance a, FunctionalUnit.Instance b) {
		Set<FunctionalUnit.Instance> allVertices = new HashSet<>();
		allVertices.addAll(this.getVertices());
		Queue<FunctionalUnit.Instance> preds = new ArrayDeque<>();
		this.getPredecessors(b).forEach((inst) -> preds.add(inst));
		while(!preds.isEmpty() && allVertices.size() > 0) {
			FunctionalUnit.Instance current = preds.poll();
			if(allVertices.contains(current)) {
				if(a == current) {
					return true;
				} else {
					this.getPredecessors(current).forEach((inst) -> preds.add(inst));
					allVertices.remove(current);
				}
			}
		}
		return false;
	}

	/**
	 * Gets the amount of units that are related to a given unit.
	 * Equivalent to calling getNeighborCount(vertex).
	 * @param vertex Unit to count relations for
	 * @return number of relations (int)
	 */
	public int getRelatedCount(FunctionalUnit.Instance vertex) {
		return this.getNeighborCount(vertex);
	}

	/**
	 * Displays the connectivity graph in a new window.
	 */
	public void display() {
		this.identifyPivot();
		int size = 500;
		FRLayout<FunctionalUnit.Instance, Edge> layout = new FRLayout<>(this);
		layout.setRepulsionMultiplier(0.2);
		layout.setMaxIterations(2100);
		layout.setSize(new Dimension(size, size));
		VisualizationViewer<FunctionalUnit.Instance, Edge> vv = new VisualizationViewer<>(
				layout);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		vv.getRenderContext().setVertexFontTransformer(new Function<FunctionalUnit.Instance, Font>() {
			public Font apply(FunctionalUnit.Instance v) {
				return ConnectivityGraph.this.pivotalUnits.contains(v) ? Transformers.FONT_LABEL : Transformers.FONT;
			}
		});

		vv.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexFillPaintTransformer);
		vv.setPreferredSize(new Dimension(size + 100, size + 100));
		DefaultModalGraphMouse<FunctionalUnit.Instance, Edge> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		JFrame frame = new JFrame("Functional Unit Connectivity Graph");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}
