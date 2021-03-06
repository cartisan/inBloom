package inBloom.test.graph;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;

import org.junit.BeforeClass;

import com.google.common.cache.LoadingCache;

import inBloom.graph.Edge;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.PlotGraphLayout;
import inBloom.graph.Transformers;
import inBloom.graph.Vertex;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import junit.framework.TestCase;

public class PlotGraphLayoutTest extends TestCase {

	Field LOC = null;
	Field SIZE = null;
	Field DRAW_GRAPH = null;

	/**
	 * Helper method to inspect a graph used for testing purposes
	 *
	 * @param g
	 * @throws Exception
	 */
	public void visuallyInspectLayout(PlotDirectedSparseGraph g) throws Exception {
		PlotGraphController controller = PlotGraphController.fromGraph(g);

		controller.visualizeGraph();
		System.out.println("Press enter to continue execution...");
		System.in.read();
	}

    @BeforeClass
    public void setUp() throws Exception {
        this.LOC = AbstractLayout.class.getDeclaredField("locations");
        this.LOC.setAccessible(true);

        this.SIZE = AbstractLayout.class.getDeclaredField("size");
        this.SIZE.setAccessible(true);

//        DRAW_GRAPH = PlotGraphController.class.getDeclaredField("drawnGraph");
//        DRAW_GRAPH.setAccessible(true);
    }

	@SuppressWarnings("unchecked")
	public void testRootLocations() throws Exception {
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// Create Trees for each agent and add the roots
		Vertex v1 = graph.addRoot("one");
		Vertex v2 = graph.addRoot("onetwo");

		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test pos of first root
//		visuallyInspectLayout(graph);
		assertEquals(Double.valueOf(PlotGraphLayout.START_X), locations.get(v1).getX());
		double yv1 = Double.valueOf(PlotGraphLayout.START_Y + PlotGraphLayout.PAD_Y);
		assertEquals(yv1 + PlotGraphLayout.STEP_OFFSET, locations.get(v1).getY());

		// test pos of next root
		double xv2 = PlotGraphLayout.START_X + PlotGraphLayout.PAD_X + Transformers.vertexSizeTransformer.apply(v1);
		double yv2 = Double.valueOf(PlotGraphLayout.START_Y + PlotGraphLayout.PAD_Y);
		assertEquals(xv2, locations.get(v2).getX());
		assertEquals(yv2 + PlotGraphLayout.STEP_OFFSET, locations.get(v2).getY());
	}

	@SuppressWarnings("unchecked")
	public void testRootLocationsMovesDueToLongLabel() throws Exception {
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		graph.setName("Test");

		// Create Trees for each agent and add the roots
		Vertex v1 = graph.addRoot("len:200 ***********************************************************************************************************************************************************************************************");

		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// !!! Check out how it looks
//		this.visuallyInspectLayout(graph);

		// test pos of first root
		assertEquals(Double.valueOf(1022.0), locations.get(v1).getX());
	}

	public void testCanvasGrows() throws Exception  {
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		Vertex vOld = graph.addRoot("root");

		for (int i = 1; i < 12; i++) {
			Vertex vNew = new Vertex("node", i, graph);
			graph.addEdge(new Edge(), vOld, vNew);
			vOld = vNew;
		}

		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		Dimension size = (Dimension) this.SIZE.get(layout);

		assertEquals(950, size.height);
		assertEquals(600, size.width);
	}

	@SuppressWarnings("unchecked")
	public void testStepAlignment() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v2, v3);

		Vertex v4 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v3, v4);

		// ... second column ...
		Vertex v5 = graph.addRoot("dog");
		Vertex v6 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v5, v6);

		Vertex v7 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v6, v7);

		// ... third column ...
		Vertex v8 = graph.addRoot("cow");
		Vertex v9 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v8, v9);

		Vertex v10 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v9, v10);

		Vertex v11 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v10, v11);

		Vertex v12 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v11, v12);

		// !!! Check out how it looks
