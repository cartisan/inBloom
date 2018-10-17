package plotmas.framing;

import java.awt.Dimension;
import java.awt.Font;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import plotmas.graph.Edge;
import plotmas.graph.Vertex;
import plotmas.graph.isomorphism.FunctionalUnit;

public class ConnectivityGraph extends UndirectedSparseGraph<FunctionalUnit.Instance, Edge> {

	private static final long serialVersionUID = 0L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ConnectivityGraph.class.getSimpleName());

	public Set<FunctionalUnit.Instance> getUnitsContaining(Vertex vertex) {
		Set<FunctionalUnit.Instance> resultSet = new HashSet<FunctionalUnit.Instance>();
		for (FunctionalUnit.Instance inst : this.getVertices()) {
			if (inst.contains(vertex)) {
				resultSet.add(inst);
			}
		}
		return resultSet;
	}

	public void removeEntailed() {
		List<FunctionalUnit.Instance> entailedUnits = new LinkedList<FunctionalUnit.Instance>();
		for (FunctionalUnit.Instance inside : this.getVertices()) {
			for (FunctionalUnit.Instance outside : this.getVertices()) {
				if (inside == outside)
					continue;
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

	public void prunePrimitives() {
		List<FunctionalUnit.Instance> primitivesToPrune = new LinkedList<FunctionalUnit.Instance>();
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

	private Set<FunctionalUnit.Instance> pivotalUnits;

	public void identifyPivot() {
		Set<FunctionalUnit.Instance> currentMax = new HashSet<FunctionalUnit.Instance>();
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
		pivotalUnits = currentMax;
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
					this.addEdge(new Edge(), others, vertex);
				}
			}
		}
		return true;
	}

	public void display() {
		identifyPivot();
		int size = 500;
		FRLayout<FunctionalUnit.Instance, Edge> layout = new FRLayout<FunctionalUnit.Instance, Edge>(this);
		layout.setRepulsionMultiplier(0.2);
		layout.setMaxIterations(2100);
		layout.setSize(new Dimension(size, size));
		VisualizationViewer<FunctionalUnit.Instance, Edge> vv = new VisualizationViewer<FunctionalUnit.Instance, Edge>(
				layout);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv.getRenderContext().setVertexShapeTransformer(Transformers.vertexShapeTransformer);
		vv.getRenderContext().setVertexFontTransformer(new Function<FunctionalUnit.Instance, Font>() {
			public Font apply(FunctionalUnit.Instance v) {
				return pivotalUnits.contains(v) ? Transformers.FONT_LABEL : Transformers.FONT;
			}
		});
		vv.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexFillPaintTransformer);
		vv.setPreferredSize(new Dimension(size + 100, size + 100)); // Sets the viewing area size
		DefaultModalGraphMouse<FunctionalUnit.Instance, Edge> gm = new DefaultModalGraphMouse<FunctionalUnit.Instance, Edge>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		JFrame frame = new JFrame("Functional Unit Connectivity Graph");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}
