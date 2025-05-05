import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;


public class HexGridTest {

    private HexGrid hexGrid;
    private ArrayList<ArrayList<Point>> testGrid;
    private Layout testLayout;

    /*
    Basic setup of HexGrid
   - *

    */

    @Before
    public void setUp() {
        testGrid = new ArrayList<>();

        ArrayList<Point> hex1 = new ArrayList<>();
        hex1.add(new Point(100, 50));
        hex1.add(new Point(150, 50));
        hex1.add(new Point(175, 87));
        hex1.add(new Point(150, 125));
        hex1.add(new Point(100, 125));
        hex1.add(new Point(75, 87));
        testGrid.add(hex1);

        ArrayList<Point> hex2 = new ArrayList<>();
        hex2.add(new Point(175, 87));
        hex2.add(new Point(225, 87));
        hex2.add(new Point(250, 125));
        hex2.add(new Point(225, 162));
        hex2.add(new Point(175, 162));
        hex2.add(new Point(150, 125));
        testGrid.add(hex2);

        hexGrid = new HexGrid(testGrid);
        testLayout = new Layout(Layout.flat, new Point(30, 30), new Point(200, 200));
    }

    // Helper method to create hexagons
    private ArrayList<Point> createHexagon(double centerX, double centerY, double size) {
        ArrayList<Point> hex = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            hex.add(new Point(centerX + size * Math.cos(angle), centerY + size * Math.sin(angle)));
        }
        return hex;
    }

    // CO ORDINATES TEST

    // Checks if the grid is not null,
    // it has 2 hexagons and
    // first hex has 6 points and specific coordinates
    @Test
    public void testHexGridSetup() {
        assertNotNull(hexGrid);
        assertEquals(2, hexGrid.grid.size());
        ArrayList<Point> firstHex = hexGrid.grid.get(0);
        assertEquals(6, firstHex.size());
        assertEquals(100.0, firstHex.get(0).x, 0.001);
        assertEquals(50.0, firstHex.get(0).y, 0.001);
    }

    // Checks if pixel coordinates match expected value using Layout transformation
    @Test
    public void testHexToPixelConversion() {
        HexCube hex = new HexCube(1, -2, 1);
        Point pixel = testLayout.hexToPixel(hex);
        double expectedX = (Layout.flat.f0 * hex.q + Layout.flat.f1 * hex.r) * testLayout.size.x + testLayout.origin.x;
        double expectedY = (Layout.flat.f2 * hex.q + Layout.flat.f3 * hex.r) * testLayout.size.y + testLayout.origin.y;
        assertEquals(expectedX, pixel.x, 0.001);
        assertEquals(expectedY, pixel.y, 0.001);
    }

    // Checks if a pixel point at origin of layout maps back to (0,0,0) in hex coordinates
    @Test
    public void testPixelToHexConversion() {
        Point pixel = new Point(testLayout.origin.x, testLayout.origin.y);
        FractionalHexCube fracHex = testLayout.pixelToHex(pixel);
        HexCube roundedHex = fracHex.hexRound();
        assertEquals(0, roundedHex.q);
        assertEquals(0, roundedHex.r);
        assertEquals(0, roundedHex.s);
    }

    // Checks if it's the correct distance formula in cube coordinates and
    // that a hex has zero distance to itself
    @Test
    public void testHexCubeDistance() {
        HexCube a = new HexCube(1, -2, 1);
        HexCube b = new HexCube(3, -5, 2);
        int expectedDistance = (Math.abs(1-3) + Math.abs(-2 - -5) + Math.abs(1-2)) / 2;
        assertEquals(expectedDistance, a.distance(b));
        assertEquals(0, a.distance(a));
    }

    // Checks if each coordinate component (q, r, s) of 2 HexCubes is correctly summed.
    @Test
    public void testHexCubeAddition() {
        HexCube a = new HexCube(1, -2, 1);
        HexCube b = new HexCube(3, 1, -4);
        HexCube result = a.add(b);
        assertEquals(4, result.q);
        assertEquals(-1, result.r);
        assertEquals(-3, result.s);
    }

    // Checks if difference of one HexCube from another is correct.
    @Test
    public void testHexCubeSubtraction() {
        HexCube a = new HexCube(1, -2, 1);
        HexCube b = new HexCube(3, 1, -4);
        HexCube result = a.subtract(b);
        assertEquals(-2, result.q);
        assertEquals(-3, result.r);
        assertEquals(5, result.s);
    }

    // Checks if each neighbouring hex is 1 unit away from the origin
    @Test
    public void testHexCubeNeighbor() {
        HexCube origin = new HexCube(0, 0, 0);
        for (int i = 0; i < 6; i++) {
            HexCube neighbor = origin.neighbor(i);
            assertEquals(1, origin.distance(neighbor));
        }
    }

    // Checks if fractional hex coordinates are rounded to the nearest valid hex cube coordinate
    @Test
    public void testFractionalHexRounding() {
        FractionalHexCube fracHex = new FractionalHexCube(1.3, -2.1, 0.8);
        HexCube rounded = fracHex.hexRound();
        assertEquals(1, rounded.q);
        assertEquals(-2, rounded.r);
        assertEquals(1, rounded.s);
    }

    // LAYOUT AND GEOMETRY TESTS

    // Checks the average of the hexagon's vertices gives the expected center point
    @Test
    public void testHexCenterCalculation() {
        ArrayList<Point> hexagon = testGrid.get(0);
        Point center = hexGrid.getHexCenter(hexagon);
        double expectedX = (100 + 150 + 175 + 150 + 100 + 75) / 6.0;
        double expectedY = (50 + 50 + 87 + 125 + 125 + 87) / 6.0;
        assertEquals(expectedX, center.x, 0.001);
        assertEquals(expectedY, center.y, 0.001);
    }

    // Checks that the 6 corner points are returned for a hexagon
    @Test
    public void testLayoutHexCorners() {
        Layout layout = new Layout(Layout.flat, new Point(10, 10), new Point(100, 100));
        HexCube hex = new HexCube(1, 0, -1);
        ArrayList<Point> corners = layout.polygonCorners(hex);
        assertEquals(6, corners.size());
    }


    // Checks if the method detects that hex2 is a neighbour of hex1 based on center proximity
    @Test
    public void testNeighborDetection() {
        ArrayList<Point> hex1 = new ArrayList<>();
        hex1.add(new Point(100, 100));
        hex1.add(new Point(120, 80));
        hex1.add(new Point(140, 100));
        hex1.add(new Point(140, 140));
        hex1.add(new Point(120, 160));
        hex1.add(new Point(100, 140));

        ArrayList<Point> hex2 = new ArrayList<>();
        hex2.add(new Point(140, 100));
        hex2.add(new Point(160, 80));
        hex2.add(new Point(180, 100));
        hex2.add(new Point(180, 140));
        hex2.add(new Point(160, 160));
        hex2.add(new Point(140, 140));

        ArrayList<ArrayList<Point>> neighborGrid = new ArrayList<>();
        neighborGrid.add(hex1);
        neighborGrid.add(hex2);

        HexGrid neighborHexGrid = new HexGrid(neighborGrid);
        Point center1 = neighborHexGrid.getHexCenter(hex1);
        ArrayList<Point> neighbors = neighborHexGrid.getNeighborCenters(center1);

        assertEquals(1, neighbors.size());
        Point center2 = neighborHexGrid.getHexCenter(hex2);
        assertTrue(neighbors.contains(center2));
    }

    // GAME LOGIC TESTS

    // Checks if the list contains the center of the first hex (red stone)
    @Test
    public void testFirstStonePlacement() {
        Point center = hexGrid.getHexCenter(testGrid.get(0));
        hexGrid.redStones.add(center);
        assertEquals(1, hexGrid.redStones.size());
        assertTrue(hexGrid.redStones.contains(center));
    }

    // Checks if isRedturn updates correctly when switching players to red
    @Test
    public void testTurnSwitching() {
        assertTrue(hexGrid.isRedTurn);
        hexGrid.isRedTurn = false;
        assertFalse(hexGrid.isRedTurn);
    }

    // Checks if the isGameExited() method reflects the gameExited flag properly
    @Test
    public void testExitButtonFunctionality() {
        assertFalse(hexGrid.isGameExited());
        hexGrid.gameExited = true;
        assertTrue(hexGrid.isGameExited());
    }

    // Checks if stones are cleared after clicking New Game and turn is reset to red
    @Test
    public void testNewGameReset() {
        Point center = hexGrid.getHexCenter(testGrid.get(0));
        hexGrid.redStones.add(center);
        hexGrid.blueStones.add(hexGrid.getHexCenter(testGrid.get(1)));
        hexGrid.isRedTurn = false;

        hexGrid.redStones.clear();
        hexGrid.blueStones.clear();
        hexGrid.isRedTurn = true;

        assertTrue(hexGrid.redStones.isEmpty());
        assertTrue(hexGrid.blueStones.isEmpty());
        assertTrue(hexGrid.isRedTurn);
    }

    // Checks if game state is saved before each move and if
    // undoing the last move removes the most recent stone and restores turn
    @Test
    public void testUndoFunctionality() {
        Point redMove = hexGrid.getHexCenter(testGrid.get(0));
        Point blueMove = hexGrid.getHexCenter(testGrid.get(1));

        // Red's move
        hexGrid.saveGameState();
        hexGrid.redStones.add(redMove);
        hexGrid.isRedTurn = false;

        // Blue's move
        hexGrid.saveGameState();
        hexGrid.blueStones.add(blueMove);
        hexGrid.isRedTurn = true;

        // Undo Blue's move
        hexGrid.undoLastMove();

        assertTrue(hexGrid.redStones.contains(redMove));
        assertFalse(hexGrid.blueStones.contains(blueMove));

    }

    // Checks if both stones and history stacks are cleared and turn is reset to red
    @Test
    public void testNewGameStateReset() {
        hexGrid.redStones.add(hexGrid.getHexCenter(testGrid.get(0)));
        hexGrid.blueStones.add(hexGrid.getHexCenter(testGrid.get(1)));
        hexGrid.saveGameState();
        hexGrid.isRedTurn = false;

        // Simulate new game reset
        hexGrid.redStones.clear();
        hexGrid.blueStones.clear();
        hexGrid.redStonesHistory.clear();
        hexGrid.blueStonesHistory.clear();
        hexGrid.turnHistory.clear();
        hexGrid.isRedTurn = true;

        assertTrue(hexGrid.redStones.isEmpty());
        assertTrue(hexGrid.blueStones.isEmpty());
        assertTrue(hexGrid.redStonesHistory.isEmpty());
        assertTrue(hexGrid.blueStonesHistory.isEmpty());
        assertTrue(hexGrid.turnHistory.isEmpty());
        assertTrue(hexGrid.isRedTurn);
    }


}