//		visuallyInspectLayout(graph);

		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test whether all 2's are located on same y value
		assertEquals(locations.get(v4).getY(), locations.get(v7).getY());
		assertEquals(locations.get(v7).getY(), locations.get(v12).getY());

		// test that the third vertices in column one and two have different y values (because of different steps)
		assertFalse(locations.get(v3).getY() == locations.get(v7).getY());
	}


	@SuppressWarnings("unchecked")
	public void testMoveWholeColumn() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v2, v3);

		Vertex v4 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v3, v4);

		// ... second column ...
		Vertex v5 = graph.addRoot("dog");
		Vertex v6 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v5, v6);

		Vertex v7 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v6, v7);

		Vertex v8 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v7, v8);

		// !!! Check out how it looks
//		visuallyInspectLayout(graph);

		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test that in first column not only step 2 was shifted down, but also everything below it
		assertEquals(locations.get(v3).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v4).getY());
	}

	@SuppressWarnings("unchecked")
	public void testMissingSteps_newColumnShifts() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v2, v3);

		Vertex v4 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v3, v4);

		// ... second column ...
		Vertex v5 = graph.addRoot("dog");
		Vertex v6 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v5, v6);

		Vertex v7 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v6, v7);

		// !!! Check out how it looks
//		visuallyInspectLayout(graph);

//		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test that in second column step 2 was shifted down, because its missing in column one but has a successor there
		assertEquals(locations.get(v3).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v7).getY());
	}

	@SuppressWarnings("unchecked")
	public void testMissingSteps_oldColumnShifts() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v2, v3);

		// ... second column ...
		Vertex v4 = graph.addRoot("dog");
		Vertex v5 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v4, v5);

		Vertex v6 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v5, v6);

		Vertex v7 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v6, v7);

		// !!! Check out how it looks
//		visuallyInspectLayout(graph);

//		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test that in first column step 3 was shifted down, because step 2 was detected in second column
		assertEquals(locations.get(v6).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v3).getY());
	}

	@SuppressWarnings("unchecked")
	public void testMissingSteps_endOfGraph() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		graph.setName("Test");

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v2, v3);

		Vertex v4 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v3, v4);

		// ... second column ...
		Vertex v5 = graph.addRoot("dog");
		Vertex v6 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v5, v6);

		Vertex v7 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v6, v7);

		Vertex v8 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v7, v8);

		// !!! Check out how it looks
//		this.visuallyInspectLayout(graph);

//		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test that in second column step 2 was shifted down, because its missing in column one but has a successor there
		assertEquals(locations.get(v3).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v7).getY());

		// test that in first column step 3 was shifted down, because step 2 was detected in second column
		assertEquals(locations.get(v8).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v4).getY());
	}

	@SuppressWarnings("unchecked")
	public void testEndOfStepChangeMovesColumns() throws Exception {
		// ...... Building test graph ......
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();

		// ... first column ...
		Vertex v1 = graph.addRoot("hen");
		Vertex v2 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v1, v2);

		Vertex v3 = new Vertex("2", 2, graph);
		graph.addEdge(new Edge(), v2, v3);

		Vertex v4 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v3, v4);

		// ... second column ...
		Vertex v5 = graph.addRoot("dog");
		Vertex v6 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(Edge.Type.ROOT), v5, v6);

		Vertex v7 = new Vertex("1", 1, graph);
		graph.addEdge(new Edge(), v6, v7);

		Vertex v8 = new Vertex("3", 3, graph);
		graph.addEdge(new Edge(), v7, v8);

		// !!! Check out how it looks
//		visuallyInspectLayout(graph);

		// ..... Testing locations ......
		Layout<Vertex, Edge> layout = new PlotGraphLayout(graph);
		LoadingCache<Vertex, Point2D> locations = (LoadingCache<Vertex, Point2D>) this.LOC.get(layout);

		// test that in first column step 2 was shifted down, because in second column end of step 1 is lower
		assertEquals(locations.get(v7).getY() + PlotGraphLayout.PAD_Y + PlotGraphLayout.STEP_OFFSET, locations.get(v3).getY());

	}
}
